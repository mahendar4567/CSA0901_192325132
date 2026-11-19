import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class FeeService {
    private final String url;
    private final String user;
    private final String password;

    public FeeService(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    public String pay(String regNo, double amount) throws SQLException {
        if (regNo == null || regNo.trim().isEmpty())
            throw new IllegalArgumentException("Registration number is required");
        if (amount <= 0)
            throw new IllegalArgumentException("Amount must be positive");

        String sql = "INSERT INTO fees(reg_no, amount) VALUES (?, ?)";
        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, regNo.trim());
            statement.setDouble(2, amount);
            statement.executeUpdate();
        }
        return "Payment recorded for " + regNo.trim() + ": Rs. " + amount;
    }
}
