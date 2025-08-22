package main;

import database.DatabaseManager;
import ui.AdminUI;
import ui.InputHandler;
import ui.ResidentUI;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class AptParkX {

    private static final String WELCOME_MESSAGE = "=== Welcome to AptParkX - Apartment Parking Management System ===";
    private static final String GOODBYE_MESSAGE = "Thank you for using AptParkX. See you later!";

    private AptParkX() {
    }

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

            }
        }
    }


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

    private static void handleAdminLogin() {
        System.out.println("\n=== Admin Login ===");
        String adminName = InputHandler.getValidStringInput("Enter Admin Username: ");
        String adminPassword = InputHandler.getValidStringInput("Enter Admin Password: ");


        String query = "SELECT * FROM admin WHERE BINARY name = ? AND BINARY password = ?";
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

    private static void handleResidentLogin() {
        System.out.println("\n=== Resident Login ===");
        String username = InputHandler.getValidStringInput("Enter Username: ");
        String password = InputHandler.getValidStringInput("Enter Password: ");

        // MODIFIED: The query now fetches last_name, wing, and house_number
        String query = "SELECT resident_id, first_name, last_name, wing, house_number FROM residents WHERE BINARY username = ? AND BINARY password = ?";
        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                // MODIFIED: Retrieving the new details from the database result
                String residentId = rs.getString("resident_id");
                String firstName = rs.getString("first_name");
                String lastName = rs.getString("last_name");
                String wing = rs.getString("wing");
                int houseNumber = rs.getInt("house_number");

                // MODIFIED: Displaying the new, detailed welcome message
                System.out.println("✓ Login successful!");
                System.out.println("Welcome, " + firstName + " " + lastName + "!");
                System.out.println("Wing : " + wing + ", House No : " + houseNumber);

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
