import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DbConfig {
    private DbConfig() {}
    public static final String URL = System.getenv().getOrDefault(
            "DB_URL", "jdbc:mysql://localhost:3306/transport");
    public static final String USER = System.getenv().getOrDefault("DB_USER", "root");
    public static final String PASSWORD = System.getenv().getOrDefault("DB_PASSWORD", "");
    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
