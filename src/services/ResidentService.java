package services;

import dao.ResidentDAO;
import database.DatabaseManager;
import database.TransactionManager;
import model.Resident;
import ui.InputHandler;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public final class ResidentService {
    private static final Scanner scanner = new Scanner(System.in);
    private final ResidentDAO residentDAO;
    private final VehicleService vehicleService; // To interact with vehicle operations


    public ResidentService() {
        this.residentDAO = new ResidentDAO();
        this.vehicleService = new VehicleService();
    }

    public void searchResident() {
        System.out.println("\n--- Search for a Resident ---");
        String residentId = InputHandler.getValidStringInput("Enter Resident ID to search: ");

        try {
            Resident resident = residentDAO.getResidentById(residentId);

            System.out.println("\n--- Search Results ---");
            System.out.println("----------------------------------------------------------------------------------------------------");
            System.out.printf("%-10s | %-15s | %-15s | %-15s | %-5s | %-10s | %s%n", "Res ID", "First Name", "Last Name", "Phone No.", "Wing", "House No", "Vehicle Count");
            System.out.println("----------------------------------------------------------------------------------------------------");

            if (resident == null) {
                System.out.println("No resident found with ID '" + residentId + "'.");
            } else {
                displayResidentInfo(resident);
            }
            System.out.println("----------------------------------------------------------------------------------------------------");

        } catch (SQLException e) {
            System.err.println("Database error during resident search: " + e.getMessage());
        }
    }

    public void addResident() {
        System.out.println("\n--- Add New Resident ---");
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            TransactionManager.beginTransaction(conn);

            ResidentDAO residentDAOForTx = new ResidentDAO(conn);

            if (residentDAOForTx.getResidentCount() >= 150) {
                System.out.println("Cannot add new resident. The apartment is at full capacity (150 residents).");
                TransactionManager.rollbackTransaction(conn);
                return;
            }

            String firstName = InputHandler.getValidStringInput("Enter First Name: ");
            String lastName = InputHandler.getValidStringInput("Enter Last Name: ");
            String contactNumber = InputHandler.getValidPhoneNumberInput("Enter Phone Number: ");
            String wing = InputHandler.getValidWingInput();

            if (residentDAOForTx.getResidentCountInWing(wing) >= 50) {
                System.out.println("Cannot add resident to Wing " + wing + ". It is at full capacity (50 residents).");
                TransactionManager.rollbackTransaction(conn);
                return;
            }

            // MODIFIED: This is the new, correct way to generate a unique Resident ID.
            int latestIdNum = residentDAOForTx.getLatestResidentIdNumber();
            String residentId = String.format("RES%03d", latestIdNum + 1);

            int houseNumber = residentDAOForTx.getResidentCountInWing(wing) + 1;
            String username = firstName.toLowerCase() + houseNumber;
            String password = contactNumber.substring(contactNumber.length() - 4) + "@" + wing + "#" + houseNumber;

            Resident newResident = new Resident(residentId, firstName, lastName, contactNumber, wing, houseNumber, 0, username, password);

            if (residentDAOForTx.addResident(newResident)) {
                System.out.println("Resident added successfully!");
                System.out.println("Generated Resident ID: " + residentId);
                System.out.println("Generated Username: " + username);
                System.out.println("Generated Password: " + password);

                vehicleService.addVehiclesForNewResident(residentId, conn);
                TransactionManager.commitTransaction(conn);
            } else {
                TransactionManager.rollbackTransaction(conn);
            }
        } catch (SQLException e) {
            System.err.println("Database error while adding resident: " + e.getMessage());
            TransactionManager.rollbackTransaction(conn);
        } finally {
            TransactionManager.endTransaction(conn);
        }
    }

    public void editResident() {
        String residentId = InputHandler.getValidStringInput("Enter the Resident ID to edit (e.g., RES001): ").toUpperCase();
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            TransactionManager.beginTransaction(conn);
            ResidentDAO residentDAOForTx = new ResidentDAO(conn);

            Resident existingResident = residentDAOForTx.getResidentById(residentId);

            if (existingResident == null) {
                System.out.println("Resident with ID '" + residentId + "' not found.");
                TransactionManager.rollbackTransaction(conn);
                return;
            }

            System.out.println("\n--- Editing Resident: " + existingResident.getFirstName() + " " + existingResident.getLastName() + " ---");
            System.out.println("Press Enter to keep the current value.");

            String newFirstName = InputHandler.getUpdatedStringInput("Enter new First Name", existingResident.getFirstName());
            String newLastName = InputHandler.getUpdatedStringInput("Enter new Last Name", existingResident.getLastName());

            // MODIFIED: This now calls the more powerful version of our new method
            String newContactNumber = InputHandler.getValidPhoneNumberInput("Enter new Phone Number", existingResident.getContactNumber());

            existingResident.setFirstName(newFirstName);
            existingResident.setLastName(newLastName);
            existingResident.setContactNumber(newContactNumber);

            String newUsername = newFirstName.toLowerCase() + existingResident.getHouseNumber();
            String newPassword = newContactNumber.substring(newContactNumber.length() - 4) + "@" + existingResident.getWing() + "#" + existingResident.getHouseNumber();
            existingResident.setUsername(newUsername);
            existingResident.setPassword(newPassword);

            if (residentDAOForTx.updateResident(existingResident)) {
                System.out.println("Resident updated successfully!");
                if (!newUsername.equals(existingResident.getUsername()) || !newPassword.equals(existingResident.getPassword())) {
                    System.out.println("Updated Username: " + newUsername);

                    System.out.println("Updated Password: " + newPassword);
                }
                TransactionManager.commitTransaction(conn);
            } else {
                TransactionManager.rollbackTransaction(conn);
            }
        } catch (SQLException e) {
            System.err.println("Database error while editing resident: " + e.getMessage());
            TransactionManager.rollbackTransaction(conn);
        } finally {
            TransactionManager.endTransaction(conn);
        }
    }

    public void deleteResident() {
        String residentId = InputHandler.getValidStringInput("Enter the Resident ID to delete: ").toUpperCase();
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            TransactionManager.beginTransaction(conn);
            ResidentDAO residentDAOForTx = new ResidentDAO(conn);

            // First, get the resident to make sure they exist and to show their name
            Resident residentToDelete = residentDAOForTx.getResidentById(residentId);
            if (residentToDelete == null) {
                System.out.println("Resident with ID '" + residentId + "' not found.");
                TransactionManager.rollbackTransaction(conn);
                return;
            }

            // MODIFIED: Added a validation loop for the confirmation prompt.
            while (true) {
                System.out.print("Are you sure you want to delete resident '" + residentId + " (" + residentToDelete.getFirstName() + " " + residentToDelete.getLastName() + ")'? " +
                        "This will also delete all their vehicles. (y/n): ");
                String choice = scanner.nextLine().trim().toLowerCase();

                if (choice.equals("y")) {
                    if (residentDAOForTx.deleteResident(residentId)) {
                        System.out.println("Resident '" + residentId + "' and all associated vehicles deleted successfully.");
                        TransactionManager.commitTransaction(conn);
                    } else {
                        System.err.println("Error: Deletion failed in the database.");
                        TransactionManager.rollbackTransaction(conn);
                    }
                    break; // Exit the loop
                } else if (choice.equals("n")) {
                    System.out.println("Deletion cancelled.");
                    TransactionManager.rollbackTransaction(conn);
                    break; // Exit the loop
                } else {
                    System.out.println("Invalid input. Please enter 'y' for yes or 'n' for no.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error during deletion: " + e.getMessage());
            TransactionManager.rollbackTransaction(conn);
        } finally {
            TransactionManager.endTransaction(conn);
        }
    }

    public void viewAllResidents(String wing) {
        String title = (wing != null) ? "\n--- Residents in Wing " + wing + " ---" : "\n--- All Residents ---";
        System.out.println(title);

        try {
            List<Resident> residents = residentDAO.getAllResidents(wing);

            System.out.println("-----------------------------------------------------------------------------------------------------");
            System.out.printf("%-10s | %-15s | %-15s | %-15s | %-5s | %-10s | %s%n", "Res ID", "First Name", "Last Name", "Phone No.", "Wing", "House No", "Vehicle Count");
            System.out.println("-----------------------------------------------------------------------------------------------------");

            if (residents.isEmpty()) {
                System.out.println("No residents found for this selection.");
            } else {
                for (Resident resident : residents) {
                    displayResidentInfo(resident);
                }
            }
            System.out.println("-----------------------------------------------------------------------------------------------------");
        } catch (SQLException e) {
            System.err.println("Database error while viewing residents: " + e.getMessage());
        }
    }
    private void displayResidentInfo(Resident resident) {
        System.out.printf("%-10s | %-15s | %-15s | %-15s | %-5s | %-10d | %d%n",
                resident.getResidentId(), resident.getFirstName(),
                resident.getLastName(), resident.getContactNumber(),
                resident.getWing(), resident.getHouseNumber(),
                resident.getVehicleCount());
    }
}
