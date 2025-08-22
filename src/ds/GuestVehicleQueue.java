package ds;

import database.DatabaseManager;
import model.Vehicle;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GuestVehicleQueue {

    // Public inner class to hold all data for the view
    public static class WaitingListItem {
        public final int waitlistId;
        public final Vehicle vehicle;
        public final Timestamp timeAdded;

        public WaitingListItem(int waitlistId, Vehicle vehicle, Timestamp timeAdded) {
            this.waitlistId = waitlistId;
            this.vehicle = vehicle;
            this.timeAdded = timeAdded;
        }
    }

    private final WaitingListItem[] queueArray;
    private final int maxSize;
    private int front;
    private int rear;
    private int nItems;

    public GuestVehicleQueue(int size) {
        this.maxSize = size;
        this.queueArray = new WaitingListItem[maxSize];
        this.front = 0;
        this.rear = -1;
        this.nItems = 0;
        loadFromDatabase();
    }

    private void loadFromDatabase() {
        String sql = "SELECT * FROM guest_waiting_list ORDER BY time_added ASC";
        try (Connection conn = DatabaseManager.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                if (!isFull()) {
                    Vehicle vehicle = new Vehicle(
                            rs.getString("vehicle_number"),
                            rs.getString("resident_id"),
                            rs.getString("vehicle_type"),
                            rs.getString("vehicle_brand"),
                            "Guest"
                    );
                    WaitingListItem item = new WaitingListItem(rs.getInt("waitlist_id"), vehicle, rs.getTimestamp("time_added"));

                    if (rear == maxSize - 1) rear = -1;
                    queueArray[++rear] = item;
                    nItems++;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading guest waiting list: " + e.getMessage());
        }
    }

    public void enqueue(Vehicle vehicle) {
        if (isFull()) {
            System.out.println("The waiting list is full. Cannot add more vehicles.");
            return;
        }

        String sql = "INSERT INTO guest_waiting_list (resident_id, vehicle_number, vehicle_brand, vehicle_type, time_added) VALUES (?, ?, ?, ?, NOW())";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, vehicle.getResidentId());
            ps.setString(2, vehicle.getVehicleNumber());
            ps.setString(3, vehicle.getVehicleBrand());
            ps.setString(4, vehicle.getVehicleType());

            if (ps.executeUpdate() > 0) {
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    int id = rs.getInt(1);
                    WaitingListItem newItem = new WaitingListItem(id, vehicle, new Timestamp(System.currentTimeMillis()));
                    if (rear == maxSize - 1) rear = -1;
                    queueArray[++rear] = newItem;
                    nItems++;
                    System.out.println("Vehicle " + vehicle.getVehicleNumber() + " has been added to the waiting list.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error adding vehicle to waiting list: " + e.getMessage());
        }
    }

    public Vehicle dequeue() {
        if (isEmpty()) {
            return null;
        }

        WaitingListItem tempItem = queueArray[front];
        String deleteSql = "DELETE FROM guest_waiting_list WHERE waitlist_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(deleteSql)) {

            ps.setInt(1, tempItem.waitlistId);
            if (ps.executeUpdate() > 0) {
                front++;
                if (front == maxSize) front = 0;
                nItems--;
                return tempItem.vehicle;
            }
        } catch (SQLException e) {
            System.err.println("Error removing vehicle from waiting list: " + e.getMessage());
        }
        return null;
    }

    public boolean isEmpty() {
        return (nItems == 0);
    }

    public boolean isFull() {
        return (nItems == maxSize);
    }

    public List<WaitingListItem> getAllWaitingListItems() {
        List<WaitingListItem> list = new ArrayList<>();
        int count = 0;
        int current = front;
        while (count < nItems) {
            list.add(queueArray[current++]);
            if (current == maxSize) {
                current = 0;
            }
            count++;
        }
        return list;
    }
}