package ui;

import services.ParkingService;
import services.ResidentService;
import services.VehicleService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;
import database.DatabaseManager;
import database.TransactionManager;

public class AdminUI {
    private String username;
    private static final Scanner scanner = new Scanner(System.in);
    private final ResidentService residentService;
    private final VehicleService vehicleService;
    private final ParkingService parkingService;

    public AdminUI(String username) {
        this.username = username;
        this.residentService = new ResidentService();
        this.vehicleService = new VehicleService();
        this.parkingService = new ParkingService();
    }

    public void displayAdminMenu() {
        boolean running = true;
        while (running) {
            System.out.println("\n┌────────────────────────────────────────────┐");
            System.out.println("│                 ADMIN MENU                 │");
            System.out.println("├────────────────────────────────────────────┤");
            System.out.println("│ 1. Manage Residents                        │");
            System.out.println("│ 2. Manage Vehicles                         │");
            System.out.println("│ 3. View Parked Vehicles                    │");
            System.out.println("│ 4. View Available Parking Slots            │");
            System.out.println("│ 5. View Parking History                    │");
            System.out.println("│ 6. Change My Credentials                   │");
            System.out.println("│ 7. Back to Main Menu                       │");
            System.out.println("└────────────────────────────────────────────┘");
            System.out.print("Enter your choice (1-7): ");

            int choice = InputHandler.getValidChoice(1, 7);

            switch (choice) {
                case 1: manageResidentsMenu(); break;
                case 2: manageVehiclesMenu(); break;
                case 3: viewParkedVehiclesMenu(); break;
                case 4: viewAvailableSlotsMenu(); break;
                case 5: viewParkingHistoryMenu(); break;
                case 6:
                    if (changeCredentials()) {
                        System.out.println("Credentials changed. Logging out for security.");
                        running = false;
                    }
                    break;
                case 7:
                    System.out.println("Admin : " + this.username + "' logged out.");
                    running = false;
                    break;
            }
        }
    }

    private void manageResidentsMenu() {
        boolean running = true;
        while (running) {
            System.out.println("\n┌────────────────────────────────────────────┐");
            System.out.println("│              MANAGE RESIDENTS              │");
            System.out.println("├────────────────────────────────────────────┤");
            System.out.println("│ 1. Add New Resident                        │");
            System.out.println("│ 2. Edit Resident Details                   │");
            System.out.println("│ 3. Delete Resident                         │");
            System.out.println("│ 4. Search for a Resident                   │");
            System.out.println("│ 5. View All Residents                      │");
            System.out.println("│ 6. Manage Subscription                     │");
            System.out.println("│ 7. Back                                    │");
            System.out.println("└────────────────────────────────────────────┘");
            System.out.print("Enter your choice (1-7): ");
            int choice = InputHandler.getValidChoice(1, 7);

            switch (choice) {
                case 1: residentService.addResident(); break;
                case 2: residentService.editResident(); break;
                case 3: residentService.deleteResident(); break;
                case 4: residentService.searchResident(); break;
                case 5: viewAllResidentsByWing(); break;
                case 6:
                    // MODIFIED: Ask for ID first, then call the method with the ID
                    String residentId = InputHandler.getValidStringInput("Enter the Resident ID to manage their subscription: ");
                    residentService.upgradeSubscription(residentId);
                    break;
                case 7: running = false; break;
            }
        }
    }

