CREATE DATABASE IF NOT EXISTS campus_events;
USE campus_events;

CREATE TABLE IF NOT EXISTS students (
    student_id VARCHAR(20) PRIMARY KEY,
    student_name VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS events (
    event_id INT PRIMARY KEY AUTO_INCREMENT,
    event_name VARCHAR(120) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS registrations (
    registration_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id VARCHAR(20) NOT NULL,
    event_id INT NOT NULL,
    registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reg_student FOREIGN KEY (student_id)
        REFERENCES students(student_id),
    CONSTRAINT fk_reg_event FOREIGN KEY (event_id)
        REFERENCES events(event_id),
    CONSTRAINT uq_student_event UNIQUE (student_id, event_id)
);

INSERT INTO events(event_name) VALUES
('Tech Fest'), ('Cultural Fest'), ('Sports Meet')
ON DUPLICATE KEY UPDATE event_name = VALUES(event_name);

INSERT INTO students(student_id, student_name) VALUES
('STU001', 'Aarav Kumar'),
('STU002', 'Meera Ravi'),
('STU003', 'Rahul Das')
ON DUPLICATE KEY UPDATE student_name = VALUES(student_name);
