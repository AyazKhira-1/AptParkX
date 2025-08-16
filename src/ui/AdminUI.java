package ui;

import database.DatabaseManager;
import services.ParkingService;
import services.ResidentService;
import services.VehicleService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;

/**
 * AdminUI provides the user interface for administrative functions.
 * It interacts with the various service layers to perform its tasks.
 */
public class AdminUI {
    private String username;
    private static final Scanner scanner = new Scanner(System.in);
    private final ResidentService residentService;
    private final VehicleService vehicleService;
    private final ParkingService parkingService;

    /**
     * Constructor for AdminUI.
     *
     * @param username The username of the logged-in admin.
     */
    public AdminUI(String username) {
        this.username = username;
        // Initialize all the service classes that this UI will use
        this.residentService = new ResidentService();
        this.vehicleService = new VehicleService();
        this.parkingService = new ParkingService();
    }

    /**
     * Displays the main administrative menu and handles user navigation.
     */
    public void displayAdminMenu() {
        boolean running = true;
        while (running) {
            System.out.println("\n┌────────────────────────────────────────────┐");
            System.out.println("│                 ADMIN MENU                 │");
            System.out.println("├────────────────────────────────────────────┤");
            System.out.println("│ 1. Change Credentials                      │");
            System.out.println("│ 2. Manage Residents & Vehicles             │");
            System.out.println("│ 3. View Parked Vehicles                    │");
            System.out.println("│ 4. View Available Parking Slots            │");
            System.out.println("│ 5. View Parking History                    │");
            System.out.println("│ 6. Back to Main Menu                       │");
            System.out.println("└────────────────────────────────────────────┘");
            System.out.print("Enter your choice (1-6): ");

            int choice = InputHandler.getValidChoice(1, 6);

            switch (choice) {
                case 1:
                    if (changeCredentials()) {
                        System.out.println("Credentials changed. Logging out for security.");
                        running = false; // Exit menu to force re-login
                    }
                    break;
                case 2:
                    manageResidentsAndVehiclesMenu();
                    break;
                case 3:
                    viewParkedVehiclesMenu();
                    break;
                case 4:
                    viewAvailableSlotsMenu();
                    break;
                case 5:
                    viewParkingHistoryMenu();
                    break;
                case 6:
                    System.out.println("Admin " + this.username + " logged out.");
                    running = false;
                    break;
            }
        }
    }

    // =================================================================
    // =================== SUB-MENU METHODS ============================
    // =================================================================

    private void manageResidentsAndVehiclesMenu() {
        boolean running = true;
        while (running) {
            System.out.println("\n┌────────────────────────────────────────────┐");
            System.out.println("│        MANAGE RESIDENTS & VEHICLES         │");
            System.out.println("├────────────────────────────────────────────┤");
            System.out.println("│ 1. Manage Residents                        │");
            System.out.println("│ 2. Manage Vehicles                         │");
            System.out.println("│ 3. Back to Admin Menu                      │");
            System.out.println("└────────────────────────────────────────────┘");
            System.out.print("Enter your choice (1-3): ");
            int choice = InputHandler.getValidChoice(1, 3);

            switch (choice) {
                case 1: manageResidentsMenu(); break;
                case 2: manageVehiclesMenu(); break;
                case 3: running = false; break;
            }
        }
    }

    private void manageResidentsMenu() {
        boolean running = true;
        while (running) {
            System.out.println("\n┌────────────────────────────────────────────┐");
            System.out.println("│              MANAGE RESIDENTS              │");
            System.out.println("├────────────────────────────────────────────┤");
            System.out.println("│ 1. Search Resident                         │");
            System.out.println("│ 2. Add New Resident                        │");
            System.out.println("│ 3. Edit Existing Resident                  │");
            System.out.println("│ 4. Delete Resident                         │");
            System.out.println("│ 5. View All Residents                      │");
            System.out.println("│ 6. Back                                    │");
            System.out.println("└────────────────────────────────────────────┘");
            System.out.print("Enter your choice (1-6): ");
            int choice = InputHandler.getValidChoice(1, 6);

            switch (choice) {
                case 1: residentService.searchResident(); break;
                case 2: residentService.addResident(); break;
                case 3: residentService.editResident(); break;
                case 4: residentService.deleteResident(); break;
                case 5: viewAllResidentsByWing(); break;
                case 6: running = false; break;
            }
        }
    }

    private void manageVehiclesMenu() {
        boolean running = true;
        while (running) {
            System.out.println("\n┌────────────────────────────────────────────┐");
            System.out.println("│              MANAGE VEHICLES               │");
            System.out.println("├────────────────────────────────────────────┤");
            System.out.println("│ 1. Search Vehicle Details                  │");
            System.out.println("│ 2. Find Owner by Vehicle                   │");
            System.out.println("│ 3. Add New Vehicle                         │");
            System.out.println("│ 4. Delete Vehicle                          │");
            System.out.println("│ 5. View All Vehicles                       │");
            System.out.println("│ 6. Back                                    │");
            System.out.println("└────────────────────────────────────────────┘");
            System.out.print("Enter your choice (1-6): ");
            int choice = InputHandler.getValidChoice(1, 6);

            switch (choice) {
                case 1: vehicleService.searchVehicle(); break;
                case 2: vehicleService.findResidentByVehicle(); break;
                case 3: vehicleService.addVehicle(); break;
                case 4: vehicleService.deleteVehicle(); break;
                case 5: viewAllVehiclesSorted(); break;
                case 6: running = false; break;
            }
        }
    }

