package model;

public class Resident {

    // --- Fields ---
    private final String residentId;
    private String firstName;
    private String lastName;
    private String contactNumber;
    private final String wing;
    private final int houseNumber;
    private final int vehicleCount;
    private String username;
    private String password;

    public Resident(String residentId, String firstName, String lastName, String contactNumber, String wing, int houseNumber, int vehicleCount, String username, String password) {
        this.residentId = residentId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.contactNumber = contactNumber;
        this.wing = wing;
        this.houseNumber = houseNumber;
        this.vehicleCount = vehicleCount;
        this.username = username;
        this.password = password;
    }


    public String getResidentId() {
        return residentId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getWing() {
        return wing;
    }

    public int getHouseNumber() {
        return houseNumber;
    }

    public int getVehicleCount() {
        return vehicleCount;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    @Override
    public String toString() {
        return "Resident{" +
                "residentId='" + residentId + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", contactNumber='" + contactNumber + '\'' +
                ", wing='" + wing + '\'' +
                ", houseNumber=" + houseNumber +
                ", vehicleCount=" + vehicleCount +
                ", username='" + username + '\'' +
                '}';
    }
}
