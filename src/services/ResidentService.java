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

/**
 * ResidentService provides the business logic for resident-related operations.
 * It acts as an intermediary between the UI layer and the data access layer (DAO).
 */
public final class ResidentService {
    private static final Scanner scanner = new Scanner(System.in);
    private final ResidentDAO residentDAO;
    private final VehicleService vehicleService; // To interact with vehicle operations

    /**
     * Constructor for ResidentService.
     * Initializes the DAO and other required services.
     */
    public ResidentService() {
        this.residentDAO = new ResidentDAO();
        this.vehicleService = new VehicleService();
    }

    /**
     * Searches for a resident by ID and displays their details.
     */
    public void searchResident() {
        System.out.println("\n--- Search for a Resident ---");
        String residentId = InputHandler.getValidStringInput("Enter Resident ID to search: ");

        try {
            Resident resident = residentDAO.getResidentById(residentId);

            System.out.println("\n--- Search Results ---");
            System.out.println("------------------------------------------------------------------------------------------");
            System.out.printf("%-10s | %-15s | %-15s | %-15s | %-5s | %-10s | %s%n", "Res ID", "First Name", "Last Name", "Phone No.", "Wing", "House No", "Vehicle Count");
            System.out.println("------------------------------------------------------------------------------------------");

            if (resident == null) {
                System.out.println("No resident found with ID '" + residentId + "'.");
            } else {
                displayResidentInfo(resident);
            }
            System.out.println("------------------------------------------------------------------------------------------");

        } catch (SQLException e) {
            System.err.println("Database error during resident search: " + e.getMessage());
        }
    }

    /**
     * Handles the logic for adding a new resident.
     */
    public void addResident() {
        System.out.println("\n--- Add New Resident ---");
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            TransactionManager.beginTransaction(conn);

            ResidentDAO residentDAOForTx = new ResidentDAO(conn);

            // Check if the society is at full capacity
            if (residentDAOForTx.getResidentCount() >= 150) {
                System.out.println("Cannot add new resident. The apartment is at full capacity (150 residents).");
                TransactionManager.rollbackTransaction(conn);
                return;
            }

            // Get resident details from user input
            String firstName = InputHandler.getValidStringInput("Enter First Name: ");
            String lastName = InputHandler.getValidStringInput("Enter Last Name: ");
            String contactNumber = InputHandler.getValidPhoneNumberInput("Enter Phone Number: ");
            String wing = InputHandler.getValidWingInput();

            // Check if the selected wing is full
            if (residentDAOForTx.getResidentCountInWing(wing) >= 50) {
                System.out.println("Cannot add resident to Wing " + wing + ". It is at full capacity (50 residents).");
                TransactionManager.rollbackTransaction(conn);
                return;
            }

            // Generate unique ID, house number, and credentials
            String residentId = String.format("RES%03d", residentDAOForTx.getResidentCount() + 1);
            int houseNumber = residentDAOForTx.getResidentCountInWing(wing) + 1;
            String username = firstName.toLowerCase() + houseNumber;
            String password = contactNumber.substring(contactNumber.length() - 4) + "@" + wing + "#" + houseNumber;

            // Create a new Resident object
            Resident newResident = new Resident(residentId, firstName, lastName, contactNumber, wing, houseNumber, 0, username, password);

            // Add the resident to the database
            if (residentDAOForTx.addResident(newResident)) {
                System.out.println("Resident added successfully!");
                System.out.println("Generated Resident ID: " + residentId);
                System.out.println("Generated Username: " + username);
                System.out.println("Generated Password: " + password);

                // Prompt to add vehicles for the new resident
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

    /**
     * Handles the logic for editing an existing resident's details.
     */
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

            // Get updated information
            String newFirstName = InputHandler.getUpdatedStringInput("Enter new First Name", existingResident.getFirstName());
            String newLastName = InputHandler.getUpdatedStringInput("Enter new Last Name", existingResident.getLastName());
            String newContactNumber = InputHandler.getUpdatedStringInput("Enter new Phone Number", existingResident.getContactNumber());

            // Validate new phone number if it has changed
            if (!newContactNumber.equals(existingResident.getContactNumber()) && residentDAOForTx.phoneNumberExists(newContactNumber)) {
                System.out.println("Error: The new phone number is already registered to another resident. Update failed.");
                TransactionManager.rollbackTransaction(conn);
                return;
            }

            // Update resident object with new details
            existingResident.setFirstName(newFirstName);
            existingResident.setLastName(newLastName);
            existingResident.setContactNumber(newContactNumber);

            // Regenerate credentials based on updated info
            String newUsername = newFirstName.toLowerCase() + existingResident.getHouseNumber();
            String newPassword = newContactNumber.substring(newContactNumber.length() - 4) + "@" + existingResident.getWing() + "#" + existingResident.getHouseNumber();
            existingResident.setUsername(newUsername);
            existingResident.setPassword(newPassword);

            // Perform the update in the database
            if (residentDAOForTx.updateResident(existingResident)) {
                System.out.println("Resident updated successfully!");
                // Inform the admin of the new credentials if they changed
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

    /**
     * Handles the logic for deleting a resident.
     */
    public void deleteResident() {
        String residentId = InputHandler.getValidStringInput("Enter the Resident ID to delete: ").toUpperCase();
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            TransactionManager.beginTransaction(conn);
            ResidentDAO residentDAOForTx = new ResidentDAO(conn);

            if (residentDAOForTx.residentExists(residentId)) {
                System.out.println("Resident with ID '" + residentId + "' not found.");
                TransactionManager.rollbackTransaction(conn);
                return;
            }

            System.out.print("Are you sure you want to delete resident '" + residentId + "'? This will also delete all their vehicles. (y/n): ");
            if (scanner.nextLine().equalsIgnoreCase("y")) {
                if (residentDAOForTx.deleteResident(residentId)) {
                    System.out.println("Resident '" + residentId + "' and all associated vehicles deleted successfully.");
                    TransactionManager.commitTransaction(conn);
                } else {
                    TransactionManager.rollbackTransaction(conn);
                }
            } else {
                System.out.println("Deletion cancelled.");
                TransactionManager.rollbackTransaction(conn);
            }
        } catch (SQLException e) {
            System.err.println("Database error during deletion: " + e.getMessage());
            TransactionManager.rollbackTransaction(conn);
        } finally {
            TransactionManager.endTransaction(conn);
        }
    }

    /**
     * Displays a list of all residents, with an optional filter by wing.
     *
     * @param wing The wing to filter by (can be null to show all).
     */
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

    /**
     * Helper method to print a resident's details in a formatted row.
     *
     * @param resident The resident to display.
     */
    private void displayResidentInfo(Resident resident) {
        System.out.printf("%-10s | %-15s | %-15s | %-15s | %-5s | %-10d | %d%n",
                resident.getResidentId(), resident.getFirstName(),
                resident.getLastName(), resident.getContactNumber(),
                resident.getWing(), resident.getHouseNumber(),
                resident.getVehicleCount());
    }
}
