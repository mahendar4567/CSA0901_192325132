# Programming in Java – Assignment 4 (CO4)
## Event-Driven GUI Design

**Name:** Abiyan Yousuf V  
**Register Number:** 192525418  
**GitHub Repository:** [Add your working repository URL]  
**Java Version:** [Record installed JDK version]

> This report follows the six tasks and terminology in the supplied CO4 assignment. Replace screenshot and execution placeholders with evidence from your own machine.

---

## Task 1 – GUI Design Review

### 1.1 Diagnose the design

| Deficiency | User impact / risk |
|---|---|
| UI, validation, payment and JDBC are all in `actionPerformed()` | Code is difficult to test, reuse and maintain. |
| `==` compares action-command String references | The Pay branch may not execute; use `.equals()` or dedicated listeners. |
| No input validation | Empty or non-numeric values can cause `NumberFormatException`; invalid amounts may pass. |
| JDBC work runs on the EDT | The window freezes while the database responds. |
| `Thread.sleep(3000)` runs on the EDT | The UI cannot repaint or respond during the delay. |
| No progress or failure feedback | User cannot distinguish pending, successful and failed payment. |
| Receipt button is not implemented | It enters the `else` branch and clears fields. |
| SQL uses string concatenation | SQL injection and quoting errors are possible. |
| Connection and Statement are not closed | Database resources can leak. |
| No window-closing behaviour | The frame may not dispose cleanly. |
| Checked exceptions are not handled | JDBC and interruption failures are not handled correctly. |

### 1.2 Repaired design

`FeeWindow` only collects input and displays results. `FeeService` validates and performs JDBC work. `PreparedStatement` binds values; try-with-resources closes the connection and statement. `SwingWorker` performs slow work away from the EDT and updates the UI in `done()`.

Class structure:
```
FeeWindow (Swing UI)
  |-- collects registration number and amount
  |-- displays status / receipt
  |-- starts SwingWorker
  +--> FeeService
         |-- validates input
         |-- inserts payment using JDBC
         +-- returns result
```

### 1.3 Design defence (under 100 words)

The redesign separates presentation from business and database logic. FeeService can be tested independently, while FeeWindow only handles user interaction and feedback. PreparedStatement reduces SQL injection risk, try-with-resources closes database resources, and SwingWorker keeps slow work off the event-dispatch thread. Separate listeners make each action easier to understand and modify.

### Alternative listener and complexity analysis

A shared listener with an if–else chain may perform up to k action-command comparisons for k buttons, giving O(k) dispatch selection. Dedicated listeners or Swing Actions map an event directly to its handler, giving O(1) selection in the usual design. k listener objects require O(k) additional space; a shared listener may use O(1) listener objects but contains more branching.

---

## Task 2 – Hospital Outpatient Appointment Desk

### 2.1 Event model

| Event source | Event object | Listener | Action |
|---|---|---|---|
| Patient ID field | ActionEvent | ActionListener | Accept Enter / validate patient ID |
| Department combo | ItemEvent | ItemListener | Refresh doctor choices |
| Doctor combo | ItemEvent | ItemListener | Update selected doctor |
| Time-slot combo | ItemEvent | ItemListener | Update selected slot |
| Book button | ActionEvent | ActionListener | Validate and book appointment |
| Window close | WindowEvent | WindowAdapter | Confirm if booking is in progress; dispose if allowed |

### 2.2 Component containment tree

```
JFrame: Appointment Desk (BorderLayout)
 |-- NORTH: JPanel (FlowLayout)
 |    |-- JLabel + JTextField (patient ID)
 |    |-- JLabel + JComboBox (department)
 |    |-- JLabel + JComboBox (doctor)
 |    +-- JLabel + JComboBox (time slot)
 |-- CENTER: JPanel / JLabel (status)
 +-- SOUTH: JPanel (FlowLayout)
      |-- JButton (Book)
      +-- JButton (Clear)
```

