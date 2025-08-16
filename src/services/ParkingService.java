package services;

import database.DatabaseManager;
import ui.InputHandler;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ParkingService handles all business logic related to parking operations.
 * It directly interacts with the database to manage parked vehicles, slots, and history.
 */
public final class ParkingService {

    private final Connection connection;

    // =================================================================
    // =========== PARKING CAPACITY AND SLOT CONSTANTS =================
    // =================================================================
    public static final int TOTAL_4_WHEELER_SLOTS = 150;
    public static final int TOTAL_2_WHEELER_SLOTS = 300;
    public static final int FOUR_WHEELER_START_SLOT = 1;
    public static final int FOUR_WHEELER_END_SLOT = 150;
    public static final int TWO_WHEELER_START_SLOT = 151;
    public static final int TWO_WHEELER_END_SLOT = 450;
    public static final int SPARE_START_SLOT = 451;
    public static final int SPARE_END_SLOT = 500;
    public static final int TOTAL_SPARE_SLOTS = SPARE_END_SLOT - SPARE_START_SLOT + 1;


    /**
     * Constructor for ParkingService.
     * Initializes the database connection.
     */
    public ParkingService() {
        try {
            this.connection = DatabaseManager.getConnection();
        } catch (SQLException e) {
            System.err.println("Database connection failed in ParkingService.");
            throw new RuntimeException(e);
        }
    }

    // =================================================================
    // ================== RESIDENT-FACING METHODS ======================
    // =================================================================

    public void parkNewVehicle(String residentId) {
        try {
            List<String> unparkedVehicles = getUnparkedVehicles(residentId);
            if (unparkedVehicles.isEmpty()) {
                System.out.println("All your vehicles are already parked.");
                return;
            }

            System.out.println("\n--- Select a Vehicle to Park ---");
            for (int i = 0; i < unparkedVehicles.size(); i++) {
                System.out.println((i + 1) + ". " + unparkedVehicles.get(i));
            }
            System.out.println((unparkedVehicles.size() + 1) + ". Cancel");
            System.out.print("Enter your choice: ");
            int choice = InputHandler.getValidChoice(1, unparkedVehicles.size() + 1);

            if (choice > unparkedVehicles.size()) {
                System.out.println("Parking cancelled.");
                return;
            }

            String vehicleNumber = unparkedVehicles.get(choice - 1);
            String vehicleType = getVehicleType(vehicleNumber);
            int slotId = -1;

            if ("4-wheeler".equals(vehicleType)) {
                slotId = findAvailableSlot("parked_vehicle", FOUR_WHEELER_START_SLOT, FOUR_WHEELER_END_SLOT);
            } else if ("2-wheeler".equals(vehicleType)) {
                slotId = findAvailableSlot("parked_vehicle", TWO_WHEELER_START_SLOT, TWO_WHEELER_END_SLOT);
            }

            String tableName = "parked_vehicle";
            if (slotId == -1) {
                System.out.println("No resident parking slots available. Checking spare slots...");
                slotId = findAvailableSlot("spare_parked_vehicle", SPARE_START_SLOT, SPARE_END_SLOT);
                tableName = "spare_parked_vehicle";
            }

            if (slotId != -1) {
                String query = "INSERT INTO " + tableName + " (Slot_id, vehicle_number, time_in) VALUES (?, ?, NOW())";
                try (PreparedStatement ps = connection.prepareStatement(query)) {
                    ps.setInt(1, slotId);
                    ps.setString(2, vehicleNumber);
                    if (ps.executeUpdate() > 0) {
                        System.out.println("Vehicle " + vehicleNumber + " parked successfully in slot " + slotId + ".");
                    }
                }
            } else {
                System.out.println("Sorry, all parking slots (including spare) are full.");
            }
        } catch (SQLException e) {
            System.err.println("Database error while parking vehicle: " + e.getMessage());
        }
    }

