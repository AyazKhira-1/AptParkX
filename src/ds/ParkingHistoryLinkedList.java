package ds;


public class ParkingHistoryLinkedList {
    private Node head;

    private static class Node {
        ParkingRecord data;
        Node next;

        Node(ParkingRecord data) {
            this.data = data;
            this.next = null;
        }
    }

    public ParkingHistoryLinkedList() {
        this.head = null;
    }

    /**
     * Inserts a new parking record at the end of the list.
     * @param data The ParkingRecord to add.
     */
    public void insertAtLast(ParkingRecord data) {
        Node newNode = new Node(data);
        if (head == null) {
            head = newNode;
            return;
        }
        Node last = head;
        while (last.next != null) {
            last = last.next;
        }
        last.next = newNode;
    }

    /**
     * Displays the entire list of parking records by calling their toString() method.
     * The header is now printed separately in the service layer.
     */
    public void display() {
        if (head == null) {
            System.out.println("No parking history found for this selection.");
            return;
        }
        Node current = head;
        while (current != null) {
            // Each ParkingRecord knows how to print itself correctly.
            System.out.println(current.data.toString());
            current = current.next;
        }
    }

}