### 2.3 Handler pseudocode

**Algorithm: RefreshDoctorList**
```
BEGIN
  READ selectedDepartment
  CLEAR doctorComboBox
  IF doctorMap contains selectedDepartment THEN
    FOR EACH doctor IN doctorMap[selectedDepartment]
      ADD doctor to doctorComboBox
    END FOR
  END IF
  IF doctorComboBox is not empty THEN
    SELECT first doctor
  END IF
END
```

**Algorithm: BookAppointment**
```
BEGIN
  READ patientID, department, doctor, timeSlot
  IF patientID is empty THEN
    DISPLAY "Enter patient ID"
    RETURN
  END IF
  IF department, doctor or timeSlot is not selected THEN
    DISPLAY "Complete all appointment details"
    RETURN
  END IF
  IF doctor does not belong to selected department THEN
    DISPLAY "Invalid doctor selection"
    RETURN
  END IF
  SET bookingInProgress = TRUE
  CALL appointmentService.book(patientID, doctor, timeSlot)
  DISPLAY confirmation or booking error
  SET bookingInProgress = FALSE
END
```

### 2.4 Event trace and applet comparison

```
main()
 -> EventQueue.invokeLater(...)
 -> construct JFrame and components
 -> register listeners
 -> setVisible(true)
 -> user changes department
 -> ItemEvent -> ItemListener -> refreshDoctorList()
 -> user presses Book
 -> ActionEvent -> ActionListener -> validate/book
 -> user closes window
 -> windowClosing(WindowEvent)
 -> ask confirmation if bookingInProgress
 -> dispose() if confirmed
 -> windowClosed(WindowEvent)
```

A Swing frame is event-driven and remains active through the EDT. An applet has lifecycle callbacks: `init()` initializes once, `start()` begins/resumes, `paint()` draws, `stop()` pauses and `destroy()` releases resources. A JFrame does not automatically use the applet lifecycle.

### Alternative and complexity

Loading all doctors into `Map<String,List<Doctor>>` makes a department change an average O(1) map lookup plus O(r) to populate r doctors for that department. The map uses O(d + m) storage for d departments and m doctor entries. Fetching from a database on every department change incurs repeated query/network latency and less retained application memory.

---

## Task 3 – Campus Library Search Screen

### 3.1 Layout choices

| Container | Layout | Reason |
|---|---|---|
| Main JFrame content pane | BorderLayout | Separates top, centre and bottom; centre grows on resize. |
| Search row JPanel | FlowLayout.LEFT | Keeps field, category and Search button in a compact row. |
| Bottom controls JPanel | FlowLayout.CENTER | Keeps Prev, Next and Reserve at preferred sizes. |
| Results area | JScrollPane viewport | Central region receives extra space; results can scroll. |

### 3.2 Nested structure

```
JFrame (BorderLayout)
 |-- NORTH: JPanel (FlowLayout.LEFT)
 |    |-- JLabel "Search"
 |    |-- JTextField
 |    |-- JComboBox category
 |    +-- JButton "Search"
 |-- CENTER: JScrollPane
 |    +-- JList (results)
 +-- SOUTH: JPanel (FlowLayout.CENTER)
      |-- JButton Prev
      |-- JButton Next
      +-- JButton Reserve
```

### 3.3 Implementation

See `Task03/LibrarySearch.java`. The central scroll pane expands/contracts while the search and bottom rows keep their preferred component sizes.

### 3.4 Resizing evidence

Capture screenshots at approximately 500×350, 650×420 and 900×650. Verify that the centre results area expands/contracts while the top and bottom controls retain preferred sizes. Screenshots must come from your own execution.

### Alternative and complexity

GridBagLayout can create flexible rows and columns but needs more constraints/configuration. Absolute positioning (`setLayout(null)` and `setBounds`) does not adapt automatically to resizing, font changes or display scaling. Layout computation is generally proportional to the number of components/constraints being laid out. Nested layouts use additional panel/container objects but improve maintainability; the number of panels is O(c) for c layout groups.

