package boundary;

import control.WalkInBookingControl;
import entity.BookingType;
import entity.Reservation;
import entity.ReservationStatus;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 * Walk-In & Standard Booking reports shown as filterable tables
 * instead of a plain text dump.
 */
public class WalkInReportGUI extends JDialog {

    private static final DateTimeFormatter DISPLAY_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
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

    private JTextField startField;
    private JTextField endField;
    private JComboBox<String> arrivalTypeBox;
    private DefaultTableModel arrivalModel;
    private JLabel arrivalSummary;

    private JComboBox<String> demandTypeBox;
    private DefaultTableModel demandModel;
    private DefaultTableModel demandSummaryModel;

    public WalkInReportGUI(Window owner, WalkInBookingControl controller) {
        super(owner, "Walk-In & Standard Booking Reports", ModalityType.APPLICATION_MODAL);
        this.controller = controller;
        UiTheme.apply();

        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBackground(UiTheme.SURFACE);
        root.setBorder(new javax.swing.border.EmptyBorder(14, 14, 14, 14));

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UiTheme.FONT_TITLE);
        tabs.setBackground(UiTheme.SURFACE);
        tabs.setForeground(UiTheme.TEXT);
        tabs.addTab("  Arrivals by Date  ", buildArrivalsPanel());
        tabs.addTab("  Demand vs Rooms  ", buildDemandPanel());
        root.add(tabs, BorderLayout.CENTER);

        setContentPane(root);
        setSize(980, 640);
        setMinimumSize(new Dimension(860, 520));
        setLocationRelativeTo(owner);

