package dao;

import database.DatabaseManager;
import model.Resident;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class ResidentDAO {

    private final Connection connection;

    public ResidentDAO() {
        try {
            this.connection = DatabaseManager.getConnection();
        } catch (SQLException e) {
            System.err.println("Database connection failed in ResidentDAO.");
            throw new RuntimeException(e);
        }
    }


    public ResidentDAO(Connection connection) {
        this.connection = connection;
    }

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

    public int getLatestResidentIdNumber() throws SQLException {

        String query = "SELECT MAX(CAST(SUBSTRING(resident_id, 4) AS UNSIGNED)) FROM residents";
        try (Statement st = this.connection.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            if (rs.next()) {

                return rs.getInt(1);
            }
        }
        return 0;
    }

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

    public boolean deleteResident(String residentId) throws SQLException {
        String query = "DELETE FROM residents WHERE resident_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, residentId);
            return ps.executeUpdate() > 0;
        }
    }

    public int getResidentCount() throws SQLException {
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM residents")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public int getResidentCountInWing(String wing) throws SQLException {
        String query = "SELECT COUNT(*) FROM residents WHERE wing = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, wing);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public boolean residentExists(String residentId) throws SQLException {
        String query = "SELECT 1 FROM residents WHERE resident_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, residentId);
            ResultSet rs = ps.executeQuery();
            return !rs.next();
        }
    }

    public boolean phoneNumberExists(String phoneNumber) throws SQLException {

        String sql = "SELECT COUNT(*) FROM residents WHERE contact_number = ?";

        try (PreparedStatement pstmt = this.connection.prepareStatement(sql)) {

            pstmt.setString(1, phoneNumber);

            try (ResultSet rs = pstmt.executeQuery()) {

                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public void updateResidentVehicleCount(String residentId, int change) throws SQLException {
        String query = "UPDATE residents SET vehicle_count = vehicle_count + ? WHERE resident_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, change);
            ps.setString(2, residentId);
            ps.executeUpdate();
        }
    }
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
