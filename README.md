# AptParkX - Apartment Parking Management System 🚗

**AptParkX** is a robust, console-based Java application designed to streamline vehicle parking management for residential complexes. It offers a secure, multi-layered architecture backed by a MySQL database to handle resident registration, vehicle tracking, and automated parking slot allocation.

---

## ✨ Key Features

### 👤 Administrative Dashboard
* **Resident Management**: Add, edit, search, and delete residents. Automatically generates unique Resident IDs and secure login credentials.
* **Vehicle Oversight**: Track all registered vehicles. Enforces strict parking rules (max 1 four-wheeler and 2 two-wheelers per resident).
* **Live Monitoring**: View real-time occupancy of resident and spare parking slots, filtered by apartment wing.
* **Comprehensive Analytics**: Access detailed parking history for the entire complex, including total hours parked and revenue generated from spare slots.
* **Credential Control**: Secure admin login with the ability to change usernames and high-validation passwords.

### 🏠 Resident Portal
* **Smart Parking**: Automatically identifies unparked vehicles and suggests appropriate slots (1-150 for 4-wheelers, 151-450 for 2-wheelers).
* **Spare Parking Fallback**: Allows residents to park additional or guest vehicles in spare slots (451-500) when resident limits are reached.
* **One-Click Removal**: Seamlessly remove vehicles from slots, which automatically calculates duration and updates history.
* **Personal History**: Residents can view their specific parking logs, location of parked vehicles, and associated costs.

---

## 🛠️ Tech Stack & Architecture

* **Language**: Java (JDK 11+)
* **Database**: MySQL (using JDBC for connectivity)
* **Design Pattern**: 
    * **DAO Pattern**: Decouples business logic from data persistence.
    * **Service Layer**: Orchestrates business rules and transaction boundaries.
    * **Singleton Database Manager**: Ensures efficient connection pooling.
* **Data Integrity**: Utilizes **JDBC Transactions** (manual commit/rollback) to ensure operations like "Delete Resident" also cleanly remove all associated vehicles and parking records.

---

## 🚀 Getting Started

### 📋 Prerequisites
* Java Development Kit (JDK) 11 or higher.
* MySQL Server 8.0+.
* MySQL Connector/J (JDBC Driver).

### ⚙️ Installation & Setup
1.  **Database Configuration**:
    * Import `vehicle_parking_manager.sql` into your MySQL server.
    * Verify settings in `src/database/DatabaseManager.java` (URL, User, Password).
2.  **Project Build**:
    * Open the project in your IDE (IntelliJ/Eclipse).
    * Add the `mysql-connector-java.jar` to your project dependencies.
3.  **Run**:
    * Execute the `main` method in `src/main/AptParkX.java`.

---

## 📂 System Structure
```text
src/
├── dao/        # Data Access Objects (SQL Queries)
├── database/   # Connection & Transaction Management
├── ds/         # Custom Data Structures (LinkedLists for History)
├── model/      # Plain Old Java Objects (Resident, Vehicle)
├── services/   # Core Business Logic
└── ui/         # Console Interface & Input Validation