        generateArrivals();
        generateDemand();
    }

    public static void open(Window owner, WalkInBookingControl controller) {
        WalkInReportGUI dialog = new WalkInReportGUI(owner, controller);
        dialog.setVisible(true);
    }

    private JPanel buildArrivalsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);

        LocalDate today = LocalDate.now();
        startField = new JTextField(today.minusDays(7).format(DISPLAY_DATE), 12);
        endField = new JTextField(today.plusDays(7).format(DISPLAY_DATE), 12);
        arrivalTypeBox = new JComboBox<>(new String[] { "All", "Walk-In", "Standard" });
        UiTheme.styleTextField(startField);
        UiTheme.styleTextField(endField);

        JPanel filters = new JPanel(new GridLayout(1, 0, 10, 8));
        filters.setOpaque(true);
        filters.setBackground(UiTheme.PANEL);
        filters.setBorder(UiTheme.titledBorder("Filters"));
        filters.add(labeled("Start date", startField));
        filters.add(labeled("End date", endField));
        filters.add(labeled("Booking type", arrivalTypeBox));

        JButton generate = UiTheme.primaryButton("Generate Arrivals Report");
        generate.addActionListener(e -> generateArrivals());
        JPanel generateWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        generateWrap.setOpaque(false);
        generateWrap.add(generate);

        JPanel north = new JPanel(new BorderLayout(8, 8));
        north.setOpaque(false);
        north.add(filters, BorderLayout.CENTER);
        north.add(generateWrap, BorderLayout.SOUTH);
        panel.add(north, BorderLayout.NORTH);

        arrivalModel = new DefaultTableModel(
            new String[] { "Conf No", "Type", "Guest", "Room Type", "Check-In", "Status" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable table = new JTable(arrivalModel);
        UiTheme.styleTable(table);
        JScrollPane tableScroll = new JScrollPane(table);
        UiTheme.styleListScroll(tableScroll);
        panel.add(UiTheme.titledPanel("Matching arrivals (sorted by booking time)", tableScroll), BorderLayout.CENTER);

        arrivalSummary = UiTheme.bodyLabel(" ");
        JPanel summary = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 8));
        summary.setOpaque(true);
        summary.setBackground(UiTheme.PANEL);
        summary.setBorder(UiTheme.titledBorder("Summary"));
        summary.add(arrivalSummary);
        panel.add(summary, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildDemandPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);

        demandTypeBox = new JComboBox<>(new String[] { "All types", "Standard", "Deluxe", "Suite" });

        JPanel filters = new JPanel(new GridLayout(1, 0, 10, 8));
        filters.setOpaque(true);
        filters.setBackground(UiTheme.PANEL);
        filters.setBorder(UiTheme.titledBorder("Filters"));
        filters.add(labeled("Room type", demandTypeBox));

        JButton generate = UiTheme.accentButton("Generate Demand Report");
        generate.addActionListener(e -> generateDemand());
        JPanel generateWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        generateWrap.setOpaque(false);
        generateWrap.add(generate);

        JPanel north = new JPanel(new BorderLayout(8, 8));
        north.setOpaque(false);
        north.add(filters, BorderLayout.CENTER);
        north.add(generateWrap, BorderLayout.SOUTH);
        panel.add(north, BorderLayout.NORTH);

        demandModel = new DefaultTableModel(
            new String[] { "Pos", "Conf No", "Guest", "Room Type", "Booked", "Assignment outlook" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable demandTable = new JTable(demandModel);
        UiTheme.styleTable(demandTable);
        JScrollPane demandScroll = new JScrollPane(demandTable);
        UiTheme.styleListScroll(demandScroll);
        panel.add(UiTheme.titledPanel("Unassigned demand (longest wait first)", demandScroll), BorderLayout.CENTER);

        demandSummaryModel = new DefaultTableModel(
            new String[] { "Room Type", "Rooms", "Available", "Waiting" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        JTable summaryTable = new JTable(demandSummaryModel);
        UiTheme.styleTable(summaryTable);
        JScrollPane summaryScroll = new JScrollPane(summaryTable);
        summaryScroll.setPreferredSize(new Dimension(400, 130));
        UiTheme.styleListScroll(summaryScroll);
        panel.add(UiTheme.titledPanel("Room type summary", summaryScroll), BorderLayout.SOUTH);
        return panel;
    }

    private void generateArrivals() {
        LocalDate startDate = parseDateOrToday(startField.getText());
        LocalDate endDate = parseDateOrToday(endField.getText());
        if (startDate == null || endDate == null) {
            JOptionPane.showMessageDialog(this, "Invalid date. Try " + LocalDate.now().format(DISPLAY_DATE) + ".");
            return;
        }
        if (startDate.isAfter(endDate)) {
            JOptionPane.showMessageDialog(this, "Start date cannot be after end date.");
            return;
        }

        BookingType typeFilter = null;
        String selected = (String) arrivalTypeBox.getSelectedItem();
        if ("Walk-In".equals(selected)) {
            typeFilter = BookingType.WALK_IN;
        } else if ("Standard".equals(selected)) {
            typeFilter = BookingType.STANDARD;
        }

        Reservation[] rows = controller.getFilteredArrivals(startDate, endDate, typeFilter);
        arrivalModel.setRowCount(0);
        int walkInCount = 0;
        int standardCount = 0;
        int waitingCount = 0;
        int assignedCount = 0;
        for (int i = 0; i < rows.length; i++) {
            Reservation reservation = rows[i];
            String guestName = reservation.getGuest() == null ? "-" : reservation.getGuest().getName();
            arrivalModel.addRow(new Object[]{
                reservation.getConfirmationNumber(),
                reservation.getBookingType(),
                guestName,
                reservation.getRoomType(),
                reservation.getCheckInDate(),
                reservation.getStatus()
            });
            if (reservation.getBookingType() == BookingType.WALK_IN) {
                walkInCount++;
            } else if (reservation.getBookingType() == BookingType.STANDARD) {
                standardCount++;
            }
            if (reservation.getStatus() == ReservationStatus.WAITING) {
                waitingCount++;
            } else if (reservation.getStatus() == ReservationStatus.ASSIGNED
                || reservation.getStatus() == ReservationStatus.CHECKED_IN) {
                assignedCount++;
            }
        }

        arrivalSummary.setText(
            "Filter: " + startDate + " to " + endDate
            + "  |  Type: " + (typeFilter == null ? "All" : typeFilter)
            + "     Matching: " + rows.length
            + "     Walk-in: " + walkInCount
            + "     Standard: " + standardCount
            + "     Still waiting: " + waitingCount
            + "     Assigned/in-house: " + assignedCount
        );
    }

    private void generateDemand() {
        String selected = (String) demandTypeBox.getSelectedItem();
        String filter = "All types".equals(selected) ? null : selected;
        Reservation[] rows = controller.getFilteredDemand(filter);
        demandModel.setRowCount(0);
        for (int i = 0; i < rows.length; i++) {
            Reservation reservation = rows[i];
            String guestName = reservation.getGuest() == null ? "-" : reservation.getGuest().getName();
            String bookedDate = reservation.getBookedAt() == null ? "-" : reservation.getBookedAt().toLocalDate().toString();
            demandModel.addRow(new Object[]{
                controller.getPendingQueuePosition(reservation),
                reservation.getConfirmationNumber(),
                guestName,
                reservation.getRoomType(),
                bookedDate,
                controller.getAssignmentOutlook(reservation)
            });
        }

        demandSummaryModel.setRowCount(0);
        addDemandSummaryRow("Standard", filter);
        addDemandSummaryRow("Deluxe", filter);
        addDemandSummaryRow("Suite", filter);
    }

    private void addDemandSummaryRow(String roomType, String roomTypeFilter) {
        if (roomTypeFilter != null && !roomTypeFilter.equals(roomType)) {
            return;
        }
        demandSummaryModel.addRow(new Object[]{
            roomType,
            controller.countRoomsOfType(roomType),
            controller.countAvailableRoomsOfType(roomType),
            controller.countWaitingOfType(roomType)
        });
    }

    private JPanel labeled(String caption, java.awt.Component field) {
        JPanel wrap = new JPanel(new BorderLayout(4, 4));
        wrap.setOpaque(false);
        wrap.add(UiTheme.bodyLabel(caption), BorderLayout.NORTH);
        wrap.add(field, BorderLayout.CENTER);
        return wrap;
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
