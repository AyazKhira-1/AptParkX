// IN: ds/ParkingRecord.java
// REPLACE the entire file with this code.

package ds;

import java.math.BigDecimal;

public class ParkingRecord {

    private final int serialNumber;
    private final String ownerName;    // --- NEW FIELD ---
    private final int slotId;
    private final String vehicleNumber;
    private final String vehicleBrand;
    private final String vehicleType;
    private final String timeIn;
    private final String timeOut;
    private final BigDecimal totalHours;
    private final BigDecimal chargeAmount;
    private final String parkingType;

    // --- CONSTRUCTOR UPDATED ---
    public ParkingRecord(int serialNumber, String ownerName, int slotId, String vehicleNumber, String vehicleBrand, String vehicleType, String timeIn, String timeOut, BigDecimal totalHours, BigDecimal chargeAmount, String parkingType) {
        this.serialNumber = serialNumber;
        this.ownerName = ownerName;
        this.slotId = slotId;
        this.vehicleNumber = vehicleNumber;
        this.vehicleBrand = vehicleBrand;
        this.vehicleType = vehicleType;
        this.timeIn = timeIn;
        this.timeOut = timeOut;
        this.totalHours = totalHours;
        this.chargeAmount = chargeAmount;
        this.parkingType = parkingType;
    }

    // --- TOSTRING() UPDATED ---
    @Override
    public String toString() {
        // This format is for the ADMIN view, which now includes Owner Name.
        // It's designed to be flexible for different history views.
        if (this.ownerName != null) {
            if (this.parkingType != null) { // "All History" view with Park Type
                return String.format("%-4d | %-20s | %-8d | %-15s | %-12s | %-12s | %-10s | %-22s | %-22s | %-10.2f | %-12.2f",
                        this.serialNumber, this.ownerName, this.slotId, this.vehicleNumber, this.vehicleBrand, this.vehicleType, this.parkingType, this.timeIn, this.timeOut, this.totalHours, this.chargeAmount);
            } else { // "Resident" or "Guest" specific views
                return String.format("%-4d | %-20s | %-8d | %-15s | %-12s | %-12s | %-22s | %-22s | %-10.2f | %-12.2f",
                        this.serialNumber, this.ownerName, this.slotId, this.vehicleNumber, this.vehicleBrand, this.vehicleType, this.timeIn, this.timeOut, this.totalHours, this.chargeAmount);
            }
        }

        // This format is for the RESIDENT view, which does not need the Owner Name.
        if (this.parkingType != null) { // "All My Parking History" view
            return String.format("%-4d | %-8d | %-15s | %-12s | %-12s | %-10s | %-22s | %-22s | %-10.2f | %-12.2f",
                    this.serialNumber, this.slotId, this.vehicleNumber, this.vehicleBrand, this.vehicleType, this.parkingType, this.timeIn, this.timeOut, this.totalHours, this.chargeAmount);
        } else { // "My Resident" or "My Guest" specific views
            return String.format("%-4d | %-8d | %-15s | %-12s | %-12s | %-22s | %-22s | %-10.2f | %-12.2f",
                    this.serialNumber, this.slotId, this.vehicleNumber, this.vehicleBrand, this.vehicleType, this.timeIn, this.timeOut, this.totalHours, this.chargeAmount);
        }
    }
}