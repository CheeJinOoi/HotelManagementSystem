package boundary;

import control.FrontDeskController;
import entity.Guest;
import entity.Reservation;
import entity.Room;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 * FrontDeskGUI.java
 *
 * GUI for Front Desk lookup, billing, room availability,
 * check-out operations and management reports.
 *
 * @author Tan Jun Ren
 */
public class FrontDeskGUI extends JPanel {

    private static final String[] ALL_COLUMNS = {
        "Confirm",
        "Guest",
        "Type",
        "Room Type",
        "Check-In",
        "Status"
    };

    private static final String[] VIP_COLUMNS = {
        "Guest",
        "Confirm",
        "Room",
        "Status",
        "VIP Level"
    };

    private static final String[] ROOM_COLUMNS = {
        "Room ID",
        "Type",
        "Housekeeping",
        "Occupancy",
        "Can Assign"
    };

    private static final String[] CHECKOUT_COLUMNS = {
        "Confirm",
        "Guest",
        "Room Type",
        "Room",
        "Check-Out",
        "Status"
    };

    private static final String[] ROOM_TYPES = {
        "Standard",
        "Deluxe",
        "Suite"
    };

    private final FrontDeskController controller;

    private final DefaultTableModel tableModel;

    private final JTable resultTable;

    private final JTextField confirmationField;

    private final JTextArea outputArea;


    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public FrontDeskGUI(
            FrontDeskController controller) {

        this.controller = controller;

        UiTheme.styleRoot(this);

        setLayout(
                new BorderLayout(12, 12));

        tableModel =
                new DefaultTableModel(
                        ALL_COLUMNS,
                        0) {

                    @Override
                    public boolean isCellEditable(
                            int row,
                            int column) {

                        return false;
                    }
                };

        resultTable =
                new JTable(tableModel);

        UiTheme.styleTable(
                resultTable);

        confirmationField =
                new JTextField(12);

        UiTheme.styleTextField(
                confirmationField);

        confirmationField.setForeground(
                UiTheme.TEXT);

        confirmationField.setBackground(
                UiTheme.PANEL);

        outputArea =
                new JTextArea();

        UiTheme.styleInfoArea(
                outputArea);

        outputArea.setFont(
                UiTheme.FONT_MONO
                        .deriveFont(13f));

        initComponents();

        showAllReservations();
    }


    // =====================================================
    // REFRESH
    // =====================================================

    public void refresh() {

        controller.refreshReservationHash();

        showAllReservations();
    }


    // =====================================================
    // BUILD GUI
    // =====================================================

