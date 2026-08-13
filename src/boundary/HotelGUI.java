package boundary;

import control.FrontDeskController;
import control.HousekeepingController;
import control.VIPRoomAllocationControl;
import control.WalkInBookingControl;
import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

public class HotelGUI extends JFrame {

    private final WalkInBookingGUI walkInPanel;
    private final HousekeepingGUI housekeepingPanel;
    private final VIPRoomAllocationGUI vipPanel;
    private final FrontDeskGUI frontDeskPanel;

    public HotelGUI(WalkInBookingControl walkIn, 
                    HousekeepingController housekeeping,
                    VIPRoomAllocationControl vipControl,
                    FrontDeskController frontDesk) {
        super("TARUMT Resorts - Hotel Management System");

        this.walkInPanel = new WalkInBookingGUI(walkIn);
        this.housekeepingPanel = new HousekeepingGUI(housekeeping);
        this.vipPanel = new VIPRoomAllocationGUI(vipControl);
        this.frontDeskPanel = new FrontDeskGUI(frontDesk);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);

        walkInPanel.setOnDataChanged(this::refreshAll);
        housekeepingPanel.setOnDataChanged(this::refreshAll);
        vipPanel.setOnDataChanged(this::refreshAll);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Walk-In", walkInPanel);
        tabs.addTab("Housekeeping", housekeepingPanel);
        tabs.addTab("⭐ VIP", vipPanel);
        tabs.addTab("Front Desk", frontDeskPanel);

        tabs.addChangeListener(e -> refreshAll());

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(tabs, BorderLayout.CENTER);
    }

    private void refreshAll() {
        walkInPanel.refresh();
        housekeepingPanel.refresh();
        vipPanel.refresh();
        frontDeskPanel.refresh();
    }

    public static void open(WalkInBookingControl walkIn, 
                            HousekeepingController housekeeping,
                            VIPRoomAllocationControl vipControl,
                            FrontDeskController frontDesk) {
        SwingUtilities.invokeLater(() -> {
            HotelGUI gui = new HotelGUI(walkIn, housekeeping, vipControl, frontDesk);
            gui.setVisible(true);
        });
    }
}