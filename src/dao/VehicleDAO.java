package dao;

import database.DatabaseManager;
import model.Resident;
import model.Vehicle;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * VehicleDAO handles all database operations related to the Vehicle model.
 * This includes creating, reading, updating, and deleting vehicle records.
 */
public class VehicleDAO {

    private final Connection connection;

    /**
     * Constructor for VehicleDAO.
     * Establishes a database connection.
     */
    public VehicleDAO() {
        try {
            this.connection = DatabaseManager.getConnection();
        } catch (SQLException e) {
            System.err.println("Database connection failed in VehicleDAO.");
            throw new RuntimeException(e);
        }
    }

    /**
     * Retrieves a vehicle from the database by its number.
     *
     * @param vehicleNumber The number of the vehicle to retrieve.
     * @return A Vehicle object if found, otherwise null.
     */
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

    /**
     * Retrieves all vehicles from the database, with sorting options.
     *
     * @param sortBy The column to sort the results by (e.g., "resident_id", "vehicle_type").
     * @return A list of Vehicle objects.
     */
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

    /**
     * Adds a new vehicle to the database.
     *
     * @param vehicle The Vehicle object to add.
     * @return true if the vehicle was added successfully, false otherwise.
     */
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

    /**
     * Deletes a vehicle from the database.
     *
     * @param vehicleNumber The number of the vehicle to delete.
     * @return true if the deletion was successful, false otherwise.
     */
    public boolean deleteVehicle(String vehicleNumber) throws SQLException {
        String query = "DELETE FROM vehicle WHERE vehicle_number = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, vehicleNumber);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Checks if a vehicle exists in the database.
     *
     * @param vehicleNumber The number of the vehicle to check.
     * @return true if the vehicle exists, false otherwise.
     */
    public boolean vehicleExists(String vehicleNumber) throws SQLException {
        String query = "SELECT 1 FROM vehicle WHERE vehicle_number = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, vehicleNumber);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        }
    }

    /**
     * Gets the counts of 4-wheelers and 2-wheelers for a specific resident.
     *
     * @param residentId The ID of the resident.
     * @return An array of two integers: [count of 4-wheelers, count of 2-wheelers].
     */
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

    /**
     * Finds the resident ID associated with a given vehicle number.
     *
     * @param vehicleNumber The vehicle number to search for.
     * @return The resident's ID if found, otherwise null.
     */
    public String getResidentIdForVehicle(String vehicleNumber) throws SQLException {
        String query = "SELECT resident_id FROM vehicle WHERE vehicle_number = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, vehicleNumber);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getString("resident_id") : null;
        }
    }

    /**
     * Finds a resident by their vehicle number.
     *
     * @param vehicleNumber The vehicle number.
     * @return A Resident object if found, otherwise null.
     */
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


    /**
     * Helper method to map a ResultSet row to a Vehicle object.
     *
     * @param rs The ResultSet to map.
     * @return A new Vehicle object.
     * @throws SQLException if a database access error occurs.
     */
    private Vehicle mapResultSetToVehicle(ResultSet rs) throws SQLException {
        return new Vehicle(
                rs.getString("vehicle_number"),
                rs.getString("resident_id"),
                rs.getString("vehicle_type"),
                rs.getString("vehicle_brand")
        );
    }
}
