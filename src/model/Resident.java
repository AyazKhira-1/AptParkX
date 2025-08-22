package model;

import java.math.BigDecimal;
import java.sql.Date;

public class Resident {

    // --- Existing Fields ---
    private final String residentId;
    private String firstName;
    private String lastName;
    private String contactNumber;
    private final String wing;
    private final int houseNumber;
    private int vehicleCount; // Made this non-final to allow updates
    private String username;
    private String password;

    // --- New Subscription Fields ---
    private String subscriptionTier;
    private BigDecimal subscriptionCost;
    private Date subscriptionStartDate;
    private Date subscriptionValidUntil;

    // Updated constructor to include all fields
    public Resident(String residentId, String firstName, String lastName, String contactNumber, String wing, int houseNumber, String subscriptionTier, BigDecimal subscriptionCost, Date subscriptionStartDate, Date subscriptionValidUntil, int vehicleCount, String username, String password) {
        this.residentId = residentId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.contactNumber = contactNumber;
        this.wing = wing;
        this.houseNumber = houseNumber;
        this.subscriptionTier = subscriptionTier;
        this.subscriptionCost = subscriptionCost;
        this.subscriptionStartDate = subscriptionStartDate;
        this.subscriptionValidUntil = subscriptionValidUntil;
        this.vehicleCount = vehicleCount;
        this.username = username;
        this.password = password;
    }


    // --- Existing Getters and Setters ---
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

    public void setVehicleCount(int vehicleCount) {
        this.vehicleCount = vehicleCount;
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

    // --- New Getters and Setters for Subscription ---

    public String getSubscriptionTier() {
        return subscriptionTier;
    }

    public void setSubscriptionTier(String subscriptionTier) {
        this.subscriptionTier = subscriptionTier;
    }

    public BigDecimal getSubscriptionCost() {
        return subscriptionCost;
    }

    public void setSubscriptionCost(BigDecimal subscriptionCost) {
        this.subscriptionCost = subscriptionCost;
    }

    public Date getSubscriptionStartDate() {
        return subscriptionStartDate;
    }

    public void setSubscriptionStartDate(Date subscriptionStartDate) {
        this.subscriptionStartDate = subscriptionStartDate;
    }

    public Date getSubscriptionValidUntil() {
        return subscriptionValidUntil;
    }

    public void setSubscriptionValidUntil(Date subscriptionValidUntil) {
        this.subscriptionValidUntil = subscriptionValidUntil;
    }


    // Updated toString() for better debugging
    @Override
    public String toString() {
        return "Resident{" +
                "residentId='" + residentId + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", wing='" + wing + '\'' +
                ", houseNumber=" + houseNumber +
                ", subscriptionTier='" + subscriptionTier + '\'' +
                ", vehicleCount=" + vehicleCount +
                '}';
    }
}