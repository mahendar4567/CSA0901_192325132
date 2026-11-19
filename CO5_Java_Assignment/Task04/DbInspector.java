import java.sql.*;

public class DbInspector {
    public static void inspect(Connection con) throws SQLException {
        DatabaseMetaData md = con.getMetaData();
        System.out.println("Database: " + md.getDatabaseProductName());
        System.out.println("Database version: " + md.getDatabaseProductVersion());
        System.out.println("Driver: " + md.getDriverName());
        System.out.println("Driver version: " + md.getDriverVersion());
        try (ResultSet rs = md.getTables(con.getCatalog(), null, "%", new String[]{"TABLE"})) {
            while (rs.next()) System.out.println("Table: " + rs.getString("TABLE_NAME"));
        }
    }

    public static void main(String[] args) throws Exception {
        try (Connection con = DbConfig.connect()) {
            inspect(con);
        }
    }
}
