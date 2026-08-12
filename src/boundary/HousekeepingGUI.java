package boundary;

import control.HousekeepingController;
import entity.HousekeepingStatus;
import entity.Room;
import entity.StatusEntry;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.format.DateTimeFormatter;
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
 * HousekeepingGUI.java
 * BOUNDARY (GUI panel): Housekeeping tab inside HotelGUI.
 *
 * Left side  = room list with status / occupied flag
 * Right side = update, undo, redo, details
 *
 * Undo/Redo ask for confirmation before changing anything.
 */
public class HousekeepingGUI extends JPanel {

  private final HousekeepingController controller;
  private final DefaultListModel<String> roomListModel = new DefaultListModel<>();
  private final JList<String> roomList = new JList<>(roomListModel);
  private final JTextArea infoArea = new JTextArea();
  private Runnable onDataChanged;

  public HousekeepingGUI(HousekeepingController controller) {
    this.controller = controller;
    setLayout(new BorderLayout(8, 8));
    initComponents();
    refreshRooms();
  }

  public void setOnDataChanged(Runnable onDataChanged) {
    this.onDataChanged = onDataChanged;
  }

  public void refresh() {
    refreshRooms();
  }

  private void notifyDataChanged() {
    if (onDataChanged != null) {
      onDataChanged.run();
    }
  }

  private void initComponents() {
    roomList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    JScrollPane listScroll = new JScrollPane(roomList);
    listScroll.setPreferredSize(new Dimension(280, 300));
    listScroll.setBorder(javax.swing.BorderFactory.createTitledBorder("Rooms"));
    add(listScroll, BorderLayout.WEST);

    JPanel right = new JPanel(new BorderLayout(6, 6));
    add(right, BorderLayout.CENTER);

    JPanel buttons = new JPanel(new GridLayout(0, 1, 6, 6));
    JButton btnUpdate = new JButton("Update Status");
    JButton btnUndo = new JButton("Undo Last Action");
    JButton btnRedo = new JButton("Redo Last Action");
    JButton btnDetails = new JButton("View Details");
    JButton btnRefresh = new JButton("Refresh Rooms");
    buttons.add(btnUpdate);
    buttons.add(btnUndo);
    buttons.add(btnRedo);
    buttons.add(btnDetails);
    buttons.add(btnRefresh);
    right.add(buttons, BorderLayout.NORTH);

    infoArea.setEditable(false);
    infoArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
    JScrollPane infoScroll = new JScrollPane(infoArea);
    infoScroll.setBorder(javax.swing.BorderFactory.createTitledBorder("Room details"));
    right.add(infoScroll, BorderLayout.CENTER);

    btnUpdate.addActionListener(e -> showUpdateDialog());
    btnUndo.addActionListener(e -> {
      if (!confirmAction("Undo the last housekeeping action?", "Confirm Undo")) {
        return;
      }
      String res = controller.undoLastAction();
      JOptionPane.showMessageDialog(this, res);
      refreshRooms();
      notifyDataChanged();
    });
    btnRedo.addActionListener(e -> {
      if (!confirmAction("Redo the last housekeeping action?", "Confirm Redo")) {
        return;
      }
      String res = controller.redoLastAction();
      JOptionPane.showMessageDialog(this, res);
      refreshRooms();
      notifyDataChanged();
    });
    btnDetails.addActionListener(e -> showSelectedDetails());
    btnRefresh.addActionListener(e -> refreshRooms());
    roomList.addListSelectionListener(e -> {
      if (!e.getValueIsAdjusting()) {
        showSelectedDetails();
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

  private void showSelectedDetails() {
    String selected = roomList.getSelectedValue();
    if (selected == null) {
      return;
    }
    String roomId = selected.split(" - ")[0];
    Room room = controller.findRoomById(roomId);
    if (room == null) {
      infoArea.setText("Room not found.");
      return;
    }
    StringBuilder sb = new StringBuilder();
    sb.append("Room ID: ").append(room.getRoomId()).append('\n');
    sb.append("Room Type: ").append(room.getRoomType()).append('\n');
    sb.append("Current Status: ").append(room.getCurrentStatus()).append('\n');
    sb.append("Occupied: ").append(room.isOccupied()
        ? "Yes (" + room.getAssignedConfirmationNumber() + ")" : "No").append('\n');
    sb.append("Last Updated By: ").append(room.getLastUpdatedBy()).append('\n');
    sb.append("Last Updated Time: ").append(room.getLastUpdatedTime()).append('\n');
    sb.append("\nTask Log:\n");
    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    StatusEntry[] entries = room.getTaskLog().toArray();
    for (StatusEntry en : entries) {
      sb.append(" - ").append(en.getTimestamp().format(fmt))
          .append(" | ").append(en.getStatus())
          .append(" | ").append(en.getUpdatedBy() == null ? "Unknown" : en.getUpdatedBy())
          .append(" | ").append(en.getNote() == null ? "" : en.getNote())
          .append('\n');
    }
    infoArea.setText(sb.toString());
  }

  private void showUpdateDialog() {
    String selected = roomList.getSelectedValue();
    if (selected == null) {
      JOptionPane.showMessageDialog(this, "Select a room to update.");
      return;
    }
    String roomId = selected.split(" - ")[0];

    JPanel panel = new JPanel(new GridLayout(0, 1, 4, 4));
    JComboBox<HousekeepingStatus> combo = new JComboBox<>(HousekeepingStatus.values());
    panel.add(new JLabel("Select new status:"));
    panel.add(combo);
    JTextField staffField = new JTextField();
    panel.add(new JLabel("Staff name:"));
    panel.add(staffField);
    JTextField noteField = new JTextField();
    panel.add(new JLabel("Note:"));
    panel.add(noteField);

    int result = JOptionPane.showConfirmDialog(this, panel, "Update Room " + roomId,
        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
    if (result == JOptionPane.OK_OPTION) {
      HousekeepingStatus newStatus = (HousekeepingStatus) combo.getSelectedItem();
      String staff = staffField.getText().trim();
      String note = noteField.getText().trim();
      String res = controller.updateRoomStatus(roomId, newStatus, staff, note);
      JOptionPane.showMessageDialog(this, res);
      refreshRooms();
      notifyDataChanged();
    }
  }

  private void refreshRooms() {
    roomListModel.clear();
    Room[] rooms = controller.getAllRooms();
    for (Room r : rooms) {
      roomListModel.addElement(r.getRoomId() + " - " + r.getCurrentStatus()
          + (r.isOccupied() ? " [Occupied]" : ""));
    }
  }
}
