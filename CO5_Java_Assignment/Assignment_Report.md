# Programming in Java – Assignment 5 (CO5)
## Data Persistence Engineering – Files and Databases

**Name:** Abiyan Yousuf V  
**Register Number:** 192525418  
**GitHub Repository:** Add the working URL for `CSA09-192525418-Assignment5`  
**Java version:** Record the JDK used for execution.

This report follows the uploaded 27-page CO5 assignment. Part A covers A1–A5; Part B covers B1–B4. The assignment requires actual execution evidence and measured results. Do not replace the blank measurement fields with invented values.

---

# PART A – PERSISTENCE DESIGN & PSEUDOCODE

## A1 – Design a File Format: Transport Office Bus-Pass Records

### A1.1 Record layout

**Delimited text (UTF-8; one CSV record per logical row):**

`passId,regNo,name,routeNo,boardingStop,validityDate,amountPaid`

Example: `P1001,192311001,Arun,12,Central,2026-12-31,4500.00`

Field order: pass ID, register number, name, route number, boarding stop, validity date (`YYYY-MM-DD`), amount paid. Use a CSV-aware parser/writer that quotes commas, quotes and line breaks, doubling embedded quote characters. Do not parse quoted CSV using `String.split(",")`. The included sample uses simple fields only.

**Binary DataOutputStream layout:**
1. `writeUTF(passId)`
2. `writeUTF(regNo)`
3. `writeUTF(name)`
4. `writeInt(routeNo)`
5. `writeUTF(boardingStop)`
6. `writeUTF(validityDate)`
7. `writeDouble(amountPaid)`

The reader must use the same order and types. `writeUTF` is length-prefixed modified UTF-8, not fixed width. A version header and record count/record lengths help detect incompatible or truncated files.

### A1.2 Text file operations – SIMATS-style pseudocode

**Algorithm: AddNewPass**
```
BEGIN
  VALIDATE all required fields and numeric ranges
  ENCODE the record as one correctly escaped CSV row
  OPEN file using UTF-8 BufferedWriter in CREATE + APPEND mode
  WRITE the encoded row
  WRITE a line separator
  FLUSH and CLOSE the writer
  REPORT successful addition
END
```

**Algorithm: FindPassByID**
```
BEGIN
  IF file does not exist THEN
    REPORT "Pass file not found"
    RETURN NOT_FOUND
  END IF
  OPEN file using UTF-8 BufferedReader
  FOR EACH line in file
    IF line is blank THEN
      RECORD damaged-line warning
      CONTINUE
    END IF
    PARSE line using CSV-aware parser
    IF parsing fails OR field count is not 7 THEN
      RECORD line number as damaged
      CONTINUE
    END IF
    VALIDATE field types and values
    IF passId equals requested ID THEN
      RETURN parsed pass
    END IF
  END FOR
  RETURN NOT_FOUND
END
```

**Algorithm: ListPassesByRoute**
```
BEGIN
  INITIALIZE empty result list
  IF file does not exist THEN
    REPORT "Pass file not found"
    RETURN empty result
  END IF
  OPEN file using UTF-8 BufferedReader
  FOR EACH line in file
    PARSE and validate the line
    IF line is damaged THEN
      RECORD warning and CONTINUE
    END IF
    IF routeNo equals requested route THEN
      ADD pass to result list
    END IF
  END FOR
  RETURN result list
END
```

### A1.3 Damaged data and recovery

| Condition | Detection | Handling |
|---|---|---|
| Damaged line | CSV parse error, wrong field count, invalid date/number or missing required value | Skip the line and log line number/reason; do not silently accept it. |
| Missing file | `Files.exists(path)` or `NoSuchFileException` | Show a clear error; create a file only for an explicit add/create operation. |
| Incomplete write | Truncated final line, missing newline, invalid record length/checksum | Keep backup/journal; write a replacement to a temporary file and atomically move it where supported; validate/ignore incomplete append tail. |

For stronger integrity, add record length/checksum, backup and compacted snapshots. A successful append is not automatically guaranteed durable after sudden power loss.

**Alternative – RandomAccessFile:** Fixed-length record i can be read by `seek(i × recordSize)` in O(1) when its index is known. Finding by ID still requires O(n) scan unless an ID-to-index index is maintained. Padding wastes space for short fields; oversized values require truncation or overflow handling. A hash index costs O(n) space and average O(1) lookup after O(n) construction.