    private void manageVehiclesMenu() {
        boolean running = true;
        while (running) {
            System.out.println("\n┌────────────────────────────────────────────┐");
            System.out.println("│              MANAGE VEHICLES               │");
            System.out.println("├────────────────────────────────────────────┤");
            System.out.println("│ 1. Add New Vehicle for Resident            │");
            System.out.println("│ 2. Delete Vehicle                          │");
            System.out.println("│ 3. Search Vehicle Details                  │");
            System.out.println("│ 4. Find All Vehicles by Resident           │");
            System.out.println("│ 5. View All Vehicles                       │");
            System.out.println("│ 6. Back                                    │");
            System.out.println("└────────────────────────────────────────────┘");
            System.out.print("Enter your choice (1-6): ");
            int choice = InputHandler.getValidChoice(1, 6);

            switch (choice) {
                case 1:
                    String residentId = InputHandler.getValidStringInput("Enter the Resident ID to add a vehicle for: ").toUpperCase();
                    vehicleService.addVehicleForResident(residentId, "ADMIN");
                    break;
                case 2: vehicleService.deleteVehicle(); break;
                case 3: vehicleService.searchVehicle(); break;
                case 4: vehicleService.displayVehiclesByResident(); break; // MODIFIED METHOD CALL
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
            System.out.println("│ 1. Resident Parking (by Wing)              │");
            System.out.println("│ 2. Guest Parking                           │");
            System.out.println("│ 3. All Parking (Resident + Guest)          │");
            System.out.println("│ 4. View Guest Parking Waiting List         │"); // NEW OPTION
            System.out.println("│ 5. Back to Admin Menu                      │");
            System.out.println("└────────────────────────────────────────────┘");
            System.out.print("Enter your choice (1-5): ");
            int choice = InputHandler.getValidChoice(1, 5);

            switch (choice) {
                case 1:
                    parkingService.viewResidentParkingByWing();
                    break;
                case 2:
                    parkingService.viewGuestParking();
                    break;
                case 3:
                    parkingService.viewAllParkedVehicles();
                    break;
                case 4:
                    parkingService.viewGuestWaitingList();
                    break;
                case 5:
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
            System.out.println("│ 2. Guest Parking                           │");
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

    // IN: ui/AdminUI.java
// REPLACE the entire viewParkingHistoryMenu method with this one.

    private void viewParkingHistoryMenu() {
        boolean running = true;
        while(running) {
            System.out.println("\n┌────────────────────────────────────────────┐");
            System.out.println("│           VIEW PARKING HISTORY             │");
            System.out.println("├────────────────────────────────────────────┤");
            System.out.println("│ 1. Resident Parking History (by Wing)      │");
            System.out.println("│ 2. Guest Parking History                   │");
            System.out.println("│ 3. All Parking History                     │");
            System.out.println("│ 4. Generate Report of All Parking History  │");
            System.out.println("│ 5. Back to Admin Menu                      │");
            System.out.println("└────────────────────────────────────────────┘");
            System.out.print("Enter your choice (1-5): ");
            int choice = InputHandler.getValidChoice(1, 5);

            switch (choice) {
                case 1:
                    parkingService.viewResidentParkingHistory();
                    break;
                case 2:
                    parkingService.viewGuestParkingHistory();
                    break;
                case 3:
                    parkingService.viewAllParkingHistory();
                    break;
                case 4:
                    parkingService.generateParkingHistoryReport();
                    break;
                case 5:
                    running = false;
                    break;
            }
        }
    }

    private boolean changeCredentials() {
        System.out.println("\n--- Change Credentials ---");
        String newUsername = InputHandler.getValidStringInput("Enter new username: ");

        // Re-introducing the password validation loop
        String newPassword;
        while (true) {
            newPassword = InputHandler.getValidStringInput("Enter new password: ");
            if (InputHandler.isPasswordValid(newPassword)) {
                break; // Exit the loop if the password is valid
            } else {
                System.out.println("\n- Invalid Password! Must contain at least:");
                System.out.println("  - 8 characters");
                System.out.println("  - 2 digits");
                System.out.println("  - 1 uppercase letter");
                System.out.println("  - 1 special character");
            }
        }

        String query = "UPDATE admin SET name = ?, password = ? WHERE name = ?";
        Connection con = null;
        try {
            con = DatabaseManager.getConnection();
            TransactionManager.beginTransaction(con);

            try (PreparedStatement ps = con.prepareStatement(query)) {
                ps.setString(1, newUsername);
                ps.setString(2, newPassword);
                ps.setString(3, this.username);

                if (ps.executeUpdate() > 0) {
                    TransactionManager.commitTransaction(con);
                    System.out.println("Credentials updated successfully from '" + this.username + "' to '" + newUsername + "'");
                    this.username = newUsername;
                    return true;
                } else {
                    TransactionManager.rollbackTransaction(con);
                    System.out.println("Update failed. Admin user '" + this.username + "' not found.");
                    return false;
                }
            }
        } catch (SQLException e) {
            System.err.println("Database update failed: " + e.getMessage());
            TransactionManager.rollbackTransaction(con);
            return false;
        } finally {
            TransactionManager.endTransaction(con);
        }
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