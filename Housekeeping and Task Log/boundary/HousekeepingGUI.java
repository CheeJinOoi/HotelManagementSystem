package boundary;

import control.HousekeepingController;
import entity.HousekeepingStatus;
import entity.Room;
import entity.StatusEntry;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.format.DateTimeFormatter;

public class HousekeepingGUI extends JFrame {
    private final HousekeepingController controller;
    private final DefaultListModel<String> roomListModel = new DefaultListModel<>();
    private final JList<String> roomList = new JList<>(roomListModel);

    public HousekeepingGUI(HousekeepingController controller) {
        super("TARUMT Housekeeping");
        this.controller = controller;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 450);
        setLocationRelativeTo(null);
        initComponents();
        refreshRooms();
    }

    private void initComponents() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        setContentPane(root);

        // Left: room list
        roomList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane listScroll = new JScrollPane(roomList);
        listScroll.setPreferredSize(new Dimension(280, 300));
        root.add(listScroll, BorderLayout.WEST);

        // Right: buttons and details
        JPanel right = new JPanel(new BorderLayout(6, 6));
        root.add(right, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new GridLayout(0, 1, 6, 6));
        JButton btnUpdate = new JButton("Update Status");
        JButton btnUndo = new JButton("Undo Last Action");
        JButton btnRedo = new JButton("Redo Last Action");
        JButton btnDetails = new JButton("View Details");
        buttons.add(btnUpdate);
        buttons.add(btnUndo);
        buttons.add(btnRedo);
        buttons.add(btnDetails);
        right.add(buttons, BorderLayout.NORTH);

        JTextArea infoArea = new JTextArea();
        infoArea.setEditable(false);
        infoArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane infoScroll = new JScrollPane(infoArea);
        right.add(infoScroll, BorderLayout.CENTER);

        // Button actions
        btnUpdate.addActionListener(e -> showUpdateDialog());
        btnUndo.addActionListener(e -> {
            String res = controller.undoLastAction();
            JOptionPane.showMessageDialog(this, res);
            refreshRooms();
        });
        btnRedo.addActionListener(e -> {
            String res = controller.redoLastAction();
            JOptionPane.showMessageDialog(this, res);
            refreshRooms();
        });
        btnDetails.addActionListener(e -> {
            String selected = roomList.getSelectedValue();
            if (selected == null) {
                JOptionPane.showMessageDialog(this, "Select a room first.");
                return;
            }
            String roomId = selected.split(" - ")[0];
            Room room = controller.findRoomById(roomId);
            if (room == null) {
                JOptionPane.showMessageDialog(this, "Room not found.");
                return;
            }
            StringBuilder sb = new StringBuilder();
            sb.append("Room ID: ").append(room.getRoomId()).append('\n');
            sb.append("Room Type: ").append(room.getRoomType()).append('\n');
            sb.append("Current Status: ").append(room.getCurrentStatus()).append('\n');
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
        });
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
        }
    }

    private void refreshRooms() {
        roomListModel.clear();
        Room[] rooms = controller.getAllRooms();
        for (Room r : rooms) {
            roomListModel.addElement(r.getRoomId() + " - " + r.getCurrentStatus());
        }
    }
}
