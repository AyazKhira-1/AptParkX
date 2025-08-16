package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DatabaseManager provides a centralized way to manage the database connection.
 * It uses the Singleton pattern to ensure only one connection instance is created.
 */
public final class DatabaseManager {
    // Database connection parameters
    private static final String DB_URL = "jdbc:mysql://localhost:3306/vehicle_parking_manager";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = ""; // Add your database password here if you have one
    private static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";

    // The single instance of the connection
    private static Connection connectionInstance = null;

    // Static block to register the JDBC driver once when the class is loaded.
    static {
        try {
            Class.forName(JDBC_DRIVER);
        } catch (ClassNotFoundException e) {
            // This is a fatal error, so we throw a RuntimeException to stop the application.
            System.err.println("FATAL: MySQL JDBC Driver not found. Please add it to your project's classpath.");
            throw new RuntimeException("JDBC Driver not found!", e);
        }
    }

    /**
     * Private constructor to prevent instantiation from outside the class.
     */
    private DatabaseManager() {
        // This class should not be instantiated.
    }

    /**
     * Provides a global point of access to the single database connection instance.
     * If the connection does not exist or is closed, it creates a new one.
     * This method is thread-safe.
     *
     * @return The single instance of the database Connection.
     * @throws SQLException if a database access error occurs.
     */
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

    /**
     * Closes the database connection if it is open.
     * This can be called when the application is shutting down to release resources.
     */
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
