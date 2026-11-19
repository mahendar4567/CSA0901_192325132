# B2 – Adversarial SQL Input

Compile: `javac LoginDemo.java`  
Run with MySQL Connector/J: `java -cp ".;mysql-connector-j.jar" LoginDemo`

Execute `sql/login_demo_schema.sql` first. This is a deliberately unsafe demonstration and must be run only against the isolated local test database. Test a normal invalid login and an input such as `' OR '1'='1' -- `, then compare the unsafe and PreparedStatement outputs. SQL comment behaviour may vary by dialect; report actual results.