## A2 – Don’t Read What You Don’t Need

### A2.1 Predicted cost
A 50 MiB file contains 52,428,800 bytes. Calling `FileInputStream.read()` once per byte causes about 52.4 million Java-level calls, plus the final EOF call. This adds method-call and loop overhead; truly unbuffered access may also cause many OS reads.

### A2.2 Buffered redesign
Use `BufferedReader.readLine()` for text logs or a 64 KiB `BufferedInputStream` byte array. For 50 MiB, 64 KiB chunks require roughly 800 buffer-fill reads rather than 52 million single-byte calls. The byte version counts LF bytes and is suitable for UTF-8/ASCII-compatible line separators; CRLF contains one LF.

### A2.3 When buffering gives little benefit
It adds little benefit for a tiny one-time read or a source already delivering large in-memory blocks. It may add latency if every small interactive record must be flushed immediately.

**Alternative – `Files.readAllLines()`:** Both approaches take O(N) time in file bytes. `readAllLines()` stores all lines, requiring O(N) memory plus String/List overhead. Buffered streaming uses O(B) working memory for buffer size B, excluding any retained results. Use `readAllLines()` for small files that comfortably fit memory; stream large logs.

## A3 – Discover the Data-Access Design Pattern

### A3.1 Common pattern
Repeated steps: obtain JDBC connection, create statement, bind/build SQL, execute, iterate ResultSet, map columns to objects, handle SQLException, and close ResultSet/Statement/Connection. Differences: entity table/columns, key type, SQL and mapping.

### A3.2 Generic DAO design
```
interface Dao<T, K> {
  K insert(T value) throws SQLException;
  Optional<T> findById(K id) throws SQLException;
  List<T> findAll() throws SQLException;
  boolean update(T value) throws SQLException;
  boolean delete(K id) throws SQLException;
}
```
`StudentDao implements Dao<Student,Integer>` and owns Student-specific SQL and ResultSet mapping. Inject a `ConnectionProvider` (DriverManager or preferably a connection pool).

### A3.3 Object-to-table mapping

| Java field | SQL column | Mapping / constraint |
|---|---|---|
| `Student.id` (`int`) | `student.id INT` | Primary key; generated or assigned ID |
| `Student.name` (`String`) | `student.name VARCHAR(100)` | Required text |
| `Student.email` (`String`) | `student.email VARCHAR(150)` | Optional or UNIQUE depending on requirements |

**Alternative – SQL in service/GUI:** Renaming a column can require edits in many classes. DAO centralizes schema-specific SQL and mapping. Runtime Big-O per operation is essentially the same; database query cost dominates and DAO adds negligible method-call overhead. Returned-data space is also effectively the same.

## A4 – What Should Be Serialized?

### A4.1 Fields and object graph
The live `java.sql.Connection` must be `transient`: it is an active external resource and cannot be restored as a live connection from an object snapshot. A password should not be serialized; preferably do not retain plaintext and store a salted password hash in the database. If the legacy field remains, mark it transient.

When Hostel is serialized, Java follows references Hostel → Room list → Resident list and serializes reachable non-static, non-transient fields. Every reachable object must be serializable or `NotSerializableException` can occur. Shared object references are preserved. Transient connection/password fields are skipped.

### A4.2 Schema change and serialVersionUID
With the same explicit `serialVersionUID`, an older stream can generally be read by a newer class after adding `phoneNumber`; the absent String field receives `null`. A different UID causes `InvalidClassException`. Without an explicit UID, compiler-computed values may change with class edits. Use explicit versioning and migration/default logic when new fields need meaningful values.

**Alternative CSV/JSON:** Text is language-independent and inspectable; serialization directly restores Java object graphs but is Java-specific and tightly coupled to class structure. Flat CSV may be smaller; JSON carries field names/structure and can be larger. Save/load n residents is O(n); storage is O(n). Optional JSON fields can be defaulted by a parser; CSV needs versioned headers/columns. Both need validation/migration.

## A5 – Design for Correctness and Safety

