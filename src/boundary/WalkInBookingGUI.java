package boundary;

import control.WalkInBookingControl;
import entity.Guest;
import entity.Reservation;
import entity.Room;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Window;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * WalkInBookingGUI.java
 * BOUNDARY (GUI panel): Walk-In & Standard Booking tab inside HotelGUI.
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
  private final DefaultTableModel queueTableModel;
  private final DefaultTableModel roomTableModel;
  private final JTable queueTable;
  private final JTable roomTable;
  private final JTextArea infoArea;
  private Runnable onDataChanged;

  public WalkInBookingGUI(WalkInBookingControl controller) {
    this.controller = controller;
    UiTheme.styleRoot(this);
    setLayout(new BorderLayout(12, 12));

    queueTableModel = new DefaultTableModel(
        new String[] { "#", "Confirm", "Type", "Guest", "Room Type", "Check-In", "Status" }, 0) {
      @Override
      public boolean isCellEditable(int row, int column) {
        return false;
      }
    };
    roomTableModel = new DefaultTableModel(
        new String[] { "Room ID", "Type", "Housekeeping", "Occupancy", "Can Assign", "Assigned To" }, 0) {
      @Override
      public boolean isCellEditable(int row, int column) {
        return false;
      }
    };
    queueTable = new JTable(queueTableModel);
    roomTable = new JTable(roomTableModel);
    UiTheme.styleTable(queueTable);
    UiTheme.styleTable(roomTable);
    styleAssignableColumn(roomTable);

    infoArea = new JTextArea();
    UiTheme.styleInfoArea(infoArea);

    initComponents();
    refresh();
  }

  public void setOnDataChanged(Runnable onDataChanged) {
    this.onDataChanged = onDataChanged;
  }

  public void refresh() {
    String selectedConfirm = selectedConfirmation();
    String selectedRoom = selectedRoomId();
    refreshQueueTable();
    refreshRoomTable();
    if (selectedConfirm != null) {
      selectConfirmation(selectedConfirm);
    } else if (queueTableModel.getRowCount() > 0) {
      queueTable.setRowSelectionInterval(0, 0);
    }
    if (selectedRoom != null) {
      selectRoom(selectedRoom);
    }
    if (queueTable.getSelectedRow() >= 0) {
      showSelectedDetails();
    } else if (queueTableModel.getRowCount() == 0) {
      infoArea.setText("Pending queue is empty.\nUse the room status table to see which rooms are ready to assign.");
    }
  }

  private void notifyDataChanged() {
    if (onDataChanged != null) {
      onDataChanged.run();
    }
  }

  private void initComponents() {
    JButton btnWalkIn = UiTheme.primaryButton("Register Walk-In");
    JButton btnStandard = UiTheme.primaryButton("Standard Booking");
    JButton btnAssign = UiTheme.accentButton("Assign Next Guest");
    JButton btnCancel = UiTheme.dangerButton("Cancel Waiting");
    JButton btnCheckout = UiTheme.secondaryButton("Check-Out Guest");
    JButton btnRooms = UiTheme.secondaryButton("Room Status");
    JButton btnReports = UiTheme.secondaryButton("Reports");
    JButton btnRefresh = UiTheme.secondaryButton("Refresh");
    add(UiTheme.buttonRow(
        btnWalkIn, btnStandard, btnAssign, btnCancel, btnCheckout, btnRooms, btnReports, btnRefresh),
        BorderLayout.NORTH);

    JPanel tablesPanel = new JPanel(new BorderLayout(12, 12));
    tablesPanel.setOpaque(false);
    add(tablesPanel, BorderLayout.CENTER);

    JScrollPane queueScroll = new JScrollPane(queueTable);
    queueScroll.setPreferredSize(new Dimension(520, 420));
    UiTheme.styleListScroll(queueScroll);
    tablesPanel.add(UiTheme.titledPanel("Pending queue (front = next to assign)", queueScroll), BorderLayout.WEST);

    JScrollPane roomScroll = new JScrollPane(roomTable);
    roomScroll.setPreferredSize(new Dimension(520, 420));
    UiTheme.styleListScroll(roomScroll);
    tablesPanel.add(UiTheme.titledPanel("Room Status", roomScroll), BorderLayout.CENTER);

    JScrollPane infoScroll = new JScrollPane(infoArea);
    infoScroll.setPreferredSize(new Dimension(320, 120));
    UiTheme.styleListScroll(infoScroll);
    add(UiTheme.titledPanel("Information", infoScroll), BorderLayout.SOUTH);

    btnWalkIn.addActionListener(e -> showWalkInDialog());
    btnStandard.addActionListener(e -> showStandardBookingDialog());
    btnAssign.addActionListener(e -> assignNextGuest());
    btnCancel.addActionListener(e -> cancelSelectedOrPrompt());
    btnCheckout.addActionListener(e -> checkOutReservation());
    btnRooms.addActionListener(e -> showRoomStatusBoard());
    btnReports.addActionListener(e -> chooseAndShowReport());
    btnRefresh.addActionListener(e -> refresh());

    queueTable.getSelectionModel().addListSelectionListener(e -> {
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
    UiTheme.styleTextField(nameField);
    UiTheme.styleTextField(icField);
    UiTheme.styleTextField(phoneField);
    UiTheme.styleTextField(nightsField);

    JPanel panel = new JPanel(new GridLayout(0, 1, 4, 4));
    panel.setBackground(UiTheme.SURFACE);
    panel.add(UiTheme.bodyLabel("Guest name:"));
    panel.add(nameField);
    panel.add(UiTheme.bodyLabel("IC / passport:"));
    panel.add(icField);
    panel.add(UiTheme.bodyLabel("Phone:"));
    panel.add(phoneField);
    panel.add(UiTheme.bodyLabel("Room type:"));
    panel.add(roomTypeBox);
    panel.add(UiTheme.bodyLabel("Number of nights:"));
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
    refresh();
    notifyDataChanged();
  }

  private void showStandardBookingDialog() {
    JTextField nameField = new JTextField();
    JTextField icField = new JTextField();
    JTextField phoneField = new JTextField();
    JComboBox<String> roomTypeBox = new JComboBox<>(ROOM_TYPES);
    JTextField checkInField = new JTextField();
    JTextField checkOutField = new JTextField();
    UiTheme.styleTextField(nameField);
    UiTheme.styleTextField(icField);
    UiTheme.styleTextField(phoneField);
    UiTheme.styleTextField(checkInField);
    UiTheme.styleTextField(checkOutField);
    String todayExample = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

    JPanel panel = new JPanel(new GridLayout(0, 1, 4, 4));
    panel.setBackground(UiTheme.SURFACE);
    panel.add(UiTheme.bodyLabel("Guest name:"));
    panel.add(nameField);
    panel.add(UiTheme.bodyLabel("IC / passport:"));
    panel.add(icField);
    panel.add(UiTheme.bodyLabel("Phone:"));
    panel.add(phoneField);
    panel.add(UiTheme.bodyLabel("Room type:"));
    panel.add(roomTypeBox);
    panel.add(UiTheme.bodyLabel("Check-in date (e.g. " + todayExample + ", blank = today):"));
    panel.add(checkInField);
    panel.add(UiTheme.bodyLabel("Check-out date (e.g. " + todayExample + "):"));
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
    refresh();
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
          + "Room type: " + front.getRoomType() + "\n\n"
          + readyRoomsSummary(front.getRoomType());
    }
    if (!confirmAction(message, "Confirm Assign")) {
      return;
    }
    showResult(controller.assignNextGuestToRoom());
    refresh();
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
    refresh();
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
    refresh();
    notifyDataChanged();
  }

  private void showRoomStatusBoard() {
    refreshRoomTable();
    if (roomTableModel.getRowCount() > 0) {
      roomTable.setRowSelectionInterval(0, 0);
      roomTable.scrollRectToVisible(roomTable.getCellRect(0, 0, true));
    }
    infoArea.setText(controller.formatRoomStatusBoard());
  }

  private void chooseAndShowReport() {
    Window owner = SwingUtilities.getWindowAncestor(this);
    WalkInReportGUI.open(owner, controller);
    infoArea.setText("Opened Walk-In & Standard Booking reports.\n"
        + "Use the Arrivals and Demand tabs to filter and generate tables.");
  }

  private void refreshQueueTable() {
    queueTableModel.setRowCount(0);
    Reservation[] pending = controller.getPendingReservations();
    for (int i = 0; i < pending.length; i++) {
      Reservation reservation = pending[i];
      String guestName = reservation.getGuest() == null ? "-" : reservation.getGuest().getName();
      queueTableModel.addRow(new Object[] {
          i + 1,
          reservation.getConfirmationNumber(),
          reservation.getBookingType(),
          guestName,
          reservation.getRoomType(),
          reservation.getCheckInDate(),
          reservation.getStatus()
      });
    }
  }

  private void refreshRoomTable() {
    roomTableModel.setRowCount(0);
    Room[] rooms = controller.getAllRooms();
    if (rooms == null) {
      return;
    }
    for (int i = 0; i < rooms.length; i++) {
      Room room = rooms[i];
      if (room == null) {
        continue;
      }
      String assigned = "-";
      if (room.getAssignedConfirmationNumber() != null) {
        Reservation reservation = controller.findReservation(room.getAssignedConfirmationNumber());
        if (reservation != null && reservation.getGuest() != null) {
          assigned = reservation.getGuest().getName();
        } else {
          assigned = room.getAssignedConfirmationNumber();
        }
      }
      roomTableModel.addRow(new Object[] {
          room.getRoomId(),
          room.getRoomType(),
          room.getCurrentStatus(),
          room.isOccupied() ? "Occupied" : "Free",
          room.isReadyForAssignment() ? "Yes" : "No",
          assigned
      });
    }
  }

  private void showSelectedDetails() {
    String confirmation = selectedConfirmation();
    if (confirmation == null) {
      return;
    }
    Reservation found = controller.findReservation(confirmation);
    if (found == null) {
      return;
    }
    StringBuilder details = new StringBuilder(controller.formatReservationDetails(found));
    details.append('\n').append(readyRoomsSummary(found.getRoomType()));
    infoArea.setText(details.toString());
    selectFirstAssignableRoom(found.getRoomType());
  }

  private String readyRoomsSummary(String roomType) {
    Room[] rooms = controller.getAllRooms();
    StringBuilder readyIds = new StringBuilder();
    int ready = 0;
    if (rooms != null) {
      for (int i = 0; i < rooms.length; i++) {
        Room room = rooms[i];
        if (room != null && roomType.equals(room.getRoomType()) && room.isReadyForAssignment()) {
          if (ready > 0) {
            readyIds.append(", ");
          }
          readyIds.append(room.getRoomId());
          ready++;
        }
      }
    }
    if (ready == 0) {
      return "Ready " + roomType + " rooms: none. Check Housekeeping or check out a guest first.";
    }
    return "Ready " + roomType + " rooms (" + ready + "): " + readyIds;
  }

  private void selectFirstAssignableRoom(String roomType) {
    for (int i = 0; i < roomTableModel.getRowCount(); i++) {
      String type = String.valueOf(roomTableModel.getValueAt(i, 1));
      String canAssign = String.valueOf(roomTableModel.getValueAt(i, 4));
      if (roomType.equals(type) && "Yes".equals(canAssign)) {
        roomTable.setRowSelectionInterval(i, i);
        roomTable.scrollRectToVisible(roomTable.getCellRect(i, 0, true));
        return;
      }
    }
  }

  private String selectedConfirmation() {
    int row = queueTable.getSelectedRow();
    if (row < 0 || row >= queueTableModel.getRowCount()) {
      return null;
    }
    return String.valueOf(queueTableModel.getValueAt(row, 1));
  }

  private String selectedRoomId() {
    int row = roomTable.getSelectedRow();
    if (row < 0 || row >= roomTableModel.getRowCount()) {
      return null;
    }
    return String.valueOf(roomTableModel.getValueAt(row, 0));
  }

  private void selectConfirmation(String confirmation) {
    for (int i = 0; i < queueTableModel.getRowCount(); i++) {
      if (confirmation.equals(String.valueOf(queueTableModel.getValueAt(i, 1)))) {
        queueTable.setRowSelectionInterval(i, i);
        queueTable.scrollRectToVisible(queueTable.getCellRect(i, 0, true));
        return;
      }
    }
  }

  private void selectRoom(String roomId) {
    for (int i = 0; i < roomTableModel.getRowCount(); i++) {
      if (roomId.equals(String.valueOf(roomTableModel.getValueAt(i, 0)))) {
        roomTable.setRowSelectionInterval(i, i);
        return;
      }
    }
  }

  private void styleAssignableColumn(JTable table) {
    table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
      @Override
      public Component getTableCellRendererComponent(
          JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
        if (isSelected) {
          c.setBackground(UiTheme.SELECTION);
          c.setForeground(UiTheme.TEXT);
        } else {
          boolean assignable = row < tbl.getRowCount()
              && "Yes".equals(String.valueOf(tbl.getValueAt(row, 4)));
          c.setBackground(assignable ? new Color(0xEC, 0xF8, 0xF3) : (row % 2 == 0 ? UiTheme.PANEL : UiTheme.TABLE_ALT));
          if (column == 4 && assignable) {
            c.setForeground(UiTheme.SUCCESS);
          } else {
            c.setForeground(UiTheme.TEXT);
          }
        }
        return c;
      }
    });
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
