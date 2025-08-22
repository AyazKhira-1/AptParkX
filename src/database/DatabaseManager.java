package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseManager {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/vehicle_parking_manager";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = ""; // Add your database password here if you have one



    private static Connection connectionInstance = null;

    private DatabaseManager() {

    }

    public static synchronized Connection getConnection() throws SQLException {
        if (connectionInstance == null || connectionInstance.isClosed()) {
            try {
                connectionInstance = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                connectionInstance.setAutoCommit(false); // Disable auto-commit for transaction management
            } catch (SQLException e) {
                System.err.println("Failed to establish a new database connection.");
                throw e; // Re-throw the exception to be handled by the caller
            }
        }
        return connectionInstance;
    }

    public static synchronized void closeConnection() {
        if (connectionInstance != null) {
            try {
                if (!connectionInstance.isClosed()) {
                    connectionInstance.close();
                    System.out.println("Database connection closed successfully.");
                }
            } catch (SQLException e) {
                System.err.println("Failed to close the database connection: " + e.getMessage());
            } finally {
                connectionInstance = null;
            }
        }
    }
}
