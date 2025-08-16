package model;

/**
 * The Resident class is a model that represents a resident in the apartment complex.
 * It holds all the information related to a single resident, corresponding to the
 * columns in the 'residents' database table.
 */
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

    // --- Constructor ---

    /**
     * Constructs a new Resident object with all its properties.
     *
     * @param residentId    The unique identifier for the resident (e.g., "RES001").
     * @param firstName     The first name of the resident.
     * @param lastName      The last name of the resident.
     * @param contactNumber The 10-digit contact number of the resident.
     * @param wing          The apartment wing the resident lives in (e.g., "A", "B", "C").
     * @param houseNumber   The house number within the wing.
     * @param vehicleCount  The number of vehicles owned by the resident.
     * @param username      The login username for the resident.
     * @param password      The login password for the resident.
     */
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


    // --- Getters and Setters ---

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

    // --- toString() method for debugging ---

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
