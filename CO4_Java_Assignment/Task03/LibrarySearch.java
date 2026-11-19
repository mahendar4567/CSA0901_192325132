import javax.swing.*;
import java.awt.*;

public class LibrarySearch extends JFrame {
    private final JTextField query = new JTextField(20);
    private final JComboBox<String> category =
            new JComboBox<String>(new String[]{"All", "Book", "Author", "Subject"});
    private final DefaultListModel<String> model = new DefaultListModel<String>();

    public LibrarySearch() {
        super("Campus Library Search");
        setLayout(new BorderLayout(8, 8));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Search:"));
        top.add(query);
        top.add(category);
        JButton search = new JButton("Search");
        top.add(search);
        add(top, BorderLayout.NORTH);

        model.addElement("Java Programming");
        model.addElement("Data Structures");
        model.addElement("Database Systems");
        model.addElement("Operating Systems");
        model.addElement("Computer Networks");
        JList<String> results = new JList<String>(model);
        add(new JScrollPane(results), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottom.add(new JButton("Prev"));
        bottom.add(new JButton("Next"));
        bottom.add(new JButton("Reserve"));
        add(bottom, BorderLayout.SOUTH);

        search.addActionListener(e -> {
            String text = query.getText().trim();
            if (text.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Enter a search term");
            } else {
                JOptionPane.showMessageDialog(this,
                        "Searching " + category.getSelectedItem() + " for: " + text);
            }
        });

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(650, 420);
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LibrarySearch().setVisible(true));
    }
}
