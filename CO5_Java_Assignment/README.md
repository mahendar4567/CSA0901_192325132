# CO5 Java Assignment 5 – Data Persistence Engineering

**Student:** Abiyan Yousuf V  
**Register Number:** 192525418  
**Course:** Programming in Java (CSA09)  
**Course Outcome:** CO5 – Java I/O, serialization and JDBC  
**Repository name:** `CSA09-192525418-Assignment5`  
**Java:** JDK 11 or later recommended (uses `Files.writeString`)

## Project contents
- `Assignment_Report.md` – Part A design answers, pseudocode, alternatives and Part B implementation/analysis guide.
- `Task01` – bus-pass delimited file store, damaged-record handling, buffered/unbuffered line counter, growth benchmark.
- `Task02` – controlled SQL injection demonstration: unsafe Statement and safe PreparedStatement.
- `Task03` – JDBC Statement, PreparedStatement and batch insertion benchmark, transaction rollback test.
- `Task04` – migration from CSV file to MySQL in batches and DatabaseMetaData inspection.
- `sql` – MySQL schema for bus passes and isolated login demonstration.
- `input` – sample bus-pass CSV.

## Compile and run
Run commands from the task folder. For JDBC tasks, include MySQL Connector/J in the classpath and configure credentials using environment variables `DB_URL`, `DB_USER`, `DB_PASSWORD`.

| Task | Compile | Run |
|---|---|---|
| B1 | `javac PassStore.java LineCounter.java PassStoreTest.java` | `java PassStoreTest` and `java LineCounter` |
| B2 | `javac LoginDemo.java` | `java LoginDemo` |
| B3 | `javac PassLoader.java PassBenchmark.java` | `java PassBenchmark` |
| B4 | `javac PassLoader.java Migrator.java DbInspector.java` | `java Migrator input/passes.csv rejects.csv` and `java DbInspector` |

Example Windows runtime classpath:
`java -cp ".;mysql-connector-j.jar" PassBenchmark`

Example Linux/macOS runtime classpath:
`java -cp ".:mysql-connector-j.jar" PassBenchmark`

## Database setup
1. Execute `sql/bus_pass_schema.sql` in MySQL.
2. Execute `sql/login_demo_schema.sql` only for the isolated B2 demonstration.
3. Set `DB_URL` (default in code: `jdbc:mysql://localhost:3306/transport`), `DB_USER` and `DB_PASSWORD`.
4. Do not commit real credentials. Use a local test database only for the deliberately unsafe SQL injection demonstration.

## Important assignment evidence
The assignment requires actual measurements and screenshots. Run the programs on your own machine and record:
- B1 line-count timings for 1 MB, 10 MB and 50 MB, both byte-at-a-time and buffered.
- B1 lookup timings for 1,000 to 100,000 records.
- B2 observed unsafe and safe login outcomes plus damaged-file cases.
- B3 timings for Statement, PreparedStatement and batch at 1,000, 10,000 and 100,000 rows, including auto-commit/transaction failure test.
- B4 DatabaseMetaData output and migration reject report.

No benchmark values are fabricated in this project. Timing tables in the report are intentionally left blank for your actual execution results.