---

## Task 4 – Event-Handling Forensics: Drawing Pad

### 4.1 Defect

The method is misspelled as `mouseclicked`; Java listener methods are case-sensitive and the correct callback is `mouseClicked(MouseEvent e)`. The label never changes because the original code does not call `status.setText(...)`.

### 4.2 Failing interaction

Click several times, minimise and restore the window or cover and uncover it. Dots disappear because `getGraphics()` draws directly to the current screen surface. A later repaint redraws the component without knowing about those dots.

### 4.3 Repair

Store each click as a `Point`, update the label, call `repaint()`, and draw all stored points in `paint()`. See `Task04/DrawPad.java`.

### 4.4 Prevention

`@Override` makes the compiler check that the method overrides a superclass method. If it is placed on the misspelled `mouseclicked`, compilation reports that it does not override or implement a method from a supertype.

### Alternative: BufferedImage

Draw each dot into an off-screen BufferedImage when clicked; `paint()` then copies the image. A point-list repaint iterates through n points: O(n). Copying the pre-rendered image is approximately O(width × height) pixel transfer and does not iterate through n points each repaint. The point list uses O(n) storage; an image uses O(width × height) pixels, typically several bytes per pixel.

---

## Task 5 – Four Event-Handling Styles: Currency Converter

### 5.1 Implementations

The same conversion operation is connected using:
1. Frame implements ActionListener (`ConverterA`).
2. Named inner class (`ConverterB`).
3. Anonymous inner class (`ConverterC`).
4. Lambda expression (`Converter`).

All versions also use WindowAdapter for closing. The completed lambda version uses one listener for the Convert button and Enter in the amount field. It shows a result dialog and reports invalid amounts.

### 5.2 Requirement change

To support Enter in the amount field, register the same handler with `amount.addActionListener(...)`. To show a dialog, call `JOptionPane.showMessageDialog(...)` from the conversion method. The supplied conversion rates are illustrative, not live exchange rates.

### 5.3 Comparison

| Style | Readability | Field access | Reuse/change |
|---|---|---|---|
| Frame implements ActionListener | Simple for tiny frame; can become crowded | Direct | Harder to reuse across unrelated components |
| Named inner class | Explicit and descriptive | Direct to enclosing fields | Good reuse within enclosing class |
| Anonymous inner class | Verbose but local | Direct | Usually local to one registration |
| Lambda | Concise for a single-method listener | Captures enclosing fields | Easy to share across triggers |

For a large application, a shared `Action` or controller/service is preferable so buttons, menu items and keyboard shortcuts invoke the same command without duplicating business logic.

### Alternative and complexity

A shared `AbstractAction` can be attached to a button, menu item and key binding. One shared Action uses O(1) object storage; separate listener objects for k triggers use O(k) objects. Adding a new trigger to a shared Action requires registering that Action with the new component.

---

## Task 6 – Campus Event Registration Desktop Application

### Stage 1: Users, screens, data and rules

| Area | Requirement |
|---|---|
| Users | Student registers for an event; committee member views, filters and exports registrations. |
| Screens | Registration form; committee registration table; filter/export controls; confirmation/error dialogs. |
| Data | Student ID, student name, event ID/name, registration timestamp and registration ID. |
| Validation | Required fields must be present; event must exist; invalid student/event IDs are rejected. |
| Duplicate rule | A student can register only once for a given event; database enforces UNIQUE(student_id,event_id). |
| Responsiveness | Database work runs in SwingWorker, never blocks the EDT. |
| Export | Committee can export currently filtered rows to CSV. |

### Stage 2: Screen and event flow

