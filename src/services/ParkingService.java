package services;

import dao.ResidentDAO;
import dao.VehicleDAO;
import database.DatabaseManager;
import database.TransactionManager;
import model.Vehicle;
import ui.InputHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public final class ParkingService {

    private final Connection connection; // Service for vehicle-related operations



    private static class ParkedVehicleInfo {
        final Vehicle vehicle;
        final String parkingType; // "Resident" or "Spare"

        ParkedVehicleInfo(Vehicle vehicle, String parkingType) {
            this.vehicle = vehicle;
            this.parkingType = parkingType;
        }
    }

    public static final int TOTAL_4_WHEELER_SLOTS = 150;
    public static final int TOTAL_2_WHEELER_SLOTS = 300;
    public static final int FOUR_WHEELER_START_SLOT = 1;
    public static final int FOUR_WHEELER_END_SLOT = 150;
    public static final int TWO_WHEELER_START_SLOT = 151;
    public static final int TWO_WHEELER_END_SLOT = 450;
    public static final int SPARE_START_SLOT = 451;
    public static final int SPARE_END_SLOT = 500;
    public static final int TOTAL_SPARE_SLOTS = SPARE_END_SLOT - SPARE_START_SLOT + 1;



    public ParkingService() {
        try {
            this.connection = DatabaseManager.getConnection();
        } catch (SQLException e) {
            System.err.println("Database connection failed in ParkingService.");
            throw new RuntimeException(e);
        }
    }

    public void parkNewVehicle(String residentId) {
        System.out.println("\n--- Park a New Vehicle ---");
        Connection conn = null;
        boolean success = false; // Overall success flag for the transaction

        try {
            conn = this.connection;
            TransactionManager.beginTransaction(conn);

            // Step 1: Get the most current data from the database.
            List<Vehicle> currentlyParked = getCurrentlyParkedVehicles(residentId);
            List<Vehicle> allResidentVehicles = getAllVehiclesForResident(residentId);
            List<Vehicle> unparkedVehicles = getUnparkedVehicles(allResidentVehicles, currentlyParked);
            List<Vehicle> residentParked = getResidentParkedVehicles(residentId);

            // --- Display Status ---
            displayParkedVehicleStatus(residentId);

            // --- Add checks requested by user ---
            long fourWheelerCount = countParkedVehicleOfType(residentParked, "4-wheeler");
            long twoWheelerCount = countParkedVehicleOfType(residentParked, "2-wheeler");

            if (unparkedVehicles.isEmpty()) {
                System.out.println("INFO: You do not have any registered vehicles available to park.");
            }
            if (fourWheelerCount >= 1 && twoWheelerCount >= 2) {
                System.out.println("INFO: Your resident parking slot is full. You can only park a new vehicle in a spare slot.");
            }

            // Step 2: Display parking options to the user.
            System.out.println("\n--- Choose an Option ---");
            for (int i = 0; i < unparkedVehicles.size(); i++) {
                System.out.printf("%d. Park: %s%n", i + 1, unparkedVehicles.get(i).toString());
            }
            int spareOption = unparkedVehicles.size() + 1;
            int cancelOption = unparkedVehicles.size() + 2;
            System.out.printf("%d. Park a new/guest vehicle in Spare Parking%n", spareOption);
            System.out.printf("%d. Cancel%n", cancelOption);

            System.out.print("Enter your choice: ");
            int choice = InputHandler.getValidChoice(1, cancelOption);

            // Step 3: Process user's choice.
            if (choice == cancelOption) {
                System.out.println("Parking cancelled.");
                // success remains false, will lead to rollback
            } else if (choice == spareOption) {
                // Handle the process of adding a new vehicle and parking it in a spare slot.
                success = handleNewVehicleInSpareParking(residentId);
            } else {
                // Handle the original logic for parking an existing, registered vehicle.
                if (choice > unparkedVehicles.size()) {
                    System.out.println("Invalid choice.");
                } else {
                    Vehicle selectedVehicle = unparkedVehicles.get(choice - 1);
                    // This method now internally fetches resident-parked vehicles for limit checks.
                    success = handleResidentVehicleParking(selectedVehicle, residentId);
                }
            }

            // Step 4: Finalize the transaction based on the outcome.
            if (success) {
                TransactionManager.commitTransaction(conn);
            } else {
                TransactionManager.rollbackTransaction(conn);
            }

        } catch (SQLException e) {
            System.err.println("A database error occurred during the parking process: " + e.getMessage());
            TransactionManager.rollbackTransaction(conn); // Rollback on any SQL exception.
        } finally {
            // Always ensure the connection is returned to its default state.
            TransactionManager.endTransaction(conn);
        }
    }

    public void removeParkedVehicle(String residentId) {
        Connection conn = null;
        try {
            conn = this.connection;
            TransactionManager.beginTransaction(conn);


            List<ParkedVehicleInfo> parkedVehicles = getAllParkedVehiclesWithLocation(residentId);
            if (parkedVehicles.isEmpty()) {
                System.out.println("You have no vehicles currently parked.");
                TransactionManager.rollbackTransaction(conn);
                return;
            }


            System.out.println("\n--- Select a Vehicle to Remove ---");
            for (int i = 0; i < parkedVehicles.size(); i++) {
                ParkedVehicleInfo info = parkedVehicles.get(i);
                System.out.printf("%d. %s (Parked in: %s)%n", i + 1, info.vehicle, info.parkingType);
            }
            System.out.println((parkedVehicles.size() + 1) + ". Cancel");
            System.out.print("Enter your choice: ");
            int choice = InputHandler.getValidChoice(1, parkedVehicles.size() + 1);

            if (choice > parkedVehicles.size()) {
                System.out.println("Removal cancelled.");
                TransactionManager.rollbackTransaction(conn);
                return;
            }


            ParkedVehicleInfo selected = parkedVehicles.get(choice - 1);
            String vehicleNumber = selected.vehicle.getVehicleNumber();
            String parkingType = selected.parkingType;
            String sourceTable = "Spare".equals(parkingType) ? "spare_parked_vehicle" : "parked_vehicle";
            int slotId = getSlotIdForVehicle(vehicleNumber, sourceTable);

            System.out.println("DEBUG: Processing vehicle: " + vehicleNumber + " in slot: " + slotId + " from table: " + sourceTable);

            String archiveQuery = "INSERT INTO parking_records (Slot_id, vehicle_number, time_out) VALUES(?, ?, NOW())";
            try (PreparedStatement ps = connection.prepareStatement(archiveQuery)) {
                ps.setInt(1, slotId);
                ps.setString(2, vehicleNumber);
                int rowsInserted = ps.executeUpdate();
                System.out.println("DEBUG: Archived " + rowsInserted + " record(s) to parking_records");
            }


            String deleteParkingQuery = "DELETE FROM " + sourceTable + " WHERE vehicle_number = ?";
            try (PreparedStatement ps = connection.prepareStatement(deleteParkingQuery)) {
                ps.setString(1, vehicleNumber);
                int rowsDeleted = ps.executeUpdate();
                System.out.println("DEBUG: Deleted " + rowsDeleted + " record(s) from " + sourceTable);
            }


            if ("Spare".equals(parkingType)) {
                String deleteVehicleQuery = "DELETE FROM vehicle WHERE vehicle_number = ?";
                try (PreparedStatement ps = connection.prepareStatement(deleteVehicleQuery)) {
                    ps.setString(1, vehicleNumber);
                    int rowsDeleted = ps.executeUpdate();
                    System.out.println("DEBUG: Deleted " + rowsDeleted + " record(s) from vehicle table");
                }
                System.out.println("Vehicle " + vehicleNumber + " removed from spare slot " + slotId + " and permanently deleted from your vehicle list.");
            } else {
                System.out.println("Vehicle " + vehicleNumber + " removed from resident slot " + slotId + ".");
            }

            TransactionManager.commitTransaction(conn);

        } catch (SQLException e) {
            System.err.println("Database error while removing vehicle: " + e.getMessage());
            TransactionManager.rollbackTransaction(conn);
        } catch (Exception e) {
            System.err.println("Unexpected error while removing vehicle: " + e.getMessage());
            TransactionManager.rollbackTransaction(conn);
        } finally {
            TransactionManager.endTransaction(conn);
        }
    }


    public void viewMyParkedVehicles(String residentId) {
        System.out.println("\n--- My Parked Vehicles ---");
        String query = "SELECT ap.Slot_id, ap.vehicle_number, v.vehicle_brand, v.vehicle_type, ap.time_in, " +
                "CASE WHEN ap.Slot_id BETWEEN ? AND ? THEN 'Resident' ELSE 'Spare' END AS parking_type " +
                "FROM (SELECT Slot_id, vehicle_number, time_in FROM parked_vehicle " +
                "      UNION ALL " +
                "      SELECT Slot_id, vehicle_number, time_in FROM spare_parked_vehicle) AS ap " +
                "JOIN vehicle v ON ap.vehicle_number = v.vehicle_number " +
                "WHERE v.resident_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, FOUR_WHEELER_START_SLOT);
            ps.setInt(2, TWO_WHEELER_END_SLOT);
            ps.setString(3, residentId);
            ResultSet rs = ps.executeQuery();

            System.out.println("------------------------------------------------------------------------------------------------");
            System.out.printf("%-8s | %-15s | %-12s | %-12s | %-20s | %-10s%n", "Slot ID", "Vehicle No.", "Brand", "Type", "Time In", "Park Type");
            System.out.println("------------------------------------------------------------------------------------------------");

            if (!rs.isBeforeFirst()) {
                System.out.println("You have no vehicles currently parked.");
            } else {
                while (rs.next()) {
                    System.out.printf("%-8d | %-15s | %-12s | %-12s | %-20s | %-10s%n",
                            rs.getInt("Slot_id"),
                            rs.getString("vehicle_number"),
                            rs.getString("vehicle_brand"),
                            rs.getString("vehicle_type"),
                            rs.getTimestamp("time_in").toString(),
                            rs.getString("parking_type"));
                }
            }
            System.out.println("------------------------------------------------------------------------------------------------");
        } catch (SQLException e) {
            System.err.println("Database error while viewing your parked vehicles: " + e.getMessage());
        }
    }

    public void displayMyParkingHistory(String residentId, String parkingType) {
        String title;
        String query = "SELECT pr.record_id, pr.Slot_id, pr.vehicle_number, v.vehicle_brand, v.vehicle_type, pr.time_in, pr.time_out, pr.total_hours, pr.charge_amount " +
                "FROM parking_records pr " +
                "JOIN vehicle v ON pr.vehicle_number = v.vehicle_number " +
                "WHERE v.resident_id = ? ";

        switch (parkingType) {
            case "Resident":
                title = "\n--- My Resident Parking History ---";
                query += "AND pr.Slot_id BETWEEN ? AND ? ";
                break;
            case "Spare":
                title = "\n--- My Spare Parking History ---";
                query += "AND pr.Slot_id BETWEEN ? AND ? ";
                break;
            default: // "All"
                title = "\n--- My Complete Parking History ---";
                break;
        }
        query += "ORDER BY pr.time_out DESC";

        System.out.println(title);

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, residentId);
            if ("Resident".equals(parkingType)) {
                ps.setInt(2, FOUR_WHEELER_START_SLOT);
                ps.setInt(3, TWO_WHEELER_END_SLOT);
            } else if ("Spare".equals(parkingType)) {
                ps.setInt(2, SPARE_START_SLOT);
                ps.setInt(3, SPARE_END_SLOT);
            }

            ResultSet rs = ps.executeQuery();

            if ("Resident".equals(parkingType)) {
                // Resident-specific printing
                System.out.println("-----------------------------------------------------------------------------------------------------------");
                System.out.printf("%-8s | %-15s | %-12s | %-12s | %-20s | %-20s | %-10s%n",
                        "Slot ID", "Vehicle No.", "Brand", "Type", "Time In", "Time Out", "Total Hrs");
                System.out.println("-----------------------------------------------------------------------------------------------------------");

                if (!rs.isBeforeFirst()) {
                    System.out.println("No parking history found for this selection.");
                } else {
                    while (rs.next()) {
                        System.out.printf("%-8d | %-15s | %-12s | %-12s | %-20s | %-20s | %-10.2f%n",
                                rs.getInt("Slot_id"),
                                rs.getString("vehicle_number"),
                                rs.getString("vehicle_brand"),
                                rs.getString("vehicle_type"),
                                rs.getTimestamp("time_in").toString(),
                                rs.getTimestamp("time_out").toString(),
                                rs.getBigDecimal("total_hours"));
                    }
                }
                System.out.println("-----------------------------------------------------------------------------------------------------------");
            } else { // For "Spare" and "All"
                System.out.println("-------------------------------------------------------------------------------------------------------------------------");
                System.out.printf("%-8s | %-15s | %-12s | %-12s | %-20s | %-20s | %-10s | %-12s%n",
                        "Slot ID", "Vehicle No.", "Brand", "Type", "Time In", "Time Out", "Total Hrs", "Charge(Rs.)");
                System.out.println("-------------------------------------------------------------------------------------------------------------------------");

                if (!rs.isBeforeFirst()) {
                    System.out.println("No parking history found for this selection.");
                } else {
                    while (rs.next()) {
                        System.out.printf("%-8d | %-15s | %-12s | %-12s | %-20s | %-20s | %-10.2f | %-12.2f%n",
                                rs.getInt("Slot_id"),
                                rs.getString("vehicle_number"),
                                rs.getString("vehicle_brand"),
                                rs.getString("vehicle_type"),
                                rs.getTimestamp("time_in").toString(),
                                rs.getTimestamp("time_out").toString(),
                                rs.getBigDecimal("total_hours"),
                                rs.getBigDecimal("charge_amount"));
                    }
                }
                System.out.println("-------------------------------------------------------------------------------------------------------------------------");
            }
        } catch (SQLException e) {
            System.err.println("Database error while viewing your parking history: " + e.getMessage());
        }
    }

    public void viewParkingSlots() {
        String wing = selectWing();
        if ("BACK".equals(wing)) return;
        displayParkedVehicles("Resident", wing);
    }

    public void viewSpareParkedVehicles() {
        displayParkedVehicles("Spare", null);
    }

    public void viewAllParkedVehicles() {
        displayParkedVehicles("All", null);
    }

    public void viewAvailableParkingSlots() {
        System.out.println("\n--- Available Resident Parking Slots (All Wings) ---");
        try {

            int occupied4Wheeler = getOccupiedSlotCount("parked_vehicle", FOUR_WHEELER_START_SLOT, FOUR_WHEELER_END_SLOT, null);
            int occupied2Wheeler = getOccupiedSlotCount("parked_vehicle", TWO_WHEELER_START_SLOT, TWO_WHEELER_END_SLOT, null);


            System.out.println("--------------------------------------------------------------------");
            System.out.printf("%-15s | %-12s | %-15s | %-15s%n", "Vehicle Type", "Total Slots", "Occupied Slots", "Available Slots");
            System.out.println("--------------------------------------------------------------------");
            System.out.printf("%-15s | %-12d | %-15d | %-15d%n", "4-Wheeler", TOTAL_4_WHEELER_SLOTS, occupied4Wheeler, TOTAL_4_WHEELER_SLOTS - occupied4Wheeler);
            System.out.printf("%-15s | %-12d | %-15d | %-15d%n", "2-Wheeler", TOTAL_2_WHEELER_SLOTS, occupied2Wheeler, TOTAL_2_WHEELER_SLOTS - occupied2Wheeler);
            System.out.println("--------------------------------------------------------------------");

        } catch (SQLException e) {
            System.err.println("Database error while calculating available slots: " + e.getMessage());
        }
    }

    public void viewAvailableSpareParkingSlots() {
        System.out.println("\n--- Available Spare Parking Slots ---");
        try {
            int occupiedSpareSlots = getOccupiedSlotCount("spare_parked_vehicle", SPARE_START_SLOT, SPARE_END_SLOT, null);
            int availableSpareSlots = TOTAL_SPARE_SLOTS - occupiedSpareSlots;

            System.out.println("----------------------------------------------------------");
            System.out.printf("%-20s | %-15s | %-15s%n", "Total Spare Slots", "Occupied Slots", "Available Slots");
            System.out.println("----------------------------------------------------------");
            System.out.printf("%-20d | %-15d | %-15d%n", TOTAL_SPARE_SLOTS, occupiedSpareSlots, availableSpareSlots);
            System.out.println("----------------------------------------------------------");

        } catch (SQLException e) {
            System.err.println("Database error while calculating available spare slots: " + e.getMessage());
        }
    }

    public void viewResidentParkingHistory() {
        String wing = selectWing();
        if ("BACK".equals(wing)) return;
        displayParkingHistory("Resident", wing);
    }

    public void viewSpareParkingHistory() {
        displayParkingHistory("Spare", null); // No wing selection needed
    }

    public void viewAllParkingHistory() {
        displayParkingHistory("All", null);
    }

    private boolean handleResidentVehicleParking(Vehicle selectedVehicle, String residentId) {
        // Get vehicles parked only in resident slots to check against resident limits.
        List<Vehicle> residentParked = getResidentParkedVehicles(residentId);


        int slotStart, slotEnd;
        if ("4-wheeler".equalsIgnoreCase(selectedVehicle.getVehicleType())) {
            slotStart = FOUR_WHEELER_START_SLOT;
            slotEnd = FOUR_WHEELER_END_SLOT;
            if (hasParkedVehicleOfType(residentParked)) {
                System.out.println("✗ Parking failed: You have already parked your one allowed 4-wheeler in a resident slot.");
                return false;
            }
        } else {
            slotStart = TWO_WHEELER_START_SLOT;
            slotEnd = TWO_WHEELER_END_SLOT;
            if (countParkedVehicleOfType(residentParked, "2-wheeler") >= 2) {
                System.out.println("✗ Parking failed: You have already parked your two allowed 2-wheelers in resident slots.");
                return false;
            }
        }

        // Get a valid, unoccupied slot from the user.
        int chosenSlotId;
        while (true) {
            System.out.printf("Please choose a slot for your %s.%n", selectedVehicle.getVehicleType());
            System.out.printf("Enter a parking slot number between %d and %d (or 0 to cancel): ", slotStart, slotEnd);
            chosenSlotId = InputHandler.getIntegerInput();

            if (chosenSlotId == 0) {
                System.out.println("Parking cancelled.");
                return false;
            }

            if (chosenSlotId < slotStart || chosenSlotId > slotEnd) {
                System.out.printf("✗ Invalid slot. %ss must be parked in slots %d-%d.%n", selectedVehicle.getVehicleType(), slotStart, slotEnd);
                continue;
            }

            if (isSlotOccupied(chosenSlotId)) {
                System.out.println("✗ Slot " + chosenSlotId + " is already occupied. Please choose another slot.");
            } else {
                break;
            }
        }

        // Park the vehicle in the chosen slot.
        System.out.println("✓ Slot " + chosenSlotId + " is available. Parking your vehicle...");
        return parkVehicleInDatabase(selectedVehicle, chosenSlotId);
    }


    private boolean handleNewVehicleInSpareParking(String residentId) throws SQLException {

        VehicleDAO vehicleDAOForTx = new VehicleDAO(connection);
        ResidentDAO residentDAOForTx = new ResidentDAO(connection);

        String vehicleType = InputHandler.getValidVehicleTypeInput("Select Vehicle Type");

        String vehicleNumber;
        while (true) {
            vehicleNumber = InputHandler.getValidStringInput("Enter Vehicle Number (e.g., GJ05CD5678): ").toUpperCase();
            if (!InputHandler.isValidVehicleNumber(vehicleNumber)) {
                System.out.println("Invalid format. Please use the format 'LLDDLLDDDD' (e.g., GJ05CD5678).");
                continue;
            }
            if (vehicleDAOForTx.vehicleExists(vehicleNumber)) {
                System.out.println("Error: Vehicle number '" + vehicleNumber + "' already exists.");
                continue;
            }
            break;
        }

        String vehicleBrand = InputHandler.getValidStringInput("Enter Vehicle Brand: ");
        Vehicle newVehicle = new Vehicle(vehicleNumber, residentId, vehicleType, vehicleBrand);

        if (vehicleDAOForTx.addVehicle(newVehicle)) {
            residentDAOForTx.updateResidentVehicleCount(residentId, 1); // Increment count
            System.out.println("Vehicle '" + vehicleNumber + "' added successfully!");
        }


        int chosenSlotId;
        while (true) {
            System.out.printf("Please choose a spare slot for your new %s.%n", newVehicle.getVehicleType());
            System.out.printf("Enter a parking slot number between %d and %d (or 0 to cancel): ", SPARE_START_SLOT, SPARE_END_SLOT);
            chosenSlotId = InputHandler.getIntegerInput();

            if (chosenSlotId == 0) {
                System.out.println("Parking cancelled.");
                return false;
            }

            if (chosenSlotId < SPARE_START_SLOT || chosenSlotId > SPARE_END_SLOT) {
                System.out.printf("✗ Invalid slot. Spare slots are in the range %d-%d.%n", SPARE_START_SLOT, SPARE_END_SLOT);
                continue;
            }

            if (isSpareSlotOccupied(chosenSlotId)) {
                System.out.println("✗ Slot " + chosenSlotId + " is already occupied. Please choose another slot.");
            } else {
                break;
            }
        }

        System.out.println("✓ Spare slot " + chosenSlotId + " is available. Parking your vehicle...");
        return parkVehicleInSpareDatabase(newVehicle, chosenSlotId);
    }


    private List<Vehicle> getCurrentlyParkedVehicles(String residentId) {
        List<Vehicle> parked = new ArrayList<>();
        String query = "SELECT v.vehicle_number, v.vehicle_type, v.vehicle_brand, v.resident_id " +
                "FROM ( " +
                "    SELECT vehicle_number FROM parked_vehicle " +
                "    UNION ALL " +
                "    SELECT vehicle_number FROM spare_parked_vehicle " +
                ") AS all_parked " +
                "JOIN vehicle v ON all_parked.vehicle_number = v.vehicle_number " +
                "WHERE v.resident_id = ?";

        try (PreparedStatement ps = this.connection.prepareStatement(query)) {
            ps.setString(1, residentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Vehicle vehicle = createVehicleFromResultSet(rs);
                parked.add(vehicle);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all currently parked vehicles: " + e.getMessage());
        }
        return parked;
    }

    private List<Vehicle> getResidentParkedVehicles(String residentId) {
        List<Vehicle> parked = new ArrayList<>();
        String query = "SELECT v.vehicle_number, v.vehicle_type, v.vehicle_brand, v.resident_id " +
                "FROM parked_vehicle pv " +
                "JOIN vehicle v ON pv.vehicle_number = v.vehicle_number " +
                "WHERE v.resident_id = ?";

        try (PreparedStatement ps = this.connection.prepareStatement(query)) {
            ps.setString(1, residentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Vehicle vehicle = createVehicleFromResultSet(rs);
                parked.add(vehicle);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching resident parked vehicles: " + e.getMessage());
        }
        return parked;
    }


    private List<Vehicle> getAllVehiclesForResident(String residentId) {
        List<Vehicle> allVehicles = new ArrayList<>();
        String query = "SELECT vehicle_number, resident_id, vehicle_type, vehicle_brand FROM vehicle WHERE resident_id = ?";
        try (PreparedStatement ps = this.connection.prepareStatement(query)) {
            ps.setString(1, residentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                allVehicles.add(createVehicleFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all vehicles for resident: " + e.getMessage());
        }
        return allVehicles;
    }


    private List<ParkedVehicleInfo> getAllParkedVehiclesWithLocation(String residentId) {
        List<ParkedVehicleInfo> allParked = new ArrayList<>();
        String query = "(SELECT v.vehicle_number, v.vehicle_type, v.vehicle_brand, v.resident_id, 'Resident' as parking_type " +
                "FROM parked_vehicle pv JOIN vehicle v ON pv.vehicle_number = v.vehicle_number WHERE v.resident_id = ?) " +
                "UNION ALL " +
                "(SELECT v.vehicle_number, v.vehicle_type, v.vehicle_brand, v.resident_id, 'Spare' as parking_type " +
                "FROM spare_parked_vehicle spv JOIN vehicle v ON spv.vehicle_number = v.vehicle_number WHERE v.resident_id = ?)";

        try (PreparedStatement ps = this.connection.prepareStatement(query)) {
            ps.setString(1, residentId);
            ps.setString(2, residentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Vehicle vehicle = createVehicleFromResultSet(rs);
                String parkingType = rs.getString("parking_type");
                allParked.add(new ParkedVehicleInfo(vehicle, parkingType));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all parked vehicles: " + e.getMessage());
        }
        return allParked;
    }



    private List<Vehicle> getUnparkedVehicles(List<Vehicle> allVehicles, List<Vehicle> parkedVehicles) {
        List<Vehicle> unparked = new ArrayList<>();
        List<String> parkedNumbers = new ArrayList<>();
        for (Vehicle parkedVehicle : parkedVehicles) {
            parkedNumbers.add(parkedVehicle.getVehicleNumber());
        }

        for (Vehicle vehicle : allVehicles) {
            if (!parkedNumbers.contains(vehicle.getVehicleNumber())) {
                unparked.add(vehicle);
            }
        }
        return unparked;
    }


    private Vehicle createVehicleFromResultSet(ResultSet rs) throws SQLException {
        return new Vehicle(
                rs.getString("vehicle_number"),
                rs.getString("resident_id"),
                rs.getString("vehicle_type"),
                rs.getString("vehicle_brand")
        );
    }


    private void displayParkedVehicleStatus(String residentId) {
        List<Vehicle> residentParked = getResidentParkedVehicles(residentId);
        long fourWheelerCount = countParkedVehicleOfType(residentParked, "4-wheeler");
        long twoWheelerCount = countParkedVehicleOfType(residentParked, "2-wheeler");

        System.out.printf("Resident 4-Wheeler Slots Used: %d / 1%n", fourWheelerCount);
        System.out.printf("Resident 2-Wheeler Slots Used: %d / 2%n", twoWheelerCount);
    }


    private boolean hasParkedVehicleOfType(List<Vehicle> parkedList) {
        for (Vehicle v : parkedList) {
            if (v.getVehicleType().equalsIgnoreCase("4-wheeler")) {
                return true;
            }
        }
        return false;
    }


    private long countParkedVehicleOfType(List<Vehicle> parkedList, String type) {
        long count = 0;
        for (Vehicle v : parkedList) {
            if (v.getVehicleType().equalsIgnoreCase(type)) {
                count++;
            }
        }
        return count;
    }


    private boolean isSlotOccupied(int slotId) {
        String query = "SELECT COUNT(*) FROM parked_vehicle WHERE Slot_id = ?";
        try (PreparedStatement ps = this.connection.prepareStatement(query)) {
            ps.setInt(1, slotId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking slot status: " + e.getMessage());
        }
        return true; // Fail-safe: assume slot is occupied if there's a DB error.
    }


    private boolean isSpareSlotOccupied(int slotId) {
        String query = "SELECT COUNT(*) FROM spare_parked_vehicle WHERE Slot_id = ?";
        try (PreparedStatement ps = this.connection.prepareStatement(query)) {
            ps.setInt(1, slotId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking spare slot status: " + e.getMessage());
        }
        return true; // Fail-safe
    }

    private boolean parkVehicleInDatabase(Vehicle vehicle, int slotId) {
        String query = "INSERT INTO parked_vehicle (Slot_id, vehicle_number, time_in) VALUES (?, ?, NOW())";
        try (PreparedStatement ps = this.connection.prepareStatement(query)) {
            ps.setInt(1, slotId);
            ps.setString(2, vehicle.getVehicleNumber());
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✓ Success! Vehicle " + vehicle.getVehicleNumber() + " has been parked in slot " + slotId + ".");
            }
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("✗ Error: Could not park vehicle in database.");
            System.err.println(e.getMessage());
            return false;
        }
    }
    private boolean parkVehicleInSpareDatabase(Vehicle vehicle, int slotId) {
        String query = "INSERT INTO spare_parked_vehicle (Slot_id, vehicle_number, time_in) VALUES (?, ?, NOW())";
        try (PreparedStatement ps = this.connection.prepareStatement(query)) {
            ps.setInt(1, slotId);
            ps.setString(2, vehicle.getVehicleNumber());
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✓ Success! Vehicle " + vehicle.getVehicleNumber() + " has been parked in spare slot " + slotId + ".");
            }
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("✗ Error: Could not park vehicle in spare slot database.");
            System.err.println(e.getMessage());
            return false;
        }
    }


    private void displayParkedVehicles(String viewType, String wing) {
        String query;
        String title;
        int serialNumber = 1;

        switch (viewType) {
            case "Resident":
                title = (wing != null) ? "\n--- Parked Vehicles of Wing " + wing + " Residents ---" : "\n--- All Resident Parked Vehicles ---";
                query = "SELECT pv.Slot_id, pv.vehicle_number, v.resident_id, r.first_name, r.last_name, r.wing, v.vehicle_type, v.vehicle_brand, pv.time_in " +
                        "FROM parked_vehicle pv " +
                        "JOIN vehicle v ON pv.vehicle_number = v.vehicle_number " +
                        "JOIN residents r ON v.resident_id = r.resident_id";
                if (wing != null) {
                    query += " WHERE r.wing = ?";
                }
                query += " ORDER BY pv.time_in DESC";
                break;

            case "Spare":
                title = "\n--- Spare Parked Vehicles ---";
                query = "SELECT spv.Slot_id, spv.vehicle_number, v.resident_id, r.first_name, r.last_name, r.wing, v.vehicle_type, v.vehicle_brand, spv.time_in " +
                        "FROM spare_parked_vehicle spv " +
                        "JOIN vehicle v ON spv.vehicle_number = v.vehicle_number " +
                        "JOIN residents r ON v.resident_id = r.resident_id ORDER BY spv.time_in DESC";
                break;

            case "All":
                title = "\n--- All Parked Vehicles (Resident + Spare) ---";
                query = "(SELECT pv.Slot_id, pv.vehicle_number, r.resident_id, r.first_name, r.last_name, r.wing, v.vehicle_type, pv.time_in as entry_time, 'Resident' as parking_type " +
                        "FROM parked_vehicle pv " +
                        "JOIN vehicle v ON pv.vehicle_number = v.vehicle_number " +
                        "JOIN residents r ON v.resident_id = r.resident_id) " +
                        "UNION ALL " +
                        "(SELECT spv.Slot_id, spv.vehicle_number, r.resident_id, r.first_name, r.last_name, r.wing, v.vehicle_type, spv.time_in as entry_time, 'Spare' as parking_type " +
                        "FROM spare_parked_vehicle spv " +
                        "JOIN vehicle v ON spv.vehicle_number = v.vehicle_number " +
                        "JOIN residents r ON v.resident_id = r.resident_id) " +
                        "ORDER BY entry_time DESC";
                break;

            default:
                return;
        }

        System.out.println(title);

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            if ("Resident".equals(viewType) && wing != null) {
                ps.setString(1, wing);
            }
            ResultSet rs = ps.executeQuery();

            if (!rs.isBeforeFirst()) {
                System.out.println("No vehicles are currently parked in this selection.");
                return;
            }


            switch (viewType) {
                case "Resident":
                    if (wing != null) { // Specific wing
                        System.out.println("------------------------------------------------------------------------------------------------------------------");
                        System.out.printf("%-5s | %-8s | %-15s | %-10s | %-18s | %-12s | %-12s | %-20s%n", "Sr.", "Slot ID", "Vehicle No.", "Res. ID", "Owner Name", "Type", "Brand", "Time In");
                        System.out.println("------------------------------------------------------------------------------------------------------------------");
                        while (rs.next()) {
                            String ownerName = rs.getString("first_name") + " " + rs.getString("last_name");
                            System.out.printf("%-5d | %-8d | %-15s | %-10s | %-18s | %-12s | %-12s | %-20s%n",
                                    serialNumber++, rs.getInt("Slot_id"), rs.getString("vehicle_number"),
                                    rs.getString("resident_id"), ownerName, rs.getString("vehicle_type"),
                                    rs.getString("vehicle_brand"), rs.getTimestamp("time_in").toString());
                        }
                        System.out.println("------------------------------------------------------------------------------------------------------------------");
                    } else { // All wings
                        System.out.println("---------------------------------------------------------------------------------------------------------------------------------");
                        System.out.printf("%-5s | %-8s | %-15s | %-10s | %-18s | %-5s | %-12s | %-12s | %-20s%n", "Sr.", "Slot ID", "Vehicle No.", "Res. ID", "Owner Name", "Wing", "Type", "Brand", "Time In");
                        System.out.println("---------------------------------------------------------------------------------------------------------------------------------");
                        while (rs.next()) {
                            String ownerName = rs.getString("first_name") + " " + rs.getString("last_name");
                            System.out.printf("%-5d | %-8d | %-15s | %-10s | %-18s | %-5s | %-12s | %-12s | %-20s%n",
                                    serialNumber++, rs.getInt("Slot_id"), rs.getString("vehicle_number"),
                                    rs.getString("resident_id"), ownerName, rs.getString("wing"),
                                    rs.getString("vehicle_type"), rs.getString("vehicle_brand"),
                                    rs.getTimestamp("time_in").toString());
                        }
                        System.out.println("---------------------------------------------------------------------------------------------------------------------------------");
                    }
                    break;

                case "Spare":
                    System.out.println("---------------------------------------------------------------------------------------------------------------------------------");
                    System.out.printf("%-5s | %-8s | %-15s | %-10s | %-18s | %-5s | %-12s | %-12s | %-20s%n", "Sr.", "Slot ID", "Vehicle No.", "Res. ID", "Owner Name", "Wing", "Type", "Brand", "Time In");
                    System.out.println("---------------------------------------------------------------------------------------------------------------------------------");
                    while (rs.next()) {
                        String ownerName = rs.getString("first_name") + " " + rs.getString("last_name");
                        System.out.printf("%-5d | %-8d | %-15s | %-10s | %-18s | %-5s | %-12s | %-12s | %-20s%n",
                                serialNumber++, rs.getInt("Slot_id"), rs.getString("vehicle_number"),
                                rs.getString("resident_id"), ownerName, rs.getString("wing"),
                                rs.getString("vehicle_type"), rs.getString("vehicle_brand"), rs.getTimestamp("time_in").toString());
                    }
                    System.out.println("---------------------------------------------------------------------------------------------------------------------------------");
                    break;

                case "All":
                    System.out.println("--------------------------------------------------------------------------------------------------------------------------------");
                    System.out.printf("%-5s | %-8s | %-15s | %-10s | %-18s | %-5s | %-12s | %-20s | %-10s%n", "Sr.", "Slot ID", "Vehicle No.", "Res. ID", "Owner Name", "Wing", "Type", "Time In", "Park Type");
                    System.out.println("--------------------------------------------------------------------------------------------------------------------------------");
                    while (rs.next()) {
                        String ownerName = rs.getString("first_name") + " " + rs.getString("last_name");
                        System.out.printf("%-5d | %-8d | %-15s | %-10s | %-18s | %-5s | %-12s | %-20s | %-10s%n",
                                serialNumber++, rs.getInt("Slot_id"), rs.getString("vehicle_number"),
                                rs.getString("resident_id"), ownerName, rs.getString("wing"),
                                rs.getString("vehicle_type"), rs.getTimestamp("entry_time").toString(),
                                rs.getString("parking_type"));
                    }
                    System.out.println("--------------------------------------------------------------------------------------------------------------------------------");
                    break;
            }

        } catch (SQLException e) {
            System.err.println("Database error while viewing parked vehicles: " + e.getMessage());
        }
    }

    private void displayParkingHistory(String parkingType, String wing) {
        String title;
       
        String query = "SELECT r.first_name, r.last_name, v.resident_id, pr.Slot_id, pr.vehicle_number, v.vehicle_type, v.vehicle_brand, " +
                "pr.time_in, pr.time_out, pr.total_hours, r.wing, pr.charge_amount " +
                "FROM parking_records pr " +
                "LEFT JOIN vehicle v ON pr.vehicle_number = v.vehicle_number " +
                "LEFT JOIN residents r ON v.resident_id = r.resident_id";

        String whereClause = "";
        String viewTypeForRecord;

        switch (parkingType) {
            case "Resident":
                // MODIFIED: Titles are now more accurate as per your request.
                title = (wing != null) ? "\n--- Parking History of Wing " + wing + " Members ---" : "\n--- Parking History of All Wing Members ---";
                whereClause = " WHERE pr.Slot_id BETWEEN " + FOUR_WHEELER_START_SLOT + " AND " + TWO_WHEELER_END_SLOT;
                viewTypeForRecord = (wing != null) ? "RESIDENT_WING" : "RESIDENT_ALL";
                break;
            case "Spare":
                title = "\n--- Spare Parking History ---";
                whereClause = " WHERE pr.Slot_id BETWEEN " + SPARE_START_SLOT + " AND " + SPARE_END_SLOT;
                viewTypeForRecord = "SPARE";
                break;
            default: // "All"
                title = "\n--- Complete Parking History (All Wings) ---";
                viewTypeForRecord = "ALL_HISTORY";
                break;
        }

        if (wing != null) {
            // Append the wing condition to the WHERE clause
            if (whereClause.isEmpty()) {
                whereClause = " WHERE r.wing = ?";
            } else {
                whereClause += " AND r.wing = ?";
            }
        }

        // Add the completed WHERE clause to the main query
        query += whereClause;

        // Add the ORDER BY clause
        if ("All".equals(parkingType)) {
            query += " ORDER BY pr.charge_amount DESC, pr.time_out DESC";
        } else {
            query += " ORDER BY pr.time_out DESC";
        }

        System.out.println(title);

        ds.ParkingHistoryLinkedList historyList = new ds.ParkingHistoryLinkedList();
        try (java.sql.PreparedStatement ps = connection.prepareStatement(query)) {
            if (wing != null) {
                ps.setString(1, wing);
            }
            java.sql.ResultSet rs = ps.executeQuery();
            int serialNumber = 1;

            while (rs.next()) {
                String ownerName = rs.getString("first_name") + " " + rs.getString("last_name");
                double cost = rs.getDouble("charge_amount");
                String generatedParkingType = cost > 0 ? "Spare" : "Resident";

                ds.ParkingRecord record = new ds.ParkingRecord(
                        serialNumber++,
                        ownerName,
                        rs.getString("resident_id"),
                        rs.getInt("Slot_id"),
                        rs.getString("vehicle_number"),
                        rs.getString("vehicle_type"),
                        rs.getString("vehicle_brand"),
                        rs.getString("wing"),
                        rs.getTimestamp("time_in").toString(),
                        rs.getTimestamp("time_out").toString(),
                        rs.getBigDecimal("total_hours").doubleValue(),
                        generatedParkingType,
                        cost,
                        viewTypeForRecord
                );
                historyList.insertAtLast(record);
            }

            switch (viewTypeForRecord) {
                case "RESIDENT_WING":
                    System.out.println("-------------------------------------------------------------------------------------------------------------------------------------------------------------");
                    System.out.printf("%-5s | %-18s | %-10s | %-8s | %-15s | %-12s | %-15s | %-20s | %-20s | %-10s%n",
                            "Sr.", "Owner Name", "Res. ID", "Slot ID", "Vehicle No.", "Type", "Brand", "Time In", "Time Out", "Total Hrs");
                    System.out.println("-------------------------------------------------------------------------------------------------------------------------------------------------------------");
                    break;
                case "RESIDENT_ALL":
                    System.out.println("-------------------------------------------------------------------------------------------------------------------------------------------------------------------");
                    System.out.printf("%-5s | %-18s | %-10s | %-8s | %-15s | %-12s | %-15s | %-5s | %-20s | %-20s | %-10s%n",
                            "Sr.", "Owner Name", "Res. ID", "Slot ID", "Vehicle No.", "Type", "Brand", "Wing", "Time In", "Time Out", "Total Hrs");
                    System.out.println("-------------------------------------------------------------------------------------------------------------------------------------------------------------------");
                    break;
                case "SPARE":
                    System.out.println("-------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
                    System.out.printf("%-5s | %-18s | %-10s | %-8s | %-15s | %-12s | %-15s | %-5s | %-20s | %-20s | %-10s | %-10s%n",
                            "Sr.", "Owner Name", "Res. ID", "Slot ID", "Vehicle No.", "Type", "Brand", "Wing", "Time In", "Time Out", "Total Hrs", "Cost (Rs.)");
                    System.out.println("-------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
                    break;
                case "ALL_HISTORY":
                    System.out.println("------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
                    System.out.printf("%-5s | %-18s | %-10s | %-8s | %-15s | %-12s | %-15s | %-5s | %-20s | %-20s | %-10s | %-12s | %-10s%n",
                            "Sr.", "Owner Name", "Res. ID", "Slot ID", "Vehicle No.", "Type", "Brand", "Wing", "Time In", "Time Out", "Total Hrs", "Parking Type", "Cost (Rs.)");
                    System.out.println("------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
                    break;
            }
            historyList.display();

        } catch (java.sql.SQLException e) {
            System.err.println("Database error while viewing parking history: " + e.getMessage());
        }
    }

    private int getOccupiedSlotCount(String tableName, int startSlot, int endSlot, String wing) throws SQLException {
        String query = "SELECT COUNT(pv.Slot_id) FROM " + tableName + " pv JOIN vehicle v ON pv.vehicle_number = v.vehicle_number ";
        if (wing != null) {
            query += "JOIN residents r ON v.resident_id = r.resident_id WHERE r.wing = ? AND pv.Slot_id BETWEEN ? AND ?";
        } else {
            query += "WHERE pv.Slot_id BETWEEN ? AND ?";
        }

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            if (wing != null) {
                ps.setString(1, wing);
                ps.setInt(2, startSlot);
                ps.setInt(3, endSlot);
            } else {
                ps.setInt(1, startSlot);
                ps.setInt(2, endSlot);
            }
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private int getSlotIdForVehicle(String vehicleNumber, String tableName) throws SQLException {
        String query = "SELECT Slot_id FROM " + tableName + " WHERE vehicle_number = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, vehicleNumber);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt("Slot_id") : -1;
        }
    }

    private String selectWing() {
        System.out.println("\n--- Select a Wing ---");
        System.out.println("1. Wing A");
        System.out.println("2. Wing B");
        System.out.println("3. Wing C");
        System.out.println("4. All Wings");
        System.out.println("5. Back to Menu");
        System.out.print("select option: ");
        int choice = InputHandler.getValidChoice(1, 5);
        return switch (choice) {
            case 1 -> "A";
            case 2 -> "B";
            case 3 -> "C";
            case 4 -> null; // Represents all wings
            default -> "BACK";
        };
    }
}