    private void viewParkedVehiclesMenu() {
        boolean running = true;
        while(running) {
            System.out.println("\n┌────────────────────────────────────────────┐");
            System.out.println("│           VIEW PARKED VEHICLES             │");
            System.out.println("├────────────────────────────────────────────┤");
            System.out.println("│ 1. Resident Parking                        │");
            System.out.println("│ 2. Spare Parking                           │");
            System.out.println("│ 3. All Parking (Resident + Spare)          │");
            System.out.println("│ 4. Back to Admin Menu                      │");
            System.out.println("└────────────────────────────────────────────┘");
            System.out.print("Enter your choice (1-4): ");
            int choice = InputHandler.getValidChoice(1, 4);

            switch (choice) {
                case 1:
                    parkingService.viewParkingSlots();
                    break;
                case 2:
                    parkingService.viewSpareParkedVehicles();
                    break;
                case 3:
                    parkingService.viewAllParkedVehicles();
                    break;
                case 4:
                    running = false;
                    break;
            }
        }
    }

    private void viewAvailableSlotsMenu() {
        boolean running = true;
        while(running) {
            System.out.println("\n┌────────────────────────────────────────────┐");
            System.out.println("│        VIEW AVAILABLE PARKING SLOTS        │");
            System.out.println("├────────────────────────────────────────────┤");
            System.out.println("│ 1. Resident Parking                        │");
            System.out.println("│ 2. Spare Parking                           │");
            System.out.println("│ 3. Back to Admin Menu                      │");
            System.out.println("└────────────────────────────────────────────┘");
            System.out.print("Enter your choice (1-3): ");
            int choice = InputHandler.getValidChoice(1, 3);

            switch (choice) {
                case 1: parkingService.viewAvailableParkingSlots(); break;
                case 2: parkingService.viewAvailableSpareParkingSlots(); break;
                case 3: running = false; break;
            }
        }
    }

    private void viewParkingHistoryMenu() {
        boolean running = true;
        while(running) {
            System.out.println("\n┌────────────────────────────────────────────┐");
            System.out.println("│           VIEW PARKING HISTORY             │");
            System.out.println("├────────────────────────────────────────────┤");
            System.out.println("│ 1. Resident Parking History                │");
            System.out.println("│ 2. Spare Parking History                   │");
            System.out.println("│ 3. All Parking History (Resident + Spare)  │");
            System.out.println("│ 4. Back to Admin Menu                      │");
            System.out.println("└────────────────────────────────────────────┘");
            System.out.print("Enter your choice (1-4): ");
            int choice = InputHandler.getValidChoice(1, 4);

            switch (choice) {
                case 1:
                    parkingService.viewResidentParkingHistory();
                    break;
                case 2:
                    parkingService.viewSpareParkingHistory();
                    break;
                case 3:
                    parkingService.viewAllParkingHistory();
                    break;
                case 4:
                    running = false;
                    break;
            }
        }
    }


    // =================================================================
    // ===================== HELPER METHODS ============================
    // =================================================================

    private boolean changeCredentials() {
        System.out.println("\n--- Change Credentials ---");
        String newUsername = InputHandler.getValidStringInput("Enter new username: ");
        String newPassword;
        while (true) {
            System.out.print("Enter new password: ");
            newPassword = scanner.nextLine();
            if (InputHandler.isPasswordValid(newPassword)) {
                break;
            } else {
                System.out.println("\n- Invalid Password! Must contain at least 8 chars, 2 digits, 1 uppercase, 1 special char.");
            }
        }

        String query = "UPDATE admin SET name = ?, password = ? WHERE name = ?";
        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, newUsername);
            ps.setString(2, newPassword);
            ps.setString(3, this.username);
            if (ps.executeUpdate() > 0) {
                System.out.println("Credentials updated successfully from '" + this.username + "' to '" + newUsername + "'");
                this.username = newUsername; // Update current session username
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Database update failed: " + e.getMessage());
        }
        return false;
    }

    private void viewAllResidentsByWing() {
        System.out.println("\n--- Filter Residents By ---");
        System.out.println("1. Wing A\n2. Wing B\n3. Wing C\n4. View All\n5. Back");
        System.out.print("Select an option: ");
        int choice = InputHandler.getValidChoice(1, 5);
        String wing = switch (choice) {
            case 1 -> "A";
            case 2 -> "B";
            case 3 -> "C";
            default -> null;
        };
        if (choice != 5) {
            residentService.viewAllResidents(wing);
        }
    }

    private void viewAllVehiclesSorted() {
        System.out.println("\n--- Sort Vehicles By ---");
        System.out.println("1. Resident ID\n2. Vehicle Type\n3. Vehicle Brand\n4. Default (Vehicle Number)\n5. Back");
        System.out.print("Select an option: ");
        int choice = InputHandler.getValidChoice(1, 5);
        String sortBy = switch (choice) {
            case 1 -> "resident_id";
            case 2 -> "vehicle_type";
            case 3 -> "vehicle_brand";
            default -> "vehicle_number";
        };
        if (choice != 5) {
            vehicleService.viewAllVehicles(sortBy);
        }
    }
}
