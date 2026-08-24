package boundary;

import control.HousekeepingController;
import entity.HousekeepingStatus;
import entity.Room;
import entity.StatusEntry;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

/**
 * HousekeepingGUI.java
 * BOUNDARY (GUI panel): Housekeeping tab inside HotelGUI.
 */
public class HousekeepingGUI extends JPanel {

  private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private final HousekeepingController controller;
  private final DefaultTableModel roomTableModel;
  private final JTable roomTable;
  private final JTextArea infoArea;
  private final StatusStepperRenderer statusHitTester = new StatusStepperRenderer();
  private Runnable onDataChanged;

  public HousekeepingGUI(HousekeepingController controller) {
    this.controller = controller;
    UiTheme.styleRoot(this);
    setLayout(new BorderLayout(12, 12));

    roomTableModel = new DefaultTableModel(
        new String[] { "Room ID", "Type", "Status", "Occupancy", "Assigned To", "Updated By", "Updated At" }, 0) {
      @Override
      public boolean isCellEditable(int row, int column) {
        return false;
      }
    };
    roomTable = new JTable(roomTableModel);
    UiTheme.styleTable(roomTable);
    roomTable.setRowHeight(34);
    styleStatusColumn(roomTable);
    roomTable.getColumnModel().getColumn(2).setPreferredWidth(220);
    roomTable.getColumnModel().getColumn(2).setMinWidth(180);

    infoArea = new JTextArea();
    UiTheme.styleInfoArea(infoArea);

    initComponents();
    refreshRooms();
  }

  public void setOnDataChanged(Runnable onDataChanged) {
    this.onDataChanged = onDataChanged;
  }

  public void refresh() {
    String selected = selectedRoomId();
    refreshRooms();
    if (selected != null) {
      selectRoom(selected);
      showSelectedDetails();
    }
  }

  private void notifyDataChanged() {
    if (onDataChanged != null) {
      onDataChanged.run();
    }
  }

  private void initComponents() {
    JButton btnUndo = UiTheme.secondaryButton("Undo Last Action");
    JButton btnRedo = UiTheme.secondaryButton("Redo Last Action");
    JButton btnDetails = UiTheme.accentButton("View Details");
    JButton btnRefresh = UiTheme.secondaryButton("Refresh Rooms");
    JButton btnStatusReport = UiTheme.accentButton("Status Report");
    JButton btnTaskReport = UiTheme.accentButton("Task History Report");
    add(UiTheme.buttonRow(btnUndo, btnRedo, btnDetails, btnRefresh, btnStatusReport, btnTaskReport), BorderLayout.NORTH);

    JScrollPane tableScroll = new JScrollPane(roomTable);
    tableScroll.setPreferredSize(new Dimension(720, 420));
    UiTheme.styleListScroll(tableScroll);
    add(UiTheme.titledPanel("Room Status", tableScroll), BorderLayout.CENTER);

    JScrollPane infoScroll = new JScrollPane(infoArea);
    infoScroll.setPreferredSize(new Dimension(720, 160));
    UiTheme.styleListScroll(infoScroll);
    add(UiTheme.titledPanel("Room details / Task log", infoScroll), BorderLayout.SOUTH);

    btnUndo.addActionListener(e -> {
      if (!confirmAction("Undo the last housekeeping action?", "Confirm Undo")) {
        return;
      }
      String res = controller.undoLastAction();
      JOptionPane.showMessageDialog(this, res);
      refresh();
      notifyDataChanged();
    });
    btnRedo.addActionListener(e -> {
      if (!confirmAction("Redo the last housekeeping action?", "Confirm Redo")) {
        return;
      }
      String res = controller.redoLastAction();
      JOptionPane.showMessageDialog(this, res);
      refresh();
      notifyDataChanged();
    });
    btnDetails.addActionListener(e -> showSelectedDetails());
    btnRefresh.addActionListener(e -> refresh());
    btnStatusReport.addActionListener(e -> showStatusWorkloadReport());
    btnTaskReport.addActionListener(e -> showTaskHistoryReport());

    roomTable.getSelectionModel().addListSelectionListener(e -> {
      if (!e.getValueIsAdjusting()) {
        showSelectedDetails();
      }
    });
    MouseAdapter statusArrowMouse = new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent e) {
        if (e.getButton() != MouseEvent.BUTTON1) {
          return;
        }
        int row = roomTable.rowAtPoint(e.getPoint());
        String hit = hitStatusArrow(e);
        if (row < 0 || hit == null) {
          return;
        }
        roomTable.setRowSelectionInterval(row, row);
        applyStatusStep("next".equals(hit));
      }

