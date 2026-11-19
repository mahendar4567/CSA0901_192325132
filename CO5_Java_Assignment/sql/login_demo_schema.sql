CREATE DATABASE IF NOT EXISTS transport;
USE transport;

CREATE TABLE IF NOT EXISTS demo_users (
    id VARCHAR(40) PRIMARY KEY,
    pwd VARCHAR(100) NOT NULL
) ENGINE=InnoDB;

-- Test-only account. Do not use real credentials or deploy this table.
INSERT INTO demo_users(id, pwd) VALUES ('student1', 'pass123')
ON DUPLICATE KEY UPDATE pwd = VALUES(pwd);
