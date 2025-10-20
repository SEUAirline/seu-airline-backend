CREATE DATABASE IF NOT EXISTS seu_airline CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE seu_airline;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    full_name VARCHAR(100),
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(20),
    role VARCHAR(20) NOT NULL,
    status TINYINT DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS airlines (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(10) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    logo_url VARCHAR(255),
    description TEXT,
    contact_number VARCHAR(20),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS airports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(10) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    city VARCHAR(50) NOT NULL,
    country VARCHAR(50) NOT NULL,
    timezone VARCHAR(50),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

INSERT INTO users (username, password, full_name, email, phone, role, status) 
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTBv.09h90p8tV98a4OVaHfQn5L34Fxy', 'Admin', 'admin@example.com', '13800138000', 'ADMIN', 1)
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

INSERT INTO users (username, password, full_name, email, phone, role, status) 
VALUES ('user1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTBv.09h90p8tV98a4OVaHfQn5L34Fxy', 'Test User', 'user@example.com', '13900139000', 'PASSENGER', 1)
ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

SHOW TABLES;
SELECT id, username, role, status FROM users;