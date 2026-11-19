# CO4 Java Assignment 4 – Event-Driven GUI Design

**Student:** Abiyan Yousuf V  
**Register Number:** 192525418  
**Course:** Programming in Java (CSA09)  
**Course Outcome:** CO4 – Event-driven GUI design  
**Java:** JDK 8 or later  
**Repository:** `CSA09-192525418-Assignment4`

## Project structure
- `Assignment_Report.md` – solutions, event tables, pseudocode, design decisions and complexity comparisons.
- `Task01` – fee-payment GUI redesign and FeeService.
- `Task02` – hospital outpatient appointment desk.
- `Task03` – campus library search screen and layout resizing.
- `Task04` – persistent drawing pad with mouse events.
- `Task05` – currency converter in four event-handling styles.
- `Task06` – campus event registration Swing/JDBC application and SQL schema.

## Compile instructions
Compile each task from its own folder:

| Task | Compile | Run |
|---|---|---|
| Q1 | `javac FeeService.java FeeWindow.java` | `java FeeWindow` |
| Q2 | `javac AppointmentDesk.java` | `java AppointmentDesk` |
| Q3 | `javac LibrarySearch.java` | `java LibrarySearch` |
| Q4 | `javac DrawPad.java` | `java DrawPad` |
| Q5 | `javac Converter.java ConverterA.java ConverterB.java ConverterC.java` | `java Converter` (or run A/B/C) |
| Q6 | `javac RegistrationDAO.java RegistrationService.java RegistrationFrame.java` | `java RegistrationFrame` |

## Database setup for Q1 and Q6
1. Install MySQL and create the databases/tables using `Task01/fee_schema.sql` and `Task06/campus_events.sql`.
2. Add MySQL Connector/J to the classpath when running the JDBC applications.
3. Replace sample JDBC URL, username and password with local values. Do not commit real credentials.
4. For Q6, seed the `students` and `events` tables before testing registrations.

Example classpath (Windows):
`java -cp ".;mysql-connector-j.jar" RegistrationFrame`

## Submission checklist
- Run every task individually and add screenshots from your own computer.
- For Q3, capture three window sizes.
- For Q4, capture before and after minimizing/restoring.
- For Q5, test valid and invalid amounts and Enter-key conversion in each style.
- For Q6, test valid, invalid, duplicate, filtered and CSV-export cases.
- Add the actual GitHub repository URL to this README and report.
- Commit progressively, at least one commit per task.
- This is an individual assignment: verify that all code and report content are understood and can be explained in the viva.

**Note:** The assignment's conversion rates are illustrative, not live exchange rates. GUI programs require a desktop environment. JDBC programs require a configured MySQL server and Connector/J.
