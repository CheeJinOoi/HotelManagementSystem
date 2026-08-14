package boundary;

import control.FrontDeskController;
import control.HousekeepingController;
import control.VIPRoomAllocationControl;
import control.WalkInBookingControl;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;

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
        UiTheme.apply();

        this.walkInPanel = new WalkInBookingGUI(walkIn);
        this.housekeepingPanel = new HousekeepingGUI(housekeeping);
        this.vipPanel = new VIPRoomAllocationGUI(vipControl);
        this.frontDeskPanel = new FrontDeskGUI(frontDesk);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(1280, 800);
        setMinimumSize(new Dimension(1020, 660));
        setLocationRelativeTo(null);

        walkInPanel.setOnDataChanged(this::refreshAll);
        housekeepingPanel.setOnDataChanged(this::refreshAll);
        vipPanel.setOnDataChanged(this::refreshAll);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UiTheme.SURFACE);
        root.add(buildBrandHeader(), BorderLayout.NORTH);
        root.add(buildTabs(), BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel buildBrandHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UiTheme.CHARCOAL);
        header.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(0, 0, 3, 0, UiTheme.TEAL),
            new EmptyBorder(16, 22, 16, 22)));

        JLabel brand = new JLabel("TARUMT RESORTS");
        brand.setFont(UiTheme.FONT_BRAND);
        brand.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Operations Console    Walk-In  ·  Housekeeping  ·  VIP  ·  Front Desk");
        subtitle.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        subtitle.setForeground(new Color(0x99, 0xF6, 0xE4));

        JPanel text = new JPanel(new BorderLayout(0, 4));
        text.setOpaque(false);
        text.add(brand, BorderLayout.NORTH);
        text.add(subtitle, BorderLayout.SOUTH);

        JLabel badge = new JLabel("ONLINE");
        badge.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
        badge.setForeground(Color.WHITE);
        badge.setBackground(UiTheme.TEAL);
        badge.setOpaque(true);
        badge.setBorder(new EmptyBorder(5, 12, 5, 12));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 10));
        right.setOpaque(false);
        right.add(badge);

        header.add(text, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UiTheme.FONT_TITLE);
        tabs.setBackground(UiTheme.SURFACE);
        tabs.setForeground(UiTheme.TEXT);
        tabs.setOpaque(true);
        tabs.setBorder(BorderFactory.createEmptyBorder(10, 12, 12, 12));

        tabs.addTab("  Walk-In  ", walkInPanel);
        tabs.addTab("  Housekeeping  ", housekeepingPanel);
        tabs.addTab("  VIP  ", vipPanel);
        tabs.addTab("  Front Desk  ", frontDeskPanel);

        tabs.addChangeListener(e -> refreshAll());
        return tabs;
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
            UiTheme.apply();
            IntroSplash.showThen(() -> {
                HotelGUI gui = new HotelGUI(walkIn, housekeeping, vipControl, frontDesk);
                gui.setVisible(true);
            });
        });
    }
}
