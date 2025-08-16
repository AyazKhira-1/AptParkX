package dao;

import database.DatabaseManager;
import model.Resident;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ResidentDAO handles all database operations related to the Resident model.
 * This includes creating, reading, updating, and deleting resident records.
 */
public class ResidentDAO {

    private final Connection connection;

    /**
     * Default constructor for ResidentDAO.
     * Establishes a database connection.
     */
    public ResidentDAO() {
        try {
            this.connection = DatabaseManager.getConnection();
        } catch (SQLException e) {
            System.err.println("Database connection failed in ResidentDAO.");
            throw new RuntimeException(e);
        }
    }

    /**
     * Constructor that accepts an existing database connection.
     * @param connection The database connection to use.
     */
    public ResidentDAO(Connection connection) {
        this.connection = connection;
    }


    /**
     * Retrieves a resident from the database by their ID.
     *
     * @param residentId The ID of the resident to retrieve.
     * @return A Resident object if found, otherwise null.
     */
    public Resident getResidentById(String residentId) throws SQLException {
        String query = "SELECT * FROM residents WHERE resident_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, residentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToResident(rs);
            }
        }
        return null;
    }

    /**
     * Retrieves all residents from the database, with an option to filter by wing.
     *
     * @param wing The wing to filter by (e.g., "A", "B", "C"). If null, all residents are returned.
     * @return A list of Resident objects.
     */
    public List<Resident> getAllResidents(String wing) throws SQLException {
        List<Resident> residents = new ArrayList<>();
        String query = "SELECT * FROM residents";
        if (wing != null && !wing.isEmpty()) {
            query += " WHERE wing = ?";
        }

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            if (wing != null && !wing.isEmpty()) {
                ps.setString(1, wing);
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                residents.add(mapResultSetToResident(rs));
            }
        }
        return residents;
    }

    /**
     * Adds a new resident to the database.
     *
     * @param resident The Resident object to add.
     * @return true if the resident was added successfully, false otherwise.
     */
    public boolean addResident(Resident resident) throws SQLException {
        String query = "INSERT INTO residents (resident_id, first_name, last_name, contact_number, wing, house_number, vehicle_count, username, password) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, resident.getResidentId());
            ps.setString(2, resident.getFirstName());
            ps.setString(3, resident.getLastName());
            ps.setString(4, resident.getContactNumber());
            ps.setString(5, resident.getWing());
            ps.setInt(6, resident.getHouseNumber());
            ps.setInt(7, resident.getVehicleCount());
            ps.setString(8, resident.getUsername());
            ps.setString(9, resident.getPassword());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Updates an existing resident's information in the database.
     *
     * @param resident The Resident object with updated information.
     * @return true if the update was successful, false otherwise.
     */
    public boolean updateResident(Resident resident) throws SQLException {
        String query = "UPDATE residents SET first_name = ?, last_name = ?, contact_number = ?, username = ?, password = ? WHERE resident_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, resident.getFirstName());
            ps.setString(2, resident.getLastName());
            ps.setString(3, resident.getContactNumber());
            ps.setString(4, resident.getUsername());
            ps.setString(5, resident.getPassword());
            ps.setString(6, resident.getResidentId());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Deletes a resident from the database.
     *
     * @param residentId The ID of the resident to delete.
     * @return true if the deletion was successful, false otherwise.
     */
    public boolean deleteResident(String residentId) throws SQLException {
        String query = "DELETE FROM residents WHERE resident_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, residentId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Gets the total count of residents in the database.
     *
     * @return The total number of residents.
     */
    public int getResidentCount() throws SQLException {
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM residents")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /**
     * Gets the count of residents in a specific wing.
     *
     * @param wing The wing to count residents in.
     * @return The number of residents in the specified wing.
     */
    public int getResidentCountInWing(String wing) throws SQLException {
        String query = "SELECT COUNT(*) FROM residents WHERE wing = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, wing);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /**
     * Checks if a resident exists in the database.
     *
     * @param residentId The ID of the resident to check.
     * @return true if the resident exists, false otherwise.
     */
    public boolean residentExists(String residentId) throws SQLException {
        String query = "SELECT 1 FROM residents WHERE resident_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, residentId);
            ResultSet rs = ps.executeQuery();
            return !rs.next();
        }
    }

    /**
     * Checks if a phone number already exists in the residents table.
     *
     * @param phoneNumber The phone number to check.
     * @return true if the number exists, false otherwise.
     */
    public boolean phoneNumberExists(String phoneNumber) throws SQLException {
        String query = "SELECT 1 FROM residents WHERE contact_number = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, phoneNumber);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        }
    }

    /**
     * Updates the vehicle count for a specific resident.
     *
     * @param residentId The ID of the resident.
     * @param change     The amount to change the vehicle count by (e.g., 1 to add, -1 to remove).
     */
    public void updateResidentVehicleCount(String residentId, int change) throws SQLException {
        String query = "UPDATE residents SET vehicle_count = vehicle_count + ? WHERE resident_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, change);
            ps.setString(2, residentId);
            ps.executeUpdate();
        }
    }

    /**
     * Helper method to map a ResultSet row to a Resident object.
     *
     * @param rs The ResultSet to map.
     * @return A new Resident object.
     * @throws SQLException if a database access error occurs.
     */
    private Resident mapResultSetToResident(ResultSet rs) throws SQLException {
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
