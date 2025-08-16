package services;

import dao.ResidentDAO;
import dao.VehicleDAO;
import model.Resident;
import model.Vehicle;
import ui.InputHandler;

import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

/**
 * VehicleService provides the business logic for vehicle-related operations.
 * It acts as an intermediary between the UI and the DAO layers.
 */
public final class VehicleService {
    private static final Scanner scanner = new Scanner(System.in);
    private final VehicleDAO vehicleDAO;
    private final ResidentDAO residentDAO;

    /**
     * Constructor for VehicleService.
     * Initializes the necessary DAOs.
     */
    public VehicleService() {
        this.vehicleDAO = new VehicleDAO();
        this.residentDAO = new ResidentDAO();
    }

    /**
     * Finds and displays the full details of a resident based on their vehicle number.
     */
    public void findResidentByVehicle() {
        System.out.println("\n--- Find Resident by Vehicle Number ---");
        String vehicleNumber = InputHandler.getValidStringInput("Enter the Vehicle Number: ").toUpperCase();

        try {
            Resident resident = vehicleDAO.findResidentByVehicleNumber(vehicleNumber);

            System.out.println("\n--- Resident Details for Vehicle: " + vehicleNumber + " ---");
            System.out.println("------------------------------------------------------------------------------------------");
            System.out.printf("%-10s | %-15s | %-15s | %-15s | %-5s | %-10s | %s%n", "Res ID", "First Name", "Last Name", "Phone No.", "Wing", "House No", "Vehicle Count");
            System.out.println("------------------------------------------------------------------------------------------");

            if (resident == null) {
                System.out.println("No resident found for vehicle number '" + vehicleNumber + "'.");
            } else {
                System.out.printf("%-10s | %-15s | %-15s | %-15s | %-5s | %-10d | %d%n",
                        resident.getResidentId(), resident.getFirstName(),
                        resident.getLastName(), resident.getContactNumber(),
                        resident.getWing(), resident.getHouseNumber(),
                        resident.getVehicleCount());
            }
            System.out.println("------------------------------------------------------------------------------------------");

        } catch (SQLException e) {
            System.err.println("Database error during resident search by vehicle: " + e.getMessage());
        }
    }

    /**
     * Searches for a vehicle by its number and displays its details along with owner information.
     */
    public void searchVehicle() {
        System.out.println("\n--- Search for a Vehicle ---");
        String vehicleNumber = InputHandler.getValidStringInput("Enter the Vehicle Number to search (e.g., GJ05CD5678): ").toUpperCase();

        try {
            Vehicle vehicle = vehicleDAO.getVehicleByNumber(vehicleNumber);

            System.out.println("\n--- Search Results ---");
            System.out.println("-------------------------------------------------------------------------------------------------");
            System.out.printf("%-18s | %-15s | %-15s | %-12s | %-20s | %-5s%n", "Vehicle Number", "Type", "Brand", "Resident ID", "Owner Name", "Wing");
            System.out.println("-------------------------------------------------------------------------------------------------");

            if (vehicle == null) {
                System.out.println("No vehicle found with the number '" + vehicleNumber + "'.");
            } else {
                Resident owner = residentDAO.getResidentById(vehicle.getResidentId());
                String ownerName = (owner != null) ? owner.getFirstName() + " " + owner.getLastName() : "N/A";
                String wing = (owner != null) ? owner.getWing() : "N/A";

                System.out.printf("%-18s | %-15s | %-15s | %-12s | %-20s | %-5s%n",
                        vehicle.getVehicleNumber(), vehicle.getVehicleType(),
                        vehicle.getVehicleBrand(), vehicle.getResidentId(),
                        ownerName, wing);
            }
            System.out.println("-------------------------------------------------------------------------------------------------");

        } catch (SQLException e) {
            System.err.println("Database error during vehicle search: " + e.getMessage());
        }
    }


    /**
     * Handles the logic for adding a new vehicle to an existing resident.
     */
    public void addVehicle() {
        System.out.println("\n--- Add New Vehicle ---");
        String residentId = InputHandler.getValidStringInput("Enter the Resident ID for the vehicle: ").toUpperCase();
        try {
            if (residentDAO.residentExists(residentId)) {
                System.out.println("Resident with ID '" + residentId + "' not found.");
                return;
            }

            int[] vehicleCounts = vehicleDAO.getResidentVehicleCounts(residentId);
            if (vehicleCounts[0] >= 1 && vehicleCounts[1] >= 2) {
                System.out.println("Error: This resident already has the maximum number of vehicles (1 four-wheeler and 2 two-wheelers).");
                return;
            }

            String vehicleType = InputHandler.getValidVehicleTypeInput("Select Vehicle Type");
            addVehicleDetails(residentId, vehicleType);

        } catch (SQLException e) {
            System.err.println("Database error while adding vehicle: " + e.getMessage());
        }
    }

