package ds;

public class ParkingRecord {
    // Fields to hold all possible data for any history view
    private final int serialNumber;
    private final String ownerName;
    private final String residentId;
    private final int slotId;
    private final String vehicleNumber;
    private final String vehicleType;
    private final String vehicleBrand;
    private final String wing;
    private final String timeIn;
    private final String timeOut;
    private final double totalHours;
    private final String parkingType;
    private final double cost;

    // A field to tell the record how to format itself
    private final String viewType;

    public ParkingRecord(int serialNumber, String ownerName, String residentId, int slotId, String vehicleNumber,
                         String vehicleType, String vehicleBrand, String wing, String timeIn, String timeOut,
                         double totalHours, String parkingType, double cost, String viewType) {
        this.serialNumber = serialNumber;
        this.ownerName = ownerName;
        this.residentId = residentId;
        this.slotId = slotId;
        this.vehicleNumber = vehicleNumber;
        this.vehicleType = vehicleType;
        this.vehicleBrand = vehicleBrand;
        this.wing = wing;
        this.timeIn = timeIn;
        this.timeOut = timeOut;
        this.totalHours = totalHours;
        this.parkingType = parkingType;
        this.cost = cost;
        this.viewType = viewType;
    }

    @Override
    public String toString() {
        // This method now formats the output string based on the view type
        switch (viewType) {
            case "RESIDENT_WING":
                return String.format("%-5d | %-18s | %-10s | %-8d | %-15s | %-12s | %-15s | %-20s | %-20s | %-10.2f",
                        serialNumber, ownerName, residentId, slotId, vehicleNumber, vehicleType, vehicleBrand, timeIn, timeOut, totalHours);
            case "RESIDENT_ALL":
                return String.format("%-5d | %-18s | %-10s | %-8d | %-15s | %-12s | %-15s | %-5s | %-20s | %-20s | %-10.2f",
                        serialNumber, ownerName, residentId, slotId, vehicleNumber, vehicleType, vehicleBrand, wing, timeIn, timeOut, totalHours);
            case "SPARE":
                return String.format("%-5d | %-18s | %-10s | %-8d | %-15s | %-12s | %-15s | %-5s | %-20s | %-20s | %-10.2f | %-10.2f",
                        serialNumber, ownerName, residentId, slotId, vehicleNumber, vehicleType, vehicleBrand, wing, timeIn, timeOut, totalHours, cost);
            case "ALL_HISTORY":
                return String.format("%-5d | %-18s | %-10s | %-8d | %-15s | %-12s | %-15s | %-5s | %-20s | %-20s | %-10.2f | %-12s | %-10.2f",
                        serialNumber, ownerName, residentId, slotId, vehicleNumber, vehicleType, vehicleBrand, wing, timeIn, timeOut, totalHours, parkingType, cost);
            default:
                return "Error: Unknown view type.";
        }
    }
}