-- =========================================================================
-- FileMS - Municipal Maintenance Department
-- Database Deployment Script
-- =========================================================================

-- 1. Create the Database
CREATE DATABASE IF NOT EXISTS filems_db;
USE filems_db;

-- 2. Drop existing tables (Must drop CourtCases first due to Foreign Keys)
DROP TABLE IF EXISTS CourtCases;
DROP TABLE IF EXISTS Persons;
DROP TABLE IF EXISTS Judges;

-- =========================================================================
-- TABLE CREATION
-- =========================================================================

-- 3. Create Judges Table (Mapped from Person -> Judge)
CREATE TABLE Judges (
    judge_id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL
);

-- 4. Create Persons Table (Mapped from Person -> InvolvedParty)
CREATE TABLE Persons (
    person_id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    dob DATE
);

-- 5. Create CourtCases Table (The core domain object)
CREATE TABLE CourtCases (
    case_number VARCHAR(50) PRIMARY KEY,
    applicant_id INT,
    respondent_id INT,
    child_id INT,
    judge_id INT,
    court_order TEXT,
    order_date DATE,
    
    -- Foreign Key Constraints linking back to Persons and Judges
    CONSTRAINT fk_applicant FOREIGN KEY (applicant_id) REFERENCES Persons(person_id) ON DELETE SET NULL,
    CONSTRAINT fk_respondent FOREIGN KEY (respondent_id) REFERENCES Persons(person_id) ON DELETE SET NULL,
    CONSTRAINT fk_child FOREIGN KEY (child_id) REFERENCES Persons(person_id) ON DELETE SET NULL,
    CONSTRAINT fk_judge FOREIGN KEY (judge_id) REFERENCES Judges(judge_id) ON DELETE SET NULL
);

-- =========================================================================
-- INITIAL TEST DATA (Optional - for Panel Demonstration)
-- =========================================================================

-- Insert Sample Judges
INSERT INTO Judges (first_name, last_name) VALUES 
('Marcus', 'Holloway'),
('Elena', 'Rostova');

-- Insert Sample Involved Parties
INSERT INTO Persons (first_name, last_name, dob) VALUES 
('Sarah', 'Jenkins', '1985-04-12'),
('TechCorp', 'Industries', NULL),
('Liam', 'Jenkins', '2015-08-22');

-- Insert Sample Court Case
INSERT INTO CourtCases (case_number, applicant_id, respondent_id, child_id, judge_id, court_order, order_date) VALUES 
('CV-2026-0042', 1, 2, NULL, 1, 'Motion to Dismiss Denied. Proceed to discovery phase.', '2026-04-10'),
('FM-2026-0118', 1, 2, 3, 2, 'Temporary custody arrangement established pending final review.', '2026-04-14');