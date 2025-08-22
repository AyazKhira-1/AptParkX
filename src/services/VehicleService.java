package services;

import dao.ResidentDAO;
import dao.VehicleDAO;
import database.DatabaseManager;
import database.TransactionManager;
import model.Resident;
import model.Vehicle;
import ui.InputHandler;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;

public final class VehicleService {
    private static final Scanner scanner = new Scanner(System.in);
    private final VehicleDAO vehicleDAO;
    private final ResidentDAO residentDAO;

    public VehicleService() {
        this.vehicleDAO = new VehicleDAO();
        this.residentDAO = new ResidentDAO();
    }

    private static class VehicleWithParkingStatus {
        final Vehicle vehicle;
        final String isParked;
        final String parkingLocation;

        VehicleWithParkingStatus(Vehicle vehicle, String isParked, String parkingLocation) {
            this.vehicle = vehicle;
            this.isParked = isParked;
            this.parkingLocation = parkingLocation;
        }
    }

    public void addVehicleForResident(String residentId, String actorRole) {
        String possessive = actorRole.equals("ADMIN") ? "The resident's" : "Your";
        String title = actorRole.equals("ADMIN") ? "--- Add New Vehicle for Resident " + residentId + " ---" : "--- Add New Vehicle to Your Profile ---";
        System.out.println("\n" + title);

        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            TransactionManager.beginTransaction(conn);
            VehicleDAO vehicleDAOForTx = new VehicleDAO(conn);
            ResidentDAO residentDAOForTx = new ResidentDAO(conn);

            Resident resident = residentDAOForTx.getResidentById(residentId);
            if (resident == null) {
                System.out.println("Error: Resident with ID '" + residentId + "' not found.");
                TransactionManager.rollbackTransaction(conn);
                return;
            }

            int[] vehicleCounts = vehicleDAOForTx.getResidentVehicleCounts(residentId);
            int fourWheelerCount = vehicleCounts[0];
            int twoWheelerCount = vehicleCounts[1];

            System.out.printf("%s current registered vehicles: %d (4-wheeler), %d (2-wheeler).%n", possessive, fourWheelerCount, twoWheelerCount);
            System.out.printf("%s subscription tier: %s%n", possessive, resident.getSubscriptionTier());

            String vehicleType = InputHandler.getValidVehicleTypeInput("Select Vehicle Type to Add");
            int[] limits = getLimitsForTier(resident.getSubscriptionTier());
            int fourWheelerLimit = limits[0];
            int twoWheelerLimit = limits[1];

            boolean canAdd = ("4-wheeler".equals(vehicleType) && fourWheelerCount < fourWheelerLimit) ||
                    ("2-wheeler".equals(vehicleType) && twoWheelerCount < twoWheelerLimit);

            if (canAdd) {
                String vehicleNumber = getUniqueVehicleNumber(vehicleDAOForTx);
                String vehicleBrand = InputHandler.getValidStringInput("Enter Vehicle Brand: ");
                Vehicle newVehicle = new Vehicle(vehicleNumber, residentId, vehicleType, vehicleBrand, "Resident");

                if (vehicleDAOForTx.addVehicle(newVehicle)) {
                    residentDAOForTx.updateResidentVehicleCount(residentId, 1);
                    System.out.printf("✓ Vehicle '%s' added successfully under the %s plan!%n", vehicleNumber, resident.getSubscriptionTier());
                    TransactionManager.commitTransaction(conn);
                } else {
                    System.err.println("Failed to add the vehicle.");
                    TransactionManager.rollbackTransaction(conn);
                }
            } else {
                System.out.printf("✗ Action Failed: %s has reached the vehicle limit for the '%s' subscription plan.%n", possessive, resident.getSubscriptionTier());
                System.out.println("To add more vehicles, please upgrade the subscription plan from the 'Manage Subscription' menu.");
                TransactionManager.rollbackTransaction(conn);
            }
        } catch (SQLException e) {
            System.err.println("Database error while adding vehicle: " + e.getMessage());
            TransactionManager.rollbackTransaction(conn);
        } finally {
            TransactionManager.endTransaction(conn);
        }
    }

    public void addVehiclesForNewResident(String residentId, Connection conn) throws SQLException {
        VehicleDAO vehicleDAOForTx = new VehicleDAO(conn);
        ResidentDAO residentDAOForTx = new ResidentDAO(conn);

        while (true) {
            System.out.print("Do you want to add vehicles for this new resident now? (y/n): ");
            String choice = scanner.nextLine().trim().toLowerCase();
            if ("y".equals(choice)) {
                break;
            }
            if ("n".equals(choice)) {
                System.out.println("Vehicle addition skipped. You can add them later.");
                return;
            }
            System.out.println("Invalid input. Please enter 'y' for yes or 'n' for no.");
        }

        System.out.println("\nA new resident (Silver plan) can have one 4-wheeler and up to two 2-wheelers.");
        int[] counts = vehicleDAOForTx.getResidentVehicleCounts(residentId);

        // Logic to add the 4-wheeler with a validation loop
        if (counts[0] < 1) {
            while (true) {
                System.out.print("Add a 4-wheeler? (y/n): ");
                String addFourWheeler = scanner.nextLine().trim().toLowerCase();
                if ("y".equals(addFourWheeler)) {
                    System.out.println("\n-> Adding 4-wheeler...");
                    String vehicleNumber = getUniqueVehicleNumber(vehicleDAOForTx);
                    String vehicleBrand = InputHandler.getValidStringInput("Enter Vehicle Brand: ");
                    Vehicle newVehicle = new Vehicle(vehicleNumber, residentId, "4-wheeler", vehicleBrand, "Resident");
                    if (vehicleDAOForTx.addVehicle(newVehicle)) {
                        residentDAOForTx.updateResidentVehicleCount(residentId, 1);
                        System.out.println("4-wheeler '" + vehicleNumber + "' added.");
                    }
                    break;
                }
                if ("n".equals(addFourWheeler)) {
                    break;
                }
                System.out.println("Invalid input. Please enter 'y' or 'n'.");
            }
        }

        // Logic to add up to two 2-wheelers with a validation loop
        for (int i = 0; i < 2; i++) {
            counts = vehicleDAOForTx.getResidentVehicleCounts(residentId);
            if (counts[1] < 2) {
                boolean stopAsking = false;
                while (true) {
                    System.out.print("Add a 2-wheeler? (y/n): ");
                    String addTwoWheeler = scanner.nextLine().trim().toLowerCase();
                    if ("y".equals(addTwoWheeler)) {
                        System.out.println("\n-> Adding 2-wheeler " + (i + 1) + "...");
                        String vehicleNumber = getUniqueVehicleNumber(vehicleDAOForTx);
                        String vehicleBrand = InputHandler.getValidStringInput("Enter Vehicle Brand: ");
                        Vehicle newVehicle = new Vehicle(vehicleNumber, residentId, "2-wheeler", vehicleBrand, "Resident");
                        if (vehicleDAOForTx.addVehicle(newVehicle)) {
                            residentDAOForTx.updateResidentVehicleCount(residentId, 1);
                            System.out.println("2-wheeler '" + vehicleNumber + "' added.");
                        }
                        break;
                    }
                    if ("n".equals(addTwoWheeler)) {
                        stopAsking = true;
                        break;
                    }
                    System.out.println("Invalid input. Please enter 'y' or 'n'.");
                }
                if (stopAsking) {
                    break;
                }
            }
        }
    }

    private int[] getLimitsForTier(String tier) {
        return switch (tier) {
            case "Gold" -> new int[]{2, 3};
            case "Platinum" -> new int[]{3, 5};
            default -> new int[]{1, 2}; // Silver
        };
    }

    private String getUniqueVehicleNumber(VehicleDAO vehicleDAO) throws SQLException {
        String vehicleNumber;
        while (true) {
            vehicleNumber = InputHandler.getValidVehicleNumberInput("Enter Vehicle Number: ");

            if (vehicleDAO.vehicleExists(vehicleNumber)) {
                System.out.println("Error: Vehicle number '" + vehicleNumber + "' already exists in the database.");
                continue;
            }
            return vehicleNumber;
        }
    }

    public void displayVehiclesByResident() {
        System.out.println("\n--- Find All Vehicles by Resident ID ---");
        String residentId = InputHandler.getValidStringInput("Enter the Resident ID to search for: ").toUpperCase();

        try {
            if (residentDAO.getResidentById(residentId) == null) {
                System.out.println("Error: Resident with ID '" + residentId + "' not found.");
                return;
            }

            List<VehicleWithParkingStatus> vehicleList = new ArrayList<>();
            String query = "SELECT v.vehicle_number, v.resident_id, v.vehicle_type, v.vehicle_brand, v.owner_type, " +
                    "CASE WHEN pv.Slot_id IS NOT NULL OR spv.Slot_id IS NOT NULL THEN 'Yes' ELSE 'No' END AS is_parked, " +
                    "CASE " +
                    "    WHEN pv.Slot_id IS NOT NULL THEN 'Resident Parking' " +
                    "    WHEN spv.Slot_id IS NOT NULL THEN 'Guest Parking' " +
                    "    ELSE '-' " +
                    "END AS parking_location " +
                    "FROM vehicle v " +
                    "LEFT JOIN parked_vehicle pv ON v.vehicle_number = pv.vehicle_number " +
                    "LEFT JOIN spare_parked_vehicle spv ON v.vehicle_number = spv.vehicle_number " +
                    "WHERE v.resident_id = ?";

            try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(query)) {
                ps.setString(1, residentId);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    Vehicle vehicle = new Vehicle(
                            rs.getString("vehicle_number"), rs.getString("resident_id"),
                            rs.getString("vehicle_type"), rs.getString("vehicle_brand"),
                            rs.getString("owner_type")
                    );
                    vehicleList.add(new VehicleWithParkingStatus(
                            vehicle,
                            rs.getString("is_parked"),
                            rs.getString("parking_location")
                    ));
                }
            }

            System.out.println("\n--- Vehicles Registered to " + residentId + " ---");
            System.out.println("---------------------------------------------------------------------------------------------------------");
            System.out.printf("%-4s | %-15s | %-12s | %-12s | %-10s | %-12s | %s%n", "Sr.", "Vehicle Number", "Brand", "Type", "Owner Type", "Is Parked?", "Parking Location");
            System.out.println("---------------------------------------------------------------------------------------------------------");

            if (vehicleList.isEmpty()) {
                System.out.println("No vehicles found for this resident.");
            } else {
                int serial = 1;
                for (VehicleWithParkingStatus item : vehicleList) {
                    System.out.printf("%-4d | %-15s | %-12s | %-12s | %-10s | %-12s | %s%n",
                            serial++,
                            item.vehicle.getVehicleNumber(),
                            item.vehicle.getVehicleBrand(),
                            item.vehicle.getVehicleType(),
                            item.vehicle.getOwnerType(),
                            item.isParked,
                            item.parkingLocation
                    );
                }
            }
            System.out.println("---------------------------------------------------------------------------------------------------------");

        } catch (SQLException e) {
            System.err.println("Database error while fetching vehicle details: " + e.getMessage());
        }
    }

    public void searchVehicle() {
        System.out.println("\n--- Search for a Vehicle ---");
        String vehicleNumber = InputHandler.getValidStringInput("Enter the Vehicle Number to search: ").toUpperCase();
        try {
            Vehicle vehicle = vehicleDAO.getVehicleByNumber(vehicleNumber);
            System.out.println("\n--- Search Results ---");
            if (vehicle == null) {
                System.out.println("No vehicle found with the number '" + vehicleNumber + "'.");
            } else {
                Resident owner = residentDAO.getResidentById(vehicle.getResidentId());
                String ownerName = (owner != null) ? owner.getFirstName() + " " + owner.getLastName() : "N/A";
                System.out.printf("Vehicle Number: %s%n", vehicle.getVehicleNumber());
                System.out.printf("Type: %s, Brand: %s%n", vehicle.getVehicleType(), vehicle.getVehicleBrand());
                System.out.printf("Owner: %s (%s)%n", ownerName, vehicle.getResidentId());
            }
        } catch (SQLException e) {
            System.err.println("Database error during vehicle search: " + e.getMessage());
        }
    }

    public void deleteVehicle() {
        System.out.println("\n--- Delete Vehicle from Profile ---");
        String vehicleNumber = InputHandler.getValidStringInput("Enter the Vehicle Number to delete: ").toUpperCase();
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            // Get full vehicle details first, which now includes ownerType
            Vehicle vehicleToDelete = new VehicleDAO(conn).getVehicleByNumber(vehicleNumber);

            if (vehicleToDelete == null) {
                System.out.println("Vehicle with number '" + vehicleNumber + "' not found.");
                return;
            }
            String residentId = vehicleToDelete.getResidentId();
            String ownerType = vehicleToDelete.getOwnerType();

            // Confirmation Loop
            String choice;
            while (true) {
                System.out.print("Are you sure you want to permanently delete vehicle '" + vehicleNumber + "'? (y/n): ");
                choice = scanner.nextLine().trim().toLowerCase();
                if (choice.equals("y") || choice.equals("n")) break;
                System.out.println("Invalid input. Please enter 'y' or 'n'.");
            }

            if (!choice.equals("y")) {
                System.out.println("Deletion cancelled.");
                return;
            }

            // Begin Transaction for all database operations
            TransactionManager.beginTransaction(conn);
            ResidentDAO residentDAOForTx = new ResidentDAO(conn);
            VehicleDAO vehicleDAOForTx = new VehicleDAO(conn);

            // Step 1: Check if the vehicle is parked and un-park it automatically
            int slotId = getSlotIdForVehicle(vehicleNumber, conn);
            if (slotId != -1) {
                System.out.println("Note: This vehicle is currently parked. It will be un-parked and archived automatically.");
                String sourceTable = (slotId > 1200) ? "spare_parked_vehicle" : "parked_vehicle";

                // Create history record
                String archiveQuery = "INSERT INTO parking_records (Slot_id, vehicle_number, vehicle_brand, vehicle_type, time_out) VALUES(?, ?, ?, ?, NOW())";
                try (PreparedStatement ps = conn.prepareStatement(archiveQuery)) {
                    ps.setInt(1, slotId);
                    ps.setString(2, vehicleNumber);
                    ps.setString(3, vehicleToDelete.getVehicleBrand());
                    ps.setString(4, vehicleToDelete.getVehicleType());
                    ps.executeUpdate();
                }

                // Delete from parking table
                String deleteParkingQuery = "DELETE FROM " + sourceTable + " WHERE vehicle_number = ?";
                try (PreparedStatement ps = conn.prepareStatement(deleteParkingQuery)) {
                    ps.setString(1, vehicleNumber);
                    ps.executeUpdate();
                }
            }

            // Step 2: Delete from the main vehicle table
            vehicleDAOForTx.deleteVehicle(vehicleNumber);

            // Step 3: Conditionally update the resident's vehicle count
            if ("Resident".equalsIgnoreCase(ownerType)) {
                residentDAOForTx.updateResidentVehicleCount(residentId, -1);
            }

            TransactionManager.commitTransaction(conn);
            System.out.println("✓ Vehicle '" + vehicleNumber + "' was successfully deleted from the system.");

        } catch (SQLException e) {
            System.err.println("Database error during deletion: " + e.getMessage());
            TransactionManager.rollbackTransaction(conn);
        } finally {
            TransactionManager.endTransaction(conn);
        }
    }

    // Private helper method to find a parked vehicle's slot ID
    private int getSlotIdForVehicle(String vehicleNumber, Connection conn) throws SQLException {
        String query = "SELECT Slot_id FROM parked_vehicle WHERE vehicle_number = ? " +
                "UNION " +
                "SELECT Slot_id FROM spare_parked_vehicle WHERE vehicle_number = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, vehicleNumber);
            ps.setString(2, vehicleNumber);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt("Slot_id") : -1;
        }
    }

    public void viewAllVehicles(String sortBy) {
        System.out.println("\n--- All Vehicles (Sorted by " + sortBy.replace("_", " ") + ") ---");
        try {
            List<Vehicle> vehicles = vehicleDAO.getAllVehicles(sortBy);
            System.out.println("-----------------------------------------------------------------------");
            System.out.printf("%-12s | %-18s | %-15s | %-15s%n", "Resident ID", "Vehicle Number", "Vehicle Type", "Vehicle Brand");
            System.out.println("-----------------------------------------------------------------------");
            if (vehicles.isEmpty()) {
                System.out.println("No vehicles found.");
            } else {
                for (Vehicle vehicle : vehicles) {
                    System.out.printf("%-12s | %-18s | %-15s | %-15s%n",
                            vehicle.getResidentId(), vehicle.getVehicleNumber(),
                            vehicle.getVehicleType(), vehicle.getVehicleBrand());
                }
            }
            System.out.println("-----------------------------------------------------------------------");
        } catch (SQLException e) {
            System.err.println("Database error while viewing vehicles: " + e.getMessage());
        }
    }
}