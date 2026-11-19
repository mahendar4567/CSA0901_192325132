import java.sql.SQLException;
import java.util.List;

public class RegistrationService {
    private final RegistrationDAO dao;

    public RegistrationService(RegistrationDAO dao) {
        this.dao = dao;
    }

    public void register(String studentId, String studentName, int eventId) throws SQLException {
        if (studentId == null || studentId.trim().isEmpty())
            throw new IllegalArgumentException("Student ID is required");
        if (studentName == null || studentName.trim().isEmpty())
            throw new IllegalArgumentException("Student name is required");
        if (eventId <= 0)
            throw new IllegalArgumentException("Select a valid event");
        dao.register(studentId.trim(), studentName.trim(), eventId);
    }

    public List<String[]> findPage(Integer eventId, int pageSize, int offset)
            throws SQLException {
        return dao.findPage(eventId, pageSize, offset);
    }

    public List<String[]> findAllEvents() throws SQLException {
        return dao.findAllEvents();
    }
}
