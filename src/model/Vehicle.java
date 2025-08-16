package model;

/**
 * The Vehicle class is a model that represents a vehicle owned by a resident.
 * It holds all the information related to a single vehicle, corresponding to the
 * columns in the 'vehicle' database table.
 */
public class Vehicle {

    // --- Fields ---
    private final String vehicleNumber;
    private final String residentId;
    private final String vehicleType;
    private final String vehicleBrand;

    // --- Constructor ---

    /**
     * Constructs a new Vehicle object with all its properties.
     *
     * @param vehicleNumber The unique registration number of the vehicle (e.g., "GJ05CD5678").
     * @param residentId    The ID of the resident who owns the vehicle.
     * @param vehicleType   The type of the vehicle (e.g., "4-wheeler", "2-wheeler").
     * @param vehicleBrand  The brand or manufacturer of the vehicle (e.g., "Maruti Suzuki", "Honda").
     */
    public Vehicle(String vehicleNumber, String residentId, String vehicleType, String vehicleBrand) {
        this.vehicleNumber = vehicleNumber;
        this.residentId = residentId;
        this.vehicleType = vehicleType;
        this.vehicleBrand = vehicleBrand;
    }

    // --- Getters and Setters ---

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public String getResidentId() {
        return residentId;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public String getVehicleBrand() {
        return vehicleBrand;
    }

    // --- toString() method for debugging ---

    @Override
    public String toString() {
        return "Vehicle{" +
                "vehicleNumber='" + vehicleNumber + '\'' +
                ", residentId='" + residentId + '\'' +
                ", vehicleType='" + vehicleType + '\'' +
                ", vehicleBrand='" + vehicleBrand + '\'' +
                '}';
    }
}
