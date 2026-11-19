import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class FeeWindow extends JFrame {
    private final JTextField regNo = new JTextField(12);
    private final JTextField amount = new JTextField(10);
    private final JLabel status = new JLabel("Enter payment details");
    private final FeeService service = new FeeService(
            "jdbc:mysql://localhost:3306/college", "root", "your_password");

    public FeeWindow() {
        super("Student Fee Payment");
        setLayout(new GridLayout(4, 2, 8, 8));
        JButton pay = new JButton("Pay");
        JButton clear = new JButton("Clear");
        JButton receipt = new JButton("Receipt");

        add(new JLabel("Register number"));
        add(regNo);
        add(new JLabel("Amount"));
        add(amount);
        add(pay);
        add(clear);
        add(receipt);
        add(status);

        pay.addActionListener(e -> makePayment());
        clear.addActionListener(e -> {
            regNo.setText("");
            amount.setText("");
            status.setText("Fields cleared");
        });
        receipt.addActionListener(e ->
                JOptionPane.showMessageDialog(this, status.getText(), "Receipt",
                        JOptionPane.INFORMATION_MESSAGE));

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
        pack();
        setLocationRelativeTo(null);
    }

    private void makePayment() {
        final String id = regNo.getText().trim();
        final double parsed;
        try {
            parsed = Double.parseDouble(amount.getText().trim());
            if (parsed <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            status.setText("Enter a valid positive amount");
            return;
        }
        if (id.isEmpty()) {
            status.setText("Enter registration number");
            return;
        }

        status.setText("Processing...");
        new SwingWorker<String, Void>() {
            @Override protected String doInBackground() throws Exception {
                return service.pay(id, parsed);
            }
            @Override protected void done() {
                try {
                    status.setText(get());
                } catch (Exception ex) {
                    status.setText("Payment failed");
                    Throwable cause = ex.getCause() == null ? ex : ex.getCause();
                    JOptionPane.showMessageDialog(FeeWindow.this, cause.getMessage(),
                            "Payment error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FeeWindow().setVisible(true));
    }
}
