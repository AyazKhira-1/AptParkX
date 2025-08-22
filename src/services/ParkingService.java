package services;

import dao.ResidentDAO;
import dao.VehicleDAO;
import ds.ParkingHistoryLinkedList;
import ds.ParkingRecord;
import model.Resident;
import model.Vehicle;
import ui.InputHandler;
import database.DatabaseManager;
import database.TransactionManager;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import ds.GuestVehicleQueue;
import ds.GuestVehicleQueue.WaitingListItem;
import java.sql.Statement;
import java.sql.Timestamp;

public final class ParkingService {

    private final Connection connection;
    private final ResidentDAO residentDAO;
    private final VehicleDAO vehicleDAO;

    // A private inner class to hold detailed information for display
    private static class ParkedVehicleDetails {
        final int slotId;
        final Vehicle vehicle;
        final java.sql.Timestamp timeIn; // MODIFIED: Changed from String to Timestamp
        final String parkingType;

        ParkedVehicleDetails(int slotId, Vehicle vehicle, java.sql.Timestamp timeIn, String parkingType) {
            this.slotId = slotId;
            this.vehicle = vehicle;
            this.timeIn = timeIn;
            this.parkingType = parkingType;
        }
    }

    // --- NEW SLOT CONFIGURATION ---
    public static final int WING_A_CAPACITY = 400;
    public static final int WING_B_CAPACITY = 400;
    public static final int WING_C_CAPACITY = 400;
    public static final int TOTAL_RESIDENT_SLOTS = 1200;

    public static final int SPARE_START_SLOT = 1201;
    public static final int SPARE_END_SLOT = 1350;
    public static final int TOTAL_SPARE_SLOTS = SPARE_END_SLOT - SPARE_START_SLOT + 1;

    public ParkingService() {
        try {
            this.connection = DatabaseManager.getConnection();
            this.residentDAO = new ResidentDAO(this.connection);
            this.vehicleDAO = new VehicleDAO(this.connection);
        } catch (SQLException e) {
            System.err.println("Database connection failed in ParkingService.");
            throw new RuntimeException(e);
        }
    }

    // --- RESIDENT-FACING METHODS ---

