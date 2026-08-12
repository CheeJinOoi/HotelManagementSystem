package boundary;

import control.WalkInBookingControl;
import entity.BookingType;
import entity.Guest;
import entity.Reservation;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

/**
 * WalkInBookingGUI.java
 * BOUNDARY (GUI panel): Walk-In & Standard Booking tab inside HotelGUI.
 *
 * Left side  = pending FIFO queue
 * Right side = action buttons + details / reports area
 *
 * Important actions (assign / cancel / check-out) ask OK/Cancel first.
 * X or Cancel on that dialog aborts the action.
 *
 * @author vinsx
 */
public class WalkInBookingGUI extends JPanel {

  private static final String[] ROOM_TYPES = { "Standard", "Deluxe", "Suite" };
  private static final DateTimeFormatter[] DATE_FORMATS = {
      DateTimeFormatter.ISO_LOCAL_DATE,
      DateTimeFormatter.ofPattern("d/M/uuuu"),
      DateTimeFormatter.ofPattern("d-M-uuuu"),
      DateTimeFormatter.ofPattern("d.M.uuuu"),
      DateTimeFormatter.ofPattern("uuuu/M/d"),
      DateTimeFormatter.ofPattern("d/M/uu"),
      DateTimeFormatter.ofPattern("d-M-uu")
  };

  private final WalkInBookingControl controller;
  private final DefaultListModel<String> queueModel = new DefaultListModel<>();
  private final JList<String> queueList = new JList<>(queueModel);
  private final JTextArea infoArea = new JTextArea();
  private Runnable onDataChanged;

  public WalkInBookingGUI(WalkInBookingControl controller) {
    this.controller = controller;
    setLayout(new BorderLayout(8, 8));
    initComponents();
    refreshQueue();
  }

  public void setOnDataChanged(Runnable onDataChanged) {
    this.onDataChanged = onDataChanged;
  }

  public void refresh() {
    refreshQueue();
  }

  private void notifyDataChanged() {
    if (onDataChanged != null) {
      onDataChanged.run();
    }
  }

  private void initComponents() {
    queueList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    queueList.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
    JScrollPane listScroll = new JScrollPane(queueList);
    listScroll.setPreferredSize(new Dimension(430, 300));
    listScroll.setBorder(javax.swing.BorderFactory.createTitledBorder("Pending queue (front = next to assign)"));
    add(listScroll, BorderLayout.WEST);

    JPanel right = new JPanel(new BorderLayout(6, 6));
    add(right, BorderLayout.CENTER);

    JPanel buttons = new JPanel(new GridLayout(0, 1, 6, 6));
    JButton btnWalkIn = new JButton("Register Walk-In");
    JButton btnStandard = new JButton("Create Standard Booking");
    JButton btnAssign = new JButton("Assign Next Guest");
    JButton btnCancel = new JButton("Cancel Waiting Reservation");
    JButton btnCheckout = new JButton("Check-Out Guest");
    JButton btnReports = new JButton("Generate Reports");
    JButton btnRefresh = new JButton("Refresh Queue");
    buttons.add(btnWalkIn);
    buttons.add(btnStandard);
    buttons.add(btnAssign);
    buttons.add(btnCancel);
    buttons.add(btnCheckout);
    buttons.add(btnReports);
    buttons.add(btnRefresh);
    right.add(buttons, BorderLayout.NORTH);

    infoArea.setEditable(false);
    infoArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
    JScrollPane infoScroll = new JScrollPane(infoArea);
    infoScroll.setBorder(javax.swing.BorderFactory.createTitledBorder("Details / Reports"));
    right.add(infoScroll, BorderLayout.CENTER);

    btnWalkIn.addActionListener(e -> showWalkInDialog());
    btnStandard.addActionListener(e -> showStandardBookingDialog());
    btnAssign.addActionListener(e -> assignNextGuest());
    btnCancel.addActionListener(e -> cancelSelectedOrPrompt());
    btnCheckout.addActionListener(e -> checkOutReservation());
    btnReports.addActionListener(e -> chooseAndShowReport());
    btnRefresh.addActionListener(e -> refreshQueue());

    queueList.addListSelectionListener(e -> {
      if (!e.getValueIsAdjusting()) {
        showSelectedDetails();
      }
    });
  }

