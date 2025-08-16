package ui;

import dao.ResidentDAO; // We need this to check for phone number existence

import java.util.InputMismatchException;
import java.util.Scanner;
import java.sql.SQLException;

/**
 * InputHandler is a final utility class that provides static methods for handling
 * and validating all user input throughout the application.
 */
public final class InputHandler {
    private static final Scanner scanner = new Scanner(System.in);
    private static final ResidentDAO residentDAO = new ResidentDAO(); // For validation checks

    /**
     * Private constructor to prevent this utility class from being instantiated.
     */
    private InputHandler() {
    }

    /**
     * Gets a valid integer choice from the user within a specified range.
     *
     * @param min The minimum acceptable value.
     * @param max The maximum acceptable value.
     * @return A valid integer choice from the user.
     */
    public static int getValidChoice(int min, int max) {
        while (true) {
            try {
                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume the rest of the line
                if (choice >= min && choice <= max) {
                    return choice;
                } else {
                    System.out.print("Invalid choice. Please select a number between " + min + " and " + max + ": ");
                }
            } catch (InputMismatchException e) {
                System.out.print("Invalid input. Please enter a number: ");
                scanner.nextLine(); // Clear the invalid input from the scanner
            }
        }
    }

    /**
     * Prompts the user for a string and ensures it is not empty.
     *
     * @param prompt The message to display to the user.
     * @return A non-empty string provided by the user.
     */
    public static String getValidStringInput(String prompt) {
        String input;
        while (true) {
            System.out.print(prompt);
            input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                return input;
            }
            System.out.println("Input cannot be empty. Please try again.");
        }
    }

    /**
     * Prompts the user for an updated string value, allowing them to keep the old value.
     *
     * @param prompt   The message to display.
     * @param oldValue The current value of the field.
     * @return The new value, or the old value if the user enters nothing.
     */
    public static String getUpdatedStringInput(String prompt, String oldValue) {
        System.out.print(prompt + " (current: " + oldValue + ", press Enter to keep): ");
        String input = scanner.nextLine().trim();
        return input.isEmpty() ? oldValue : input;
    }

    /**
     * Gets a valid 10-digit phone number and ensures it's not already in use.
     *
     * @param prompt The message to display to the user.
     * @return A unique and valid 10-digit phone number.
     */
    public static String getValidPhoneNumberInput(String prompt) {
        String input;
        while (true) {
            input = getValidStringInput(prompt);
            if (!input.matches("[6-9]\\d{9}")) {
                System.out.println("Invalid format. Phone number must be 10 digits and start with 6, 7, 8, or 9.");
                continue;
            }
            try {
                if (residentDAO.phoneNumberExists(input)) {
                    System.out.println("Error: This phone number is already registered. Please enter a different one.");
                    continue;
                }
            } catch (SQLException e) {
                System.err.println("Database error while validating phone number: " + e.getMessage());
                // We assume it exists to be safe and prevent duplicates on error
                continue;
            }
            return input;
        }
    }

    /**
     * Gets a valid wing input from the user (A, B, or C).
     *
     * @return The validated wing character as a String.
     */
    public static String getValidWingInput() {
        String input;
        while (true) {
            System.out.print("Enter Wing (A, B, C): ");
            input = scanner.nextLine().trim().toUpperCase();
            if (input.equals("A") || input.equals("B") || input.equals("C")) {
                return input;
            }
            System.out.println("Invalid input. Please enter A, B, or C.");
        }
    }

    /**
     * Validates the format of a vehicle registration number.
     *
     * @param vehicleNumber The vehicle number to validate.
     * @return true if the format is valid, false otherwise.
     */
    public static boolean isValidVehicleNumber(String vehicleNumber) {
        // Expected format: 2 letters, 2 digits, 2 letters, 4 digits (e.g., GJ05CD5678)
        return vehicleNumber != null && vehicleNumber.matches("[A-Z]{2}\\d{2}[A-Z]{2}\\d{4}");
    }

    /**
     * Prompts the user to select a vehicle type.
     *
     * @param prompt The message to display.
     * @return "4-wheeler" or "2-wheeler" based on user selection.
     */
    public static String getValidVehicleTypeInput(String prompt) {
        System.out.println("1. 4-wheeler");
        System.out.println("2. 2-wheeler");
        System.out.print(prompt + ": ");
        int choice = getValidChoice(1, 2);
        return (choice == 1) ? "4-wheeler" : "2-wheeler";
    }

    /**
     * Validates the strength of a password based on predefined rules.
     *
     * @param password The password string to validate.
     * @return true if the password meets the criteria, false otherwise.
     */
    public static boolean isPasswordValid(String password) {
        if (password == null || password.length() < 8) return false;
        // Regex checks for: at least 8 chars, 1 uppercase, 2 digits, 1 special char.
        return password.matches("^(?=.*[A-Z])(?=.*\\d.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,}$");
    }
}
