# B3 – Statement vs PreparedStatement vs Batch

Compile: `javac PassLoader.java PassBenchmark.java`  
Run with MySQL Connector/J: `java -cp ".;mysql-connector-j.jar" PassBenchmark`

Execute `sql/bus_pass_schema.sql` first. The benchmark clears the bus_pass table between approaches and tests 1,000, 10,000 and 100,000 generated rows. It also prints ResultSetMetaData and tests rollback by inserting a non-numeric amount halfway through a batch. Run only on a disposable assignment test database because the benchmark deletes table rows.
