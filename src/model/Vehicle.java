package model;

public class Vehicle {


    private final String vehicleNumber;
    private final String residentId;
    private final String vehicleType;
    private final String vehicleBrand;

    public Vehicle(String vehicleNumber, String residentId, String vehicleType, String vehicleBrand) {
        this.vehicleNumber = vehicleNumber;
        this.residentId = residentId;
        this.vehicleType = vehicleType;
        this.vehicleBrand = vehicleBrand;
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



    @Override
    public String toString() {
        return vehicleNumber + ", " + vehicleType + ", " + vehicleBrand;
    }
}