    private void initComponents() {

        JPanel north =
                new JPanel(
                        new BorderLayout(8, 8));

        north.setOpaque(false);


        // =================================================
        // LOOKUP PANEL
        // =================================================

        JPanel lookup =
                new JPanel(
                        new FlowLayout(
                                FlowLayout.LEFT,
                                10,
                                4));

        lookup.setOpaque(true);

        lookup.setBackground(
                UiTheme.PANEL);

        lookup.setBorder(
                UiTheme.titledBorder(
                        "Lookup"));

        lookup.add(
                UiTheme.bodyLabel(
                        "Confirmation Number"));

        lookup.add(
                confirmationField);


        JButton searchGuestButton =
                UiTheme.primaryButton(
                        "Search Guest");

        JButton searchReservationButton =
                UiTheme.accentButton(
                        "Search Reservation");

        JButton billButton =
                UiTheme.secondaryButton(
                        "Check Bill");

        JButton checkOutButton =
                UiTheme.secondaryButton(
                        "Check Out");


        lookup.add(
                searchGuestButton);

        lookup.add(
                searchReservationButton);

        lookup.add(
                billButton);

        lookup.add(
                checkOutButton);


        north.add(
                lookup,
                BorderLayout.NORTH);


        // =================================================
        // SECOND BUTTON ROW
        // =================================================

        JButton availabilityButton =
                UiTheme.secondaryButton(
                        "Room Availability");

        JButton vipReportButton =
                UiTheme.secondaryButton(
                        "VIP Guest Report");

        JButton frontDeskReportButton =
                UiTheme.secondaryButton(
                        "Front Desk Report");

        JButton checkOutReportButton =
                UiTheme.secondaryButton(
                        "Check-Out Report");

        JButton allButton =
                UiTheme.secondaryButton(
                        "View All");

        JButton refreshButton =
                UiTheme.secondaryButton(
                        "Refresh");


        north.add(
                UiTheme.buttonRow(
                        availabilityButton,
                        vipReportButton,
                        frontDeskReportButton,
                        checkOutReportButton,
                        allButton,
                        refreshButton),
                BorderLayout.SOUTH);


        add(
                north,
                BorderLayout.NORTH);


        // =================================================
        // RESULT TABLE
        // =================================================

        JScrollPane tableScroll =
                new JScrollPane(
                        resultTable);

        tableScroll.setPreferredSize(
                new Dimension(
                        720,
                        360));

        UiTheme.styleListScroll(
                tableScroll);

        add(
                UiTheme.titledPanel(
                        "Results",
                        tableScroll),
                BorderLayout.CENTER);


        // =================================================
        // DETAILS / REPORTS
        // =================================================

        JScrollPane outputScroll =
                new JScrollPane(
                        outputArea);

        outputScroll.setPreferredSize(
                new Dimension(
                        720,
                        170));

        UiTheme.styleListScroll(
                outputScroll);

        add(
                UiTheme.titledPanelWithView(
                        "Details / Reports",
                        outputScroll,
                        () ->
                                UiTheme.showDetailsDialog(
                                        this,
                                        "Details / Reports",
                                        outputArea.getText())),
                BorderLayout.SOUTH);


        // =================================================
        // BUTTON ACTIONS
        // =================================================

        searchGuestButton
                .addActionListener(
                        e -> searchGuest());

        searchReservationButton
                .addActionListener(
                        e -> searchReservation());

        billButton
                .addActionListener(
                        e -> checkBill());

        checkOutButton
                .addActionListener(
                        e -> checkOutGuest());

        availabilityButton
                .addActionListener(
                        e -> searchRoomAvailability());

        vipReportButton
                .addActionListener(
                        e -> showVIPGuestReport());

        frontDeskReportButton
                .addActionListener(
                        e -> showFrontDeskReport());

        checkOutReportButton
                .addActionListener(
                        e -> showCheckOutReport());

        allButton
                .addActionListener(
                        e -> showAllReservations());

        refreshButton
                .addActionListener(
                        e -> refresh());

        confirmationField
                .addActionListener(
                        e -> searchGuest());


        // =================================================
        // TABLE SELECTION
        // =================================================

        resultTable
                .getSelectionModel()
                .addListSelectionListener(
                        e -> {

                            if (!e.getValueIsAdjusting()
                                    && resultTable
                                    .getSelectedRow()
                                    >= 0
                                    && tableModel
                                    .getColumnCount()
                                    > 0) {

                                String confirm =
                                        selectedConfirmation();

                                if (confirm != null) {

                                    confirmationField
                                            .setText(
                                                    confirm);
                                }
                            }
                        });


        // =================================================
        // DOUBLE CLICK TABLE
        // =================================================

        resultTable.addMouseListener(
                new MouseAdapter() {

                    @Override
                    public void mouseClicked(
                            MouseEvent e) {

                        if (e.getClickCount()
                                == 2) {

                            searchReservation();
                        }
                    }
                });
    }


    // =====================================================
    // SEARCH GUEST
    // =====================================================