    public void parkVehicle(String residentId) {
        System.out.println("\n--- Park a Resident Vehicle ---");
        try {
            Resident resident = residentDAO.getResidentById(residentId);
            if (resident == null) {
                System.out.println("Could not find resident data.");
                return;
            }

            int wingCapacity = getWingCapacity(resident.getWing());
            int currentWingUsage = getParkedCountForWing(resident.getWing());

            System.out.printf("Wing %s Capacity Status: %d / %d slots used.%n", resident.getWing(), currentWingUsage, wingCapacity);
            if (currentWingUsage >= wingCapacity) {
                System.out.println("Sorry, the maximum parking capacity for your wing has been reached.");
                return;
            }

            List<Vehicle> allVehicles = vehicleDAO.getVehiclesByResidentId(residentId);
            List<Vehicle> parkedVehicles = vehicleDAO.getCurrentlyParkedVehiclesByResident(residentId);
            List<String> parkedNumbers = parkedVehicles.stream().map(Vehicle::getVehicleNumber).collect(Collectors.toList());
            List<Vehicle> unparkedVehicles = allVehicles.stream()
                    .filter(v -> !parkedNumbers.contains(v.getVehicleNumber()))
                    .collect(Collectors.toList());

            if (unparkedVehicles.isEmpty()) {
                System.out.println("You have no registered vehicles available to park.");
                return;
            }

            System.out.println("\n--- Select a Vehicle to Park ---");
            for (int i = 0; i < unparkedVehicles.size(); i++) {
                System.out.printf("%d. %s (%s)%n", i + 1, unparkedVehicles.get(i).getVehicleNumber(), unparkedVehicles.get(i).getVehicleBrand());
            }
            System.out.printf("%d. Cancel%n", unparkedVehicles.size() + 1);
            int choice = InputHandler.getValidChoice(1, unparkedVehicles.size() + 1);

            if (choice > unparkedVehicles.size()) {
                System.out.println("Parking cancelled.");
                return;
            }
            Vehicle vehicleToPark = unparkedVehicles.get(choice - 1);

            int chosenSlotId;
            while (true) {
                System.out.printf("Enter a parking slot number between 1 and %d (or 0 to cancel): ", TOTAL_RESIDENT_SLOTS);
                chosenSlotId = InputHandler.getIntegerInput();
                if (chosenSlotId == 0) { System.out.println("Parking cancelled."); return; }
                if (chosenSlotId < 1 || chosenSlotId > TOTAL_RESIDENT_SLOTS) {
                    System.out.printf("Invalid slot. Please choose a slot between 1 and %d.%n", TOTAL_RESIDENT_SLOTS);
                    continue;
                }
                if (isSlotOccupied("parked_vehicle", chosenSlotId)) {
                    System.out.println("Slot " + chosenSlotId + " is already occupied.");
                } else {
                    break;
                }
            }

            String query = "INSERT INTO parked_vehicle (Slot_id, vehicle_number, time_in) VALUES (?, ?, NOW())";
            try (PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setInt(1, chosenSlotId);
                ps.setString(2, vehicleToPark.getVehicleNumber());
                if (ps.executeUpdate() > 0) {
                    System.out.printf("✓ Success! Vehicle %s has been parked in slot %d.%n", vehicleToPark.getVehicleNumber(), chosenSlotId);
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error during parking process: " + e.getMessage());
        }
    }

    public void parkGuestVehicle(String residentId) {
        System.out.println("\n--- Park a Guest Vehicle ---");
        try {
            int occupiedSpare = getOccupiedSlotCount("spare_parked_vehicle", SPARE_START_SLOT, SPARE_END_SLOT);
            if (occupiedSpare >= TOTAL_SPARE_SLOTS) {
                System.out.println("Sorry, all guest parking slots are currently occupied.");
                return;
            }

            TransactionManager.beginTransaction(connection);
            String vehicleNumber = InputHandler.getValidVehicleNumberInput("Enter Guest Vehicle Number: ");

            if (vehicleDAO.vehicleExists(vehicleNumber)) {
                System.out.println("Error: This vehicle is already registered in the system.");
                TransactionManager.rollbackTransaction(connection);
                return;
            }
            String vehicleType = InputHandler.getValidVehicleTypeInput("Select Vehicle Type");
            String vehicleBrand = InputHandler.getValidStringInput("Enter Vehicle Brand: ");

            Vehicle guestVehicle = new Vehicle(vehicleNumber, residentId, vehicleType, vehicleBrand, "Guest");

            // MODIFIED: This query no longer includes the 'owner_type' column to match your database.
            String addVehicleQuery = "INSERT INTO vehicle (resident_id, vehicle_number, vehicle_type, vehicle_brand, owner_type) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = connection.prepareStatement(addVehicleQuery)) {
                ps.setString(1, guestVehicle.getResidentId());
                ps.setString(2, guestVehicle.getVehicleNumber());
                ps.setString(3, guestVehicle.getVehicleType());
                ps.setString(4, guestVehicle.getVehicleBrand());
                ps.setString(5, guestVehicle.getOwnerType()); // Pass the owner type
                ps.executeUpdate();
            }

            int chosenSlotId;
            while (true) {
                System.out.printf("Enter a guest parking slot between %d and %d (or 0 to cancel): ", SPARE_START_SLOT, SPARE_END_SLOT);
                chosenSlotId = InputHandler.getIntegerInput();
                if (chosenSlotId == 0) { System.out.println("Guest parking cancelled."); TransactionManager.rollbackTransaction(connection); return; }
                if (chosenSlotId < SPARE_START_SLOT || chosenSlotId > SPARE_END_SLOT) {
                    System.out.printf("Invalid slot. Please choose a slot between %d and %d.%n", SPARE_START_SLOT, SPARE_END_SLOT);
                    continue;
                }
                if (isSlotOccupied("spare_parked_vehicle", chosenSlotId)) {
                    System.out.println("Slot " + chosenSlotId + " is already occupied.");
                } else {
                    break;
                }
            }

            String query = "INSERT INTO spare_parked_vehicle (Slot_id, vehicle_number, time_in) VALUES (?, ?, NOW())";
            try (PreparedStatement ps = connection.prepareStatement(query)) {
                ps.setInt(1, chosenSlotId);
                ps.setString(2, guestVehicle.getVehicleNumber());
                if (ps.executeUpdate() > 0) {
                    System.out.printf("✓ Success! Guest vehicle %s parked in slot %d.%n", vehicleNumber, chosenSlotId);
                    TransactionManager.commitTransaction(connection);
                } else {
                    TransactionManager.rollbackTransaction(connection);
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error during guest parking: " + e.getMessage());
            TransactionManager.rollbackTransaction(connection);
        } finally {
            TransactionManager.endTransaction(connection);
        }
    }


    public void removeParkedVehicle(String residentId) {
        System.out.println("\n--- Remove a Parked Vehicle ---");
        try {
            List<ParkedVehicleDetails> parkedVehicles = getDetailedParkedVehiclesForResident(residentId);
            if (parkedVehicles.isEmpty()) {
                System.out.println("You have no vehicles currently parked.");
                return;
            }
            displayParkedVehicleTable(parkedVehicles);

            System.out.printf("Enter the Serial Number of the vehicle to remove (or %d to cancel): ", parkedVehicles.size() + 1);
            int choice = InputHandler.getValidChoice(1, parkedVehicles.size() + 1);

            if (choice > parkedVehicles.size()) {
                System.out.println("Removal cancelled.");
                return;
            }

            ParkedVehicleDetails vehicleToRemoveDetails = parkedVehicles.get(choice - 1);
            Vehicle vehicleToRemove = vehicleToRemoveDetails.vehicle;
            String vehicleNumber = vehicleToRemove.getVehicleNumber();
            int slotId = vehicleToRemoveDetails.slotId;
            boolean isGuestVehicle = "Guest".equals(vehicleToRemoveDetails.parkingType);
            String sourceTable = isGuestVehicle ? "spare_parked_vehicle" : "parked_vehicle";

            TransactionManager.beginTransaction(connection);

            java.sql.Timestamp timeIn = null;
            String getTimeInQuery = "SELECT time_in FROM " + sourceTable + " WHERE vehicle_number = ?";
            try (PreparedStatement ps = connection.prepareStatement(getTimeInQuery)) {
                ps.setString(1, vehicleNumber);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    timeIn = rs.getTimestamp("time_in");
                }
            }
            if (timeIn == null) {
                throw new SQLException("Could not retrieve original park time for vehicle " + vehicleNumber);
            }

            // --- MODIFIED: The query now includes the new resident_id column ---
            String archiveQuery = "INSERT INTO parking_records (Slot_id, vehicle_number, resident_id, vehicle_brand, vehicle_type, time_in, time_out) VALUES(?, ?, ?, ?, ?, ?, NOW())";
            long recordId = 0;
            try(PreparedStatement ps = connection.prepareStatement(archiveQuery, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, slotId);
                ps.setString(2, vehicleNumber);
                ps.setString(3, residentId); // <-- SAVES THE OWNER'S ID
                ps.setString(4, vehicleToRemove.getVehicleBrand());
                ps.setString(5, vehicleToRemove.getVehicleType());
                ps.setTimestamp(6, timeIn);
                ps.executeUpdate();
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    recordId = rs.getLong(1);
                }
            }

            String deleteQuery = "DELETE FROM " + sourceTable + " WHERE vehicle_number = ?";
            try(PreparedStatement ps = connection.prepareStatement(deleteQuery)) {
                ps.setString(1, vehicleNumber);
                ps.executeUpdate();
            }

            if (isGuestVehicle) {
                String deleteMainVehicleQuery = "DELETE FROM vehicle WHERE vehicle_number = ?";
                try (PreparedStatement ps = connection.prepareStatement(deleteMainVehicleQuery)) {
                    ps.setString(1, vehicleNumber);
                    ps.executeUpdate();
                }
            }

            if (isGuestVehicle && recordId > 0) {
                String costQuery = "SELECT charge_amount FROM parking_records WHERE record_id = ?";
                try (PreparedStatement ps = connection.prepareStatement(costQuery)) {
                    ps.setLong(1, recordId);
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        BigDecimal cost = rs.getBigDecimal("charge_amount");
                        if (cost != null && cost.compareTo(BigDecimal.ZERO) > 0) {
                            System.out.printf("Parking charge for guest vehicle %s: ₹%.2f%n", vehicleNumber, cost);
                        }
                    }
                }
            }

            TransactionManager.commitTransaction(connection);
            System.out.printf("✓ Vehicle %s removed from slot %d successfully.%n", vehicleNumber, slotId);

        } catch (SQLException e) {
            System.err.println("Database error while removing vehicle: " + e.getMessage());
            TransactionManager.rollbackTransaction(connection);
        } finally {
            TransactionManager.endTransaction(connection);
        }
    }

