# Evidence to add before submission

Capture genuine execution screenshots and preserve raw output/timing notes.

- **B1:** PassStore normal find, missing ID, malformed line; line-counter runs at 1 MiB, 10 MiB, 50 MiB; lookup scaling at 1k, 10k, 50k, 100k.
- **B2:** isolated test database setup; unsafe and safe login results for normal invalid credentials and crafted test ID; damaged file cases.
- **B3:** table metadata and row counts; timings for three methods at 1k, 10k, 100k; auto-commit failure vs transaction rollback; verify row count after rollback.
- **B4:** migration output, reject report, DatabaseMetaData output and screenshots of final schema/tables.

Record JDK, OS, CPU/RAM, database version, JDBC driver version, filesystem, number of repetitions and whether database is local. Do not invent or estimate observed benchmark numbers.
