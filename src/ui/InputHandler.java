package ui;

import dao.ResidentDAO; // We need this to check for phone number existence

import java.util.InputMismatchException;
import java.util.Scanner;
import java.sql.SQLException;


public final class InputHandler {
    private static final Scanner scanner = new Scanner(System.in);

    private InputHandler() {
    }

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

    public static int getIntegerInput() {
        while (true) {
            try {
                int input = scanner.nextInt();
                scanner.nextLine(); // Consume the rest of the line
                return input;
            } catch (InputMismatchException e) {
                System.out.print("Invalid input. Please enter a whole number: ");
                scanner.nextLine(); // Clear the invalid input
            }
        }
    }

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

    public static String getUpdatedStringInput(String prompt, String oldValue) {
        System.out.print(prompt + " (current: " + oldValue + ", press Enter to keep): ");
        String input = scanner.nextLine().trim();
        return input.isEmpty() ? oldValue : input;
    }

    public static String getValidPhoneNumberInput(String prompt) {
        return getValidPhoneNumberInput(prompt, null);
    }

    // IN: ui/InputHandler.java
// REPLACE the entire getValidPhoneNumberInput method with this one.

    public static String getValidPhoneNumberInput(String prompt, String oldValue) {
        String newNumber;
        while (true) {
            if (oldValue != null) {
                System.out.print(prompt + " (current: " + oldValue + ", press Enter to keep): ");
            } else {
                System.out.print(prompt);
            }
            newNumber = scanner.nextLine().trim();

            if (oldValue != null && newNumber.isEmpty()) {
                return oldValue;
            }

            if (newNumber.isEmpty()) {
                System.out.println("Input cannot be empty. Please try again.");
                continue;
            }

            if (!newNumber.matches("[6-9]\\d{9}")) {
                System.out.println("Invalid format. Phone number must be 10 digits and start with 6, 7, 8, or 9.");
                continue;
            }

            if (newNumber.equals(oldValue)) {
                return newNumber;
            }

            try {
                ResidentDAO residentDAO = new ResidentDAO();
                if (residentDAO.phoneNumberExists(newNumber)) {
                    System.out.println("Error: This phone number is already registered to another resident.");
                    continue;
                }
            } catch (SQLException e) {
                System.err.println("Database error while validating phone number: " + e.getMessage());
                continue;
            }

            return newNumber;
        }
    }

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

    public static boolean isValidVehicleNumber(String vehicleNumber) {
        // Expected format: 2 letters, 2 digits, 2 letters, 4 digits (e.g., GJ05CD5678)
        return vehicleNumber != null && vehicleNumber.matches("[A-Z]{2}\\d{2}[A-Z]{2}\\d{4}");
    }

    // NEW METHOD: Loops until a valid vehicle number format is entered.
    public static String getValidVehicleNumberInput(String prompt) {
        String vehicleNumber;
        while (true) {
            vehicleNumber = getValidStringInput(prompt).toUpperCase();
            if (isValidVehicleNumber(vehicleNumber)) {
                return vehicleNumber;
            } else {
                System.out.println("Invalid format. Please use the format 'LLDDLLDDDD' (e.g., GJ01AB1234).");
            }
        }
    }

    public static String getValidVehicleTypeInput(String prompt) {
        System.out.println("1. 4-wheeler");
        System.out.println("2. 2-wheeler");
        System.out.print(prompt + ": ");
        int choice = getValidChoice(1, 2);
        return (choice == 1) ? "4-wheeler" : "2-wheeler";
    }

    public static boolean isPasswordValid(String password) {
        if (password == null || password.length() < 8) return false;
        // Regex checks for: at least 8 chars, 1 uppercase, 2 digits, 1 special char.
        return password.matches("^(?=.*[A-Z])(?=.*\\d.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,}$");
    }
}