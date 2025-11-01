-- Database Schema for User Entity
-- MySQL Database

CREATE DATABASE IF NOT EXISTS territory_db;
USE territory_db;

-- Users Table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    active BOOLEAN DEFAULT TRUE,
    role VARCHAR(20) DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_active (active),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Sample Data
INSERT INTO users (username, email, password, first_name, last_name, role, created_by) VALUES
('admin', 'admin@example.com', 'password123', 'Admin', 'User', 'ADMIN', 'system'),
('john_doe', 'john.doe@example.com', 'password123', 'John', 'Doe', 'USER', 'system'),
('jane_smith', 'jane.smith@example.com', 'password123', 'Jane', 'Smith', 'USER', 'system'),
('manager', 'manager@example.com', 'password123', 'Manager', 'User', 'MANAGER', 'system'),
('bob_wilson', 'bob.wilson@example.com', 'password123', 'Bob', 'Wilson', 'USER', 'system');

-- Territories Table (example for territory persistence)
CREATE TABLE IF NOT EXISTS territories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    parent_id BIGINT,
    level INT DEFAULT 0,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    INDEX idx_code (code),
    INDEX idx_parent_id (parent_id),
    INDEX idx_level (level),
    INDEX idx_active (active),
    FOREIGN KEY (parent_id) REFERENCES territories(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Sample Territory Data
INSERT INTO territories (code, name, description, level, created_by) VALUES
('ID', 'Indonesia', 'Country level', 0, 'system'),
('ID-JKT', 'Jakarta', 'Province level', 1, 'system'),
('ID-JKT-PST', 'Jakarta Pusat', 'City level', 2, 'system'),
('ID-JKT-SLT', 'Jakarta Selatan', 'City level', 2, 'system');

UPDATE territories SET parent_id = (SELECT id FROM (SELECT * FROM territories) AS t WHERE code = 'ID') WHERE code IN ('ID-JKT');
UPDATE territories SET parent_id = (SELECT id FROM (SELECT * FROM territories) AS t WHERE code = 'ID-JKT') WHERE code IN ('ID-JKT-PST', 'ID-JKT-SLT');

