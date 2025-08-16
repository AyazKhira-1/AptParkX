package ui;

import services.ParkingService;

/**
 * ResidentUI provides the user interface for resident-specific functions.
 * It allows residents to manage their parked vehicles and view their history.
 */
public class ResidentUI {
    private final String residentId;
    private final ParkingService parkingService;

    /**
     * Constructor for ResidentUI.
     *
     * @param residentId The ID of the logged-in resident.
     */
    public ResidentUI(String residentId) {
        this.residentId = residentId;
        this.parkingService = new ParkingService(); // Initialize the service
    }

    /**
     * Displays the resident menu and handles user navigation.
     */
    public void displayResidentMenu() {
        boolean running = true;
        while (running) {
            System.out.println("\n┌──────────────────────────────────┐");
            System.out.println("│          RESIDENT MENU           │");
            System.out.println("├──────────────────────────────────┤");
            System.out.println("│ 1. Park New Vehicle              │");
            System.out.println("│ 2. Remove Parked Vehicle         │");
            System.out.println("│ 3. View My Parked Vehicles       │");
            System.out.println("│ 4. View My Parking History       │");
            System.out.println("│ 5. Logout                        │");
            System.out.println("└──────────────────────────────────┘");
            System.out.print("Enter your choice (1-5): ");

            int choice = InputHandler.getValidChoice(1, 5);

            switch (choice) {
                case 1:
                    parkingService.parkNewVehicle(this.residentId);
                    break;
                case 2:
                    parkingService.removeParkedVehicle(this.residentId);
                    break;
                case 3:
                    parkingService.viewMyParkedVehicles(this.residentId);
                    break;
                case 4:
                    viewMyParkingHistoryMenu();
                    break;
                case 5:
                    System.out.println("Logging out. Goodbye!");
                    running = false;
                    break;
            }
        }
    }

    /**
     * Displays a sub-menu for viewing different types of parking history.
     */
    private void viewMyParkingHistoryMenu() {
        System.out.println("\n┌──────────────────────────────────┐");
        System.out.println("│      VIEW MY PARKING HISTORY     │");
        System.out.println("├──────────────────────────────────┤");
        System.out.println("│ 1. Resident Parking History      │");
        System.out.println("│ 2. Spare Parking History         │");
        System.out.println("│ 3. All My Parking History        │");
        System.out.println("│ 4. Back to Menu                  │");
        System.out.println("└──────────────────────────────────┘");
        System.out.print("Select an option: ");
        int choice = InputHandler.getValidChoice(1, 4);

        String parkingType;
        switch (choice) {
            case 1: parkingType = "Resident"; break;
            case 2: parkingType = "Spare"; break;
            case 3: parkingType = "All"; break;
            default: return; // Go back
        }
        parkingService.displayMyParkingHistory(this.residentId, parkingType);
    }
}