    /**
     * Core logic to add vehicle details after validation.
     *
     * @param residentId   The ID of the resident.
     * @param vehicleType  The type of vehicle ("4-wheeler" or "2-wheeler").
     */
    private void addVehicleDetails(String residentId, String vehicleType) throws SQLException {
        int[] vehicleCounts = vehicleDAO.getResidentVehicleCounts(residentId);

        if ("4-wheeler".equals(vehicleType) && vehicleCounts[0] >= 1) {
            System.out.println("Error: This resident already owns a 4-wheeler. Cannot add another.");
            return;
        }
        if ("2-wheeler".equals(vehicleType) && vehicleCounts[1] >= 2) {
            System.out.println("Error: This resident already owns two 2-wheelers. Cannot add more.");
            return;
        }

        String vehicleNumber;
        while (true) {
            vehicleNumber = InputHandler.getValidStringInput("Enter Vehicle Number (e.g., GJ05CD5678): ").toUpperCase();
            if (!InputHandler.isValidVehicleNumber(vehicleNumber)) {
                System.out.println("Invalid format. Please use the format 'LLDDLLDDDD' (e.g., GJ05CD5678).");
                continue;
            }
            if (vehicleDAO.vehicleExists(vehicleNumber)) {
                System.out.println("Error: Vehicle number '" + vehicleNumber + "' already exists.");
                continue;
            }
            break;
        }

        String vehicleBrand = InputHandler.getValidStringInput("Enter Vehicle Brand: ");
        Vehicle newVehicle = new Vehicle(vehicleNumber, residentId, vehicleType, vehicleBrand);

        if (vehicleDAO.addVehicle(newVehicle)) {
            System.out.println("Vehicle '" + vehicleNumber + "' added successfully!");
            residentDAO.updateResidentVehicleCount(residentId, 1); // Increment count
        }
    }

    /**
     * Handles the logic for deleting a vehicle.
     */
    public void deleteVehicle() {
        System.out.println("\n--- Delete Vehicle ---");
        String vehicleNumber = InputHandler.getValidStringInput("Enter the Vehicle Number to delete: ").toUpperCase();
        try {
            String residentId = vehicleDAO.getResidentIdForVehicle(vehicleNumber);
            if (residentId == null) {
                System.out.println("Vehicle with number '" + vehicleNumber + "' not found.");
                return;
            }

            System.out.print("Are you sure you want to delete vehicle '" + vehicleNumber + "'? (y/n): ");
            if (scanner.nextLine().equalsIgnoreCase("y")) {
                if (vehicleDAO.deleteVehicle(vehicleNumber)) {
                    System.out.println("Vehicle deleted successfully.");
                    residentDAO.updateResidentVehicleCount(residentId, -1); // Decrement count
                }
            } else {
                System.out.println("Deletion cancelled.");
            }
        } catch (SQLException e) {
            System.err.println("Failed to delete vehicle: " + e.getMessage());
        }
    }

    /**
     * Displays a list of all vehicles, sorted by a specified column.
     *
     * @param sortBy The column to sort by.
     */
    public void viewAllVehicles(String sortBy) {
        System.out.println("\n--- All Vehicles (Sorted by " + sortBy.replace("_", " ") + ") ---");
        try {
            List<Vehicle> vehicles = vehicleDAO.getAllVehicles(sortBy);
            System.out.println("-----------------------------------------------------------------------");
            System.out.printf("%-12s | %-18s | %-15s | %-15s%n", "Resident ID", "Vehicle Number", "Vehicle Type", "Vehicle Brand");
            System.out.println("-----------------------------------------------------------------------");
            if (vehicles.isEmpty()) {
                System.out.println("No vehicles found.");
            } else {
                for (Vehicle vehicle : vehicles) {
                    System.out.printf("%-12s | %-18s | %-15s | %-15s%n",
                            vehicle.getResidentId(), vehicle.getVehicleNumber(),
                            vehicle.getVehicleType(), vehicle.getVehicleBrand());
                }
            }
            System.out.println("-----------------------------------------------------------------------");
        } catch (SQLException e) {
            System.err.println("Database error while viewing vehicles: " + e.getMessage());
        }
    }

    /**
     * A guided process for a new resident to add their vehicles.
     *
     * @param residentId The ID of the new resident.
     */
    public void addVehiclesForNewResident(String residentId) {
        System.out.print("Do you want to add vehicles for this new resident now? (y/n): ");
        if (!scanner.nextLine().equalsIgnoreCase("y")) {
            return;
        }
        System.out.println("\nA resident can have one 4-wheeler and up to two 2-wheelers.");

        try {
            // Add 4-wheeler
            System.out.print("Add a 4-wheeler? (y/n): ");
            if (scanner.nextLine().equalsIgnoreCase("y")) {
                System.out.println("\n-> Adding 4-wheeler...");
                addVehicleDetails(residentId, "4-wheeler");
            }

            // Add 2-wheelers
            for (int i = 0; i < 2; i++) {
                System.out.print("Add a 2-wheeler? (y/n): ");
                if (scanner.nextLine().equalsIgnoreCase("y")) {
                    System.out.println("\n-> Adding 2-wheeler " + (i + 1) + "...");
                    addVehicleDetails(residentId, "2-wheeler");
                } else {
                    break; // Stop asking if user says no
                }
            }
        } catch (SQLException e) {
            System.err.println("An error occurred while adding vehicles for the new resident: " + e.getMessage());
        }
    }
}
