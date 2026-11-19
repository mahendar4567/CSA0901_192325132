import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ConverterC extends JFrame {
    private final JTextField amount = new JTextField(10);
    private final JComboBox<String> target = new JComboBox<String>(new String[]{"USD", "EUR"});
    private final JLabel result = new JLabel(" ");

    public ConverterC() {
        super("Converter - Anonymous Inner Class");
        setLayout(new FlowLayout());
        JButton button = new JButton("Convert");
        add(new JLabel("INR")); add(amount); add(target); add(button); add(result);

        ActionListener handler = new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) { convert(); }
        };
        button.addActionListener(handler);
        amount.addActionListener(handler);

        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { dispose(); }
        });
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        pack(); setLocationRelativeTo(null);
    }

    private void convert() {
        try {
            double value = Double.parseDouble(amount.getText().trim());
            if (value < 0) throw new NumberFormatException();
            double rate = "USD".equals(target.getSelectedItem()) ? 0.012 : 0.011;
            String output = String.format("%.2f %s", value * rate, target.getSelectedItem());
            result.setText(output);
            JOptionPane.showMessageDialog(this, output);
        } catch (NumberFormatException ex) {
            result.setText("Invalid amount");
            JOptionPane.showMessageDialog(this, "Invalid amount");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ConverterC().setVisible(true));
    }
}
