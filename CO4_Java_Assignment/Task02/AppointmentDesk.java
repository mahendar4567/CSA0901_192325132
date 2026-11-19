import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class AppointmentDesk extends JFrame {
    private final JTextField patientId = new JTextField(12);
    private final JComboBox<String> department =
            new JComboBox<String>(new String[]{"Cardiology", "Neurology", "Orthopaedics"});
    private final JComboBox<String> doctor = new JComboBox<String>();
    private final JComboBox<String> slot =
            new JComboBox<String>(new String[]{"09:00", "10:00", "11:00", "14:00"});
    private final JLabel status = new JLabel("Enter appointment details");
    private final Map<String, java.util.List<String>> doctorMap =
            new LinkedHashMap<String, java.util.List<String>>();
    private boolean bookingInProgress = false;

    public AppointmentDesk() {
        super("Hospital Outpatient Appointment Desk");
        doctorMap.put("Cardiology", Arrays.asList("Dr. Kumar", "Dr. Meena"));
        doctorMap.put("Neurology", Arrays.asList("Dr. Ravi", "Dr. Nisha"));
        doctorMap.put("Orthopaedics", Arrays.asList("Dr. Arun", "Dr. Priya"));

        setLayout(new BorderLayout(8, 8));
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT));
        form.add(new JLabel("Patient ID"));
        form.add(patientId);
        form.add(new JLabel("Department"));
        form.add(department);
        form.add(new JLabel("Doctor"));
        form.add(doctor);
        form.add(new JLabel("Time slot"));
        form.add(slot);
        add(form, BorderLayout.NORTH);
        add(status, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout());
        JButton book = new JButton("Book");
        JButton clear = new JButton("Clear");
        buttons.add(book);
        buttons.add(clear);
        add(buttons, BorderLayout.SOUTH);

        department.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) refreshDoctorList();
        });
        patientId.addActionListener(e -> validatePatient());
        book.addActionListener(e -> bookAppointment());
        clear.addActionListener(e -> {
            patientId.setText("");
            status.setText("Fields cleared");
        });
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                if (bookingInProgress) {
                    int choice = JOptionPane.showConfirmDialog(AppointmentDesk.this,
                            "A booking is in progress. Close anyway?", "Confirm",
                            JOptionPane.YES_NO_OPTION);
                    if (choice == JOptionPane.YES_OPTION) dispose();
                } else {
                    dispose();
                }
            }
        });

        refreshDoctorList();
        setSize(720, 180);
        setLocationRelativeTo(null);
    }

    private void refreshDoctorList() {
        doctor.removeAllItems();
        java.util.List<String> doctors =
                doctorMap.get((String) department.getSelectedItem());
        if (doctors != null) {
            for (String name : doctors) doctor.addItem(name);
        }
    }

    private void validatePatient() {
        if (patientId.getText().trim().isEmpty())
            status.setText("Enter patient ID");
        else
            status.setText("Patient ID accepted");
    }

    private void bookAppointment() {
        String id = patientId.getText().trim();
        String dept = (String) department.getSelectedItem();
        String doc = (String) doctor.getSelectedItem();
        String time = (String) slot.getSelectedItem();

        if (id.isEmpty()) {
            status.setText("Enter patient ID");
            return;
        }
        if (dept == null || doc == null || time == null) {
            status.setText("Complete all appointment details");
            return;
        }
        if (!doctorMap.containsKey(dept) || !doctorMap.get(dept).contains(doc)) {
            status.setText("Invalid doctor selection");
            return;
        }

        bookingInProgress = true;
        status.setText("Booking appointment...");
        // Simulated service call; replace with database/service implementation as required.
        new SwingWorker<String, Void>() {
            protected String doInBackground() throws Exception {
                Thread.sleep(500);
                return "Appointment confirmed: " + id + " / " + doc + " / " + time;
            }
            protected void done() {
                try {
                    status.setText(get());
                } catch (Exception ex) {
                    status.setText("Booking failed");
                } finally {
                    bookingInProgress = false;
                }
            }
        }.execute();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AppointmentDesk().setVisible(true));
    }
}
