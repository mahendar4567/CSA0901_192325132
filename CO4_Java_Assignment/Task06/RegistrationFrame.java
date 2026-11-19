import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;

public class RegistrationFrame extends JFrame {
    private static final int PAGE_SIZE = 100;
    private final RegistrationDAO dao = new RegistrationDAO(
            "jdbc:mysql://localhost:3306/campus_events", "root", "your_password");
    private final RegistrationService service = new RegistrationService(dao);

    private final JTextField studentId = new JTextField(12);
    private final JTextField studentName = new JTextField(16);
    private final JComboBox<EventOption> eventChoice = new JComboBox<EventOption>();
    private final JComboBox<String> filter = new JComboBox<String>();
    private final JLabel status = new JLabel("Ready");
    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"Registration ID", "Student ID", "Student Name", "Event", "Registered At"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(model);
    private int page = 0;

    private static class EventOption {
        final int id;
        final String name;
        EventOption(int id, String name) { this.id = id; this.name = name; }
        public String toString() { return name; }
    }

    public RegistrationFrame() {
        super("Campus Event Registration");
        setLayout(new BorderLayout(8, 8));
        setJMenuBar(createMenuBar());

        JPanel registration = new JPanel(new FlowLayout(FlowLayout.LEFT));
        registration.setBorder(BorderFactory.createTitledBorder("Student Registration"));
        JButton register = new JButton("Register");
        registration.add(new JLabel("Student ID"));
        registration.add(studentId);
        registration.add(new JLabel("Student Name"));
        registration.add(studentName);
        registration.add(new JLabel("Event"));
        registration.add(eventChoice);
        registration.add(register);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controls.setBorder(BorderFactory.createTitledBorder("Committee View"));
        JButton refresh = new JButton("Refresh");
        JButton previous = new JButton("Prev");
        JButton next = new JButton("Next");
        JButton export = new JButton("Export CSV");
        controls.add(new JLabel("Event filter"));
        controls.add(filter);
        controls.add(refresh);
        controls.add(previous);
        controls.add(next);
        controls.add(export);

        JPanel north = new JPanel(new GridLayout(2, 1));
        north.add(registration);
        north.add(controls);
        add(north, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);

        register.addActionListener(e -> registerStudent());
        refresh.addActionListener(e -> { page = 0; loadRows(); });
        previous.addActionListener(e -> {
            if (page > 0) { page--; loadRows(); }
        });
        next.addActionListener(e -> { page++; loadRows(); });
        export.addActionListener(e -> exportCsv());
        filter.addActionListener(e -> { page = 0; loadRows(); });

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1050, 520);
        setLocationRelativeTo(null);
        loadEvents();
    }

    private JMenuBar createMenuBar() {
        JMenuBar bar = new JMenuBar();
        JMenu registration = new JMenu("Registration");
        JMenuItem newRegistration = new JMenuItem("New Registration");
        JMenuItem exit = new JMenuItem("Exit");
        newRegistration.addActionListener(e -> studentId.requestFocusInWindow());
        exit.addActionListener(e -> dispose());
        registration.add(newRegistration);
        registration.add(exit);

        JMenu committee = new JMenu("Committee");
        JMenuItem view = new JMenuItem("View Registrations");
        JMenuItem export = new JMenuItem("Export CSV");
        view.addActionListener(e -> { page = 0; loadRows(); });
        export.addActionListener(e -> exportCsv());
        committee.add(view);
        committee.add(export);

        bar.add(registration);
        bar.add(committee);
        return bar;
    }

    private void loadEvents() {
        new SwingWorker<List<String[]>, Void>() {
            protected List<String[]> doInBackground() throws Exception {
                return service.findAllEvents();
            }
            protected void done() {
                try {
                    List<String[]> events = get();
                    eventChoice.removeAllItems();
                    filter.removeAllItems();
                    filter.addItem("All");
                    for (String[] event : events) {
                        int id = Integer.parseInt(event[0]);
                        eventChoice.addItem(new EventOption(id, event[1]));
                        filter.addItem(id + " - " + event[1]);
                    }
                    loadRows();
                } catch (Exception ex) {
                    status.setText("Could not load events: " + message(ex));
                }
            }
        }.execute();
    }

    private Integer selectedFilterId() {
        String selected = (String) filter.getSelectedItem();
        if (selected == null || "All".equals(selected)) return null;
        try {
            return Integer.valueOf(selected.substring(0, selected.indexOf(" - ")));
        } catch (Exception ex) {
            return null;
        }
    }

    private void registerStudent() {
        String id = studentId.getText().trim();
        String name = studentName.getText().trim();
        EventOption event = (EventOption) eventChoice.getSelectedItem();
        if (id.isEmpty() || name.isEmpty() || event == null) {
            status.setText("Enter student ID, name and select an event");
            return;
        }

        new SwingWorker<Void, Void>() {
            protected Void doInBackground() throws Exception {
                service.register(id, name, event.id);
                return null;
            }
            protected void done() {
                try {
                    get();
                    status.setText("Registration successful");
                    JOptionPane.showMessageDialog(RegistrationFrame.this,
                            "Registration completed successfully.");
                    page = 0;
                    loadRows();
                } catch (Exception ex) {
                    String detail = message(ex);
                    if (detail.toLowerCase().contains("duplicate") ||
                            detail.toLowerCase().contains("uq_student_event")) {
                        detail = "Already registered for this event";
                    }
                    status.setText("Registration failed: " + detail);
                    JOptionPane.showMessageDialog(RegistrationFrame.this, detail,
                            "Registration Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void loadRows() {
        final Integer eventId = selectedFilterId();
        final int offset = page * PAGE_SIZE;
        status.setText("Loading registrations...");
        new SwingWorker<List<String[]>, Void>() {
            protected List<String[]> doInBackground() throws Exception {
                return service.findPage(eventId, PAGE_SIZE, offset);
            }
            protected void done() {
                try {
                    List<String[]> rows = get();
                    model.setRowCount(0);
                    for (String[] row : rows) model.addRow(row);
                    status.setText("Page " + (page + 1) + " - " + rows.size() + " registration(s)");
                } catch (Exception ex) {
                    status.setText("Could not load registrations: " + message(ex));
                }
            }
        }.execute();
    }

    private void exportCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("registrations.csv"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File file = chooser.getSelectedFile();
        try (PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            for (int c = 0; c < model.getColumnCount(); c++) {
                if (c > 0) writer.print(",");
                writer.print(csv(model.getColumnName(c)));
            }
            writer.println();
            for (int r = 0; r < model.getRowCount(); r++) {
                for (int c = 0; c < model.getColumnCount(); c++) {
                    if (c > 0) writer.print(",");
                    writer.print(csv(String.valueOf(model.getValueAt(r, c))));
                }
                writer.println();
            }
            status.setText("Exported current table page to " + file.getName());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String csv(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private String message(Exception ex) {
        Throwable cause = ex.getCause() == null ? ex : ex.getCause();
        return cause.getMessage() == null ? cause.toString() : cause.getMessage();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new RegistrationFrame().setVisible(true));
    }
}