    public void removeParkedVehicle(String residentId) {
        try {
            List<String> parkedVehicles = getParkedVehicles(residentId);
            if (parkedVehicles.isEmpty()) {
                System.out.println("You have no vehicles currently parked.");
                return;
            }

            System.out.println("\n--- Select a Vehicle to Remove ---");
            for (int i = 0; i < parkedVehicles.size(); i++) {
                System.out.println((i + 1) + ". " + parkedVehicles.get(i));
            }
            System.out.println((parkedVehicles.size() + 1) + ". Cancel");
            System.out.print("Enter your choice: ");
            int choice = InputHandler.getValidChoice(1, parkedVehicles.size() + 1);

            if (choice > parkedVehicles.size()) {
                System.out.println("Removal cancelled.");
                return;
            }

            String vehicleNumber = parkedVehicles.get(choice - 1);
            String tableName = isVehicleInSpareParking(vehicleNumber) ? "spare_parked_vehicle" : "parked_vehicle";
            int slotId = getSlotIdForVehicle(vehicleNumber, tableName);

            String insertRecordQuery = "INSERT INTO parking_records (Slot_id, time_out) VALUES (?, NOW())";
            try (PreparedStatement ps = connection.prepareStatement(insertRecordQuery)) {
                ps.setInt(1, slotId);
                ps.executeUpdate();
            }

            String deleteQuery = "DELETE FROM " + tableName + " WHERE vehicle_number = ?";
            try (PreparedStatement ps = connection.prepareStatement(deleteQuery)) {
                ps.setString(1, vehicleNumber);
                if (ps.executeUpdate() > 0) {
                    System.out.println("Vehicle " + vehicleNumber + " removed from slot " + slotId + " successfully.");
                }
            }

        } catch (SQLException e) {
            System.err.println("Database error while removing vehicle: " + e.getMessage());
        }
    }