```
JFrame: Campus Event Registration
JMenuBar
 |-- Registration
 |    |-- New Registration
 |    +-- Exit
 +-- Committee
      |-- View Registrations
      +-- Export CSV

Registration JPanel:
 Student ID JTextField
 Student Name JTextField
 Event JComboBox
 Register JButton
 Status JLabel

Committee JPanel:
 Event Filter JComboBox
 Refresh JButton
 JTable inside JScrollPane
 Export CSV JButton

Register -> validate -> service -> JDBC insert -> success/error dialog
Filter -> SwingWorker query -> update JTable model on EDT
Export -> choose file -> write visible filtered table rows
```

### Stage 3: Critical decisions

Validation belongs in both UI (immediate feedback) and service layer (rules cannot be bypassed by another caller). Duplicate checking is enforced by a database unique constraint, not only a preliminary SELECT, because concurrent requests can both pass a check. JDBC access belongs in a DAO/repository layer. SwingWorker performs database work in `doInBackground()` and updates Swing components in `done()` on the EDT.

### Stage 4: Class design

```
RegistrationFrame (Swing UI)
       | uses
RegistrationService (validation / business rules)
       | uses
RegistrationDAO (JDBC)
       | accesses
MySQL tables: students, events, registrations
```

**Algorithm: Register**
```
BEGIN
  READ studentID, studentName, selectedEvent
  IF any required value is empty THEN DISPLAY validation message; RETURN
  DISABLE Register button
  START SwingWorker
    CALL RegistrationService.register(studentID, studentName, eventID)
    DAO INSERT using PreparedStatement
    Database UNIQUE constraint rejects duplicate student/event pair
  WHEN WORKER COMPLETES
    ENABLE Register button
    DISPLAY confirmation, duplicate message or safe database error
END
```

**Algorithm: Filter**
```
BEGIN
  READ selectedEventFilter
  START SwingWorker
    IF filter is All THEN rows = DAO.findAll()
    ELSE rows = DAO.findByEvent(eventID)
  WHEN WORKER COMPLETES
    CLEAR table model
    ADD each returned row to table model
    DISPLAY number of rows
END
```

### Stage 5: Database and implementation

The SQL schema is in `Task06/campus_events.sql`. The application is separated into:
- `RegistrationFrame.java` – Swing UI, event handlers, SwingWorker and CSV export.
- `RegistrationService.java` – validation and business rules.
- `RegistrationDAO.java` – JDBC operations using PreparedStatement and try-with-resources.

The DAO maps result rows into Java objects rather than exposing ResultSet to the UI. MySQL Connector/J is required at runtime.

### Test cases

1. Valid student/event pair inserts one row.
2. Blank student ID is rejected before JDBC.
3. Unknown student/event is rejected by validation or foreign key.
4. Same student registers for the same event twice; second insert is rejected by the unique constraint.
5. Database unavailable produces an error without freezing the window.
6. Filter displays only registrations for selected event.
7. Export creates a CSV matching the currently filtered table.

### Alternative: pagination

Loading all 100,000 registrations requires O(N) transfer, object creation and table-model memory. It can make the EDT unresponsive if performed directly. With page size P, each query transfers O(P) rows and the UI retains O(P) rows at a time; total work across all pages remains O(N), but initial load and memory are bounded. LIMIT/OFFSET may become slower for large offsets; keyset pagination using the last seen registration ID or timestamp can scale better. SwingWorker keeps the EDT responsive while each page is fetched.

---

## Individual execution and evidence checklist

- [ ] Task 1: fee schema configured; invalid input and successful/failed database cases tested.
- [ ] Task 2: department change, doctor list, booking and close-confirmation tested.
- [ ] Task 3: screenshots at three sizes.
- [ ] Task 4: click-count screenshot and before/after minimise/restore screenshots.
- [ ] Task 5: valid and invalid conversion for all four styles; Enter key tested.
- [ ] Task 6: SQL script executed; valid, invalid, duplicate, filter and CSV export tested.
- [ ] GitHub link added to report and README.
- [ ] Progressive commits made for all six tasks.
