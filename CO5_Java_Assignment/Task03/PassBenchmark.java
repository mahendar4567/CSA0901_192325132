import java.sql.*;
import java.util.*;

public class PassBenchmark {
    private static final String URL = System.getenv().getOrDefault(
            "DB_URL", "jdbc:mysql://localhost:3306/transport");
    private static final String USER = System.getenv().getOrDefault("DB_USER", "root");
    private static final String PASSWORD = System.getenv().getOrDefault("DB_PASSWORD", "");

    private static List<String[]> generate(int n, boolean invalidMiddle) {
        List<String[]> rows = new ArrayList<String[]>(n);
        for (int i = 1; i <= n; i++) {
            String amount = invalidMiddle && i == n / 2 ? "not-a-number" : "4500.00";
            rows.add(new String[]{"P" + n + "_" + i, "19231" + (i % 100000),
                    "Student" + i, String.valueOf((i % 20) + 1), amount});
        }
        return rows;
    }

    private static void clear(Connection con) throws SQLException {
        try (Statement st = con.createStatement()) { st.executeUpdate("DELETE FROM bus_pass"); }
    }

    private static long time(Connection con, String method, List<String[]> rows) throws Exception {
        clear(con);
        long start = System.nanoTime();
        if ("Statement".equals(method)) PassLoader.insertWithStatement(con, rows);
        else if ("Prepared".equals(method)) PassLoader.insertWithPrepared(con, rows);
        else PassLoader.loadAll(con, rows);
        return (System.nanoTime() - start) / 1_000_000;
    }

    private static void metadata(Connection con) throws SQLException {
        String sql = "SELECT pass_id, reg_no, name, route_no, amount FROM bus_pass";
        try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            ResultSetMetaData md = rs.getMetaData();
            for (int i = 1; i <= md.getColumnCount(); i++)
                System.out.println(md.getColumnName(i) + " : " + md.getColumnTypeName(i));
            int count = 0;
            while (rs.next()) count++;
            System.out.println("Rows = " + count);
        }
    }

    public static void main(String[] args) throws Exception {
        try (Connection con = DriverManager.getConnection(URL, USER, PASSWORD)) {
            for (int n : new int[]{1000, 10000, 100000}) {
                List<String[]> rows = generate(n, false);
                System.out.println("Rows=" + n);
                System.out.println("Statement ms=" + time(con, "Statement", rows));
                System.out.println("PreparedStatement ms=" + time(con, "Prepared", rows));
                System.out.println("Batch ms=" + time(con, "Batch", rows));
            }
            metadata(con);

            clear(con);
            try {
                PassLoader.loadAll(con, generate(1000, true));
                System.out.println("Unexpected: invalid batch succeeded");
            } catch (RuntimeException ex) {
                System.out.println("Invalid input caused rollback: " + ex.getMessage());
            }
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM bus_pass")) {
                rs.next();
                System.out.println("Rows after rollback = " + rs.getInt(1));
            }
        }
    }
}
