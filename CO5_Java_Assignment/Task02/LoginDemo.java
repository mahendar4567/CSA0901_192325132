import java.sql.*;
import java.util.Scanner;

public class LoginDemo {
    private static final String URL = System.getenv().getOrDefault(
            "DB_URL", "jdbc:mysql://localhost:3306/transport");
    private static final String USER = System.getenv().getOrDefault("DB_USER", "root");
    private static final String PASSWORD = System.getenv().getOrDefault("DB_PASSWORD", "");

    // Intentionally vulnerable: use only in the isolated local assignment database.
    static boolean unsafeLogin(Connection con, String id, String password) throws SQLException {
        String sql = "SELECT id FROM demo_users WHERE id='" + id +
                "' AND pwd='" + password + "'";
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            return rs.next();
        }
    }

    static boolean safeLogin(Connection con, String id, String password) throws SQLException {
        String sql = "SELECT id FROM demo_users WHERE id=? AND pwd=?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, id);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD);
             Scanner scanner = new Scanner(System.in)) {
            System.out.print("User ID (enter test value or SQL test string): ");
            String id = scanner.nextLine();
            System.out.print("Password: ");
            String password = scanner.nextLine();

            System.out.println("Unsafe result: " +
                    (unsafeLogin(con, id, password) ? "Login accepted" : "Login rejected"));
            System.out.println("PreparedStatement result: " +
                    (safeLogin(con, id, password) ? "Login accepted" : "Login rejected"));
        }
    }
}