      @Override
      public void mouseMoved(MouseEvent e) {
        String hit = hitStatusArrow(e);
        if ("prev".equals(hit) || "next".equals(hit)) {
          roomTable.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } else {
          roomTable.setCursor(Cursor.getDefaultCursor());
        }
      }

      @Override
      public void mouseExited(MouseEvent e) {
        roomTable.setCursor(Cursor.getDefaultCursor());
      }
    };
    roomTable.addMouseListener(statusArrowMouse);
    roomTable.addMouseMotionListener(statusArrowMouse);
  }

  /** Returns "prev" or "next" only when the pointer is on an arrow label. */
  private String hitStatusArrow(MouseEvent e) {
    int row = roomTable.rowAtPoint(e.getPoint());
    int col = roomTable.columnAtPoint(e.getPoint());
    if (row < 0 || col != 2) {
      return null;
    }
    Rectangle cell = roomTable.getCellRect(row, col, false);
    Object value = roomTable.getValueAt(row, col);
    statusHitTester.getTableCellRendererComponent(roomTable, value, false, false, row, col);
    statusHitTester.setBounds(0, 0, cell.width, cell.height);
    statusHitTester.doLayout();
    Component inner = statusHitTester.getComponentAt(e.getX() - cell.x, e.getY() - cell.y);
    if (inner == null) {
      return null;
    }
    String name = inner.getName();
    if ("prev".equals(name) || "next".equals(name)) {
      return name;
    }
    return null;
  }

  private void applyStatusStep(boolean forward) {
    String roomId = selectedRoomId();
    if (roomId == null) {
      infoArea.setText("Select a room, then click ◀ or ▶ on its status.");
      return;
    }
    String result = forward
        ? controller.stepStatusForward(roomId)
        : controller.stepStatusBack(roomId);
    refresh();
    notifyDataChanged();
    infoArea.setText(result);
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

  private void showSelectedDetails() {
    String roomId = selectedRoomId();
    if (roomId == null) {
      infoArea.setText(statusSummary());
      return;
    }
    Room room = controller.findRoomById(roomId);
    if (room == null) {
      infoArea.setText("Room not found.");
      return;
    }
    StringBuilder sb = new StringBuilder();
    sb.append("Room ID        : ").append(room.getRoomId()).append('\n');
    sb.append("Room Type      : ").append(room.getRoomType()).append('\n');
    sb.append("Current Status : ").append(room.getCurrentStatus()).append('\n');
    sb.append("Occupancy      : ").append(room.isOccupied()
        ? "Occupied (" + room.getAssignedConfirmationNumber() + ")" : "Free").append('\n');
    sb.append("Ready to assign: ").append(room.isReadyForAssignment() ? "Yes" : "No").append('\n');
    sb.append("Last Updated By: ").append(room.getLastUpdatedBy()).append('\n');
    sb.append("Last Updated   : ").append(room.getLastUpdatedTime() == null
        ? "-" : room.getLastUpdatedTime().format(TIME_FMT)).append('\n');
    sb.append("\nTask Log:\n");
    StatusEntry[] entries = room.getTaskLog().toArray();
    for (int i = 0; i < entries.length; i++) {
      StatusEntry en = entries[i];
      sb.append(" - ").append(en.getTimestamp().format(TIME_FMT))
          .append(" | ").append(en.getStatus())
          .append(" | ").append(en.getUpdatedBy() == null ? "Unknown" : en.getUpdatedBy())
          .append(" | ").append(en.getNote() == null ? "" : en.getNote())
          .append('\n');
    }
    infoArea.setText(sb.toString());
  }

  private void showStatusWorkloadReport() {
    String[] statusOptions = { "All", "Dirty", "Cleaning In Progress", "Inspected", "Clean" };
    String statusChoice = (String) JOptionPane.showInputDialog(
        this, "Status filter", "Status Workload Report", JOptionPane.QUESTION_MESSAGE,
        null, statusOptions, statusOptions[0]);
    if (statusChoice == null) {
      return;
    }
    String roomType = chooseRoomType();
    String[] occupancyOptions = { "All", "Occupied", "Free" };
    String occupancyChoice = (String) JOptionPane.showInputDialog(
        this, "Occupancy filter", "Status Workload Report", JOptionPane.QUESTION_MESSAGE,
        null, occupancyOptions, occupancyOptions[0]);
    if (occupancyChoice == null) {
      return;
    }
    HousekeepingStatus status = statusChoice.equals("All") ? null : HousekeepingStatus.values()[
        statusChoice.equals("Dirty") ? 0 : statusChoice.equals("Cleaning In Progress") ? 1
            : statusChoice.equals("Inspected") ? 2 : 3];
    Boolean occupied = occupancyChoice.equals("All") ? null : occupancyChoice.equals("Occupied");
    infoArea.setText(controller.generateStatusWorkloadReport(
        status, roomType == null || roomType.trim().isEmpty() ? null : roomType.trim(), occupied));
  }

  private void showTaskHistoryReport() {
    String roomType = chooseRoomType();
    if (roomType == null) {
      return;
    }
    String minimumText = JOptionPane.showInputDialog(
        this, "Minimum task-log entries:", "0");
    if (minimumText == null) {
      return;
    }
    int minimumEntries;
    try {
      minimumEntries = Integer.parseInt(minimumText.trim());
      if (minimumEntries < 0) {
        throw new NumberFormatException();
      }
    } catch (NumberFormatException ex) {
      JOptionPane.showMessageDialog(this, "Enter a non-negative whole number.",
          "Invalid filter", JOptionPane.WARNING_MESSAGE);
      return;
    }
    infoArea.setText(controller.generateTaskHistoryReport(
        roomType, minimumEntries));
  }

  private String chooseRoomType() {
    String[] roomTypes = { "All", "Standard", "Deluxe", "Suite" };
    String selected = (String) JOptionPane.showInputDialog(
        this, "Room type filter", "Housekeeping Report", JOptionPane.QUESTION_MESSAGE,
        null, roomTypes, roomTypes[0]);
    if (selected == null || selected.equals("All")) {
      return selected == null ? null : null;
    }
    return selected;
  }

  private void refreshRooms() {
    String selected = selectedRoomId();
    roomTableModel.setRowCount(0);
    Room[] rooms = controller.getAllRooms();
    for (int i = 0; i < rooms.length; i++) {
      Room room = rooms[i];
      roomTableModel.addRow(new Object[] {
          room.getRoomId(),
          room.getRoomType(),
          room.getCurrentStatus(),
          room.isOccupied() ? "Occupied" : "Free",
          room.isOccupied() && room.getAssignedConfirmationNumber() != null
              ? room.getAssignedConfirmationNumber() : "-",
          room.getLastUpdatedBy(),
          room.getLastUpdatedTime() == null ? "-" : room.getLastUpdatedTime().format(TIME_FMT)
      });
    }
    if (selected != null) {
      selectRoom(selected);
    } else if (roomTableModel.getRowCount() > 0 && roomTable.getSelectedRow() < 0) {
      roomTable.setRowSelectionInterval(0, 0);
    }
    if (roomTable.getSelectedRow() < 0) {
      infoArea.setText(statusSummary());
    }
  }

  private String statusSummary() {
    Room[] rooms = controller.getAllRooms();
    int dirty = 0;
    int cleaning = 0;
    int inspected = 0;
    int ready = 0;
    int occupied = 0;
    for (int i = 0; i < rooms.length; i++) {
      HousekeepingStatus status = rooms[i].getCurrentStatus();
      if (status == HousekeepingStatus.DIRTY) {
        dirty++;
      } else if (status == HousekeepingStatus.CLEANING_IN_PROGRESS) {
        cleaning++;
      } else if (status == HousekeepingStatus.INSPECTED) {
        inspected++;
      } else if (status == HousekeepingStatus.READY_FOR_CHECKIN) {
        ready++;
      }
      if (rooms[i].isOccupied()) {
        occupied++;
      }
    }
    return "Room status summary\n"
        + "Total rooms : " + rooms.length + "\n"
        + "Dirty       : " + dirty + "\n"
        + "Cleaning    : " + cleaning + "\n"
        + "Inspected   : " + inspected + "\n"
        + "Clean       : " + ready + "\n"
        + "Occupied    : " + occupied + "\n\n"
        + "Click ◀ or ▶ on a room's status to change it immediately.";
  }

  private String selectedRoomId() {
    int row = roomTable.getSelectedRow();
    if (row < 0 || row >= roomTableModel.getRowCount()) {
      return null;
    }
    return String.valueOf(roomTableModel.getValueAt(row, 0));
  }

  private void selectRoom(String roomId) {
    for (int i = 0; i < roomTableModel.getRowCount(); i++) {
      if (roomId.equals(String.valueOf(roomTableModel.getValueAt(i, 0)))) {
        roomTable.setRowSelectionInterval(i, i);
        roomTable.scrollRectToVisible(roomTable.getCellRect(i, 0, true));
        return;
      }
    }
  }

  private void styleStatusColumn(JTable table) {
    table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
      @Override
      public Component getTableCellRendererComponent(
          JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
        applyStatusRowColors(c, tbl, row, isSelected, false);
        return c;
      }
    });
    table.getColumnModel().getColumn(2).setCellRenderer(new StatusStepperRenderer());
  }

  private static void applyStatusRowColors(
      Component c, JTable tbl, int row, boolean isSelected, boolean statusColumn) {
    if (isSelected) {
      c.setBackground(UiTheme.SELECTION);
      c.setForeground(UiTheme.TEXT);
      return;
    }
    String status = row < tbl.getRowCount() ? String.valueOf(tbl.getValueAt(row, 2)) : "";
    if ("Clean".equals(status) || "Ready For Check-In".equals(status)) {
      c.setBackground(new Color(0xEC, 0xF8, 0xF3));
      c.setForeground(statusColumn ? UiTheme.SUCCESS : UiTheme.TEXT);
    } else if ("Dirty".equals(status)) {
      c.setBackground(new Color(0xFE, 0xF3, 0xC7));
      c.setForeground(statusColumn ? UiTheme.WARN : UiTheme.TEXT);
    } else {
      c.setBackground(row % 2 == 0 ? UiTheme.PANEL : UiTheme.TABLE_ALT);
      c.setForeground(UiTheme.TEXT);
    }
  }

  private static final class StatusStepperRenderer extends JPanel implements TableCellRenderer {
    private final JLabel prev = new JLabel("◀");
    private final JLabel status = new JLabel();
    private final JLabel next = new JLabel("▶");

    private StatusStepperRenderer() {
      setLayout(new GridBagLayout());
      setOpaque(true);
      Font arrowFont = UiTheme.FONT_TITLE.deriveFont(Font.BOLD, 14f);
      prev.setName("prev");
      next.setName("next");
      status.setName("status");
      prev.setFont(arrowFont);
      next.setFont(arrowFont);
      status.setFont(UiTheme.FONT_BODY);
      prev.setBorder(new EmptyBorder(4, 10, 4, 10));
      next.setBorder(new EmptyBorder(4, 10, 4, 10));
      status.setBorder(new EmptyBorder(4, 8, 4, 8));
      add(prev, new java.awt.GridBagConstraints(
          0, 0, 1, 1, 0, 0, java.awt.GridBagConstraints.CENTER,
          java.awt.GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
      add(status, new java.awt.GridBagConstraints(
          1, 0, 1, 1, 0, 0, java.awt.GridBagConstraints.CENTER,
          java.awt.GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
      add(next, new java.awt.GridBagConstraints(
          2, 0, 1, 1, 0, 0, java.awt.GridBagConstraints.CENTER,
          java.awt.GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    }

    @Override
    public Component getTableCellRendererComponent(
        JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
      status.setText(value == null ? "" : String.valueOf(value));
      applyStatusRowColors(this, tbl, row, isSelected, true);
      Color fg = isSelected ? UiTheme.TEXT : getForeground();
      prev.setForeground(fg);
      next.setForeground(fg);
      status.setForeground(fg);
      prev.setOpaque(false);
      next.setOpaque(false);
      status.setOpaque(false);
      return this;
    }
  }
}
