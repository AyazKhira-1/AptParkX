# AptParkX - Apartment Parking Management System

**AptParkX** is a comprehensive, console-based application designed to manage the vehicle parking system for a residential apartment complex. Built with Java and powered by a MySQL database, this application provides a robust solution for both administrators and residents to handle parking-related tasks efficiently.

The system is designed with a clean, multi-layered architecture and utilizes **JDBC transactions** to ensure data integrity, maintainability, scalability, and a clear separation of concerns.

---

## ✨ Features

The application offers two distinct user roles with a rich set of features for each:

### 👤 Admin Features
- **Secure Login:** Admins have a separate, secure login to access their dashboard.
- **Credential Management:** Ability to change their own username and password with strong password validation.
- **Resident Management:**
    - Add new residents with auto-generated credentials.
    - View a complete list of all residents, with options to filter by apartment wing (A, B, C).
    - Search for specific residents by their ID.
    - Edit resident details (name, contact info).
    - Delete residents and all their associated vehicles from the system.
- **Vehicle Management:**
    - Add new vehicles for residents, enforcing parking rules (e.g., max 1 four-wheeler, 2 two-wheelers).
    - View a comprehensive list of all vehicles, with sorting options.
    - Search for a specific vehicle by its registration number.
    - Find a resident's details by searching for their vehicle number.
    - Delete vehicles from the system.
- **Parking Oversight:**
    - View all currently parked vehicles in resident, spare, or both parking areas, with filtering by wing.
    - Check the real-time availability of parking slots for each vehicle type and wing.
    - View a complete parking history for resident, spare, or all parking, with filtering by wing.

### 🚗 Resident Features
- **Secure Login:** Residents can log in with their unique, auto-generated credentials.
- **Parking Operations:**
    - Park any of their unparked vehicles. The system automatically assigns the correct slot type and falls back to spare parking if the resident lot is full.
    - Remove a parked vehicle from its slot.
- **Personalized Views:**
    - View a list of all their currently parked vehicles, including their location (resident or spare slot).
    - View their personal parking history, with options to see resident, spare, or all records.

---

## 🛠️ Tech Stack & Architecture

- **Language:** Java
- **Database:** MySQL
- **Driver:** MySQL Connector/J (JDBC)
- **Architecture:** Multi-Layered
    - **UI Layer:** Handles all user interaction and console output (`AdminUI`, `ResidentUI`, `InputHandler`).
    - **Service Layer:** Contains the core business logic and orchestrates operations (`ResidentService`, `VehicleService`, `ParkingService`). This layer is responsible for managing transactions.
    - **DAO (Data Access Object) Layer:** Manages all direct database communication (`ResidentDAO`, `VehicleDAO`).
    - **Model Layer:** Represents the application's data structures (`Resident`, `Vehicle`).
    - **Database Manager:** A singleton class to provide a centralized database connection point, now configured to support manual transaction management.
- **Data Integrity:**
    - **JDBC Transactions:** Critical operations that involve multiple database writes (like adding a resident with vehicles, or removing a parked vehicle) are wrapped in transactions to ensure atomicity. If any part of the operation fails, all changes are rolled back to prevent data inconsistency.

---

## 🚀 Getting Started

Follow these instructions to get the project up and running on your local machine.

### Prerequisites
- Java Development Kit (JDK) 11 or higher
- An IDE like IntelliJ IDEA or Eclipse
- MySQL Server
- MySQL Connector/J library

### 1. Database Setup
1.  Open your MySQL management tool (like phpMyAdmin or MySQL Workbench).
2.  Create a new database named `vehicle_parking_manager`.
3.  Import the `vehicle_parking_manager.sql` file provided in the repository to set up all the necessary tables, data, and triggers.

### 2. Project Configuration
1.  Clone the repository to your local machine.
2.  Open the project in your preferred IDE (e.g., IntelliJ IDEA).
3.  **Add the MySQL Connector/J JAR file** to your project's libraries/dependencies.
    - In IntelliJ IDEA: Go to `File` -> `Project Structure` -> `Modules` -> `Dependencies` -> `+` -> `JARs or directories...` and select the downloaded MySQL Connector JAR file.
4.  Navigate to `src/database/DatabaseManager.java` and, if necessary, update the `DB_USER` and `DB_PASSWORD` constants to match your MySQL credentials.

### 3. Running the Application
1.  Locate the `src/main/AptParkX.java` file.
2.  Run the `main` method from within your IDE.
3.  The application will start in your console.

---

## 📂 Project Structure

```
src/
├── dao/
│   ├── ResidentDAO.java
│   └── VehicleDAO.java
├── database/
│   ├── DatabaseManager.java
│   └── TransactionManager.java
├── main/
│   └── AptParkX.java
├── model/
│   ├── Resident.java
│   └── Vehicle.java
├── services/
│   ├── ParkingService.java
│   ├── ResidentService.java
│   └── VehicleService.java
└── ui/
    ├── AdminUI.java
    ├── InputHandler.java
    └── ResidentUI.java
