package main;

import database.DatabaseManager;
import ui.AdminUI;
import ui.InputHandler;
import ui.ResidentUI;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * AptParkX is the main class and the entry point for the application.
 * It handles the main menu, user login, and delegates tasks to the appropriate UI classes.
 */
public final class AptParkX {

    private static final String WELCOME_MESSAGE = "=== Welcome to AptParkX - Apartment Parking Management System ===";
    private static final String GOODBYE_MESSAGE = "Thank you for using AptParkX. See you later!";

    /**
     * Private constructor to prevent this class from being instantiated.
     */
    private AptParkX() {
    }

    /**
     * The main method that runs the application.
     *
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {
        System.out.println("\n" + WELCOME_MESSAGE + "\n");
        boolean running = true;

        while (running) {
            try {
                displayMainMenu();
                int choice = InputHandler.getValidChoice(1, 3);

                switch (choice) {
                    case 1:
                        handleAdminLogin();
                        break;
                    case 2:
                        handleResidentLogin();
                        break;
                    case 3:
                        System.out.println(GOODBYE_MESSAGE);
                        DatabaseManager.closeConnection(); // Gracefully close the database connection
                        running = false;
                        break;
                }
            } catch (Exception e) {
                System.err.println("An unexpected error occurred in the main loop: " + e.getMessage());
                // This prevents the application from crashing on unexpected errors.
            }
        }
    }

    /**
     * Displays the main login menu for the user.
     */
    private static void displayMainMenu() {
        System.out.println("\n┌─────────────────────────────────────┐");
        System.out.println("│              MAIN MENU              │");
        System.out.println("├─────────────────────────────────────┤");
        System.out.println("│ 1. Admin Login                      │");
        System.out.println("│ 2. Resident Login                   │");
        System.out.println("│ 3. Exit                             │");
        System.out.println("└─────────────────────────────────────┘");
        System.out.print("Enter your choice (1-3): ");
    }

    /**
     * Handles the administrator login process.
     */
    private static void handleAdminLogin() {
        System.out.println("\n=== Admin Login ===");
        String adminName = InputHandler.getValidStringInput("Enter Admin Username: ");
        String adminPassword = InputHandler.getValidStringInput("Enter Admin Password: ");

        String query = "SELECT * FROM admin WHERE name = ? AND password = ?";
        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, adminName);
            ps.setString(2, adminPassword);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                System.out.println("✓ Admin login successful!");
                System.out.println("Welcome, " + adminName + "!");
                AdminUI adminUI = new AdminUI(adminName);
                adminUI.displayAdminMenu();
            } else {
                System.out.println("✗ Admin login failed: Invalid Admin Credentials.");
            }
        } catch (SQLException e) {
            System.err.println("Database error during admin login: " + e.getMessage());
        }
    }

    /**
     * Handles the resident login process.
     */
    private static void handleResidentLogin() {
        System.out.println("\n=== Resident Login ===");
        String username = InputHandler.getValidStringInput("Enter Username: ");
        String password = InputHandler.getValidStringInput("Enter Password: ");

        String query = "SELECT resident_id, first_name FROM residents WHERE username = ? AND password = ?";
        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String residentId = rs.getString("resident_id");
                String firstName = rs.getString("first_name");
                System.out.println("✓ Login successful!");
                System.out.println("Welcome, " + firstName + "!");

                ResidentUI residentUI = new ResidentUI(residentId);
                residentUI.displayResidentMenu();
            } else {
                System.out.println("✗ Login failed: Invalid username or password.");
            }
        } catch (SQLException e) {
            System.err.println("Database error during resident login: " + e.getMessage());
        }
    }
}
