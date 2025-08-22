package ui;

import services.ParkingService;
import services.ResidentService;
import services.VehicleService;

public class ResidentUI {
    private final String residentId;
    private final ParkingService parkingService;
    private final VehicleService vehicleService;
    private final ResidentService residentService;

    public ResidentUI(String residentId) {
        this.residentId = residentId;
        this.parkingService = new ParkingService();
        this.vehicleService = new VehicleService();
        this.residentService = new ResidentService();
    }

    public void displayResidentMenu() {
        boolean running = true;
        while (running) {
            System.out.println("\n┌──────────────────────────────────┐");
            System.out.println("│          RESIDENT MENU           │");
            System.out.println("├──────────────────────────────────┤");
            System.out.println("│ 1. Park My Vehicle               │");
            System.out.println("│ 2. Park Guest Vehicle            │");
            System.out.println("│ 3. Remove Parked Vehicle         │");
            System.out.println("│ 4. View My Parked Vehicles       │");
            System.out.println("│ 5. Manage My Vehicles            │");
            System.out.println("│ 6. Manage My Subscription        │");
            System.out.println("│ 7. View My Parking History       │");
            System.out.println("│ 8. Logout                        │");
            System.out.println("└──────────────────────────────────┘");
            System.out.print("Enter your choice (1-8): ");

            int choice = InputHandler.getValidChoice(1, 8);

            switch (choice) {
                case 1:
                    parkingService.parkVehicle(this.residentId);
                    break;
                case 2:
                    parkingService.parkGuestVehicle(this.residentId);
                    break;
                case 3:
                    parkingService.removeParkedVehicle(this.residentId);
                    break;
                case 4:
                    parkingService.viewMyParkedVehicles(this.residentId);
                    break;
                case 5:
                    manageMyVehiclesMenu();
                    break;
                case 6:
                    manageSubscriptionMenu();
                    break;
                case 7:
                    viewMyParkingHistoryMenu();
                    break;
                case 8:
                    System.out.println("Logging out. Goodbye!");
                    running = false;
                    break;
            }
        }
    }

    private void manageMyVehiclesMenu() {
        System.out.println("\n┌──────────────────────────────────┐");
        System.out.println("│        MANAGE MY VEHICLES        │");
        System.out.println("├──────────────────────────────────┤");
        System.out.println("│ 1. Add New Vehicle               │");
        System.out.println("│ 2. Remove Vehicle from Profile   │");
        System.out.println("│ 3. Back to Menu                  │");
        System.out.println("└──────────────────────────────────┘");
        System.out.print("Select an option: ");
        int choice = InputHandler.getValidChoice(1, 3);

        switch (choice) {
            case 1:
                vehicleService.addVehicleForResident(this.residentId, "RESIDENT");
                break;
            case 2:
                vehicleService.deleteVehicle();
                break;
            case 3:
                // Go back
                break;
        }
    }

    private void manageSubscriptionMenu() {
        System.out.println("\n┌──────────────────────────────────┐");
        System.out.println("│      MANAGE MY SUBSCRIPTION      │");
        System.out.println("├──────────────────────────────────┤");
        System.out.println("│ 1. Upgrade My Subscription Plan  │");
        System.out.println("│ 2. Back to Menu                  │");
        System.out.println("└──────────────────────────────────┘");
        System.out.print("Select an option: ");
        int choice = InputHandler.getValidChoice(1, 2);

        if (choice == 1) {
            residentService.upgradeSubscription(this.residentId);
        }
    }

    private void viewMyParkingHistoryMenu() {
        System.out.println("\n┌──────────────────────────────────┐");
        System.out.println("│      VIEW MY PARKING HISTORY     │");
        System.out.println("├──────────────────────────────────┤");
        System.out.println("│ 1. My Resident Parking History   │");
        System.out.println("│ 2. My Guest Parking History      │");
        System.out.println("│ 3. All My Parking History        │");
        System.out.println("│ 4. Back to Menu                  │");
        System.out.println("└──────────────────────────────────┘");
        System.out.print("Select an option: ");
        int choice = InputHandler.getValidChoice(1, 4);

        String parkingType;
        switch (choice) {
            case 1:
                parkingType = "Resident";
                break;
            case 2:
                parkingType = "Guest"; // MODIFIED: "Spare" is now "Guest"
                break;
            case 3:
                parkingType = "All";
                break;
            default:
                return; // Go back
        }
        parkingService.displayMyParkingHistory(this.residentId, parkingType);
    }
}