    public void viewMyParkedVehicles(String residentId) {
        System.out.println("\n--- My Parked Vehicles ---");
        try {
            List<ParkedVehicleDetails> parkedVehicles = getDetailedParkedVehiclesForResident(residentId);
            displayParkedVehicleTable(parkedVehicles);
        } catch (SQLException e) {
            System.err.println("Database error while viewing your parked vehicles: " + e.getMessage());
        }
    }

    public void displayMyParkingHistory(String residentId, String parkingType) {
        displayParkingHistory(parkingType, null, residentId);
    }
    // IN: services/ParkingService.java
// REPLACE the entire generateParkingHistoryReport method with this one.

    public void generateParkingHistoryReport() {
        System.out.println("\n--- Generate Complete Parking History Report ---");
        String fileName = InputHandler.getValidStringInput("Enter the name for the report file (e.g., parking_report.txt): ");

        try {
            // 1. Fetch all data (this part is the same)
            String query = "SELECT pr.*, r.first_name, r.last_name FROM parking_records pr " +
                    "LEFT JOIN residents r ON pr.resident_id = r.resident_id ORDER BY pr.time_out DESC";

            // 2. Build the report content using basic String concatenation (+)
            String reportContent = "";

            // Build the header using our new padRight helper method
            String header = padRight("Sr.", 4) + " | " +
                    padRight("Owner Name", 20) + " | " +
                    padRight("Slot ID", 8) + " | " +
                    padRight("Vehicle No.", 15) + " | " +
                    padRight("Brand", 12) + " | " +
                    padRight("Type", 12) + " | " +
                    padRight("Park Type", 10) + " | " +
                    padRight("Time In", 22) + " | " +
                    padRight("Time Out", 22) + " | " +
                    padRight("Total Hrs", 10) + " | " +
                    padRight("Charge(Rs.)", 12) + "\n";
            reportContent += header;

            // Create a separator line of the same length as the header
            String separator = "";
            for (int i = 0; i < header.length() - 1; i++) {
                separator += "-";
            }
            reportContent += separator + "\n";


            try (PreparedStatement ps = connection.prepareStatement(query)) {
                ResultSet rs = ps.executeQuery();
                int serial = 1;

                while (rs.next()) {
                    String firstName = rs.getString("first_name");
                    String ownerName = (firstName != null) ? firstName + " " + rs.getString("last_name") : "N/A";
                    String pType = rs.getInt("Slot_id") > TOTAL_RESIDENT_SLOTS ? "Guest" : "Resident";

                    // Build each data row using our helper method and String concatenation
                    String row = padRight(String.valueOf(serial), 4) + " | " +
                            padRight(ownerName, 20) + " | " +
                            padRight(String.valueOf(rs.getInt("Slot_id")), 8) + " | " +
                            padRight(rs.getString("vehicle_number"), 15) + " | " +
                            padRight(rs.getString("vehicle_brand"), 12) + " | " +
                            padRight(rs.getString("vehicle_type"), 12) + " | " +
                            padRight(pType, 10) + " | " +
                            padRight(rs.getTimestamp("time_in").toString(), 22) + " | " +
                            padRight(rs.getTimestamp("time_out").toString(), 22) + " | " +
                            padRight(rs.getBigDecimal("total_hours").toString(), 10) + " | " +
                            padRight(rs.getBigDecimal("charge_amount").toString(), 12) + "\n";

                    reportContent += row;
                    serial++;
                }
            }

            // 3. Write the content to the file (this part is the same)
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
                writer.write(reportContent);
            }

            // 4. Show a success message with the file path (this part is the same)
            File reportFile = new File(fileName);
            System.out.println("\n✓ Report generated successfully!");
            System.out.println("File saved at: " + reportFile.getAbsolutePath());

        } catch (SQLException e) {
            System.err.println("Database error while generating report: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Error writing file: " + e.getMessage());
        }
    }

    // --- ADMIN-FACING METHODS ---

    public void viewResidentParkingByWing() {
        String wing = selectWing();
        if ("BACK".equals(wing)) return;
        displayParkedVehicles("Resident", wing);
    }

    public void viewGuestParking() {
        displayParkedVehicles("Guest", null);
    }

    public void viewAllParkedVehicles() {
        displayParkedVehicles("All", null);
    }

    public void viewResidentParkingHistory() {
        String wing = selectWing();
        if ("BACK".equals(wing)) return;
        displayParkingHistory("Resident", wing, null);
    }
    private String padRight(String text, int length) {
        if (text.length() > length) {
            // Truncate if the text is too long
            return text.substring(0, length);
        }
        // Add spaces to the end until it reaches the desired length
        String paddedText = text;
        for (int i = text.length(); i < length; i++) {
            paddedText += " ";
        }
        return paddedText;
    }

    public void viewGuestParkingHistory() {
        displayParkingHistory("Guest", null, null);
    }

    public void viewAllParkingHistory() {
        displayParkingHistory("All", null, null);
    }

    public void viewAvailableParkingSlots() {
        System.out.println("\n--- Resident Parking Availability by Wing ---");
        try {
            int occupiedA = getParkedCountForWing("A");
            int occupiedB = getParkedCountForWing("B");
            int occupiedC = getParkedCountForWing("C");
            int totalOccupied = occupiedA + occupiedB + occupiedC;

            System.out.println("--------------------------------------------------------------------");
            System.out.printf("%-10s | %-15s | %-15s | %-15s%n", "Wing", "Capacity", "Occupied", "Available");
            System.out.println("--------------------------------------------------------------------");
            System.out.printf("%-10s | %-15d | %-15d | %-15d%n", "Wing A", WING_A_CAPACITY, occupiedA, WING_A_CAPACITY - occupiedA);
            System.out.printf("%-10s | %-15d | %-15d | %-15d%n", "Wing B", WING_B_CAPACITY, occupiedB, WING_B_CAPACITY - occupiedB);
            System.out.printf("%-10s | %-15d | %-15d | %-15d%n", "Wing C", WING_C_CAPACITY, occupiedC, WING_C_CAPACITY - occupiedC);
            System.out.println("--------------------------------------------------------------------");
            System.out.printf("%-10s | %-15d | %-15d | %-15d%n", "TOTAL", TOTAL_RESIDENT_SLOTS, totalOccupied, TOTAL_RESIDENT_SLOTS - totalOccupied);
            System.out.println("--------------------------------------------------------------------");
        } catch (SQLException e) {
            System.err.println("Database error while calculating available slots: " + e.getMessage());
        }
    }

    public void viewAvailableSpareParkingSlots() {
        System.out.println("\n--- Guest Parking Availability ---");
        try {
            int occupiedSpareSlots = getOccupiedSlotCount("spare_parked_vehicle", SPARE_START_SLOT, SPARE_END_SLOT);
            int availableSpareSlots = TOTAL_SPARE_SLOTS - occupiedSpareSlots;

            System.out.println("----------------------------------------------------------");
            System.out.printf("%-20s | %-15s | %-15s%n", "Total Guest Slots", "Occupied Slots", "Available Slots");
            System.out.println("----------------------------------------------------------");
            System.out.printf("%-20d | %-15d | %-15d%n", TOTAL_SPARE_SLOTS, occupiedSpareSlots, availableSpareSlots);
            System.out.println("----------------------------------------------------------");
        } catch (SQLException e) {
            System.err.println("Database error while calculating available spare slots: " + e.getMessage());
        }
    }

    // --- PRIVATE HELPER METHODS ---

    private void displayParkedVehicleTable(List<ParkedVehicleDetails> parkedVehicles) {
        System.out.println("---------------------------------------------------------------------------------------------------------");
        System.out.printf("%-4s | %-8s | %-15s | %-12s | %-12s | %-22s | %-10s%n", "Sr.", "Slot ID", "Vehicle No.", "Brand", "Type", "Time In", "Park Type");
        System.out.println("---------------------------------------------------------------------------------------------------------");

        if (parkedVehicles.isEmpty()) {
            System.out.println("No vehicles are currently parked for this selection.");
        } else {
            int serial = 1;
            for (ParkedVehicleDetails detail : parkedVehicles) {
                System.out.printf("%-4d | %-8d | %-15s | %-12s | %-12s | %-22s | %-10s%n",
                        serial++, detail.slotId, detail.vehicle.getVehicleNumber(), detail.vehicle.getVehicleBrand(),
                        detail.vehicle.getVehicleType(), detail.timeIn, detail.parkingType);
            }
        }
        System.out.println("---------------------------------------------------------------------------------------------------------");
    }

    private List<ParkedVehicleDetails> getDetailedParkedVehiclesForResident(String residentId) throws SQLException {
        List<ParkedVehicleDetails> details = new ArrayList<>();
        String query = "SELECT ap.Slot_id, v.vehicle_number, v.resident_id, v.vehicle_type, v.vehicle_brand, v.owner_type, ap.time_in, " +
                "CASE WHEN ap.Slot_id <= ? THEN 'Resident' ELSE 'Guest' END AS parking_type " +
                "FROM (SELECT Slot_id, vehicle_number, time_in FROM parked_vehicle " +
                "      UNION ALL " +
                "      SELECT Slot_id, vehicle_number, time_in FROM spare_parked_vehicle) AS ap " +
                "JOIN vehicle v ON ap.vehicle_number = v.vehicle_number " +
                "WHERE v.resident_id = ?";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, TOTAL_RESIDENT_SLOTS);
            ps.setString(2, residentId);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                Vehicle vehicle = new Vehicle(
                        rs.getString("vehicle_number"),
                        rs.getString("resident_id"),
                        rs.getString("vehicle_type"),
                        rs.getString("vehicle_brand"),
                        rs.getString("owner_type")
                );
                details.add(new ParkedVehicleDetails(
                        rs.getInt("Slot_id"),
                        vehicle,
                        rs.getTimestamp("time_in"), // This now correctly matches the constructor
                        rs.getString("parking_type")
                ));
            }
        }
        return details;
    }

    // IN: services/ParkingService.java