  private void showWalkInDialog() {
    JTextField nameField = new JTextField();
    JTextField icField = new JTextField();
    JTextField phoneField = new JTextField();
    JComboBox<String> roomTypeBox = new JComboBox<>(ROOM_TYPES);
    JTextField nightsField = new JTextField("1");

    JPanel panel = new JPanel(new GridLayout(0, 1, 4, 4));
    panel.add(new JLabel("Guest name:"));
    panel.add(nameField);
    panel.add(new JLabel("IC / passport:"));
    panel.add(icField);
    panel.add(new JLabel("Phone:"));
    panel.add(phoneField);
    panel.add(new JLabel("Room type:"));
    panel.add(roomTypeBox);
    panel.add(new JLabel("Number of nights:"));
    panel.add(nightsField);

    int result = JOptionPane.showConfirmDialog(this, panel, "Register Walk-In",
        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
    if (result != JOptionPane.OK_OPTION) {
      return;
    }

    int nights;
    try {
      nights = Integer.parseInt(nightsField.getText().trim());
    } catch (NumberFormatException ex) {
      JOptionPane.showMessageDialog(this, "Nights must be a number.");
      return;
    }

    Guest guest = new Guest(nameField.getText().trim(), icField.getText().trim(), phoneField.getText().trim());
    String message = controller.registerWalkIn(guest, (String) roomTypeBox.getSelectedItem(), nights);
    showResult(message);
    refreshQueue();
    notifyDataChanged();
  }

  private void showStandardBookingDialog() {
    JTextField nameField = new JTextField();
    JTextField icField = new JTextField();
    JTextField phoneField = new JTextField();
    JComboBox<String> roomTypeBox = new JComboBox<>(ROOM_TYPES);
    JTextField checkInField = new JTextField();
    JTextField checkOutField = new JTextField();
    String todayExample = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

    JPanel panel = new JPanel(new GridLayout(0, 1, 4, 4));
    panel.add(new JLabel("Guest name:"));
    panel.add(nameField);
    panel.add(new JLabel("IC / passport:"));
    panel.add(icField);
    panel.add(new JLabel("Phone:"));
    panel.add(phoneField);
    panel.add(new JLabel("Room type:"));
    panel.add(roomTypeBox);
    panel.add(new JLabel("Check-in date (e.g. " + todayExample + ", blank = today):"));
    panel.add(checkInField);
    panel.add(new JLabel("Check-out date (e.g. " + todayExample + "):"));
    panel.add(checkOutField);

    int result = JOptionPane.showConfirmDialog(this, panel, "Create Standard Booking",
        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
    if (result != JOptionPane.OK_OPTION) {
      return;
    }

    LocalDate checkIn = parseDateOrToday(checkInField.getText());
    LocalDate checkOut = parseDateOrToday(checkOutField.getText());
    if (checkIn == null || checkOut == null) {
      JOptionPane.showMessageDialog(this, "Invalid date. Try " + todayExample + ".");
      return;
    }

    Guest guest = new Guest(nameField.getText().trim(), icField.getText().trim(), phoneField.getText().trim());
    String message = controller.createStandardBooking(guest, (String) roomTypeBox.getSelectedItem(), checkIn, checkOut);
    showResult(message);
    refreshQueue();
    notifyDataChanged();
  }

  private void assignNextGuest() {
    Reservation[] pending = controller.getPendingReservations();
    String message;
    if (pending.length == 0) {
      message = "The pending queue is empty. Try assign anyway?";
    } else {
      Reservation front = pending[0];
      String guestName = front.getGuest() == null ? "-" : front.getGuest().getName();
      message = "Assign a room to the next guest?\n"
          + guestName + " (" + front.getConfirmationNumber() + ")\n"
          + "Room type: " + front.getRoomType();
    }
    if (!confirmAction(message, "Confirm Assign")) {
      return;
    }
    showResult(controller.assignNextGuestToRoom());
    refreshQueue();
    notifyDataChanged();
  }

  private void cancelSelectedOrPrompt() {
    String confirmation = selectedConfirmation();
    if (confirmation == null) {
      confirmation = promptText("Enter confirmation number to cancel:");
    }
    if (confirmation == null) {
      return;
    }

    if (!confirmAction("Cancel waiting reservation " + confirmation + "?", "Confirm Cancel")) {
      return;
    }

    showResult(controller.cancelWaitingReservation(confirmation));
    refreshQueue();
    notifyDataChanged();
  }

  private void checkOutReservation() {
    String confirmation = promptText("Enter confirmation number to check out:");
    if (confirmation == null) {
      return;
    }

    if (!confirmAction("Check out reservation " + confirmation + "?", "Confirm Check-Out")) {
      return;
    }

    showResult(controller.checkOutGuest(confirmation));
    refreshQueue();
    notifyDataChanged();
  }

  private void chooseAndShowReport() {
    String[] options = {
        "1. Walk-in vs standard arrivals by date",
        "2. Unassigned demand vs available rooms"
    };
    String choice = (String) JOptionPane.showInputDialog(
        this,
        "Choose a report type:",
        "Generate Reports",
        JOptionPane.QUESTION_MESSAGE,
        null,
        options,
        options[0]);
    if (choice == null) {
      return;
    }
    if (choice.startsWith("1.")) {
      showArrivalsReport();
    } else {
      showDemandReport();
    }
  }

  private void showArrivalsReport() {
    String todayExample = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    JTextField startField = new JTextField();
    JTextField endField = new JTextField();
    JComboBox<String> typeBox = new JComboBox<>(new String[] { "All", "Walk-In", "Standard" });

    JPanel panel = new JPanel(new GridLayout(0, 1, 4, 4));
    panel.add(new JLabel("Start date (e.g. " + todayExample + ", blank = today):"));
    panel.add(startField);
    panel.add(new JLabel("End date (blank = today):"));
    panel.add(endField);
    panel.add(new JLabel("Booking type:"));
    panel.add(typeBox);

    int result = JOptionPane.showConfirmDialog(this, panel, "Arrivals Report",
        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
    if (result != JOptionPane.OK_OPTION) {
      return;
    }

    LocalDate startDate = parseDateOrToday(startField.getText());
    LocalDate endDate = parseDateOrToday(endField.getText());
    if (startDate == null || endDate == null) {
      JOptionPane.showMessageDialog(this, "Invalid date. Try " + todayExample + ".");
      return;
    }

    BookingType typeFilter = null;
    if ("Walk-In".equals(typeBox.getSelectedItem())) {
      typeFilter = BookingType.WALK_IN;
    } else if ("Standard".equals(typeBox.getSelectedItem())) {
      typeFilter = BookingType.STANDARD;
    }
    infoArea.setText(controller.generateArrivalsReport(startDate, endDate, typeFilter));
  }

  private void showDemandReport() {
    JComboBox<String> typeBox = new JComboBox<>(new String[] { "All types", "Standard", "Deluxe", "Suite" });
    JPanel panel = new JPanel(new GridLayout(0, 1, 4, 4));
    panel.add(new JLabel("Filter by room type:"));
    panel.add(typeBox);

    int result = JOptionPane.showConfirmDialog(this, panel, "Demand vs Rooms Report",
        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
    if (result != JOptionPane.OK_OPTION) {
      return;
    }

    String selected = (String) typeBox.getSelectedItem();
    String filter = "All types".equals(selected) ? null : selected;
    infoArea.setText(controller.generateDemandReport(filter));
  }

  private void refreshQueue() {
    queueModel.clear();
    Reservation[] pending = controller.getPendingReservations();
    if (pending.length == 0) {
      queueModel.addElement("(queue is empty)");
      infoArea.setText("Pending queue is empty.");
      return;
    }
    for (int i = 0; i < pending.length; i++) {
      Reservation reservation = pending[i];
      String guestName = reservation.getGuest() == null ? "-" : reservation.getGuest().getName();
      queueModel.addElement(String.format("%d. %s | %s | %s | %s | %s",
          i + 1,
          reservation.getConfirmationNumber(),
          reservation.getBookingType(),
          guestName,
          reservation.getRoomType(),
          reservation.getStatus()));
    }
    queueList.setSelectedIndex(0);
    showSelectedDetails();
  }

  private void showSelectedDetails() {
    String confirmation = selectedConfirmation();
    if (confirmation == null) {
      return;
    }
    Reservation found = controller.findReservation(confirmation);
    if (found != null) {
      infoArea.setText(controller.formatReservationDetails(found));
    }
  }

  private String selectedConfirmation() {
    String selected = queueList.getSelectedValue();
    if (selected == null || selected.startsWith("(queue")) {
      return null;
    }
    int dot = selected.indexOf('.');
    int bar = selected.indexOf('|');
    if (dot < 0 || bar < 0) {
      return null;
    }
    return selected.substring(dot + 1, bar).trim();
  }

  private boolean confirmAction(String message, String title) {
    int confirm = JOptionPane.showConfirmDialog(
        this,
        message,
        title,
        JOptionPane.OK_CANCEL_OPTION,
        JOptionPane.QUESTION_MESSAGE);
    return confirm == JOptionPane.OK_OPTION;
  }

  private String promptText(String message) {
    String input = JOptionPane.showInputDialog(this, message);
    if (input == null) {
      return null;
    }
    input = input.trim();
    if (input.isEmpty()) {
      JOptionPane.showMessageDialog(this, "Value cannot be empty.");
      return null;
    }
    return input;
  }

  private void showResult(String message) {
    JOptionPane.showMessageDialog(this, message);
    infoArea.setText(message);
  }

  private LocalDate parseDateOrToday(String text) {
    if (text == null || text.trim().isEmpty() || text.trim().equalsIgnoreCase("today")) {
      return LocalDate.now();
    }
    String trimmed = text.trim();
    for (int i = 0; i < DATE_FORMATS.length; i++) {
      try {
        return LocalDate.parse(trimmed, DATE_FORMATS[i]);
      } catch (DateTimeParseException ex) {
        // try next format
      }
    }
    return null;
  }
}