    private void searchGuest() {

        String confirmation =
                confirmationFromField();

        if (confirmation == null) {
            return;
        }

        Reservation reservation =
                controller.searchGuest(
                        confirmation);

        if (reservation == null) {

            outputArea.setText(
                    "Reservation not found.");

            return;
        }

        Guest guest =
                reservation.getGuest();

        if (guest == null) {

            outputArea.setText(
                    "Guest information not available.");

            return;
        }

        showAllReservations();

        selectConfirmation(
                confirmation);

        outputArea.setText(
                "========== GUEST INFORMATION ==========\n"
                + "Confirmation : "
                + reservation.getConfirmationNumber()
                + "\n"
                + "Name         : "
                + guest.getName()
                + "\n"
                + "IC/Passport  : "
                + guest.getIdentityNumber()
                + "\n"
                + "Phone        : "
                + guest.getPhone()
                + "\n"
                + "Room Type    : "
                + reservation.getRoomType()
                + "\n"
                + "Room         : "
                + roomNumber(reservation)
                + "\n"
                + "Status       : "
                + reservation.getStatus()
                + "\n"
                + "Check-in     : "
                + reservation.getCheckInDate()
                + "\n"
                + "Check-out    : "
                + reservation.getCheckOutDate()
                + "\n"
                + "========================================");
    }


    // =====================================================
    // SEARCH RESERVATION
    // =====================================================

    private void searchReservation() {

        String confirmation =
                confirmationFromField();

        if (confirmation == null) {
            return;
        }

        Reservation reservation =
                controller.findReservation(
                        confirmation);

        if (reservation == null) {

            outputArea.setText(
                    "Reservation not found.");

            return;
        }

        showAllReservations();

        selectConfirmation(
                confirmation);

        outputArea.setText(
                controller
                        .formatReservationDetails(
                                reservation));
    }


    // =====================================================
    // CHECK BILL
    // =====================================================

    private void checkBill() {

        String confirmation =
                confirmationFromField();

        if (confirmation == null) {
            return;
        }

        outputArea.setText(
                controller
                        .getGuestBill(
                                confirmation));
    }


    // =====================================================
    // CHECK OUT
    // =====================================================

    private void checkOutGuest() {

        String confirmation =
                confirmationFromField();

        if (confirmation == null) {
            return;
        }

        Reservation reservation =
                controller.findReservation(
                        confirmation);

        if (reservation == null) {

            outputArea.setText(
                    "Reservation not found.");

            return;
        }

        String name =
                guestName(
                        reservation);

        String message =
                "Confirmation: "
                + reservation
                        .getConfirmationNumber()
                + "\nGuest: "
                + name
                + "\nRoom: "
                + roomNumber(
                        reservation)
                + "\nCurrent Status: "
                + reservation
                        .getStatus()
                + "\n\nConfirm guest check-out?";


        int result =
                JOptionPane
                        .showConfirmDialog(
                                this,
                                message,
                                "Confirm Check-Out",
                                JOptionPane
                                        .YES_NO_OPTION,
                                JOptionPane
                                        .QUESTION_MESSAGE);


        if (result
                != JOptionPane.YES_OPTION) {

            return;
        }


        String checkOutResult =
                controller
                        .checkOutGuest(
                                confirmation);


        controller
                .refreshReservationHash();


        showAllReservations();


        outputArea.setText(
                "========================================\n"
                + "             CHECK OUT\n"
                + "========================================\n"
                + checkOutResult
                + "\n"
                + "----------------------------------------\n"
                + "Total Check-Out : "
                + controller
                        .getTotalCheckOut()
                + "\n"
                + "========================================");
    }


    // =====================================================
    // ROOM AVAILABILITY
    // =====================================================

