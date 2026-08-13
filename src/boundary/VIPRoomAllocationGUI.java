package boundary;

import control.VIPRoomAllocationControl;
import entity.Room;
import entity.VIPGuest;
import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 * VIPRoomAllocationGUI.java
 * BOUNDARY (GUI panel): VIP Room Allocation tab inside HotelGUI.
 *
 * @author chong
 * Module: VIP & Loyalty Tier Priority Room Allocation
 */
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
        setLayout(new BorderLayout(8, 8));

        // Initialize table models
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
        
        infoArea = new JTextArea();
        infoArea.setEditable(false);
        infoArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

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
        // Left panel: VIP Queue
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder("👑 VIP Waiting Queue (Highest Priority First)"));
        JScrollPane queueScroll = new JScrollPane(queueTable);
        queueScroll.setPreferredSize(new Dimension(380, 350));
        leftPanel.add(queueScroll, BorderLayout.CENTER);
        add(leftPanel, BorderLayout.WEST);

        // Right main panel
        JPanel rightPanel = new JPanel(new BorderLayout(6, 6));
        add(rightPanel, BorderLayout.CENTER);

        // Room status panel
        JPanel roomPanel = new JPanel(new BorderLayout());
        roomPanel.setBorder(BorderFactory.createTitledBorder("🏨 Room Status"));
        JScrollPane roomScroll = new JScrollPane(roomTable);
        roomScroll.setPreferredSize(new Dimension(320, 180));
        roomPanel.add(roomScroll, BorderLayout.CENTER);
        rightPanel.add(roomPanel, BorderLayout.NORTH);

        // Button panel
        JPanel buttonPanel = new JPanel(new GridLayout(0, 1, 6, 6));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JButton btnAddVIP = createButton("➕ Add VIP Guest", new Color(46, 204, 113));
        JButton btnAllocate = createButton("🏠 Allocate Room", new Color(52, 152, 219));
        JButton btnRelease = createButton("🔓 Release Room", new Color(241, 196, 15));
        JButton btnSearch = createButton("🔍 Search VIP", new Color(155, 89, 182));
        JButton btnQueueReport = createButton("📋 Queue Report", new Color(230, 126, 34));
        JButton btnAllocReport = createButton("📊 Allocation Report", new Color(142, 68, 173));
        JButton btnRefresh = createButton("🔄 Refresh", new Color(149, 165, 166));

        buttonPanel.add(btnAddVIP);
        buttonPanel.add(btnAllocate);
        buttonPanel.add(btnRelease);
        buttonPanel.add(btnSearch);
        buttonPanel.add(btnQueueReport);
        buttonPanel.add(btnAllocReport);
        buttonPanel.add(btnRefresh);

        rightPanel.add(buttonPanel, BorderLayout.CENTER);

        // Information area
        JScrollPane infoScroll = new JScrollPane(infoArea);
        infoScroll.setBorder(BorderFactory.createTitledBorder("📝 Information"));
        infoScroll.setPreferredSize(new Dimension(320, 120));
        rightPanel.add(infoScroll, BorderLayout.SOUTH);

        // Button event bindings
        btnAddVIP.addActionListener(e -> showAddVIPDialog());
        btnAllocate.addActionListener(e -> allocateRoom());
        btnRelease.addActionListener(e -> releaseRoom());
        btnSearch.addActionListener(e -> searchVIP());
        btnQueueReport.addActionListener(e -> showQueueReport());
        btnAllocReport.addActionListener(e -> showAllocationReport());
        btnRefresh.addActionListener(e -> refresh());
    }

    private JButton createButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        return button;
    }

    private void refreshQueueTable() {
        queueTableModel.setRowCount(0);
        List<VIPGuest> guests = control.getAllVIPGuests();
        if (guests == null || guests.isEmpty()) {
            return;
        }

        List<VIPGuest> sorted = new java.util.ArrayList<>(guests);
        control.quickSortByTier(sorted, 0, sorted.size() - 1);

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
        List<Room> rooms = control.getRooms();
        if (rooms == null || rooms.isEmpty()) {
            return;
        }

        for (Room room : rooms) {
            String status;
            String assigned = "-";
            if (room.isOccupied()) {
                status = "❌ Occupied";
                assigned = room.getAssignedConfirmationNumber();
            } else if (room.isReadyForAssignment()) {
                status = "✅ Available";
            } else {
                status = "🔧 " + room.getCurrentStatus();
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

        JPanel panel = new JPanel(new GridLayout(0, 1, 4, 4));
        panel.add(new JLabel("Name:"));
        panel.add(nameField);
        panel.add(new JLabel("IC/Passport:"));
        panel.add(icField);
        panel.add(new JLabel("Phone:"));
        panel.add(phoneField);
        panel.add(new JLabel("Membership ID (e.g., VIP001):"));
        panel.add(membershipField);
        panel.add(new JLabel("Tier:"));
        panel.add(tierCombo);
        panel.add(new JLabel("Loyalty Points:"));
        panel.add(pointsField);
        panel.add(new JLabel("Email:"));
        panel.add(emailField);

        int result = JOptionPane.showConfirmDialog(
            this, panel, "➕ Add VIP Guest",
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
            infoArea.setText("✅ VIP Added: " + guest);
            refresh();
            notifyDataChanged();
        }
    }

    private void allocateRoom() {
        if (control.getQueueSize() == 0) {
            JOptionPane.showMessageDialog(this, "⚠️ No VIP guests waiting.");
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
            infoArea.setText("🏠 Room allocated successfully!");
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
            infoArea.setText("🔓 Room " + roomId + " released.");
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
                "✅ VIP Found:\n" +
                "   Name        : " + guest.getName() + "\n" +
                "   IC/Passport : " + guest.getIdentityNumber() + "\n" +
                "   Phone       : " + guest.getPhone() + "\n" +
                "   Membership  : " + guest.getMembershipId() + "\n" +
                "   Tier        : " + guest.getTier().getDisplay() + "\n" +
                "   Points      : " + guest.getLoyaltyPoints() + "\n" +
                "   Email       : " + guest.getEmail()
            );
        } else {
            infoArea.setText("❌ VIP not found with ID: " + id);
        }
    }

    private void showQueueReport() {
        infoArea.setText("📋 Generating Queue Report...\n");
        control.generateQueueReport();
        infoArea.append("✅ Queue Report generated (check console output).");
        
        JOptionPane.showMessageDialog(this, 
            "Queue Report generated!\nCheck the console for detailed output.",
            "Report Generated",
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void showAllocationReport() {
        infoArea.setText("📊 Generating Allocation Report...\n");
        control.generateAllocationReport();
        infoArea.append("✅ Allocation Report generated (check console output).");
        
        JOptionPane.showMessageDialog(this,
            "Allocation Report generated!\nCheck the console for detailed output.",
            "Report Generated",
            JOptionPane.INFORMATION_MESSAGE);
    }
}