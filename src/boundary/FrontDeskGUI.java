package boundary;

import control.FrontDeskController;
import entity.Reservation;

import javax.swing.*;
import java.awt.*;

/**
 * FrontDeskGUI
 *
 * GUI for Front Desk module.
 */
public class FrontDeskGUI extends JPanel {

    private FrontDeskController controller;

    private JTextField confirmationField;

    private JTextArea outputArea;

    public FrontDeskGUI(
            FrontDeskController controller) {

        this.controller =
                controller;

        setLayout(
                new BorderLayout());

        // =================================================
        // TOP
        // =================================================

        JPanel top =
                new JPanel();

        top.add(
                new JLabel(
                        "Confirmation Number:"));

        confirmationField =
                new JTextField(10);

        top.add(
                confirmationField);

        JButton searchButton =
                new JButton("Search");

        top.add(
                searchButton);

        JButton reportButton =
                new JButton("Report");

        top.add(
                reportButton);

        add(
                top,
                BorderLayout.NORTH);

        // =================================================
        // OUTPUT
        // =================================================

        outputArea =
                new JTextArea();

        outputArea.setEditable(false);

        outputArea.setFont(
                new Font(
                        Font.MONOSPACED,
                        Font.PLAIN,
                        13));

        JScrollPane scrollPane =
                new JScrollPane(
                        outputArea);

        add(
                scrollPane,
                BorderLayout.CENTER);

        // =================================================
        // BUTTON EVENTS
        // =================================================

        searchButton.addActionListener(
                e -> searchReservation());

        reportButton.addActionListener(
                e -> showReport());
    }

    // =====================================================
    // SEARCH
    // =====================================================

    private void searchReservation() {

        String confirmation =
                confirmationField
                        .getText()
                        .trim();

        if (confirmation.isEmpty()) {

            outputArea.setText(
                    "Please enter confirmation number.");

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

        outputArea.setText(
                controller.formatReservationDetails(
                        reservation));
    }

    // =====================================================
    // REPORT
    // =====================================================

    private void showReport() {

        outputArea.setText(
                controller.generateFrontDeskReport());
    }

    // =====================================================
    // REFRESH
    // =====================================================

    public void refresh() {

        // Nothing required here currently.
    }
}