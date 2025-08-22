package model;

public class Vehicle {

    private final String vehicleNumber;
    private final String residentId;
    private final String vehicleType;
    private final String vehicleBrand;
    private final String ownerType; // NEW FIELD

    // MODIFIED CONSTRUCTOR
    public Vehicle(String vehicleNumber, String residentId, String vehicleType, String vehicleBrand, String ownerType) {
        this.vehicleNumber = vehicleNumber;
        this.residentId = residentId;
        this.vehicleType = vehicleType;
        this.vehicleBrand = vehicleBrand;
        this.ownerType = ownerType;
    }

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

    // NEW GETTER
    public String getOwnerType() {
        return ownerType;
    }

    @Override
    public String toString() {
        return vehicleNumber + ", " + vehicleType + ", " + vehicleBrand;
    }
}