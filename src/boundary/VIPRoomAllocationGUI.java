package boundary;

import control.VIPRoomAllocationControl;
import entity.Room;
import entity.VIPGuest;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

public class VIPRoomAllocationGUI extends JPanel {

    private final VIPRoomAllocationControl control;
    private final DefaultTableModel queueTableModel;
    private final DefaultTableModel roomTableModel;
    private final JTable queueTable;
    private final JTable roomTable;
    private final JTextArea infoArea;
    private Runnable onDataChanged;

    public VIPRoomAllocationGUI(VIPRoomAllocationControl control) {
        this.control = control;
        UiTheme.styleRoot(this);
        setLayout(new BorderLayout(12, 12));

        queueTableModel = new DefaultTableModel(
            new String[]{"#", "Name", "Membership ID", "Tier", "Points"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        roomTableModel = new DefaultTableModel(
            new String[]{"Room ID", "Type", "Status", "Assigned To"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        queueTable = new JTable(queueTableModel);
        roomTable = new JTable(roomTableModel);
        UiTheme.styleTable(queueTable);
        UiTheme.styleTable(roomTable);

        infoArea = new JTextArea();
        UiTheme.styleInfoArea(infoArea);

        initComponents();
        refresh();
    }

    public void setOnDataChanged(Runnable onDataChanged) {
        this.onDataChanged = onDataChanged;
    }

    public void refresh() {
        refreshQueueTable();
        refreshRoomTable();
    }

    private void notifyDataChanged() {
        if (onDataChanged != null) {
            onDataChanged.run();
        }
    }

    private void initComponents() {
        JButton btnAddVIP = UiTheme.primaryButton("Add VIP Guest");
        JButton btnAllocate = UiTheme.accentButton("Allocate Room");
        JButton btnRelease = UiTheme.secondaryButton("Release Room");
        JButton btnSearch = UiTheme.secondaryButton("Search VIP");
        JButton btnQueueReport = UiTheme.secondaryButton("Queue Report");
        JButton btnAllocReport = UiTheme.secondaryButton("Allocation Report");
        JButton btnRefresh = UiTheme.secondaryButton("Refresh");
        add(UiTheme.buttonRow(
            btnAddVIP, btnAllocate, btnRelease, btnSearch, btnQueueReport, btnAllocReport, btnRefresh),
            BorderLayout.NORTH);

        JPanel tablesPanel = new JPanel(new BorderLayout(12, 12));
        tablesPanel.setOpaque(false);
        add(tablesPanel, BorderLayout.CENTER);

        JScrollPane queueScroll = new JScrollPane(queueTable);
        queueScroll.setPreferredSize(new Dimension(400, 420));
        UiTheme.styleListScroll(queueScroll);
        tablesPanel.add(UiTheme.titledPanel("VIP Waiting Queue", queueScroll), BorderLayout.WEST);

        JScrollPane roomScroll = new JScrollPane(roomTable);
        roomScroll.setPreferredSize(new Dimension(520, 420));
        UiTheme.styleListScroll(roomScroll);
        tablesPanel.add(UiTheme.titledPanel("Room Status", roomScroll), BorderLayout.CENTER);

        JScrollPane infoScroll = new JScrollPane(infoArea);
        infoScroll.setPreferredSize(new Dimension(320, 110));
        UiTheme.styleListScroll(infoScroll);
        add(UiTheme.titledPanel("Information", infoScroll), BorderLayout.SOUTH);

        btnAddVIP.addActionListener(e -> showAddVIPDialog());
        btnAllocate.addActionListener(e -> allocateRoom());
        btnRelease.addActionListener(e -> releaseRoom());
        btnSearch.addActionListener(e -> searchVIP());
        btnQueueReport.addActionListener(e -> showQueueReport());
        btnAllocReport.addActionListener(e -> showAllocationReport());
        btnRefresh.addActionListener(e -> refresh());
    }

    private void refreshQueueTable() {
        queueTableModel.setRowCount(0);
        VIPGuest[] guests = control.getAllVIPGuests();
        if (guests == null || guests.length == 0) {
            return;
        }

        VIPGuest[] sorted = new VIPGuest[guests.length];
        System.arraycopy(guests, 0, sorted, 0, guests.length);
        control.quickSortByTier(sorted, 0, sorted.length - 1);

        int rank = 1;
        for (VIPGuest guest : sorted) {
            if (guest.getAssignedRoom() == null) {
                queueTableModel.addRow(new Object[]{
                    rank++,
                    guest.getName(),
                    guest.getMembershipId(),
                    guest.getTier().getDisplay(),
                    guest.getLoyaltyPoints()
                });
            }
        }
    }

    private void refreshRoomTable() {
        roomTableModel.setRowCount(0);
        Room[] rooms = control.getRooms();
        if (rooms == null || rooms.length == 0) {
            return;
        }

        VIPGuest[] vipGuests = control.getAllVIPGuests();

        for (Room room : rooms) {
            String status;
            String assigned = "-";
            if (room.isOccupied()) {
                status = "Occupied";
                String confNumber = room.getAssignedConfirmationNumber();
                boolean found = false;
                for (VIPGuest guest : vipGuests) {
                    if (guest.getConfirmationNumber() != null &&
                        guest.getConfirmationNumber().equals(confNumber)) {
                        assigned = guest.getName() + " (VIP)";
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    assigned = confNumber + " (Walk-In)";
                }
            } else if (room.isReadyForAssignment()) {
                status = "Available";
            } else {
                status = String.valueOf(room.getCurrentStatus());
            }
            roomTableModel.addRow(new Object[]{
                room.getRoomId(),
                room.getRoomType(),
                status,
                assigned
            });
        }
    }

    private void showAddVIPDialog() {
        JTextField nameField = new JTextField();
        JTextField icField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField membershipField = new JTextField();
        JComboBox<VIPGuest.MembershipTier> tierCombo =
            new JComboBox<>(VIPGuest.MembershipTier.values());
        JTextField pointsField = new JTextField("0");
        JTextField emailField = new JTextField();
        UiTheme.styleTextField(nameField);
        UiTheme.styleTextField(icField);
        UiTheme.styleTextField(phoneField);
        UiTheme.styleTextField(membershipField);
        UiTheme.styleTextField(pointsField);
        UiTheme.styleTextField(emailField);

        JPanel panel = new JPanel(new GridLayout(0, 1, 4, 4));
        panel.setBackground(UiTheme.SURFACE);
        panel.add(UiTheme.bodyLabel("Name:"));
        panel.add(nameField);
        panel.add(UiTheme.bodyLabel("IC/Passport:"));
        panel.add(icField);
        panel.add(UiTheme.bodyLabel("Phone:"));
        panel.add(phoneField);
        panel.add(UiTheme.bodyLabel("Membership ID (e.g., VIP001):"));
        panel.add(membershipField);
        panel.add(UiTheme.bodyLabel("Tier:"));
        panel.add(tierCombo);
        panel.add(UiTheme.bodyLabel("Loyalty Points:"));
        panel.add(pointsField);
        panel.add(UiTheme.bodyLabel("Email:"));
        panel.add(emailField);

        int result = JOptionPane.showConfirmDialog(
            this, panel, "Add VIP Guest",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String ic = icField.getText().trim();
            String phone = phoneField.getText().trim();
            String membershipId = membershipField.getText().trim();
            VIPGuest.MembershipTier tier = (VIPGuest.MembershipTier) tierCombo.getSelectedItem();
            int points;
            try {
                points = Integer.parseInt(pointsField.getText().trim());
            } catch (NumberFormatException e) {
                points = 0;
            }
            String email = emailField.getText().trim();

            if (name.isEmpty() || membershipId.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name and Membership ID are required!");
                return;
            }

            VIPGuest guest = new VIPGuest(name, ic, phone, membershipId, tier, points, email);
            control.addVIPGuest(guest);
            infoArea.setText("VIP added: " + guest);
            refresh();
            notifyDataChanged();
        }
    }

    private void allocateRoom() {
        if (control.getQueueSize() == 0) {
            JOptionPane.showMessageDialog(this, "No VIP guests waiting.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Allocate a room to the highest priority VIP guest?",
            "Confirm Allocation",
            JOptionPane.OK_CANCEL_OPTION
        );

        if (confirm == JOptionPane.OK_OPTION) {
            control.allocateRoom();
            infoArea.setText("Room allocated successfully.");
            refresh();
            notifyDataChanged();
        }
    }

    private void releaseRoom() {
        String roomId = JOptionPane.showInputDialog(this, "Enter Room ID to release:");
        if (roomId == null || roomId.trim().isEmpty()) {
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Release room " + roomId + "?",
            "Confirm Release",
            JOptionPane.OK_CANCEL_OPTION
        );

        if (confirm == JOptionPane.OK_OPTION) {
            control.releaseRoom(roomId.trim());
            infoArea.setText("Room " + roomId + " released.");
            refresh();
            notifyDataChanged();
        }
    }

    private void searchVIP() {
        String id = JOptionPane.showInputDialog(this, "Enter Membership ID to search:");
        if (id == null || id.trim().isEmpty()) {
            return;
        }

        VIPGuest guest = control.searchByMembershipId(id.trim());
        if (guest != null) {
            infoArea.setText(
                "VIP Found:\n" +
                "   Name        : " + guest.getName() + "\n" +
                "   IC/Passport : " + guest.getIdentityNumber() + "\n" +
                "   Phone       : " + guest.getPhone() + "\n" +
                "   Membership  : " + guest.getMembershipId() + "\n" +
                "   Tier        : " + guest.getTier().getDisplay() + "\n" +
                "   Points      : " + guest.getLoyaltyPoints() + "\n" +
                "   Email       : " + guest.getEmail()
            );
        } else {
            infoArea.setText("VIP not found with ID: " + id);
        }
    }

    private void showQueueReport() {
        infoArea.setText("Generating Queue Report...\n");
        control.generateQueueReport();
        infoArea.append("Queue Report generated (check console output).");

        JOptionPane.showMessageDialog(this,
            "Queue Report generated!\nCheck the console for detailed output.",
            "Report Generated",
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void showAllocationReport() {
        infoArea.setText("Generating Allocation Report...\n");
        control.generateAllocationReport();
        infoArea.append("Allocation Report generated (check console output).");

        JOptionPane.showMessageDialog(this,
            "Allocation Report generated!\nCheck the console for detailed output.",
            "Report Generated",
            JOptionPane.INFORMATION_MESSAGE);
    }
}
