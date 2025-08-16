-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Aug 15, 2025 at 04:24 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `vehicle_parking_manager`
--

-- --------------------------------------------------------

--
-- Table structure for table `admin`
--

CREATE TABLE `admin` (
  `name` varchar(20) NOT NULL,
  `password` varchar(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `admin`
--

INSERT INTO `admin` (`name`, `password`) VALUES
('Admin', 'Admin@123');

-- --------------------------------------------------------

--
-- Table structure for table `parked_vehicle`
--

CREATE TABLE `parked_vehicle` (
  `Slot_id` int(11) NOT NULL,
  `vehicle_number` varchar(20) NOT NULL,
  `time_in` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `parked_vehicle`
--

INSERT INTO `parked_vehicle` (`Slot_id`, `vehicle_number`, `time_in`) VALUES
(1, 'GJ05AB1234', '2025-08-14 08:58:32'),
(5, 'GJ05GH3456', '2025-08-14 07:38:41'),
(10, 'GJ05OP9012', '2025-08-12 18:45:07'),
(15, 'GJ05ST7890', '2025-08-12 14:56:33'),
(20, 'GJ05CD3456', '2025-08-14 07:19:07'),
(25, 'GJ05KL3456', '2025-08-13 19:51:24'),
(35, 'GJ05WX9012', '2025-08-12 18:35:39'),
(40, 'GJ05EF1234', '2025-08-14 09:31:06'),
(45, 'GJ05IJ1234', '2025-08-13 21:39:46'),
(50, 'GJ05MN5679', '2025-08-13 13:44:02'),
(55, 'GJ05QR7890', '2025-08-14 08:23:19'),
(60, 'GJ05UV9012', '2025-08-13 21:44:27'),
(65, 'GJ05YZ1235', '2025-08-12 09:25:15'),
(70, 'GJ05AB7890', '2025-08-12 19:37:34'),
(75, 'GJ05CD9012', '2025-08-12 23:48:57'),
(80, 'GJ05GH5679', '2025-08-13 10:31:53'),
(85, 'GJ05IJ7891', '2025-08-13 11:07:52'),
(90, 'GJ05OP7890', '2025-08-12 13:47:25'),
(95, 'GJ05ST3456', '2025-08-13 03:11:15'),
(100, 'GJ05WX1235', '2025-08-14 11:28:54'),
(105, 'GJ05YZ7890', '2025-08-12 20:30:39'),
(115, 'GJ05CD7891', '2025-08-12 20:05:44'),
(151, 'GJ05AB3456', '2025-08-12 21:17:43'),
(155, 'GJ05AB5678', '2025-08-13 22:19:58'),
(160, 'GJ05AB9012', '2025-08-13 18:09:56'),
(165, 'GJ05CD1234', '2025-08-13 09:51:06'),
(170, 'GJ05CD5679', '2025-08-13 23:11:42'),
(175, 'GJ05CD7890', '2025-08-14 06:59:16'),
(180, 'GJ05EF3456', '2025-08-13 15:41:09'),
(185, 'GJ05EF7890', '2025-08-12 06:38:01'),
(190, 'GJ05GH1235', '2025-08-12 22:14:20'),
(195, 'GJ05GH5678', '2025-08-12 16:29:54'),
(200, 'GJ05GH9012', '2025-08-13 11:24:11'),
(205, 'GJ05IJ3456', '2025-08-12 11:21:19'),
(210, 'GJ05IJ9012', '2025-08-13 12:52:21'),
(215, 'GJ05KL1234', '2025-08-12 19:17:51'),
(220, 'GJ05KL5678', '2025-08-14 09:31:29'),
(225, 'GJ05MN1234', '2025-08-12 15:57:52'),
(230, 'GJ05MN7890', '2025-08-12 14:27:43'),
(235, 'GJ05OP1234', '2025-08-13 14:04:20'),
(240, 'GJ05OP3456', '2025-08-14 05:58:09'),
(245, 'GJ05QR3456', '2025-08-12 13:55:13'),
(250, 'GJ05ST1234', '2025-08-13 19:17:49'),
(255, 'GJ05UV1234', '2025-08-13 07:23:45'),
(260, 'GJ05WX5678', '2025-08-12 11:46:33'),
(265, 'GJ05YZ3456', '2025-08-12 10:15:36'),
(270, 'GJ05AB1235', '2025-08-14 08:10:48'),
(275, 'GJ05AB3457', '2025-08-13 12:34:15'),
(280, 'GJ05AB5679', '2025-08-13 06:50:45'),
(285, 'GJ05AB9013', '2025-08-13 13:22:41'),
(290, 'GJ05CD1235', '2025-08-12 17:18:10'),
(295, 'GJ05CD7892', '2025-08-14 05:02:14'),
(300, 'GJ05EF1235', '2025-08-13 12:44:29'),
(305, 'GJ05EF5678', '2025-08-14 04:23:28'),
(310, 'GJ05GH3457', '2025-08-12 12:06:11'),
(315, 'GJ05GH7890', '2025-08-13 14:32:50'),
(320, 'GJ05IJ1235', '2025-08-14 22:24:38'),
(325, 'GJ05IJ5678', '2025-08-13 17:03:47'),
(330, 'GJ05KL1235', '2025-08-12 15:37:02'),
(335, 'GJ05KL7891', '2025-08-12 16:57:25'),
(340, 'GJ05MN3456', '2025-08-14 16:11:21'),
(345, 'GJ05MN9013', '2025-08-14 13:04:47'),
(350, 'GJ05OP1235', '2025-08-14 16:42:18'),
(355, 'GJ05OP5679', '2025-08-13 16:42:37'),
(360, 'GJ05QR5679', '2025-08-14 06:50:57'),
(365, 'GJ05ST5678', '2025-08-13 14:52:03'),
(370, 'GJ05UV3456', '2025-08-14 16:38:58'),
(375, 'GJ05WX7891', '2025-08-12 15:48:55'),
(380, 'GJ05YZ5678', '2025-08-12 17:26:48'),
(385, 'GJ05YZ9013', '2025-08-12 18:44:36');

-- --------------------------------------------------------

--
-- Table structure for table `parking_records`
--

CREATE TABLE `parking_records` (
  `record_id` int(11) NOT NULL,
  `Slot_id` int(11) NOT NULL,
  `vehicle_number` varchar(20) DEFAULT NULL,
  `time_in` datetime DEFAULT NULL,
  `time_out` datetime DEFAULT NULL,
  `total_hours` decimal(10,2) DEFAULT NULL,
  `charge_amount` decimal(10,2) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `parking_records`
--

INSERT INTO `parking_records` (`record_id`, `Slot_id`, `vehicle_number`, `time_in`, `time_out`, `total_hours`, `charge_amount`) VALUES
(1, 270, 'GJ05AB1235', '2025-08-14 08:10:48', '2025-08-14 20:10:48', 12.00, 0.00),
(2, 451, 'GJ05TM1001', '2025-08-15 09:15:30', '2025-08-15 20:15:30', 11.00, 60.00),
(3, 15, 'GJ05ST7890', '2025-08-12 14:56:33', '2025-08-13 10:00:00', 19.05, 0.00),
(4, 315, 'GJ05GH7890', '2025-08-13 14:32:50', '2025-08-14 09:30:00', 18.95, 0.00),
(5, 462, 'GJ05TM1005', '2025-08-14 14:55:33', '2025-08-15 08:00:00', 17.07, 200.00),
(6, 90, 'GJ05OP7890', '2025-08-12 13:47:25', '2025-08-12 22:00:00', 8.20, 0.00),
(7, 1, 'GJ05AB1234', '2025-08-14 08:58:32', '2025-08-14 19:00:00', 10.02, 0.00),
(8, 215, 'GJ05KL1234', '2025-08-12 19:17:51', '2025-08-13 08:30:00', 13.20, 0.00),
(9, 476, 'GJ05TM1011', '2025-08-14 20:12:38', '2025-08-15 10:00:00', 13.78, 160.00),
(10, 151, 'GJ05AB3456', '2025-08-12 21:17:43', '2025-08-13 09:00:00', 11.70, 0.00),
(11, 60, 'GJ05UV9012', '2025-08-13 21:44:27', '2025-08-14 08:19:47', 10.58, 0.00),
(12, 285, 'GJ05AB9013', '2025-08-13 13:22:41', '2025-08-13 23:08:43', 9.77, 0.00),
(13, 456, 'GJ05TM1003', '2025-08-14 16:22:45', '2025-08-15 03:22:45', 11.00, 60.00),
(14, 170, 'GJ05CD5679', '2025-08-13 23:11:42', '2025-08-14 10:45:22', 11.55, 0.00),
(15, 40, 'GJ05EF1234', '2025-08-14 09:31:06', '2025-08-14 21:15:59', 11.73, 0.00),
(16, 240, 'GJ05OP3456', '2025-08-14 05:58:09', '2025-08-14 18:50:56', 12.87, 0.00),
(17, 105, 'GJ05YZ7890', '2025-08-12 20:30:39', '2025-08-13 09:55:46', 13.42, 0.00),
(18, 470, 'GJ05TM1008', '2025-08-15 06:25:42', '2025-08-15 19:25:42', 13.00, 150.00),
(19, 320, 'GJ05IJ1235', '2025-08-14 22:24:38', '2025-08-15 09:23:40', 10.98, 0.00),
(20, 25, 'GJ05KL3456', '2025-08-13 19:51:24', '2025-08-14 07:01:19', 11.15, 0.00),
(21, 200, 'GJ05GH9012', '2025-08-13 11:24:11', '2025-08-13 23:49:26', 12.42, 0.00),
(22, 45, 'GJ05IJ1234', '2025-08-13 21:39:46', '2025-08-14 10:05:38', 12.42, 0.00),
(23, 275, 'GJ05AB3457', '2025-08-13 12:34:15', '2025-08-14 01:00:05', 12.42, 0.00),
(24, 160, 'GJ05AB9012', '2025-08-13 18:09:56', '2025-08-14 05:58:01', 11.80, 0.00),
(25, 340, 'GJ05MN3456', '2025-08-14 16:11:21', '2025-08-15 02:16:21', 10.08, 0.00),
(26, 472, 'GJ05TM1009', '2025-08-14 13:18:55', '2025-08-15 06:18:55', 17.00, 90.00),
(27, 70, 'GJ05AB7890', '2025-08-12 19:37:34', '2025-08-13 07:12:14', 11.57, 0.00),
(28, 185, 'GJ05EF7890', '2025-08-12 06:38:01', '2025-08-12 19:20:09', 12.70, 0.00),
(29, 255, 'GJ05UV1234', '2025-08-13 07:23:45', '2025-08-13 20:11:06', 12.78, 0.00),
(30, 115, 'GJ05CD7891', '2025-08-12 20:05:44', '2025-08-13 08:42:23', 12.60, 0.00),
(31, 459, 'GJ05TM1004', '2025-08-15 11:30:18', '2025-08-15 23:30:18', 12.00, 65.00),
(32, 330, 'GJ05KL1235', '2025-08-12 15:37:02', '2025-08-13 04:33:00', 12.92, 0.00),
(33, 5, 'GJ05GH3456', '2025-08-14 07:38:41', '2025-08-14 19:11:12', 11.53, 0.00),
(34, 220, 'GJ05KL5678', '2025-08-14 09:31:29', '2025-08-14 21:08:39', 11.62, 0.00),
(35, 175, 'GJ05CD7890', '2025-08-14 06:59:16', '2025-08-14 18:01:50', 11.03, 0.00),
(36, 465, 'GJ05TM1006', '2025-08-15 08:10:25', '2025-08-15 19:10:25', 11.00, 60.00),
(37, 80, 'GJ05GH5679', '2025-08-13 10:31:53', '2025-08-13 22:22:31', 11.83, 0.00),
(38, 295, 'GJ05CD7892', '2025-08-14 05:02:14', '2025-08-14 17:38:16', 12.60, 0.00),
(39, 10, 'GJ05OP9012', '2025-08-12 18:45:07', '2025-08-13 05:45:33', 11.00, 0.00),
(40, 205, 'GJ05IJ3456', '2025-08-12 11:21:19', '2025-08-12 23:33:51', 12.20, 0.00),
(41, 478, 'GJ05TM1012', '2025-08-15 12:45:15', '2025-08-15 22:45:15', 10.00, 55.00),
(42, 165, 'GJ05CD1234', '2025-08-13 09:51:06', '2025-08-13 21:25:17', 11.57, 0.00),
(43, 55, 'GJ05QR7890', '2025-08-14 08:23:19', '2025-08-14 20:02:11', 11.63, 0.00),
(44, 280, 'GJ05AB5679', '2025-08-13 06:50:45', '2025-08-13 18:58:21', 12.12, 0.00),
(45, 35, 'GJ05WX9012', '2025-08-12 18:35:39', '2025-08-13 06:48:27', 12.20, 0.00),
(46, 230, 'GJ05MN7890', '2025-08-12 14:27:43', '2025-08-13 02:18:28', 11.83, 0.00),
(47, 453, 'GJ05TM1002', '2025-08-15 07:45:12', '2025-08-15 19:45:12', 12.00, 140.00),
(48, 100, 'GJ05WX1235', '2025-08-14 11:28:54', '2025-08-15 01:40:55', 14.20, 0.00),
(49, 325, 'GJ05IJ5678', '2025-08-13 17:03:47', '2025-08-14 05:50:52', 12.78, 0.00),
(50, 20, 'GJ05CD3456', '2025-08-14 07:19:07', '2025-08-14 19:55:02', 12.58, 0.00),
(51, 260, 'GJ05WX5678', '2025-08-12 11:46:33', '2025-08-13 01:41:37', 13.92, 0.00),
(52, 180, 'GJ05EF3456', '2025-08-13 15:41:09', '2025-08-14 02:30:36', 10.82, 0.00),
(53, 480, 'GJ05TM1013', '2025-08-14 17:28:50', '2025-08-15 07:28:50', 14.00, 75.00),
(54, 85, 'GJ05IJ7891', '2025-08-13 11:07:52', '2025-08-13 23:00:18', 11.87, 0.00),
(55, 300, 'GJ05EF1235', '2025-08-13 12:44:29', '2025-08-14 09:03:32', 20.32, 0.00),
(56, 155, 'GJ05AB5678', '2025-08-13 22:19:58', '2025-08-14 11:15:34', 12.92, 0.00),
(57, 65, 'GJ05YZ1235', '2025-08-12 09:25:15', '2025-08-12 21:39:03', 12.22, 0.00),
(58, 245, 'GJ05QR3456', '2025-08-12 13:55:13', '2025-08-13 05:10:13', 15.25, 0.00),
(59, 467, 'GJ05TM1007', '2025-08-14 19:40:17', '2025-08-15 08:40:17', 13.00, 70.00),
(60, 190, 'GJ05GH1235', '2025-08-12 22:14:20', '2025-08-13 10:50:15', 12.58, 0.00),
(61, 50, 'GJ05MN5679', '2025-08-13 13:44:02', '2025-08-14 01:33:41', 11.82, 0.00),
(62, 265, 'GJ05YZ3456', '2025-08-12 10:15:36', '2025-08-12 23:28:19', 13.20, 0.00),
(63, 345, 'GJ05MN9013', '2025-08-14 13:04:47', '2025-08-15 03:54:47', 14.83, 0.00),
(64, 210, 'GJ05IJ9012', '2025-08-13 12:52:21', '2025-08-14 01:05:57', 12.22, 0.00),
(65, 75, 'GJ05CD9012', '2025-08-12 23:48:57', '2025-08-13 12:50:00', 13.02, 0.00),
(66, 290, 'GJ05CD1235', '2025-08-12 17:18:10', '2025-08-13 06:45:08', 13.43, 0.00),
(67, 474, 'GJ05TM1010', '2025-08-15 10:35:20', '2025-08-15 23:35:20', 13.00, 70.00),
(68, 195, 'GJ05GH5678', '2025-08-12 16:29:54', '2025-08-13 04:40:44', 12.17, 0.00),
(69, 95, 'GJ05ST3456', '2025-08-13 03:11:15', '2025-08-13 16:08:13', 12.93, 0.00),
(70, 335, 'GJ05KL7891', '2025-08-12 16:57:25', '2025-08-13 07:42:25', 14.75, 0.00),
(71, 225, 'GJ05MN1234', '2025-08-12 15:57:52', '2025-08-13 05:35:10', 13.62, 0.00),
(72, 305, 'GJ05EF5678', '2025-08-14 04:23:28', '2025-08-14 18:31:24', 14.12, 0.00),
(73, 250, 'GJ05ST1234', '2025-08-13 19:17:49', '2025-08-14 08:20:48', 13.03, 0.00),
(74, 235, 'GJ05OP1234', '2025-08-13 14:04:20', '2025-08-14 03:25:49', 13.35, 0.00),
(75, 310, 'GJ05GH3457', '2025-08-12 12:06:11', '2025-08-13 02:52:10', 14.75, 0.00);

--
-- Triggers `parking_records`
--
DELIMITER $$
CREATE TRIGGER `archive_and_calculate_charges` BEFORE INSERT ON `parking_records` FOR EACH ROW BEGIN
    DECLARE v_vehicle_number VARCHAR(20);
    DECLARE v_time_in DATETIME;
    DECLARE v_total_hours DECIMAL(10, 2);
    DECLARE v_charge_amount DECIMAL(10, 2);
    DECLARE v_vehicle_type VARCHAR(20);

    -- Fetch vehicle_number and time_in from parked_vehicle or spare_parked_vehicle
    IF NEW.Slot_id <= 450 THEN
        SELECT vehicle_number, time_in INTO v_vehicle_number, v_time_in
        FROM parked_vehicle WHERE Slot_id = NEW.Slot_id;
    ELSE
        SELECT vehicle_number, time_in INTO v_vehicle_number, v_time_in
        FROM spare_parked_vehicle WHERE Slot_id = NEW.Slot_id;
    END IF;

    -- Calculate total hours
    SET v_total_hours = TIMESTAMPDIFF(MINUTE, v_time_in, NEW.time_out) / 60.0;

    -- Fetch vehicle type
    SELECT vehicle_type INTO v_vehicle_type
    FROM vehicle WHERE vehicle_number = v_vehicle_number;

    -- Calculate charge amount
    IF NEW.Slot_id > 450 THEN
        IF v_vehicle_type = '4-wheeler' THEN
            IF v_total_hours <= 3 THEN
                SET v_charge_amount = 50;
            ELSE
                SET v_charge_amount = 50 + CEIL(v_total_hours - 3) * 10;
            END IF;
        ELSE -- 2-wheeler
            IF v_total_hours <= 3 THEN
                SET v_charge_amount = 20;
            ELSE
                SET v_charge_amount = 20 + CEIL(v_total_hours - 3) * 5;
            END IF;
        END IF;
    ELSE
        SET v_charge_amount = 0;
    END IF;

    -- Set the values for the new row in parking_records
    SET NEW.vehicle_number = v_vehicle_number;
    SET NEW.time_in = v_time_in;
    SET NEW.total_hours = v_total_hours;
    SET NEW.charge_amount = v_charge_amount;
END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Table structure for table `residents`
--

CREATE TABLE `residents` (
  `resident_id` varchar(20) NOT NULL,
  `first_name` varchar(20) NOT NULL,
  `last_name` varchar(20) NOT NULL,
  `contact_number` varchar(20) NOT NULL,
  `wing` varchar(5) NOT NULL,
  `house_number` int(11) NOT NULL,
  `vehicle_count` int(11) NOT NULL,
  `username` varchar(20) NOT NULL,
  `password` varchar(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `residents`
--

INSERT INTO `residents` (`resident_id`, `first_name`, `last_name`, `contact_number`, `wing`, `house_number`, `vehicle_count`, `username`, `password`) VALUES
('RES001', 'Rajesh', 'Kumar', '9876543210', 'C', 1, 1, 'rajesh1', '3210@C#1'),
('RES002', 'Priya', 'Singh', '8765432109', 'A', 1, 1, 'priya1', '2109@A#1'),
('RES003', 'Amit', 'Sharma', '7654321098', 'B', 1, 3, 'amit1', '1098@B#1'),
('RES004', 'Sunita', 'Patel', '6543210987', 'A', 2, 1, 'sunita2', '0987@A#2'),
('RES005', 'Vikash', 'Gupta', '9543210876', 'B', 2, 2, 'vikash2', '0876@B#2'),
('RES006', 'Meera', 'Joshi', '8432109765', 'C', 2, 2, 'meera2', '9765@C#2'),
('RES007', 'Rohit', 'Verma', '7321098654', 'A', 3, 1, 'rohit3', '8654@A#3'),
('RES008', 'Kavita', 'Agarwal', '6210987543', 'B', 3, 4, 'kavita3', '7543@B#3'),
('RES009', 'Suresh', 'Yadav', '9109876432', 'C', 3, 2, 'suresh3', '6432@C#3'),
('RES010', 'Neha', 'Mishra', '8098765321', 'A', 4, 1, 'neha4', '5321@A#4'),
('RES011', 'Deepak', 'Shah', '7987654210', 'B', 4, 2, 'deepak4', '4210@B#4'),
('RES012', 'Pooja', 'Bansal', '6876543209', 'C', 4, 1, 'pooja4', '3209@C#4'),
('RES013', 'Arjun', 'Reddy', '9765432108', 'A', 5, 4, 'arjun5', '2108@A#5'),
('RES014', 'Sita', 'Nair', '8654321097', 'B', 5, 1, 'sita5', '1097@B#5'),
('RES015', 'Ravi', 'Chopra', '7543210986', 'C', 5, 3, 'ravi5', '0986@C#5'),
('RES016', 'Anita', 'Kapoor', '6432109875', 'A', 6, 2, 'anita6', '9875@A#6'),
('RES017', 'Manoj', 'Saxena', '9321098764', 'B', 6, 1, 'manoj6', '8764@B#6'),
('RES018', 'Geeta', 'Malhotra', '8210987653', 'C', 6, 4, 'geeta6', '7653@C#6'),
('RES019', 'Ramesh', 'Tiwari', '7109876542', 'A', 7, 1, 'ramesh7', '6542@A#7'),
('RES020', 'Seema', 'Bhatt', '6098765431', 'B', 7, 2, 'seema7', '5431@B#7'),
('RES021', 'Ajay', 'Pandey', '9987654320', 'C', 7, 1, 'ajay7', '4320@C#7'),
('RES022', 'Rekha', 'Srivastava', '8876543219', 'A', 8, 2, 'rekha8', '3219@A#8'),
('RES023', 'Vinod', 'Chandra', '7765432108', 'B', 8, 4, 'vinod8', '2108@B#8'),
('RES024', 'Shanti', 'Goyal', '6654321097', 'C', 8, 1, 'shanti8', '1097@C#8'),
('RES025', 'Kiran', 'Thakur', '9543218765', 'A', 9, 1, 'kiran9', '8765@A#9'),
('RES026', 'Mahesh', 'Jain', '8432109876', 'B', 9, 2, 'mahesh9', '9876@B#9'),
('RES027', 'Lata', 'Dixit', '7321098765', 'C', 9, 2, 'lata9', '8765@C#9'),
('RES028', 'Sandeep', 'Aggarwal', '6210987654', 'A', 10, 4, 'sandeep10', '7654@A#10'),
('RES029', 'Rita', 'Khanna', '9109876543', 'B', 10, 1, 'rita10', '6543@B#10'),
('RES030', 'Pramod', 'Singhal', '8098765432', 'C', 10, 1, 'pramod10', '5432@C#10'),
('RES031', 'Usha', 'Rastogi', '7987654321', 'A', 11, 2, 'usha11', '4321@A#11'),
('RES032', 'Naresh', 'Mittal', '6876543210', 'B', 11, 2, 'naresh11', '3210@B#11'),
('RES033', 'Sudha', 'Varma', '9765432109', 'C', 11, 1, 'sudha11', '2109@C#11'),
('RES034', 'Ashok', 'Bhatnagar', '8654321098', 'A', 12, 1, 'ashok12', '1098@A#12'),
('RES035', 'Nirmala', 'Arora', '7543210987', 'B', 12, 4, 'nirmala12', '0987@B#12'),
('RES036', 'Harish', 'Khurana', '6432109876', 'C', 12, 2, 'harish12', '9876@C#12'),
('RES037', 'Vandana', 'Goel', '9321098765', 'A', 13, 2, 'vandana13', '8765@A#13'),
('RES038', 'Sunil', 'Bajaj', '8210987654', 'B', 13, 1, 'sunil13', '7654@B#13'),
('RES039', 'Kamala', 'Sethi', '7109876543', 'C', 13, 1, 'kamala13', '6543@C#13'),
('RES040', 'Rajiv', 'Lal', '6098765432', 'A', 14, 1, 'rajiv14', '5432@A#14'),
('RES041', 'Anjali', 'Dua', '9987654321', 'B', 14, 2, 'anjali14', '4321@B#14'),
('RES042', 'Dinesh', 'Tandon', '8876543210', 'C', 14, 4, 'dinesh14', '3210@C#14'),
('RES043', 'Shobha', 'Mathur', '7765432109', 'A', 15, 2, 'shobha15', '2109@A#15'),
('RES044', 'Anil', 'Sachdeva', '6654321098', 'B', 15, 1, 'anil15', '1098@B#15'),
('RES045', 'Madhuri', 'Bedi', '9543210987', 'C', 15, 1, 'madhuri15', '0987@C#15'),
('RES046', 'Govind', 'Chauhan', '8432108765', 'A', 16, 1, 'govind16', '8765@A#16'),
('RES047', 'Saroj', 'Kalra', '7321097654', 'B', 16, 2, 'saroj16', '7654@B#16'),
('RES048', 'Mukesh', 'Gulati', '6210986543', 'C', 16, 2, 'mukesh16', '6543@C#16'),
('RES049', 'Pushpa', 'Bindra', '9109875432', 'A', 17, 4, 'pushpa17', '5432@A#17'),
('RES050', 'Rakesh', 'Oberoi', '8098764321', 'B', 17, 1, 'rakesh17', '4321@B#17'),
('RES051', 'Sarita', 'Kohli', '7987653210', 'C', 17, 1, 'sarita17', '3210@C#17'),
('RES052', 'Devendra', 'Sood', '6876542109', 'A', 18, 2, 'devendra18', '2109@A#18'),
('RES053', 'Manju', 'Grover', '9765431098', 'B', 18, 2, 'manju18', '1098@B#18'),
('RES054', 'Yogesh', 'Wadhwa', '8654320987', 'C', 18, 1, 'yogesh18', '0987@C#18'),
('RES055', 'Kalpana', 'Bakshi', '7543219876', 'A', 19, 1, 'kalpana19', '9876@A#19'),
('RES056', 'Ramesh', 'Dhawan', '6432108765', 'B', 19, 4, 'ramesh19', '8765@B#19'),
('RES057', 'Savita', 'Ahuja', '9321087654', 'C', 19, 2, 'savita19', '7654@C#19'),
('RES058', 'Bharat', 'Kashyap', '8210976543', 'A', 20, 2, 'bharat20', '6543@A#20'),
('RES059', 'Leela', 'Mehrotra', '7109865432', 'B', 20, 1, 'leela20', '5432@B#20'),
('RES060', 'Jitendra', 'Bhardwaj', '6098754321', 'C', 20, 1, 'jitendra20', '4321@C#20'),
('RES061', 'Sushma', 'Garg', '9987643210', 'A', 21, 1, 'sushma21', '3210@A#21'),
('RES062', 'Pawan', 'Singla', '8876532109', 'B', 21, 2, 'pawan21', '2109@B#21'),
('RES063', 'Radha', 'Saini', '7765421098', 'C', 21, 2, 'radha21', '1098@C#21'),
('RES064', 'Jagdish', 'Bansal', '6654310987', 'A', 22, 4, 'jagdish22', '0987@A#22'),
('RES065', 'Sunita', 'Bhalla', '9543209876', 'B', 22, 1, 'sunita22', '9876@B#22'),
('RES066', 'Vijendra', 'Joshi', '8432098765', 'C', 22, 1, 'vijendra22', '8765@C#22'),
('RES067', 'Urmila', 'Malhotra', '7320987654', 'A', 23, 2, 'urmila23', '7654@A#23'),
('RES068', 'Ramesh', 'Goswami', '6209876543', 'B', 23, 2, 'ramesh23', '6543@B#23'),
('RES069', 'Shakuntala', 'Kapoor', '9098765432', 'C', 23, 1, 'shakuntala23', '5432@C#23'),
('RES070', 'Satish', 'Raghav', '8987654321', 'A', 24, 1, 'satish24', '4321@A#24'),
('RES071', 'Anita', 'Sharma', '7876543210', 'B', 24, 4, 'anita24', '3210@B#24'),
('RES072', 'Krishnan', 'Nambiar', '6765432109', 'C', 24, 2, 'krishnan24', '2109@C#24'),
('RES073', 'Sharda', 'Agarwal', '9654321098', 'A', 25, 1, 'sharda25', '1098@A#25'),
('RES074', 'Yogesh', 'Pandey', '8543210987', 'B', 25, 1, 'yogesh25', '0987@B#25'),
('RES075', 'Kamla', 'Sinha', '7432109876', 'C', 25, 1, 'kamla25', '9876@C#25'),
('RES076', 'Mahendra', 'Trivedi', '6321098765', 'A', 26, 2, 'mahendra26', '8765@A#26'),
('RES077', 'Sunita', 'Gupta', '9210987654', 'B', 26, 2, 'sunita26', '7654@B#26'),
('RES078', 'Raman', 'Khanna', '8109876543', 'C', 26, 4, 'raman26', '6543@C#26'),
('RES079', 'Sumitra', 'Jain', '7098765432', 'A', 27, 1, 'sumitra27', '5432@A#27'),
('RES080', 'Ashok', 'Verma', '6987654321', 'B', 27, 1, 'ashok27', '4321@B#27'),
('RES081', 'Indira', 'Rao', '9876543211', 'C', 27, 2, 'indira27', '3210@C#27'),
('RES082', 'Ramesh', 'Bansal', '8765432108', 'A', 28, 2, 'ramesh28', '2108@A#28'),
('RES083', 'Kamala', 'Devi', '7654321097', 'B', 28, 1, 'kamala28', '1097@B#28'),
('RES084', 'Sudarshan', 'Kumar', '6543210986', 'C', 28, 1, 'sudarshan28', '0986@C#28'),
('RES085', 'Pushpa', 'Singh', '9432109875', 'A', 29, 1, 'pushpa29', '9875@A#29'),
('RES086', 'Narain', 'Agarwal', '8321098764', 'B', 29, 2, 'narain29', '8764@B#29'),
('RES087', 'Manorama', 'Gupta', '7210987653', 'C', 29, 2, 'manorama29', '7653@C#29'),
('RES088', 'Shyam', 'Patel', '6109876542', 'A', 30, 3, 'shyam30', '6542@A#30'),
('RES089', 'Sushila', 'Mishra', '9098765431', 'B', 30, 1, 'sushila30', '5431@B#30'),
('RES090', 'Mukesh', 'Chandra', '8987654320', 'B', 31, 2, 'mukesh31', '4320@B#31'),
('RES091', 'Geeta', 'Sharma', '7876543219', 'B', 32, 1, 'geeta32', '3219@B#32'),
('RES092', 'Dinesh', 'Kumar', '6765432108', 'B', 33, 2, 'dinesh33', '2108@B#33'),
('RES093', 'Meena', 'Gupta', '9654321087', 'B', 34, 1, 'meena34', '1087@B#34'),
('RES094', 'Rajesh', 'Singh', '8543210976', 'B', 35, 3, 'rajesh35', '0976@B#35'),
('RES095', 'Sunita', 'Verma', '7432109865', 'B', 36, 2, 'sunita36', '9865@B#36'),
('RES096', 'Arun', 'Joshi', '6321098754', 'B', 37, 1, 'arun37', '8754@B#37');

-- --------------------------------------------------------

--
-- Table structure for table `spare_parked_vehicle`
--

CREATE TABLE `spare_parked_vehicle` (
  `Slot_id` int(11) NOT NULL,
  `vehicle_number` varchar(20) NOT NULL,
  `time_in` datetime NOT NULL
) ;

--
-- Dumping data for table `spare_parked_vehicle`
--

INSERT INTO `spare_parked_vehicle` (`Slot_id`, `vehicle_number`, `time_in`) VALUES
(451, 'GJ05TM1001', '2025-08-15 09:15:30'),
(453, 'GJ05TM1002', '2025-08-15 07:45:12'),
(456, 'GJ05TM1003', '2025-08-14 16:22:45'),
(459, 'GJ05TM1004', '2025-08-15 11:30:18'),
(462, 'GJ05TM1005', '2025-08-14 14:55:33'),
(465, 'GJ05TM1006', '2025-08-15 08:10:25'),
(467, 'GJ05TM1007', '2025-08-14 19:40:17'),
(470, 'GJ05TM1008', '2025-08-15 06:25:42'),
(472, 'GJ05TM1009', '2025-08-14 13:18:55'),
(474, 'GJ05TM1010', '2025-08-15 10:35:20'),
(476, 'GJ05TM1011', '2025-08-14 20:12:38'),
(478, 'GJ05TM1012', '2025-08-15 12:45:15'),
(480, 'GJ05TM1013', '2025-08-14 17:28:50');

-- --------------------------------------------------------

--
-- Table structure for table `vehicle`
--

CREATE TABLE `vehicle` (
  `vehicle_number` varchar(20) NOT NULL,
  `resident_id` varchar(20) NOT NULL,
  `vehicle_type` varchar(20) NOT NULL,
  `vehicle_brand` varchar(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `vehicle`
--

INSERT INTO `vehicle` (`vehicle_number`, `resident_id`, `vehicle_type`, `vehicle_brand`) VALUES
('GJ05AB1234', 'RES001', '4-wheeler', 'Maruti Suzuki'),
('GJ05AB1235', 'RES037', '2-wheeler', 'KTM'),
('GJ05AB1236', 'RES077', '2-wheeler', 'Yamaha'),
('GJ05AB3456', 'RES008', '2-wheeler', 'Ducati'),
('GJ05AB3457', 'RES046', '2-wheeler', 'Hero'),
('GJ05AB3458', 'RES086', '4-wheeler', 'Jeep'),
('GJ05AB5678', 'RES015', '2-wheeler', 'KTM'),
('GJ05AB5679', 'RES053', '2-wheeler', 'Hero'),
('GJ05AB5680', 'RES093', '2-wheeler', 'Suzuki'),
('GJ05AB7890', 'RES023', '4-wheeler', 'Volkswagen'),
('GJ05AB7891', 'RES062', '4-wheeler', 'Tata'),
('GJ05AB9012', 'RES030', '2-wheeler', 'KTM'),
('GJ05AB9013', 'RES069', '2-wheeler', 'TVS'),
('GJ05CD1234', 'RES023', '2-wheeler', 'Suzuki'),
('GJ05CD1235', 'RES062', '2-wheeler', 'TVS'),
('GJ05CD3456', 'RES031', '4-wheeler', 'Hyundai'),
('GJ05CD3457', 'RES070', '2-wheeler', 'Yamaha'),
('GJ05CD5679', 'RES038', '2-wheeler', 'Honda'),
('GJ05CD5680', 'RES078', '4-wheeler', 'Mercedes'),
('GJ05CD7890', 'RES008', '2-wheeler', 'Kawasaki'),
('GJ05CD7891', 'RES047', '4-wheeler', 'BMW'),
('GJ05CD7892', 'RES086', '2-wheeler', 'Suzuki'),
('GJ05CD9012', 'RES016', '4-wheeler', 'Renault'),
('GJ05CD9013', 'RES054', '2-wheeler', 'Bajaj'),
('GJ05CD9014', 'RES094', '4-wheeler', 'Ford'),
('GJ05EF1234', 'RES009', '4-wheeler', 'Toyota'),
('GJ05EF1235', 'RES047', '2-wheeler', 'Bajaj'),
('GJ05EF1236', 'RES087', '4-wheeler', 'Maruti Suzuki'),
('GJ05EF3456', 'RES016', '2-wheeler', 'Honda'),
('GJ05EF3457', 'RES055', '2-wheeler', 'TVS'),
('GJ05EF3458', 'RES094', '2-wheeler', 'KTM'),
('GJ05EF5678', 'RES023', '2-wheeler', 'KTM'),
('GJ05EF5679', 'RES063', '4-wheeler', 'Mahindra'),
('GJ05EF7890', 'RES031', '2-wheeler', 'Honda'),
('GJ05EF7891', 'RES071', '4-wheeler', 'Kia'),
('GJ05EF9012', 'RES002', '2-wheeler', 'Hero'),
('GJ05EF9013', 'RES039', '2-wheeler', 'Hero'),
('GJ05EF9014', 'RES078', '2-wheeler', 'Royal Enfield'),
('GJ05GH1234', 'RES032', '4-wheeler', 'Tata'),
('GJ05GH1235', 'RES071', '2-wheeler', 'Royal Enfield'),
('GJ05GH3456', 'RES003', '4-wheeler', 'Hyundai'),
('GJ05GH3457', 'RES040', '2-wheeler', 'Bajaj'),
('GJ05GH3458', 'RES078', '2-wheeler', 'Suzuki'),
('GJ05GH5678', 'RES009', '2-wheeler', 'Honda'),
('GJ05GH5679', 'RES048', '4-wheeler', 'Audi'),
('GJ05GH5680', 'RES087', '2-wheeler', 'KTM'),
('GJ05GH7890', 'RES017', '2-wheeler', 'Hero'),
('GJ05GH7891', 'RES056', '4-wheeler', 'Jeep'),
('GJ05GH7892', 'RES094', '2-wheeler', 'Honda'),
('GJ05GH9012', 'RES024', '2-wheeler', 'Honda'),
('GJ05GH9013', 'RES063', '2-wheeler', 'Yamaha'),
('GJ05IJ1234', 'RES018', '4-wheeler', 'BMW'),
('GJ05IJ1235', 'RES056', '2-wheeler', 'Yamaha'),
('GJ05IJ1236', 'RES095', '4-wheeler', 'Toyota'),
('GJ05IJ3456', 'RES025', '2-wheeler', 'Hero'),
('GJ05IJ3457', 'RES064', '4-wheeler', 'Ford'),
('GJ05IJ5678', 'RES032', '2-wheeler', 'Hero'),
('GJ05IJ5679', 'RES071', '2-wheeler', 'Suzuki'),
('GJ05IJ7890', 'RES003', '2-wheeler', 'Bajaj'),
('GJ05IJ7891', 'RES041', '4-wheeler', 'Nissan'),
('GJ05IJ7892', 'RES079', '2-wheeler', 'KTM'),
('GJ05IJ9012', 'RES010', '2-wheeler', 'Hero'),
('GJ05IJ9013', 'RES048', '2-wheeler', 'TVS'),
('GJ05IJ9014', 'RES088', '4-wheeler', 'Hyundai'),
('GJ05KL1234', 'RES003', '2-wheeler', 'TVS'),
('GJ05KL1235', 'RES041', '2-wheeler', 'TVS'),
('GJ05KL1236', 'RES080', '2-wheeler', 'Honda'),
('GJ05KL3456', 'RES011', '4-wheeler', 'Hyundai'),
('GJ05KL3457', 'RES049', '4-wheeler', 'Mercedes'),
('GJ05KL3458', 'RES088', '2-wheeler', 'Honda'),
('GJ05KL5678', 'RES018', '2-wheeler', 'Harley Davidson'),
('GJ05KL5679', 'RES056', '2-wheeler', 'Royal Enfield'),
('GJ05KL5680', 'RES095', '2-wheeler', 'Hero'),
('GJ05KL7890', 'RES026', '4-wheeler', 'Skoda'),
('GJ05KL7891', 'RES064', '2-wheeler', 'Royal Enfield'),
('GJ05KL9012', 'RES033', '2-wheeler', 'Bajaj'),
('GJ05KL9013', 'RES072', '4-wheeler', 'Renault'),
('GJ05MN1234', 'RES026', '2-wheeler', 'Bajaj'),
('GJ05MN1235', 'RES064', '2-wheeler', 'Suzuki'),
('GJ05MN3456', 'RES034', '2-wheeler', 'TVS'),
('GJ05MN3457', 'RES072', '2-wheeler', 'KTM'),
('GJ05MN5678', 'RES004', '2-wheeler', 'Yamaha'),
('GJ05MN5679', 'RES042', '4-wheeler', 'Kia'),
('GJ05MN5680', 'RES081', '4-wheeler', 'Volkswagen'),
('GJ05MN7890', 'RES011', '2-wheeler', 'Bajaj'),
('GJ05MN7891', 'RES049', '2-wheeler', 'Yamaha'),
('GJ05MN7892', 'RES088', '2-wheeler', 'Hero'),
('GJ05MN9012', 'RES018', '2-wheeler', 'Ducati'),
('GJ05MN9013', 'RES057', '4-wheeler', 'Maruti Suzuki'),
('GJ05MN9014', 'RES096', '2-wheeler', 'Bajaj'),
('GJ05OP1234', 'RES012', '2-wheeler', 'TVS'),
('GJ05OP1235', 'RES049', '2-wheeler', 'Royal Enfield'),
('GJ05OP1236', 'RES089', '2-wheeler', 'Bajaj'),
('GJ05OP3456', 'RES019', '2-wheeler', 'Bajaj'),
('GJ05OP3457', 'RES057', '2-wheeler', 'Suzuki'),
('GJ05OP5678', 'RES027', '4-wheeler', 'Jeep'),
('GJ05OP5679', 'RES065', '2-wheeler', 'KTM'),
('GJ05OP7890', 'RES035', '4-wheeler', 'Mahindra'),
('GJ05OP7891', 'RES073', '2-wheeler', 'Honda'),
('GJ05OP9012', 'RES005', '4-wheeler', 'Tata'),
('GJ05OP9013', 'RES042', '2-wheeler', 'Yamaha'),
('GJ05OP9014', 'RES081', '2-wheeler', 'Hero'),
('GJ05QR1234', 'RES035', '2-wheeler', 'Yamaha'),
('GJ05QR1235', 'RES074', '2-wheeler', 'Hero'),
('GJ05QR3456', 'RES005', '2-wheeler', 'Royal Enfield'),
('GJ05QR3457', 'RES042', '2-wheeler', 'Royal Enfield'),
('GJ05QR3458', 'RES082', '4-wheeler', 'Skoda'),
('GJ05QR5678', 'RES013', '4-wheeler', 'Nissan'),
('GJ05QR5679', 'RES050', '2-wheeler', 'Suzuki'),
('GJ05QR5680', 'RES090', '4-wheeler', 'Tata'),
('GJ05QR7890', 'RES020', '4-wheeler', 'Audi'),
('GJ05QR7891', 'RES058', '4-wheeler', 'Hyundai'),
('GJ05QR9012', 'RES027', '2-wheeler', 'TVS'),
('GJ05QR9013', 'RES066', '2-wheeler', 'Honda'),
('GJ05ST1234', 'RES020', '2-wheeler', 'TVS'),
('GJ05ST1235', 'RES058', '2-wheeler', 'KTM'),
('GJ05ST3456', 'RES028', '4-wheeler', 'Maruti Suzuki'),
('GJ05ST3457', 'RES067', '4-wheeler', 'Toyota'),
('GJ05ST5678', 'RES035', '2-wheeler', 'Royal Enfield'),
('GJ05ST5679', 'RES075', '2-wheeler', 'Bajaj'),
('GJ05ST7890', 'RES006', '4-wheeler', 'Mahindra'),
('GJ05ST7891', 'RES043', '4-wheeler', 'Renault'),
('GJ05ST7892', 'RES082', '2-wheeler', 'Bajaj'),
('GJ05ST9012', 'RES013', '2-wheeler', 'Yamaha'),
('GJ05ST9013', 'RES051', '2-wheeler', 'KTM'),
('GJ05ST9014', 'RES090', '2-wheeler', 'TVS'),
('GJ05TM1001', 'RES015', '2-wheeler', 'Honda'),
('GJ05TM1002', 'RES023', '4-wheeler', 'Maruti Suzuki'),
('GJ05TM1003', 'RES008', '2-wheeler', 'Bajaj'),
('GJ05TM1004', 'RES035', '2-wheeler', 'TVS'),
('GJ05TM1005', 'RES042', '4-wheeler', 'Hyundai'),
('GJ05TM1006', 'RES018', '2-wheeler', 'KTM'),
('GJ05TM1007', 'RES056', '2-wheeler', 'Suzuki'),
('GJ05TM1008', 'RES028', '4-wheeler', 'Toyota'),
('GJ05TM1009', 'RES064', '2-wheeler', 'Honda'),
('GJ05TM1010', 'RES071', '2-wheeler', 'Yamaha'),
('GJ05TM1011', 'RES013', '4-wheeler', 'Tata'),
('GJ05TM1012', 'RES049', '2-wheeler', 'TVS'),
('GJ05TM1013', 'RES078', '2-wheeler', 'Hero'),
('GJ05UV1234', 'RES006', '2-wheeler', 'Suzuki'),
('GJ05UV1235', 'RES043', '2-wheeler', 'Suzuki'),
('GJ05UV1236', 'RES083', '2-wheeler', 'TVS'),
('GJ05UV3456', 'RES013', '2-wheeler', 'Royal Enfield'),
('GJ05UV3457', 'RES052', '4-wheeler', 'Volkswagen'),
('GJ05UV3458', 'RES091', '2-wheeler', 'Yamaha'),
('GJ05UV5678', 'RES021', '2-wheeler', 'Yamaha'),
('GJ05UV5679', 'RES059', '2-wheeler', 'Honda'),
('GJ05UV7890', 'RES028', '2-wheeler', 'Yamaha'),
('GJ05UV7891', 'RES067', '2-wheeler', 'Hero'),
('GJ05UV9012', 'RES036', '4-wheeler', 'Ford'),
('GJ05UV9013', 'RES076', '4-wheeler', 'BMW'),
('GJ05WX1234', 'RES028', '2-wheeler', 'Royal Enfield'),
('GJ05WX1235', 'RES068', '4-wheeler', 'Nissan'),
('GJ05WX3456', 'RES036', '2-wheeler', 'Suzuki'),
('GJ05WX3457', 'RES076', '2-wheeler', 'TVS'),
('GJ05WX5678', 'RES007', '2-wheeler', 'KTM'),
('GJ05WX5679', 'RES044', '2-wheeler', 'KTM'),
('GJ05WX5680', 'RES084', '2-wheeler', 'Yamaha'),
('GJ05WX7890', 'RES014', '2-wheeler', 'Suzuki'),
('GJ05WX7891', 'RES052', '2-wheeler', 'Honda'),
('GJ05WX7892', 'RES092', '4-wheeler', 'Mahindra'),
('GJ05WX9012', 'RES022', '4-wheeler', 'Mercedes'),
('GJ05WX9013', 'RES060', '2-wheeler', 'Hero'),
('GJ05YZ1234', 'RES015', '4-wheeler', 'Kia'),
('GJ05YZ1235', 'RES053', '4-wheeler', 'Skoda'),
('GJ05YZ1236', 'RES092', '2-wheeler', 'Royal Enfield'),
('GJ05YZ3456', 'RES022', '2-wheeler', 'Royal Enfield'),
('GJ05YZ3457', 'RES061', '2-wheeler', 'Bajaj'),
('GJ05YZ5678', 'RES029', '2-wheeler', 'Suzuki'),
('GJ05YZ5679', 'RES068', '2-wheeler', 'Bajaj'),
('GJ05YZ7890', 'RES037', '4-wheeler', 'Toyota'),
('GJ05YZ7891', 'RES077', '4-wheeler', 'Audi'),
('GJ05YZ9012', 'RES008', '4-wheeler', 'Ford'),
('GJ05YZ9013', 'RES045', '2-wheeler', 'Honda'),
('GJ05YZ9014', 'RES085', '2-wheeler', 'Royal Enfield');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `parked_vehicle`
--
ALTER TABLE `parked_vehicle`
  ADD PRIMARY KEY (`Slot_id`),
  ADD UNIQUE KEY `vehicle_number_unique` (`vehicle_number`);

--
-- Indexes for table `parking_records`
--
ALTER TABLE `parking_records`
  ADD PRIMARY KEY (`record_id`),
  ADD KEY `fk_vehicle_number` (`vehicle_number`);

--
-- Indexes for table `residents`
--
ALTER TABLE `residents`
  ADD PRIMARY KEY (`resident_id`),
  ADD UNIQUE KEY `contact_number` (`contact_number`);

--
-- Indexes for table `spare_parked_vehicle`
--
ALTER TABLE `spare_parked_vehicle`
  ADD PRIMARY KEY (`Slot_id`),
  ADD UNIQUE KEY `spare_vehicle_number_unique` (`vehicle_number`);

--
-- Indexes for table `vehicle`
--
ALTER TABLE `vehicle`
  ADD PRIMARY KEY (`vehicle_number`),
  ADD KEY `resident_id` (`resident_id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `parking_records`
--
ALTER TABLE `parking_records`
  MODIFY `record_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=76;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `parked_vehicle`
--
ALTER TABLE `parked_vehicle`
  ADD CONSTRAINT `parked_vehicle_ibfk_1` FOREIGN KEY (`vehicle_number`) REFERENCES `vehicle` (`vehicle_number`) ON DELETE CASCADE;

--
-- Constraints for table `parking_records`
--
ALTER TABLE `parking_records`
  ADD CONSTRAINT `fk_vehicle_number` FOREIGN KEY (`vehicle_number`) REFERENCES `vehicle` (`vehicle_number`) ON DELETE SET NULL;

--
-- Constraints for table `spare_parked_vehicle`
--
ALTER TABLE `spare_parked_vehicle`
  ADD CONSTRAINT `spare_parked_vehicle_ibfk_1` FOREIGN KEY (`vehicle_number`) REFERENCES `vehicle` (`vehicle_number`) ON DELETE CASCADE;

--
-- Constraints for table `vehicle`
--
ALTER TABLE `vehicle`
  ADD CONSTRAINT `vehicle_ibfk_1` FOREIGN KEY (`resident_id`) REFERENCES `residents` (`resident_id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
