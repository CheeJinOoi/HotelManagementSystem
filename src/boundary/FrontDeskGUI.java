package boundary;

import control.FrontDeskController;
import control.FrontDeskReports;
import entity.Guest;
import entity.Reservation;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 * FrontDeskGUI
 * GUI for Front Desk lookup, reservation list, and operational reports.
 */
public class FrontDeskGUI extends JPanel {

    private static final String[] ALL_COLUMNS = {
        "Confirm", "Guest", "Type", "Room Type", "Check-In", "Status"
    };
    private static final String[] RESERVATION_REPORT_COLUMNS = {
        "Confirm", "Guest", "Room Type", "Check-In", "Status"
    };
    private static final String[] GUEST_REPORT_COLUMNS = {
        "Confirm", "Guest Name", "IC/Passport", "Phone"
    };

    private final FrontDeskController controller;
    private final FrontDeskReports reports;
    private final DefaultTableModel tableModel;
    private final JTable reservationTable;
    private final JTextField confirmationField;
    private final JTextArea outputArea;

    public FrontDeskGUI(FrontDeskController controller) {
        this.controller = controller;
        this.reports = new FrontDeskReports(controller);
        UiTheme.styleRoot(this);
        setLayout(new BorderLayout(12, 12));

        tableModel = new DefaultTableModel(ALL_COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        reservationTable = new JTable(tableModel);
        UiTheme.styleTable(reservationTable);

        confirmationField = new JTextField(12);
        UiTheme.styleTextField(confirmationField);
        confirmationField.setForeground(UiTheme.TEXT);
        confirmationField.setBackground(UiTheme.PANEL);

        outputArea = new JTextArea();
        UiTheme.styleInfoArea(outputArea);
        outputArea.setFont(UiTheme.FONT_MONO.deriveFont(13f));

        initComponents();
        showAllReservations();
    }

    public void refresh() {
        controller.refreshHashTable();
        showAllReservations();
    }

    private void initComponents() {
        JPanel north = new JPanel(new BorderLayout(8, 8));
        north.setOpaque(false);

        JPanel lookup = new JPanel(new BorderLayout(10, 8));
        lookup.setOpaque(true);
        lookup.setBackground(UiTheme.PANEL);
        lookup.setBorder(UiTheme.titledBorder("Lookup"));

        JPanel lookupFields = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 10, 4));
        lookupFields.setOpaque(false);
        lookupFields.add(UiTheme.bodyLabel("Confirmation Number"));
        lookupFields.add(confirmationField);

        JButton searchButton = UiTheme.primaryButton("Search");
        JButton detailsButton = UiTheme.accentButton("View Details");
        lookupFields.add(searchButton);
        lookupFields.add(detailsButton);
        lookup.add(lookupFields, BorderLayout.CENTER);
        north.add(lookup, BorderLayout.NORTH);

        JButton allButton = UiTheme.secondaryButton("View All Reservations");
        JButton reservationReportButton = UiTheme.secondaryButton("Reservation Report");
        JButton guestReportButton = UiTheme.secondaryButton("Guest Report");
        JButton roomReportButton = UiTheme.secondaryButton("Room Availability Report");
        JButton refreshButton = UiTheme.secondaryButton("Refresh");
        north.add(UiTheme.buttonRow(
            allButton, reservationReportButton, guestReportButton, roomReportButton, refreshButton),
            BorderLayout.SOUTH);
        add(north, BorderLayout.NORTH);

        JScrollPane tableScroll = new JScrollPane(reservationTable);
        tableScroll.setPreferredSize(new Dimension(720, 360));
        UiTheme.styleListScroll(tableScroll);
        add(UiTheme.titledPanel("Reservations", tableScroll), BorderLayout.CENTER);

        JScrollPane outputScroll = new JScrollPane(outputArea);
        outputScroll.setPreferredSize(new Dimension(720, 160));
        UiTheme.styleListScroll(outputScroll);
        add(UiTheme.titledPanel("Details / Report", outputScroll), BorderLayout.SOUTH);

        searchButton.addActionListener(e -> searchReservation());
        detailsButton.addActionListener(e -> showReservationDetails());
        allButton.addActionListener(e -> showAllReservations());
        reservationReportButton.addActionListener(e -> showReservationReport());
        guestReportButton.addActionListener(e -> showGuestReport());
        roomReportButton.addActionListener(e -> showRoomAvailabilityReport());
        refreshButton.addActionListener(e -> refresh());
        confirmationField.addActionListener(e -> searchReservation());

        reservationTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && reservationTable.getSelectedRow() >= 0) {
                confirmationField.setText(String.valueOf(
                    tableModel.getValueAt(reservationTable.getSelectedRow(), 0)));
            }
        });
        reservationTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    showReservationDetails();
                }
            }
        });
    }

    private void searchReservation() {
        String confirmation = confirmationField.getText().trim();
        if (!isValidConfirmationNumber(confirmation)) {
            outputArea.setText("Invalid confirmation number.\nConfirmation number must contain exactly 8 digits.");
            return;
        }

        Reservation reservation = controller.findReservation(confirmation);
        if (reservation == null) {
            outputArea.setText("Reservation not found.");
            return;
        }

        showAllReservations();
        selectConfirmation(confirmation);
        outputArea.setText(
            "Reservation found.\n"
            + "Confirmation : " + reservation.getConfirmationNumber() + "\n"
            + "Guest        : " + guestName(reservation) + "\n"
            + "Room Type    : " + reservation.getRoomType() + "\n"
            + "Status       : " + reservation.getStatus() + "\n\n"
            + "Use View Details (or double-click the row) for the complete record."
        );
    }

    private void showReservationDetails() {
        String confirmation = confirmationField.getText().trim();
        if (confirmation.isEmpty()) {
            confirmation = selectedConfirmation();
        }
        if (confirmation == null || confirmation.isEmpty()) {
            outputArea.setText("Enter an 8-digit confirmation number, or select a reservation first.");
            return;
        }
        if (!isValidConfirmationNumber(confirmation)) {
            outputArea.setText("Invalid confirmation number.\nConfirmation number must contain exactly 8 digits.");
            return;
        }

        Reservation reservation = controller.findReservation(confirmation);
        if (reservation == null) {
            outputArea.setText("Reservation not found.");
            return;
        }
        selectConfirmation(confirmation);
        outputArea.setText(controller.formatReservationDetails(reservation));
    }

    private void showAllReservations() {
        controller.refreshHashTable();
        setColumns(ALL_COLUMNS);
        Reservation[] reservations = controller.getAllReservations();
        if (reservations == null || reservations.length == 0) {
            outputArea.setText("No reservations found.");
            return;
        }

        for (int i = 0; i < reservations.length; i++) {
            Reservation reservation = reservations[i];
            if (reservation == null) {
                continue;
            }
            tableModel.addRow(new Object[]{
                reservation.getConfirmationNumber(),
                guestName(reservation),
                reservation.getBookingType() == null ? "-" : reservation.getBookingType(),
                reservation.getRoomType(),
                reservation.getCheckInDate(),
                reservation.getStatus()
            });
        }
        outputArea.setText(
            "All reservations\n"
            + "Total reservations: " + reservations.length + "\n"
            + "Select a row, search a confirmation number, or open a report."
        );
    }

    private void showReservationReport() {
        controller.refreshHashTable();
        Reservation[] filtered = reports.getActiveReservationsSortedByCheckIn();
        setColumns(RESERVATION_REPORT_COLUMNS);
        for (int i = 0; i < filtered.length; i++) {
            Reservation reservation = filtered[i];
            tableModel.addRow(new Object[]{
                reservation.getConfirmationNumber(),
                guestName(reservation),
                reservation.getRoomType(),
                reservation.getCheckInDate(),
                reservation.getStatus()
            });
        }
        outputArea.setText(reports.generateReservationReport());
    }

    private void showGuestReport() {
        controller.refreshHashTable();
        Reservation[] reservations = controller.getAllReservations();
        setColumns(GUEST_REPORT_COLUMNS);
        if (reservations != null) {
            for (int i = 0; i < reservations.length; i++) {
                Reservation reservation = reservations[i];
                if (reservation == null || reservation.getGuest() == null) {
                    continue;
                }
                Guest guest = reservation.getGuest();
                tableModel.addRow(new Object[]{
                    reservation.getConfirmationNumber(),
                    guest.getName(),
                    guest.getIdentityNumber(),
                    guest.getPhone()
                });
            }
        }
        outputArea.setText(reports.generateGuestReport());
    }

    private void showRoomAvailabilityReport() {
        showAllReservations();
        outputArea.setText(reports.generateRoomAvailabilityReport());
    }

    private void setColumns(String[] columns) {
        tableModel.setColumnIdentifiers(columns);
        tableModel.setRowCount(0);
    }

    private void selectConfirmation(String confirmation) {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (confirmation.equals(String.valueOf(tableModel.getValueAt(i, 0)))) {
                reservationTable.setRowSelectionInterval(i, i);
                reservationTable.scrollRectToVisible(reservationTable.getCellRect(i, 0, true));
                return;
            }
        }
    }

    private String selectedConfirmation() {
        int row = reservationTable.getSelectedRow();
        if (row < 0) {
            return null;
        }
        return String.valueOf(tableModel.getValueAt(row, 0));
    }

    private String guestName(Reservation reservation) {
        if (reservation.getGuest() == null) {
            return "-";
        }
        return reservation.getGuest().getName();
    }

    private boolean isValidConfirmationNumber(String confirmation) {
        if (confirmation == null || confirmation.length() != 8) {
            return false;
        }
        for (int i = 0; i < confirmation.length(); i++) {
            if (!Character.isDigit(confirmation.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