    public void viewMyParkedVehicles(String residentId) {
        System.out.println("\n--- My Parked Vehicles ---");
        String query = "SELECT ap.Slot_id, ap.vehicle_number, v.vehicle_type, ap.time_in, " +
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

            System.out.println("-----------------------------------------------------------------------------------------");
            System.out.printf("%-10s | %-18s | %-15s | %-20s | %-15s%n", "Slot ID", "Vehicle Number", "Vehicle Type", "Time In", "Parking Type");
            System.out.println("-----------------------------------------------------------------------------------------");

            if (!rs.isBeforeFirst()) {
                System.out.println("You have no vehicles currently parked.");
            } else {
                while (rs.next()) {
                    System.out.printf("%-10d | %-18s | %-15s | %-20s | %-15s%n",
                            rs.getInt("Slot_id"),
                            rs.getString("vehicle_number"),
                            rs.getString("vehicle_type"),
                            rs.getTimestamp("time_in").toString(),
                            rs.getString("parking_type"));
                }
            }
            System.out.println("-----------------------------------------------------------------------------------------");
        } catch (SQLException e) {
            System.err.println("Database error while viewing your parked vehicles: " + e.getMessage());
        }
    }

    public void displayMyParkingHistory(String residentId, String parkingType) {
        String title;
        String query = "SELECT pr.record_id, pr.Slot_id, pr.vehicle_number, pr.time_in, pr.time_out, pr.total_hours, pr.charge_amount " +
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

            System.out.println("----------------------------------------------------------------------------------------------------------------------");
            System.out.printf("%-10s | %-10s | %-18s | %-20s | %-20s | %-12s | %-15s%n",
                    "Record ID", "Slot ID", "Vehicle Number", "Time In", "Time Out", "Total Hours", "Charge (Rs.)");
            System.out.println("----------------------------------------------------------------------------------------------------------------------");

            if (!rs.isBeforeFirst()) {
                System.out.println("No parking history found for this selection.");
            } else {
                while (rs.next()) {
                    System.out.printf("%-10d | %-10d | %-18s | %-20s | %-20s | %-12.2f | %-15.2f%n",
                            rs.getInt("record_id"),
                            rs.getInt("Slot_id"),
                            rs.getString("vehicle_number"),
                            rs.getTimestamp("time_in").toString(),
                            rs.getTimestamp("time_out").toString(),
                            rs.getBigDecimal("total_hours"),
                            rs.getBigDecimal("charge_amount"));
                }
            }
            System.out.println("----------------------------------------------------------------------------------------------------------------------");

        } catch (SQLException e) {
            System.err.println("Database error while viewing your parking history: " + e.getMessage());
        }
    }

    // =================================================================
    // =================== ADMIN-FACING METHODS ========================
    // =================================================================

    public void viewParkingSlots() {
        String wing = selectWing();
        if ("BACK".equals(wing)) return;
        displayParkedVehicles(false, wing);
    }

    public void viewSpareParkedVehicles() {
        displayParkedVehicles(true, null);
    }

    public void viewAllParkedVehicles() {
        String wing = selectWing();
        if ("BACK".equals(wing)) return;
        displayAllParkedVehicles(wing);
    }

    public void viewAvailableParkingSlots() {
        String wing = selectWing();
        if ("BACK".equals(wing)) return;
        displayAvailableSlotsByWing(wing);
    }

    public void viewAvailableSpareParkingSlots() {
        System.out.println("\n--- Available Spare Parking Slots ---");
        try {
            int occupiedSpareSlots = getOccupiedSlotCount("spare_parked_vehicle", SPARE_START_SLOT, SPARE_END_SLOT, null);
            int availableSpareSlots = TOTAL_SPARE_SLOTS - occupiedSpareSlots;

            System.out.println("-----------------------------------------------------------------");
            System.out.printf("%-20s | %-20s | %-20s%n", "Total Spare Slots", "Occupied Slots", "Available Slots");
            System.out.println("-----------------------------------------------------------------");
            System.out.printf("%-20d | %-20d | %-20d%n", TOTAL_SPARE_SLOTS, occupiedSpareSlots, availableSpareSlots);
            System.out.println("-----------------------------------------------------------------");

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
        String wing = selectWing();
        if ("BACK".equals(wing)) return;
        displayParkingHistory("All", wing);
    }


    // =================================================================
    // ===================== HELPER METHODS ============================
    // =================================================================

    private void displayParkedVehicles(boolean isSpare, String wing) {
        String query;
        String title;

        if (isSpare) {
            title = "\n--- Spare Parked Vehicles ---";
            query = "SELECT spv.Slot_id, spv.vehicle_number, v.resident_id, r.first_name, r.last_name, r.wing, v.vehicle_type, v.vehicle_brand, spv.time_in " +
                    "FROM spare_parked_vehicle spv " +
                    "JOIN vehicle v ON spv.vehicle_number = v.vehicle_number " +
                    "JOIN residents r ON v.resident_id = r.resident_id";
        } else {
            title = (wing != null) ? "\n--- Parked Vehicles in Wing " + wing + " ---" : "\n--- All Parked Vehicles ---";
            query = "SELECT pv.Slot_id, pv.vehicle_number, v.resident_id, r.first_name, r.last_name, r.wing, v.vehicle_type, v.vehicle_brand, pv.time_in " +
                    "FROM parked_vehicle pv " +
                    "JOIN vehicle v ON pv.vehicle_number = v.vehicle_number " +
                    "JOIN residents r ON v.resident_id = r.resident_id";
            if (wing != null) {
                query += " WHERE r.wing = ?";
            }
        }

        System.out.println(title);

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            if (!isSpare && wing != null) {
                ps.setString(1, wing);
            }
            ResultSet rs = ps.executeQuery();
            System.out.println("---------------------------------------------------------------------------------------------------------------------------------");
            System.out.printf("%-10s | %-18s | %-12s | %-20s | %-5s | %-15s | %-15s | %-20s%n", "Slot ID", "Vehicle Number", "Resident ID", "Owner Name", "Wing", "Vehicle Type", "Vehicle Brand", "Time In");
            System.out.println("---------------------------------------------------------------------------------------------------------------------------------");

            if (!rs.isBeforeFirst()) {
                System.out.println("No vehicles are currently parked in this selection.");
            } else {
                while (rs.next()) {
                    String ownerName = rs.getString("first_name") + " " + rs.getString("last_name");
                    System.out.printf("%-10d | %-18s | %-12s | %-20s | %-5s | %-15s | %-15s | %-20s%n",
                            rs.getInt("Slot_id"), rs.getString("vehicle_number"),
                            rs.getString("resident_id"), ownerName,
                            rs.getString("wing"), rs.getString("vehicle_type"),
                            rs.getString("vehicle_brand"), rs.getTimestamp("time_in").toString());
                }
            }
            System.out.println("---------------------------------------------------------------------------------------------------------------------------------");
        } catch (SQLException e) {
            System.err.println("Database error while viewing parked vehicles: " + e.getMessage());
        }
    }

    private void displayAllParkedVehicles(String wing) {
        String title = (wing != null) ? "\n--- All Parked Vehicles in Wing " + wing + " ---" : "\n--- All Parked Vehicles (Resident + Spare) ---";
        String query =
                "(SELECT pv.Slot_id, pv.vehicle_number, r.resident_id, r.first_name, r.last_name, r.wing, v.vehicle_type, pv.time_in, 'Resident' as parking_type " +
                        "FROM parked_vehicle pv " +
                        "JOIN vehicle v ON pv.vehicle_number = v.vehicle_number " +
                        "JOIN residents r ON v.resident_id = r.resident_id" + (wing != null ? " WHERE r.wing = ?" : "") + ") " +
                        "UNION ALL " +
                        "(SELECT spv.Slot_id, spv.vehicle_number, r.resident_id, r.first_name, r.last_name, r.wing, v.vehicle_type, spv.time_in, 'Spare' as parking_type " +
                        "FROM spare_parked_vehicle spv " +
                        "JOIN vehicle v ON spv.vehicle_number = v.vehicle_number " +
                        "JOIN residents r ON v.resident_id = r.resident_id" + (wing != null ? " WHERE r.wing = ?" : "") + ") " +
                        "ORDER BY Slot_id";

        System.out.println(title);

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            if (wing != null) {
                ps.setString(1, wing);
                ps.setString(2, wing);
            }
            ResultSet rs = ps.executeQuery();
            System.out.println("------------------------------------------------------------------------------------------------------------------------------------------");
            System.out.printf("%-10s | %-18s | %-12s | %-20s | %-5s | %-15s | %-20s | %-12s%n", "Slot ID", "Vehicle Number", "Resident ID", "Owner Name", "Wing", "Vehicle Type", "Time In", "Parking Type");
            System.out.println("------------------------------------------------------------------------------------------------------------------------------------------");

            if (!rs.isBeforeFirst()) {
                System.out.println("No vehicles are currently parked in this selection.");
            } else {
                while (rs.next()) {
                    String ownerName = rs.getString("first_name") + " " + rs.getString("last_name");
                    System.out.printf("%-10d | %-18s | %-12s | %-20s | %-5s | %-15s | %-20s | %-12s%n",
                            rs.getInt("Slot_id"), rs.getString("vehicle_number"),
                            rs.getString("resident_id"), ownerName,
                            rs.getString("wing"), rs.getString("vehicle_type"),
                            rs.getTimestamp("time_in").toString(), rs.getString("parking_type"));
                }
            }
            System.out.println("------------------------------------------------------------------------------------------------------------------------------------------");
        } catch (SQLException e) {
            System.err.println("Database error while viewing all parked vehicles: " + e.getMessage());
        }
    }


    private void displayAvailableSlotsByWing(String wing) {
        String title = (wing != null) ? "\n--- Available Parking Slots in Wing " + wing + " ---" : "\n--- Total Available Parking Slots (All Wings) ---";
        try {
            int occupied4Wheeler = getOccupiedSlotCount("parked_vehicle", FOUR_WHEELER_START_SLOT, FOUR_WHEELER_END_SLOT, wing);
            int occupied2Wheeler = getOccupiedSlotCount("parked_vehicle", TWO_WHEELER_START_SLOT, TWO_WHEELER_END_SLOT, wing);

            int total4WheelerInView = (wing == null) ? TOTAL_4_WHEELER_SLOTS : TOTAL_4_WHEELER_SLOTS / 3;
            int total2WheelerInView = (wing == null) ? TOTAL_2_WHEELER_SLOTS : TOTAL_2_WHEELER_SLOTS / 3;

            System.out.println(title);
            System.out.println("-----------------------------------------------------------------");
            System.out.printf("%-15s | %-15s | %-15s | %-15s%n", "Vehicle Type", "Total Slots", "Occupied Slots", "Available Slots");
            System.out.println("-----------------------------------------------------------------");
            System.out.printf("%-15s | %-15d | %-15d | %-15d%n", "4-Wheeler", total4WheelerInView, occupied4Wheeler, total4WheelerInView - occupied4Wheeler);
            System.out.printf("%-15s | %-15d | %-15d | %-15d%n", "2-Wheeler", total2WheelerInView, occupied2Wheeler, total2WheelerInView - occupied2Wheeler);
            System.out.println("-----------------------------------------------------------------");

        } catch (SQLException e) {
            System.err.println("Database error while calculating available slots: " + e.getMessage());
        }
    }

    private void displayParkingHistory(String parkingType, String wing) {
        String title;
        StringBuilder queryBuilder = new StringBuilder(
                "SELECT pr.record_id, pr.Slot_id, pr.vehicle_number, r.wing, pr.time_in, pr.time_out, pr.total_hours, pr.charge_amount " +
                        "FROM parking_records pr " +
                        "LEFT JOIN vehicle v ON pr.vehicle_number = v.vehicle_number " +
                        "LEFT JOIN residents r ON v.resident_id = r.resident_id "
        );
        List<String> conditions = new ArrayList<>();

        switch (parkingType) {
            case "Resident":
                title = (wing != null) ? "\n--- Resident Parking History for Wing " + wing + " ---" : "\n--- Resident Parking History (All Wings) ---";
                conditions.add("pr.Slot_id BETWEEN " + FOUR_WHEELER_START_SLOT + " AND " + TWO_WHEELER_END_SLOT);
                break;
            case "Spare":
                title = "\n--- Spare Parking History ---";
                conditions.add("pr.Slot_id BETWEEN " + SPARE_START_SLOT + " AND " + SPARE_END_SLOT);
                break;
            default: // "All"
                title = (wing != null) ? "\n--- All Parking History for Wing " + wing + " ---" : "\n--- Complete Parking History (All Wings) ---";
                break;
        }

        if (wing != null) {
            conditions.add("r.wing = ?");
        }

        if (!conditions.isEmpty()) {
            queryBuilder.append("WHERE ").append(String.join(" AND ", conditions));
        }
        queryBuilder.append(" ORDER BY pr.time_out DESC");

        System.out.println(title);

        try (PreparedStatement ps = connection.prepareStatement(queryBuilder.toString())) {
            if (wing != null) {
                ps.setString(1, wing);
            }
            ResultSet rs = ps.executeQuery();

            System.out.println("---------------------------------------------------------------------------------------------------------------------------------");
            System.out.printf("%-10s | %-10s | %-18s | %-5s | %-20s | %-20s | %-12s | %-15s%n",
                    "Record ID", "Slot ID", "Vehicle Number", "Wing", "Time In", "Time Out", "Total Hours", "Charge (Rs.)");
            System.out.println("---------------------------------------------------------------------------------------------------------------------------------");

            if (!rs.isBeforeFirst()) {
                System.out.println("No parking history found for this selection.");
            } else {
                while (rs.next()) {
                    String wingDisplay = (rs.getString("wing") == null) ? "N/A" : rs.getString("wing");
                    System.out.printf("%-10d | %-10d | %-18s | %-5s | %-20s | %-20s | %-12.2f | %-15.2f%n",
                            rs.getInt("record_id"), rs.getInt("Slot_id"),
                            rs.getString("vehicle_number"), wingDisplay,
                            rs.getTimestamp("time_in").toString(), rs.getTimestamp("time_out").toString(),
                            rs.getBigDecimal("total_hours"), rs.getBigDecimal("charge_amount"));
                }
            }
            System.out.println("---------------------------------------------------------------------------------------------------------------------------------");

        } catch (SQLException e) {
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

    private List<String> getUnparkedVehicles(String residentId) throws SQLException {
        List<String> vehicles = new ArrayList<>();
        String query = "SELECT vehicle_number FROM vehicle WHERE resident_id = ? AND vehicle_number NOT IN " +
                "(SELECT vehicle_number FROM parked_vehicle UNION SELECT vehicle_number FROM spare_parked_vehicle)";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, residentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                vehicles.add(rs.getString("vehicle_number"));
            }
        }
        return vehicles;
    }

    private List<String> getParkedVehicles(String residentId) throws SQLException {
        List<String> vehicles = new ArrayList<>();
        String query = "SELECT vehicle_number FROM (SELECT vehicle_number FROM parked_vehicle UNION ALL SELECT vehicle_number FROM spare_parked_vehicle) AS all_parked " +
                "WHERE vehicle_number IN (SELECT vehicle_number FROM vehicle WHERE resident_id = ?)";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, residentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                vehicles.add(rs.getString("vehicle_number"));
            }
        }
        return vehicles;
    }

    private String getVehicleType(String vehicleNumber) throws SQLException {
        String query = "SELECT vehicle_type FROM vehicle WHERE vehicle_number = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, vehicleNumber);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getString("vehicle_type") : null;
        }
    }

    private int findAvailableSlot(String tableName, int startSlot, int endSlot) throws SQLException {
        String query = "SELECT Slot_id FROM " + tableName + " WHERE Slot_id BETWEEN ? AND ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, startSlot);
            ps.setInt(2, endSlot);
            ResultSet rs = ps.executeQuery();
            List<Integer> occupiedSlots = new ArrayList<>();
            while (rs.next()) {
                occupiedSlots.add(rs.getInt("Slot_id"));
            }
            for (int i = startSlot; i <= endSlot; i++) {
                if (!occupiedSlots.contains(i)) {
                    return i;
                }
            }
        }
        return -1; // No slot available
    }

    private boolean isVehicleInSpareParking(String vehicleNumber) throws SQLException {
        String query = "SELECT 1 FROM spare_parked_vehicle WHERE vehicle_number = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, vehicleNumber);
            return ps.executeQuery().next();
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
