import java.sql.*;
import java.util.List;

public class PassLoader {
    /** Loads all passes in one transaction, in batches of 1,000 rows. */
    public static int loadAll(Connection con, List<String[]> passes) throws SQLException {
        String sql = "INSERT INTO bus_pass(pass_id, reg_no, name, route_no, amount) " +
                "VALUES (?, ?, ?, ?, ?)";
        boolean oldAutoCommit = con.getAutoCommit();
        con.setAutoCommit(false);
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            int n = 0;
            for (String[] p : passes) {
                ps.setString(1, p[0]);
                ps.setString(2, p[1]);
                ps.setString(3, p[2]);
                ps.setInt(4, Integer.parseInt(p[3]));
                ps.setBigDecimal(5, new java.math.BigDecimal(p[4]));
                ps.addBatch();
                n++;
                if (n % 1000 == 0) ps.executeBatch();
            }
            ps.executeBatch();
            con.commit();
            return n;
        } catch (SQLException | RuntimeException ex) {
            try { con.rollback(); }
            catch (SQLException rollbackError) { ex.addSuppressed(rollbackError); }
            throw ex;
        } finally {
            con.setAutoCommit(oldAutoCommit);
        }
    }

    public static int insertWithStatement(Connection con, List<String[]> rows)
            throws SQLException {
        boolean old = con.getAutoCommit();
        con.setAutoCommit(false);
        int count = 0;
        try (Statement st = con.createStatement()) {
            for (String[] p : rows) {
                String sql = "INSERT INTO bus_pass(pass_id,reg_no,name,route_no,amount) VALUES (" +
                        quote(p[0]) + "," + quote(p[1]) + "," + quote(p[2]) + "," +
                        Integer.parseInt(p[3]) + "," + new java.math.BigDecimal(p[4]) + ")";
                st.executeUpdate(sql);
                count++;
            }
            con.commit();
            return count;
        } catch (SQLException | RuntimeException ex) {
            try { con.rollback(); } catch (SQLException e) { ex.addSuppressed(e); }
            throw ex;
        } finally {
            con.setAutoCommit(old);
        }
    }

    public static int insertWithPrepared(Connection con, List<String[]> rows)
            throws SQLException {
        boolean old = con.getAutoCommit();
        con.setAutoCommit(false);
        int count = 0;
        String sql = "INSERT INTO bus_pass(pass_id,reg_no,name,route_no,amount) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (String[] p : rows) {
                bind(ps, p);
                ps.executeUpdate();
                count++;
            }
            con.commit();
            return count;
        } catch (SQLException | RuntimeException ex) {
            try { con.rollback(); } catch (SQLException e) { ex.addSuppressed(e); }
            throw ex;
        } finally {
            con.setAutoCommit(old);
        }
    }

    private static void bind(PreparedStatement ps, String[] p) throws SQLException {
        ps.setString(1, p[0]);
        ps.setString(2, p[1]);
        ps.setString(3, p[2]);
        ps.setInt(4, Integer.parseInt(p[3]));
        ps.setBigDecimal(5, new java.math.BigDecimal(p[4]));
    }

    private static String quote(String value) {
        return "'" + value.replace("'", "''") + "'";
    }
}