    private void searchRoomAvailability() {

        JComboBox<String> typeBox =
                new JComboBox<>(
                        ROOM_TYPES);

        JPanel panel =
                new JPanel(
                        new java.awt.GridLayout(
                                0,
                                1,
                                4,
                                4));

        panel.setBackground(
                UiTheme.SURFACE);

        panel.add(
                UiTheme.bodyLabel(
                        "Room type:"));

        panel.add(
                typeBox);


        int result =
                JOptionPane
                        .showConfirmDialog(
                                this,
                                panel,
                                "Search Room Availability",
                                JOptionPane
                                        .OK_CANCEL_OPTION,
                                JOptionPane
                                        .PLAIN_MESSAGE);


        if (result
                != JOptionPane.OK_OPTION) {

            return;
        }


        String roomType =
                (String)
                typeBox.getSelectedItem();


        controller
                .refreshReservationHash();


        setColumns(
                ROOM_COLUMNS);


        Room[] rooms =
                controller.getAllRooms();


        if (rooms != null) {

            for (int i = 0;
                 i < rooms.length;
                 i++) {

                Room room =
                        rooms[i];

                if (room == null
                        || !roomType
                        .equalsIgnoreCase(
                                room.getRoomType())) {

                    continue;
                }


                tableModel.addRow(
                        new Object[] {
                            room.getRoomId(),
                            room.getRoomType(),
                            room.getCurrentStatus(),
                            room.isOccupied()
                                ? "Occupied"
                                : "Free",
                            room.isReadyForAssignment()
                                ? "Yes"
                                : "No"
                        });
            }
        }


        outputArea.setText(
                controller
                        .searchRoomAvailability(
                                roomType));
    }


    // =====================================================
    // VIP REPORT
    // =====================================================

    private void showVIPGuestReport() {

        controller
                .refreshReservationHash();


        Reservation[] vipReservations =
                controller
                        .getVIPReservations();


        setColumns(
                VIP_COLUMNS);


        for (int i = 0;
             i < vipReservations.length;
             i++) {

            Reservation reservation =
                    vipReservations[i];


            tableModel.addRow(
                    new Object[] {
                        guestName(
                                reservation),
                        reservation
                                .getConfirmationNumber(),
                        roomNumber(
                                reservation),
                        reservation
                                .getStatus(),
                        controller
                                .getVipLevel(
                                        reservation)
                    });
        }


        outputArea.setText(
                controller
                        .generateVIPGuestReport());
    }


    // =====================================================
    // FRONT DESK REPORT
    // =====================================================

    private void showFrontDeskReport() {

        controller
                .refreshReservationHash();


        Reservation[] sorted =
                controller
                        .getReservationsSortedByConfirmation();


        setColumns(
                ALL_COLUMNS);


        for (int i = 0;
             i < sorted.length;
             i++) {

            addReservationRow(
                    sorted[i]);
        }


        outputArea.setText(
                controller
                        .generateFrontDeskReport());
    }


    // =====================================================
    // CHECK-OUT REPORT
    // =====================================================

    private void showCheckOutReport() {

        controller
                .refreshReservationHash();


        Reservation[] checkedOut =
                controller
                        .getCheckedOutReservations();


        setColumns(
                CHECKOUT_COLUMNS);


        if (checkedOut == null
                || checkedOut.length == 0) {

            outputArea.setText(
                    "========================================\n"
                    + "          CHECK-OUT REPORT\n"
                    + "========================================\n"
                    + "No checked-out reservations found.\n"
                    + "Total Check-Out : 0\n"
                    + "========================================");

            return;
        }


        for (int i = 0;
             i < checkedOut.length;
             i++) {

            Reservation reservation =
                    checkedOut[i];

            if (reservation == null) {
                continue;
            }


            tableModel.addRow(
                    new Object[] {
                        reservation
                                .getConfirmationNumber(),
                        guestName(
                                reservation),
                        reservation
                                .getRoomType(),
                        roomNumber(
                                reservation),
                        reservation
                                .getCheckOutDate(),
                        reservation
                                .getStatus()
                    });
        }


        outputArea.setText(
                controller
                        .generateCheckOutReport());
    }


    // =====================================================
    // VIEW ALL RESERVATIONS
    // =====================================================

    private void showAllReservations() {

        controller
                .refreshReservationHash();


        setColumns(
                ALL_COLUMNS);


        Reservation[] reservations =
                controller
                        .getAllReservations();


        if (reservations == null
                || reservations.length == 0) {

            outputArea.setText(
                    "No reservations found.");

            return;
        }


        for (int i = 0;
             i < reservations.length;
             i++) {

            addReservationRow(
                    reservations[i]);
        }


        outputArea.setText(
                "All reservations\n"
                + "Hash size: "
                + controller
                        .getReservationHashSize()
                + "\n"
                + "Total reservations: "
                + reservations.length
                + "\n"
                + "Use Search Guest, Search Reservation, "
                + "Check Bill, Check Out, Room Availability, "
                + "or reports.");
    }


