package dao;

import database.DatabaseManager;
import model.Resident;
import model.Vehicle;

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
        // Basic validation to prevent SQL injection on sortBy parameter
        String[] allowedSorts = {"resident_id", "vehicle_type", "vehicle_brand", "vehicle_number"};
        boolean isValidSort = false;
        for (String allowed : allowedSorts) {
            if (allowed.equalsIgnoreCase(sortBy)) {
                isValidSort = true;
                break;
            }
        }
        if (!isValidSort) {
            sortBy = "vehicle_number"; // Default sort
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
        String query = "INSERT INTO vehicle (resident_id, vehicle_number, vehicle_type, vehicle_brand) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, vehicle.getResidentId());
            ps.setString(2, vehicle.getVehicleNumber());
            ps.setString(3, vehicle.getVehicleType());
            ps.setString(4, vehicle.getVehicleBrand());
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

    public int[] getResidentVehicleCounts(String residentId) throws SQLException {
        int[] counts = new int[2]; // [0] for 4-wheelers, [1] for 2-wheelers
        String query = "SELECT vehicle_type, COUNT(*) as count FROM vehicle WHERE resident_id = ? GROUP BY vehicle_type";
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
                // This mapping logic should ideally be in a shared space or the ResidentDAO,
                // but is included here for completeness of the method's functionality.
                return new Resident(
                        rs.getString("resident_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("contact_number"),
                        rs.getString("wing"),
                        rs.getInt("house_number"),
                        rs.getInt("vehicle_count"),
                        rs.getString("username"),
                        rs.getString("password")
                );
            }
        }
        return null;
    }

    private Vehicle mapResultSetToVehicle(ResultSet rs) throws SQLException {
        return new Vehicle(
                rs.getString("vehicle_number"),
                rs.getString("resident_id"),
                rs.getString("vehicle_type"),
                rs.getString("vehicle_brand")
        );
    }
}