### A5.1 Transaction pseudocode
```
ALGORITHM TransferWallet
BEGIN
  SET amount = 500
  OBTAIN database connection
  SET autoCommit = FALSE
  TRY
    UPDATE hostel_wallet
      SET balance = balance - amount
      WHERE wallet_id = HOSTEL_ID AND balance >= amount
    IF affected row count is not 1 THEN
      RAISE transfer failure
    END IF
    UPDATE canteen_wallet
      SET balance = balance + amount
      WHERE wallet_id = CANTEEN_ID
    IF affected row count is not 1 THEN
      RAISE transfer failure
    END IF
    COMMIT transaction
    REPORT transfer successful
  CATCH any database or validation failure
    ROLLBACK transaction
    REPORT transfer failed
  FINALLY
    RESTORE connection settings and close resources
  END
END
```
Both updates belong in one transaction. The debit condition prevents a negative balance. If either update fails, rollback restores both. Concurrent transfers need safe row locking/update and consistent lock ordering.

### A5.2 SQL injection and PreparedStatement
Unsafe concatenation makes input part of SQL syntax. An ID such as `' OR '1'='1' --` may make the WHERE condition true and comment out the password check, depending on SQL dialect.

Safe structure:
```
String sql = "SELECT id FROM users WHERE id=? AND pwd_hash=?";
PreparedStatement ps = con.prepareStatement(sql);
ps.setString(1, id);
ps.setString(2, passwordHash);
ResultSet rs = ps.executeQuery();
boolean authenticated = rs.next();
```
PreparedStatement treats parameter values as data, not SQL syntax. Store salted password hashes using a modern password-hashing algorithm; never store plaintext passwords. Validation/parameter handling is O(L) for input length L; query cost dominates.

**Alternative – escaping/regex:** Quote escaping is dialect/encoding-sensitive and easy to get wrong. Regex can validate expected identifiers but does not replace binding. PreparedStatement is the structural defence.

---

# PART B – IMPLEMENTATION & EXPERIMENTAL INVESTIGATION

## B1 – Implement and Instrument the File Store

The Java implementation is in `Task01/PassStore.java`, `LineCounter.java` and `PassStoreTest.java`. The store supports add, find and list-by-route, skips damaged records with line-number warnings, and validates amount/route. The assignment's provided `PassStore` TODO method is implemented in the robust parser-based version; the five-field simple CSV sample is intentionally limited to fields without commas.

### B1.1 Validation plan

| Test | Input | Expected behaviour |
|---|---|---|
| Normal add/find | P1001 with valid values | Record is appended and found |
| Missing ID | P9999 | `null` / not found |
| Damaged field count | `P1002,DAMAGED LINE` | Warning; skip row; search continues |
| Non-numeric amount | amount=`abc` | Damaged record warning |
| Empty line | blank line | Skip and warn |
| Route list | route 12 | Return only valid route-12 records |

A damaged line does not stop the search because parsing is isolated per line; malformed records are logged and the loop continues.

### B1.2 Design comparison
The implementation follows the line-by-line BufferedReader plan. It adds explicit field validation and warning reporting. It uses a simple comma-separated parser for this assignment's sample data; use a full CSV library if quoted commas/newlines must be supported.

### B1.3 Buffering measurement

Run `java LineCounter` and record actual values:

| File size | Byte-at-a-time (ms) | Buffered 64 KiB (ms) | Speed-up |
|---|---:|---:|---:|
| 1 MB | ______ | ______ | ______ |
| 10 MB | ______ | ______ | ______ |
| 50 MB | ______ | ______ | ______ |

Use identical generated file content and repeat trials; report median/average and machine/JDK details. The expected trend is fewer read calls and usually faster buffered processing, but actual timings depend on filesystem/cache/runtime.

### B1.4 Lookup growth

| Records | Lookup time (ms) |
|---:|---:|
| 1,000 | ______ |
| 10,000 | ______ |
| 50,000 | ______ |
| 100,000 | ______ |

A file scan is O(n) per lookup. A HashMap built once is O(n) to construct, average O(1) per lookup, and O(n) additional memory. For q searches, repeated scans are O(qn); preloading and searching is average O(n+q), with O(n) memory.

## B2 – Engineer an Adversarial Input

`Task02/LoginDemo.java` contains an unsafe concatenated query and a safe PreparedStatement query. Use only the isolated local database created by `sql/login_demo_schema.sql`. A controlled test input such as `' OR '1'='1' -- ` may alter the unsafe WHERE logic; the safe query binds it as a literal ID and should reject it. Actual unsafe behaviour depends on the SQL dialect; record your observed result.

| Adversarial input | Unsafe design risk | Robust response |
|---|---|---|
| ID contains quote and OR condition | Can alter WHERE logic | Treat as literal parameter |
| Wrong number of pass fields | Index error/incorrect parse | Skip and report |
| Non-numeric amount | NumberFormatException may stop processing | Reject malformed record |
| Empty line | May be treated as a record | Skip and report |

