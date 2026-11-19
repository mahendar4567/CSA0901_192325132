import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RegistrationDAO {
    private final String url;
    private final String user;
    private final String password;

    public RegistrationDAO(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    public void register(String studentId, String studentName, int eventId) throws SQLException {
        String studentSql = "INSERT INTO students(student_id, student_name) VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE student_name = VALUES(student_name)";
        String registrationSql =
                "INSERT INTO registrations(student_id, event_id) VALUES (?, ?)";
        try (Connection con = connect()) {
            con.setAutoCommit(false);
            try (PreparedStatement student = con.prepareStatement(studentSql);
                 PreparedStatement registration = con.prepareStatement(registrationSql)) {
                student.setString(1, studentId);
                student.setString(2, studentName);
                student.executeUpdate();

                registration.setString(1, studentId);
                registration.setInt(2, eventId);
                registration.executeUpdate();
                con.commit();
            } catch (SQLException ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    public List<String[]> findPage(Integer eventId, int limit, int offset) throws SQLException {
        String base = "SELECT r.registration_id, r.student_id, s.student_name, " +
                "e.event_name, r.registered_at FROM registrations r " +
                "JOIN students s ON r.student_id=s.student_id " +
                "JOIN events e ON r.event_id=e.event_id ";
        String where = eventId == null ? "" : "WHERE r.event_id=? ";
        String sql = base + where + "ORDER BY r.registration_id DESC LIMIT ? OFFSET ?";
        List<String[]> rows = new ArrayList<String[]>();
        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(sql)) {
            int index = 1;
            if (eventId != null) ps.setInt(index++, eventId.intValue());
            ps.setInt(index++, limit);
            ps.setInt(index, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new String[] {
                        String.valueOf(rs.getLong("registration_id")),
                        rs.getString("student_id"),
                        rs.getString("student_name"),
                        rs.getString("event_name"),
                        String.valueOf(rs.getTimestamp("registered_at"))
                    });
                }
            }
        }
        return rows;
    }

    public List<String[]> findAllEvents() throws SQLException {
        List<String[]> events = new ArrayList<String[]>();
        String sql = "SELECT event_id, event_name FROM events ORDER BY event_name";
        try (Connection con = connect();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                events.add(new String[] {
                    String.valueOf(rs.getInt("event_id")), rs.getString("event_name")
                });
            }
        }
        return events;
    }
}
