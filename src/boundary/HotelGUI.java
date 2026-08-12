package boundary;

import control.HousekeepingController;
import control.WalkInBookingControl;
import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

/**
 * HotelGUI.java
 * BOUNDARY: one combined window for both modules.
 *
 * Tab "Walk-In"     -> WalkInBookingGUI panel
 * Tab "Housekeeping" -> HousekeepingGUI panel
 *
 * When either panel changes data (assign, clean, check-out, etc.),
 * both tabs refresh so room occupancy stays consistent.
 *
 * @author vinsx
 */
public class HotelGUI extends JFrame {

  private final WalkInBookingGUI walkInPanel;
  private final HousekeepingGUI housekeepingPanel;

  public HotelGUI(WalkInBookingControl walkIn, HousekeepingController housekeeping) {
    super("TARUMT Resorts - Hotel Management System");
    this.walkInPanel = new WalkInBookingGUI(walkIn);
    this.housekeepingPanel = new HousekeepingGUI(housekeeping);

    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    setSize(980, 620);
    setLocationRelativeTo(null);

    // Keep both tabs updated after any shared-room change
    walkInPanel.setOnDataChanged(this::refreshAll);
    housekeepingPanel.setOnDataChanged(this::refreshAll);

    JTabbedPane tabs = new JTabbedPane();
    tabs.addTab("Walk-In", walkInPanel);
    tabs.addTab("Housekeeping", housekeepingPanel);
    tabs.addChangeListener(e -> refreshAll());

    getContentPane().setLayout(new BorderLayout());
    getContentPane().add(tabs, BorderLayout.CENTER);
  }

  /** Refresh queue list and room list in both tabs. */
  private void refreshAll() {
    walkInPanel.refresh();
    housekeepingPanel.refresh();
  }

  /** Open this window safely on the Swing UI thread. */
  public static void open(WalkInBookingControl walkIn, HousekeepingController housekeeping) {
    SwingUtilities.invokeLater(() -> {
      HotelGUI gui = new HotelGUI(walkIn, housekeeping);
      gui.setVisible(true);
    });
  }
}
