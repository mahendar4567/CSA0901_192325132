CREATE DATABASE IF NOT EXISTS transport;
USE transport;

CREATE TABLE IF NOT EXISTS bus_pass (
    pass_id VARCHAR(30) PRIMARY KEY,
    reg_no VARCHAR(30) NOT NULL,
    name VARCHAR(120) NOT NULL,
    route_no INT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_bus_pass_reg_no (reg_no),
    INDEX idx_bus_pass_route_no (route_no),
    CHECK (route_no > 0),
    CHECK (amount >= 0)
) ENGINE=InnoDB;