Quote escaping is error-prone and regex validation alone does not protect SQL structure. A well-designed linear regex costs O(L) for input length L; PreparedStatement binding is also proportional to input size, while database query cost dominates.

## B3 – Statement vs PreparedStatement vs Batch

The implementation is in `Task03/PassLoader.java` and `PassBenchmark.java`.

### B3.1 Correctness / metadata
Create the `bus_pass` table using `sql/bus_pass_schema.sql`. Insert 10 rows with each approach, query the table, and inspect `ResultSetMetaData`. Expected logical columns: `pass_id VARCHAR`, `reg_no VARCHAR`, `name VARCHAR`, `route_no INT`, `amount DECIMAL`; actual types depend on the schema.

### B3.2 Benchmark

| Rows | Statement (ms) | PreparedStatement (ms) | PreparedStatement batch (ms) |
|---:|---:|---:|---:|
| 1,000 | ______ | ______ | ______ |
| 10,000 | ______ | ______ | ______ |
| 100,000 | ______ | ______ | ______ |

Use the same generated data, clean table, transaction boundaries, indexes and constraints. Record database/JDBC versions, hardware and whether MySQL is local.

### B3.3 Auto-commit vs transaction
With auto-commit enabled, each successful insert commits independently; a later failure leaves earlier rows. With auto-commit disabled and rollback after failure, no rows from that transaction should remain, assuming a transactional engine such as InnoDB.

### B3.4 Engineering trade-off
Statement requires SQL construction and is unsafe for concatenated user values. PreparedStatement separates SQL from values and supports reuse. Batching reduces round trips. For a nightly 100,000-pass import, use PreparedStatement batches in a transaction with validation, bounded batch size and reject reporting. For one counter-issued pass, use a single PreparedStatement.

Alternative multi-row INSERT reduces round trips but SQL text/parameter count grow with n and huge statements consume memory. LOAD DATA can be efficient for bulk files but depends on server configuration, permissions and cleaning. Batch client memory is O(b) for batch size b; processing is approximately O(n) plus network/database overhead.

## B4 – Final Engineering Decision

### B4.1 Migration
`Task04/Migrator.java` reads the input file sequentially, validates rows, loads valid records in 1,000-row PreparedStatement batches, and writes rejected line numbers/content/reasons to a separate report. The implementation commits each batch independently; for all-or-nothing migration, wrap the entire migration in one transaction, accepting a larger transaction/log footprint.

### B4.2 DatabaseMetaData
`Task04/DbInspector.java` reports database product/version, JDBC driver/version and table names. This helps diagnose server/driver/schema differences after deployment. Metadata is diagnostic, not a replacement for schema migrations.

### B4.3 Production decision
Use a relational database such as MySQL for the 12-campus service. Concurrent counters, shared searches, duplicate prevention, transactions, controlled access and centralized reporting need database connections, indexes, constraints, backups and access control. Text files remain useful for exports/logs/migration input; Java serialization is Java-specific, hard to query and awkward for concurrent updates.

Recommended production design: central database or managed replication, connection pooling, indexes on `pass_id`, `reg_no`, `route_no`, uniqueness constraints where required, transactions, backups, audit logs and secure credential handling.

**Alternative – separate serialized file per campus:** global search must inspect all 12 files, O(N) total records, and combined storage is O(N). Concurrent writes require per-file locking plus conflict-safe synchronization/merge; duplicate IDs and merge conflicts need explicit rules. A shared database offers centralized constraints and transaction handling.

---

## Final execution checklist

- [ ] Run B1 normal, missing ID and damaged-line tests.
- [ ] Measure line counter at 1 MB, 10 MB and 50 MB.
- [ ] Measure lookup growth from 1,000 to 100,000 records.
- [ ] Run B2 against a local test database only; record unsafe and safe outcomes.
- [ ] Run B3 at 1,000, 10,000 and 100,000 rows for all three approaches.
- [ ] Force a mid-batch failure and verify rollback.
- [ ] Run B4 migration and inspect reject report.
- [ ] Capture DatabaseMetaData output.
- [ ] Add screenshots, actual timing values and interpretation.
- [ ] Add working GitHub URL to this report and README.
- [ ] Commit progressively, at least one commit per task.