    // =====================================================
    // ADD RESERVATION ROW
    // =====================================================

    private void addReservationRow(
            Reservation reservation) {

        if (reservation == null) {
            return;
        }


        tableModel.addRow(
                new Object[] {
                    reservation
                            .getConfirmationNumber(),

                    guestName(
                            reservation),

                    reservation
                            .getBookingType()
                            == null
                            ? "-"
                            : reservation
                            .getBookingType(),

                    reservation
                            .getRoomType(),

                    reservation
                            .getCheckInDate(),

                    reservation
                            .getStatus()
                });
    }


    // =====================================================
    // GET CONFIRMATION
    // =====================================================

    private String confirmationFromField() {

        String confirmation =
                confirmationField
                        .getText()
                        .trim();


        if (confirmation.isEmpty()) {

            confirmation =
                    selectedConfirmation();
        }


        if (confirmation == null
                || confirmation.isEmpty()) {

            outputArea.setText(
                    "Enter an 8-digit confirmation number, "
                    + "or select a reservation first.");

            return null;
        }


        if (!controller
                .isValidConfirmationNumber(
                        confirmation)) {

            outputArea.setText(
                    "Invalid confirmation number.\n"
                    + "Confirmation number must contain "
                    + "exactly 8 digits.");

            return null;
        }


        return confirmation;
    }


    // =====================================================
    // TABLE COLUMNS
    // =====================================================

    private void setColumns(
            String[] columns) {

        tableModel
                .setColumnIdentifiers(
                        columns);

        tableModel
                .setRowCount(0);
    }


    // =====================================================
    // SELECT CONFIRMATION
    // =====================================================

    private void selectConfirmation(
            String confirmation) {

        int confirmColumn =
                confirmationColumnIndex();


        if (confirmColumn < 0) {
            return;
        }


        for (int i = 0;
             i < tableModel
                    .getRowCount();
             i++) {

            if (confirmation.equals(
                    String.valueOf(
                            tableModel
                                    .getValueAt(
                                            i,
                                            confirmColumn)))) {

                resultTable
                        .setRowSelectionInterval(
                                i,
                                i);

                resultTable
                        .scrollRectToVisible(
                                resultTable
                                        .getCellRect(
                                                i,
                                                0,
                                                true));

                return;
            }
        }
    }


    // =====================================================
    // SELECTED CONFIRMATION
    // =====================================================

    private String selectedConfirmation() {

        int row =
                resultTable
                        .getSelectedRow();

        int confirmColumn =
                confirmationColumnIndex();


        if (row < 0
                || confirmColumn < 0) {

            return null;
        }


        return String.valueOf(
                tableModel
                        .getValueAt(
                                row,
                                confirmColumn));
    }


    // =====================================================
    // FIND CONFIRMATION COLUMN
    // =====================================================

    private int confirmationColumnIndex() {

        for (int i = 0;
             i < tableModel
                    .getColumnCount();
             i++) {

            String name =
                    tableModel
                            .getColumnName(i);


            if ("Confirm".equals(name)
                    || "Confirmation"
                    .equals(name)) {

                return i;
            }
        }


        return -1;
    }


    // =====================================================
    // GUEST NAME
    // =====================================================

    private String guestName(
            Reservation reservation) {

        if (reservation.getGuest()
                == null) {

            return "-";
        }


        return reservation
                .getGuest()
                .getName();
    }


    // =====================================================
    // ROOM NUMBER
    // =====================================================

    private String roomNumber(
            Reservation reservation) {

        if (reservation
                .getAssignedRoomId()
                == null) {

            return "-";
        }


        return reservation
                .getAssignedRoomId();
    }
}