// REPLACE the entire displayParkedVehicles method with this one.

    private void displayParkedVehicles(String viewType, String wing) {
        String title;
        String query;

        // The SQL queries remain the same, as they already fetch all the necessary data.
        switch (viewType) {
            case "Resident":
                title = (wing != null) ? "\n--- Parked Vehicles of Wing " + wing + " ---" : "\n--- All Resident Parked Vehicles ---";
                query = "SELECT pv.Slot_id, pv.vehicle_number, r.first_name, r.last_name, r.wing, v.vehicle_brand, v.vehicle_type, pv.time_in " +
                        "FROM parked_vehicle pv JOIN vehicle v ON pv.vehicle_number = v.vehicle_number JOIN residents r ON v.resident_id = r.resident_id";
                if (wing != null) query += " WHERE r.wing = ?";
                query += " ORDER BY pv.Slot_id";
                break;
            case "Guest":
                title = "\n--- Guest Parked Vehicles ---";
                query = "SELECT spv.Slot_id, spv.vehicle_number, r.first_name, r.last_name, r.wing, v.vehicle_brand, v.vehicle_type, spv.time_in " +
                        "FROM spare_parked_vehicle spv JOIN vehicle v ON spv.vehicle_number = v.vehicle_number JOIN residents r ON v.resident_id = r.resident_id " +
                        "ORDER BY spv.Slot_id";
                break;
            default: // "All"
                title = "\n--- All Parked Vehicles (Resident + Guest) ---";
                query = "(SELECT pv.Slot_id, pv.vehicle_number, r.first_name, r.last_name, r.wing, v.vehicle_brand, v.vehicle_type, 'Resident' as park_type, pv.time_in FROM parked_vehicle pv JOIN vehicle v ON pv.vehicle_number = v.vehicle_number JOIN residents r ON v.resident_id = r.resident_id) " +
                        "UNION ALL " +
                        "(SELECT spv.Slot_id, spv.vehicle_number, r.first_name, r.last_name, r.wing, v.vehicle_brand, v.vehicle_type, 'Guest' as park_type, spv.time_in FROM spare_parked_vehicle spv JOIN vehicle v ON spv.vehicle_number = v.vehicle_number JOIN residents r ON v.resident_id = r.resident_id) " +
                        "ORDER BY Slot_id";
                break;
        }

        System.out.println(title);
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            if ("Resident".equals(viewType) && wing != null) {
                ps.setString(1, wing);
            }
            ResultSet rs = ps.executeQuery();

            // --- MODIFIED DISPLAY LOGIC ---

            boolean found = false;
            int serial = 1;

            // Print the correct header based on the view
            if ("Resident".equals(viewType) && wing != null) { // Specific Wing (No Wing column)
                System.out.println("--------------------------------------------------------------------------------------------------------------");
                System.out.printf("%-4s | %-20s | %-8s | %-15s | %-12s | %-12s | %-22s%n", "Sr.", "Owner Name", "Slot ID", "Vehicle No.", "Brand", "Type", "Time In");
                System.out.println("--------------------------------------------------------------------------------------------------------------");
            } else if ("All".equals(viewType)) { // All Parked (With Wing and Park Type columns)
                System.out.println("---------------------------------------------------------------------------------------------------------------------------------");
                System.out.printf("%-4s | %-20s | %-8s | %-15s | %-12s | %-12s | %-5s | %-10s | %-22s%n", "Sr.", "Owner Name", "Slot ID", "Vehicle No.", "Brand", "Type", "Wing", "Park Type", "Time In");
                System.out.println("---------------------------------------------------------------------------------------------------------------------------------");
            } else { // Guest or Resident (All Wings) - (With Wing column)
                System.out.println("-----------------------------------------------------------------------------------------------------------------------");
                System.out.printf("%-4s | %-20s | %-8s | %-15s | %-12s | %-12s | %-5s | %-22s%n", "Sr.", "Owner Name", "Slot ID", "Vehicle No.", "Brand", "Type", "Wing", "Time In");
                System.out.println("-----------------------------------------------------------------------------------------------------------------------");
            }

            while (rs.next()) {
                found = true;
                String ownerName = rs.getString("first_name") + " " + rs.getString("last_name");

                // Print the data row that matches the header
                if ("Resident".equals(viewType) && wing != null) { // Specific Wing
                    System.out.printf("%-4d | %-20s | %-8d | %-15s | %-12s | %-12s | %-22s%n",
                            serial++, ownerName, rs.getInt("Slot_id"), rs.getString("vehicle_number"),
                            rs.getString("vehicle_brand"), rs.getString("vehicle_type"), rs.getTimestamp("time_in").toString());
                } else if ("All".equals(viewType)) { // All Parked
                    System.out.printf("%-4d | %-20s | %-8d | %-15s | %-12s | %-12s | %-5s | %-10s | %-22s%n",
                            serial++, ownerName, rs.getInt("Slot_id"), rs.getString("vehicle_number"),
                            rs.getString("vehicle_brand"), rs.getString("vehicle_type"), rs.getString("wing"),
                            rs.getString("park_type"), rs.getTimestamp("time_in").toString());
                } else { // Guest or Resident (All Wings)
                    System.out.printf("%-4d | %-20s | %-8d | %-15s | %-12s | %-12s | %-5s | %-22s%n",
                            serial++, ownerName, rs.getInt("Slot_id"), rs.getString("vehicle_number"),
                            rs.getString("vehicle_brand"), rs.getString("vehicle_type"), rs.getString("wing"),
                            rs.getTimestamp("time_in").toString());
                }
            }

            if (!found) {
                System.out.println("No vehicles found for this selection.");
            }
            // Print a closing line that matches the header
            if ("Resident".equals(viewType) && wing != null) {
                System.out.println("--------------------------------------------------------------------------------------------------------------");
            } else if ("All".equals(viewType)) {
                System.out.println("---------------------------------------------------------------------------------------------------------------------------------");
            } else {
                System.out.println("-----------------------------------------------------------------------------------------------------------------------");
            }

        } catch (SQLException e) {
            System.err.println("Database error while viewing parked vehicles: " + e.getMessage());
        }
    }
    public void viewGuestWaitingList() {
        System.out.println("\n--- Guest Parking Waiting List ---");
        // Create an instance of the queue, giving it a max capacity (e.g., 50)
        GuestVehicleQueue waitingList = new GuestVehicleQueue(50);
        List<GuestVehicleQueue.WaitingListItem> items = waitingList.getAllWaitingListItems();

        System.out.println("--------------------------------------------------------------------------------------------------");
        System.out.printf("%-4s | %-8s | %-15s | %-12s | %-12s | %-12s | %-22s%n", "Sr.", "Wait ID", "Vehicle Number", "Brand", "Type", "Resident ID", "Time Added");
        System.out.println("--------------------------------------------------------------------------------------------------");

        if (items.isEmpty()) {
            System.out.println("The waiting list is currently empty.");
        } else {
            int serial = 1;
            for (GuestVehicleQueue.WaitingListItem item : items) {
                Vehicle v = item.vehicle;
                System.out.printf("%-4d | %-8d | %-15s | %-12s | %-12s | %-12s | %-22s%n",
                        serial++,
                        item.waitlistId,
                        v.getVehicleNumber(),
                        v.getVehicleBrand(),
                        v.getVehicleType(),
                        v.getResidentId(),
                        item.timeAdded.toString()
                );
            }
        }
        System.out.println("--------------------------------------------------------------------------------------------------");
    }

    private void displayParkingHistory(String parkingType, String wing, String residentId) {
        String title;
        // --- MODIFIED: The query now joins with the residents table to get the owner's name ---
        String query = "SELECT pr.*, r.first_name, r.last_name FROM parking_records pr " +
                "LEFT JOIN residents r ON pr.resident_id = r.resident_id";

        List<String> conditions = new ArrayList<>();
        if (residentId != null) { conditions.add("pr.resident_id = ?"); }
        if (wing != null) { conditions.add("r.wing = ?"); }

        switch (parkingType) {
            case "Resident":
                title = "\n--- Resident Parking History ---";
                conditions.add("pr.Slot_id <= " + TOTAL_RESIDENT_SLOTS);
                break;
            case "Guest":
                title = "\n--- Guest Parking History ---";
                conditions.add("pr.Slot_id > " + TOTAL_RESIDENT_SLOTS);
                break;
            default: // All
                title = "\n--- Complete Parking History ---";
                break;
        }

        if (!conditions.isEmpty()) { query += " WHERE " + String.join(" AND ", conditions); }
        query += " ORDER BY pr.time_out DESC";
        System.out.println(title);

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            int paramIndex = 1;
            if (residentId != null) ps.setString(paramIndex++, residentId);
            if (wing != null) ps.setString(paramIndex, wing);

            ResultSet rs = ps.executeQuery();
            ParkingHistoryLinkedList historyList = new ParkingHistoryLinkedList();
            int serial = 1;
            while (rs.next()) {
                // --- ADDED: Logic to get the owner name from the result set ---
                String firstName = rs.getString("first_name");
                String ownerName = (firstName != null) ? firstName + " " + rs.getString("last_name") : "N/A";

                // For resident-facing views, we don't need the name.
                if (residentId != null) {
                    ownerName = null;
                }

                String pType = "All".equals(parkingType) ? (rs.getInt("Slot_id") > TOTAL_RESIDENT_SLOTS ? "Guest" : "Resident") : null;

                // --- MODIFIED: Pass the ownerName to the constructor ---
                historyList.insertAtLast(new ParkingRecord(
                        serial++, ownerName, rs.getInt("Slot_id"), rs.getString("vehicle_number"),
                        rs.getString("vehicle_brand"), rs.getString("vehicle_type"),
                        rs.getTimestamp("time_in").toString(), rs.getTimestamp("time_out").toString(),
                        rs.getBigDecimal("total_hours"), rs.getBigDecimal("charge_amount"), pType
                ));
            }

            // --- MODIFIED: Updated headers to include "Owner Name" ---
            if (residentId != null) { // Resident View
                if ("All".equals(parkingType)) {
                    System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------");
                    System.out.printf("%-4s | %-8s | %-15s | %-12s | %-12s | %-10s | %-22s | %-22s | %-10s | %-12s%n", "Sr.", "Slot ID", "Vehicle No.", "Brand", "Type", "Park Type", "Time In", "Time Out", "Total Hrs", "Charge(Rs.)");
                    System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------");
                } else {
                    System.out.println("----------------------------------------------------------------------------------------------------------------------");
                    System.out.printf("%-4s | %-8s | %-15s | %-12s | %-12s | %-22s | %-22s | %-10s | %-12s%n", "Sr.", "Slot ID", "Vehicle No.", "Brand", "Type", "Time In", "Time Out", "Total Hrs", "Charge(Rs.)");
                    System.out.println("----------------------------------------------------------------------------------------------------------------------");
                }
            } else { // Admin View
                if ("All".equals(parkingType)) {
                    System.out.println("----------------------------------------------------------------------------------------------------------------------------------------------------------------");
                    System.out.printf("%-4s | %-20s | %-8s | %-15s | %-12s | %-12s | %-10s | %-22s | %-22s | %-10s | %-12s%n", "Sr.", "Owner Name", "Slot ID", "Vehicle No.", "Brand", "Type", "Park Type", "Time In", "Time Out", "Total Hrs", "Charge(Rs.)");
                    System.out.println("----------------------------------------------------------------------------------------------------------------------------------------------------------------");
                } else {
                    System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------");
                    System.out.printf("%-4s | %-20s | %-8s | %-15s | %-12s | %-12s | %-22s | %-22s | %-10s | %-12s%n", "Sr.", "Owner Name", "Slot ID", "Vehicle No.", "Brand", "Type", "Time In", "Time Out", "Total Hrs", "Charge(Rs.)");
                    System.out.println("-----------------------------------------------------------------------------------------------------------------------------------------------");
                }
            }
            historyList.display();
        } catch (SQLException e) {
            System.err.println("Database error while displaying history: " + e.getMessage());
        }
    }

    private String selectWing() {
        System.out.println("\n--- Select a Wing ---");
        System.out.println("1. Wing A");
        System.out.println("2. Wing B");
        System.out.println("3. Wing C");
        System.out.println("4. All Wings");
        System.out.println("5. Back to Menu");
        System.out.print("Select an option: ");
        int choice = InputHandler.getValidChoice(1, 5);
        return switch (choice) {
            case 1 -> "A";
            case 2 -> "B";
            case 3 -> "C";
            case 4 -> null;
            default -> "BACK";
        };
    }

    private int getWingCapacity(String wing) {
        return switch (wing) {
            case "A" -> WING_A_CAPACITY;
            case "B" -> WING_B_CAPACITY;
            case "C" -> WING_C_CAPACITY;
            default -> 0;
        };
    }

    private int getParkedCountForWing(String wing) throws SQLException {
        String query = "SELECT COUNT(*) FROM parked_vehicle pv " +
                "JOIN vehicle v ON pv.vehicle_number = v.vehicle_number " +
                "JOIN residents r ON v.resident_id = r.resident_id WHERE r.wing = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, wing);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private boolean isSlotOccupied(String tableName, int slotId) throws SQLException {
        String query = "SELECT COUNT(*) FROM " + tableName + " WHERE Slot_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, slotId);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    private int getOccupiedSlotCount(String tableName, int startSlot, int endSlot) throws SQLException {
        String query = "SELECT COUNT(*) FROM " + tableName + " WHERE Slot_id BETWEEN ? AND ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, startSlot);
            ps.setInt(2, endSlot);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
}