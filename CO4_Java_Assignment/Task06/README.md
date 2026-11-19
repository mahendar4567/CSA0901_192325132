# Q6 – Campus Event Registration Desktop Application

Compile: `javac RegistrationDAO.java RegistrationService.java RegistrationFrame.java`  
Run with MySQL Connector/J on the classpath: `java RegistrationFrame`

1. Execute `campus_events.sql` in MySQL.
2. Configure the JDBC URL, username and password in `RegistrationFrame.java`.
3. Add MySQL Connector/J to the runtime classpath.
4. Register a student for an event; duplicate `(student_id,event_id)` pairs are rejected by the database unique constraint.
5. Use the committee filter, Prev/Next page controls and Export CSV.

Database calls run in SwingWorker. The table loads 100 rows per page using SQL LIMIT/OFFSET. The CSV export writes the currently displayed filtered page.
