package dao;

import database.DatabaseManager;
import model.Resident;
import model.Vehicle;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VehicleDAO {

    private final Connection connection;

    public VehicleDAO() {
        try {
            this.connection = DatabaseManager.getConnection();
        } catch (SQLException e) {
            System.err.println("Database connection failed in VehicleDAO.");
            throw new RuntimeException(e);
        }
    }

    public VehicleDAO(Connection connection) {
        this.connection = connection;
    }

    public List<Vehicle> getVehiclesByResidentId(String residentId) throws SQLException {
        List<Vehicle> vehicles = new ArrayList<>();
        String query = "SELECT * FROM vehicle WHERE resident_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, residentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                vehicles.add(mapResultSetToVehicle(rs));
            }
        }
        return vehicles;
    }

    public List<Vehicle> getCurrentlyParkedVehiclesByResident(String residentId) throws SQLException {
        List<Vehicle> parked = new ArrayList<>();
        String query = "SELECT v.* FROM vehicle v JOIN ( " +
                "SELECT vehicle_number FROM parked_vehicle " +
                "UNION " +
                "SELECT vehicle_number FROM spare_parked_vehicle " +
                ") AS all_parked ON v.vehicle_number = all_parked.vehicle_number " +
                "WHERE v.resident_id = ?";

        try (PreparedStatement ps = this.connection.prepareStatement(query)) {
            ps.setString(1, residentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                parked.add(mapResultSetToVehicle(rs));
            }
        }
        return parked;
    }

    public Vehicle getVehicleByNumber(String vehicleNumber) throws SQLException {
        String query = "SELECT * FROM vehicle WHERE vehicle_number = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, vehicleNumber);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToVehicle(rs);
            }
        }
        return null;
    }

    public List<Vehicle> getAllVehicles(String sortBy) throws SQLException {
        List<Vehicle> vehicles = new ArrayList<>();
        String[] allowedSorts = {"resident_id", "vehicle_type", "vehicle_brand", "vehicle_number"};
        boolean isValidSort = false;
        for (String allowed : allowedSorts) {
            if (allowed.equalsIgnoreCase(sortBy)) {
                isValidSort = true;
                break;
            }
        }
        if (!isValidSort) {
            sortBy = "vehicle_number";
        }

        String query = "SELECT * FROM vehicle ORDER BY " + sortBy;

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                vehicles.add(mapResultSetToVehicle(rs));
            }
        }
        return vehicles;
    }

    public boolean addVehicle(Vehicle vehicle) throws SQLException {
        // This query now includes owner_type
        String query = "INSERT INTO vehicle (resident_id, vehicle_number, vehicle_type, vehicle_brand, owner_type) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, vehicle.getResidentId());
            ps.setString(2, vehicle.getVehicleNumber());
            ps.setString(3, vehicle.getVehicleType());
            ps.setString(4, vehicle.getVehicleBrand());
            ps.setString(5, vehicle.getOwnerType());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteVehicle(String vehicleNumber) throws SQLException {
        String query = "DELETE FROM vehicle WHERE vehicle_number = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, vehicleNumber);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean vehicleExists(String vehicleNumber) throws SQLException {
        String query = "SELECT 1 FROM vehicle WHERE vehicle_number = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, vehicleNumber);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        }
    }

    // MODIFIED: This method now uses your suggested logic to ignore guest vehicles.
    public int[] getResidentVehicleCounts(String residentId) throws SQLException {
        int[] counts = new int[2]; // [0] for 4-wheelers, [1] for 2-wheelers
        String query = "SELECT v.vehicle_type, COUNT(v.vehicle_number) as count " +
                "FROM vehicle v " +
                "LEFT JOIN spare_parked_vehicle spv ON v.vehicle_number = spv.vehicle_number " +
                "WHERE v.resident_id = ? AND spv.vehicle_number IS NULL " +
                "GROUP BY v.vehicle_type";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, residentId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                if ("4-wheeler".equals(rs.getString("vehicle_type"))) {
                    counts[0] = rs.getInt("count");
                } else if ("2-wheeler".equals(rs.getString("vehicle_type"))) {
                    counts[1] = rs.getInt("count");
                }
            }
        }
        return counts;
    }

    public String getResidentIdForVehicle(String vehicleNumber) throws SQLException {
        String query = "SELECT resident_id FROM vehicle WHERE vehicle_number = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, vehicleNumber);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getString("resident_id") : null;
        }
    }

    public Resident findResidentByVehicleNumber(String vehicleNumber) throws SQLException {
        String query = "SELECT r.* FROM residents r JOIN vehicle v ON r.resident_id = v.resident_id WHERE v.vehicle_number = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, vehicleNumber);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Resident(
                        rs.getString("resident_id"), rs.getString("first_name"), rs.getString("last_name"),
                        rs.getString("contact_number"), rs.getString("wing"), rs.getInt("house_number"),
                        rs.getString("subscription_tier"), rs.getBigDecimal("subscription_cost"),
                        rs.getDate("subscription_start_date"), rs.getDate("subscription_valid_until"),
                        rs.getInt("vehicle_count"), rs.getString("username"), rs.getString("password")
                );
            }
        }
        return null;
    }

    private Vehicle mapResultSetToVehicle(ResultSet rs) throws SQLException {
        // MODIFIED: This now reads the owner_type column and passes it to the new constructor
        return new Vehicle(
                rs.getString("vehicle_number"),
                rs.getString("resident_id"),
                rs.getString("vehicle_type"),
                rs.getString("vehicle_brand"),
                rs.getString("owner_type")
        );
    }

}