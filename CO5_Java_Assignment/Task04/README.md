# B4 – Migration and Database Metadata

Compile: `javac ../Task03/PassLoader.java DbConfig.java DbInspector.java Migrator.java` (run from a combined classpath/build folder) or compile all Java sources together.

Run metadata: `java -cp ".;mysql-connector-j.jar" DbInspector`  
Run migration: `java -cp ".;mysql-connector-j.jar" Migrator input/passes.csv rejects.csv`

The migration reads sequentially, validates five fields, commits each batch of up to 1,000 valid records and records rejected lines. The sample parser is for simple CSV without quoted commas. Record actual metadata output and migration counts.
