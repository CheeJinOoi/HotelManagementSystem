package boundary;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.ColorUIResource;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

/**
 * Shared Swing theme for TARUMT Resorts operations UI.
 * Charcoal + teal (modern ops dashboard), high-contrast text.
 * Buttons are custom-painted so colors stay readable on Windows.
 */
public final class UiTheme {

  public static final Color CHARCOAL = new Color(0x16, 0x1B, 0x22);
  public static final Color CHARCOAL_SOFT = new Color(0x1E, 0x26, 0x2F);
  public static final Color TEAL = new Color(0x0D, 0x94, 0x88);
  public static final Color TEAL_DARK = new Color(0x0F, 0x76, 0x6E);
  public static final Color SURFACE = new Color(0xF3, 0xF4, 0xF6);
  public static final Color PANEL = new Color(0xFF, 0xFF, 0xFF);
  public static final Color BORDER = new Color(0xD4, 0xD4, 0xD8);
  public static final Color TEXT = new Color(0x18, 0x18, 0x1B);
  public static final Color MUTED = new Color(0x52, 0x52, 0x5B);
  public static final Color SUCCESS = new Color(0x16, 0x7A, 0x4A);
  public static final Color WARN = new Color(0xC2, 0x7A, 0x14);
  public static final Color DANGER = new Color(0xDC, 0x26, 0x26);
  public static final Color PRIMARY = TEAL_DARK;
  public static final Color SECONDARY = new Color(0xE4, 0xE4, 0xE7);
  public static final Color TABLE_ALT = new Color(0xF4, 0xF4, 0xF5);
  public static final Color TABLE_HEADER_BG = new Color(0xEC, 0xF4, 0xF3);
  public static final Color SELECTION = new Color(0xCC, 0xEB, 0xE7);

  /** Kept so existing screens keep compiling; maps to the new palette. */
  public static final Color NAVY = CHARCOAL;
  public static final Color GOLD = TEAL;

  public static final Font FONT_BRAND = new Font(Font.SANS_SERIF, Font.BOLD, 22);
  public static final Font FONT_TITLE = new Font(Font.SANS_SERIF, Font.BOLD, 14);
  public static final Font FONT_BODY = new Font(Font.SANS_SERIF, Font.PLAIN, 13);
  public static final Font FONT_BUTTON = new Font(Font.SANS_SERIF, Font.BOLD, 12);
  public static final Font FONT_MONO = new Font(Font.MONOSPACED, Font.PLAIN, 13);

  private static boolean applied;

  private UiTheme() {
  }

  public static void apply() {
    if (applied) {
      return;
    }
    try {
      UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
    } catch (Exception ignored) {
      try {
        UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
      } catch (Exception ignored2) {
        // keep default
      }
    }

    UIManager.put("Panel.background", new ColorUIResource(SURFACE));
    UIManager.put("OptionPane.background", new ColorUIResource(SURFACE));
    UIManager.put("OptionPane.messageForeground", new ColorUIResource(TEXT));
    UIManager.put("Label.foreground", new ColorUIResource(TEXT));
    UIManager.put("TextField.background", new ColorUIResource(PANEL));
    UIManager.put("TextField.foreground", new ColorUIResource(TEXT));
    UIManager.put("TextField.caretForeground", new ColorUIResource(TEXT));
    UIManager.put("TextArea.background", new ColorUIResource(PANEL));
    UIManager.put("TextArea.foreground", new ColorUIResource(TEXT));
    UIManager.put("TextArea.caretForeground", new ColorUIResource(TEXT));
    UIManager.put("List.background", new ColorUIResource(PANEL));
    UIManager.put("List.foreground", new ColorUIResource(TEXT));
    UIManager.put("List.selectionBackground", new ColorUIResource(SELECTION));
    UIManager.put("List.selectionForeground", new ColorUIResource(TEXT));
    UIManager.put("Table.background", new ColorUIResource(PANEL));
    UIManager.put("Table.foreground", new ColorUIResource(TEXT));
    UIManager.put("Table.selectionBackground", new ColorUIResource(SELECTION));
    UIManager.put("Table.selectionForeground", new ColorUIResource(TEXT));
    UIManager.put("TableHeader.background", new ColorUIResource(TABLE_HEADER_BG));
    UIManager.put("TableHeader.foreground", new ColorUIResource(CHARCOAL));
    UIManager.put("TabbedPane.foreground", new ColorUIResource(TEXT));
    UIManager.put("TabbedPane.selected", new ColorUIResource(PANEL));
    UIManager.put("TabbedPane.background", new ColorUIResource(SURFACE));
    UIManager.put("ComboBox.foreground", new ColorUIResource(TEXT));
    UIManager.put("ComboBox.background", new ColorUIResource(PANEL));
    UIManager.put("Button.focus", new ColorUIResource(new Color(0, 0, 0, 0)));
    applied = true;
  }

  public static void styleRoot(JPanel panel) {
    panel.setOpaque(true);
    panel.setBackground(SURFACE);
    panel.setForeground(TEXT);
    panel.setBorder(new EmptyBorder(14, 14, 14, 14));
  }

  public static JPanel titledPanel(String title, JComponent content) {
    JPanel wrap = new JPanel(new java.awt.BorderLayout());
    wrap.setOpaque(true);
    wrap.setBackground(PANEL);
    wrap.setForeground(TEXT);
    wrap.setBorder(titledBorder(title));
    wrap.add(content, java.awt.BorderLayout.CENTER);
    return wrap;
  }

  public static Border titledBorder(String title) {
    TitledBorder titled = BorderFactory.createTitledBorder(
        new CompoundBorder(
            new MatteBorder(0, 3, 0, 0, TEAL),
            new LineBorder(BORDER, 1, true)),
        title,
        TitledBorder.LEFT,
        TitledBorder.TOP,
        FONT_TITLE,
        CHARCOAL);
    return new CompoundBorder(titled, new EmptyBorder(8, 10, 10, 10));
  }

  public static JButton primaryButton(String text) {
    return styledButton(text, PRIMARY, Color.WHITE);
  }

  public static JButton secondaryButton(String text) {
    return styledButton(text, SECONDARY, TEXT);
  }

  public static JButton dangerButton(String text) {
    return styledButton(text, DANGER, Color.WHITE);
  }

  public static JButton accentButton(String text) {
    return styledButton(text, TEAL, Color.WHITE);
  }

  private static JButton styledButton(String text, Color background, Color foreground) {
    JButton button = new JButton(text) {
      @Override
      protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color fill = background;
        if (getModel().isPressed()) {
          fill = background.darker();
        } else if (getModel().isRollover()) {
          fill = brighter(background, 16);
        }
        g2.setColor(fill);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
        g2.setColor(new Color(0, 0, 0, 28));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
        g2.dispose();
        super.paintComponent(g);
      }
    };
    button.setForeground(foreground);
    button.setBackground(background);
    button.setFont(FONT_BUTTON);
    button.setFocusPainted(false);
    button.setContentAreaFilled(false);
    button.setOpaque(false);
    button.setBorder(new EmptyBorder(11, 16, 11, 16));
    button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    return button;
  }

  private static Color brighter(Color color, int amount) {
    return new Color(
        Math.min(255, color.getRed() + amount),
        Math.min(255, color.getGreen() + amount),
        Math.min(255, color.getBlue() + amount));
  }

  public static void styleList(JList<?> list) {
    list.setFont(FONT_BODY);
    list.setBackground(PANEL);
    list.setForeground(TEXT);
    list.setSelectionBackground(SELECTION);
    list.setSelectionForeground(TEXT);
    list.setFixedCellHeight(30);
    list.setBorder(new EmptyBorder(4, 8, 4, 8));
    list.setOpaque(true);
  }

  public static void styleListScroll(JScrollPane scroll) {
    scroll.getViewport().setBackground(PANEL);
    scroll.getViewport().setOpaque(true);
    scroll.setBackground(PANEL);
    scroll.setBorder(BorderFactory.createLineBorder(BORDER));
  }

  public static void styleInfoArea(JTextArea area) {
    area.setEditable(false);
    area.setFont(FONT_MONO);
    area.setBackground(PANEL);
    area.setForeground(TEXT);
    area.setCaretColor(TEXT);
    area.setSelectedTextColor(TEXT);
    area.setSelectionColor(SELECTION);
    area.setOpaque(true);
    area.setBorder(new EmptyBorder(10, 12, 10, 12));
    area.setLineWrap(false);
  }

  public static void styleTextField(JTextField field) {
    field.setFont(FONT_BODY);
    field.setBackground(PANEL);
    field.setForeground(TEXT);
    field.setCaretColor(TEXT);
    field.setSelectedTextColor(TEXT);
    field.setSelectionColor(SELECTION);
    field.setOpaque(true);
    field.setBorder(new CompoundBorder(
        new LineBorder(BORDER, 1, true),
        new EmptyBorder(7, 10, 7, 10)));
  }

  public static void styleTable(JTable table) {
    table.setFont(FONT_BODY);
    table.setRowHeight(30);
    table.setShowHorizontalLines(true);
    table.setShowVerticalLines(false);
    table.setGridColor(BORDER);
    table.setSelectionBackground(SELECTION);
    table.setSelectionForeground(TEXT);
    table.setBackground(PANEL);
    table.setForeground(TEXT);
    table.setOpaque(true);
    table.setFillsViewportHeight(true);
    table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    table.setIntercellSpacing(new Dimension(0, 1));

    JTableHeader header = table.getTableHeader();
    header.setFont(FONT_TITLE);
    header.setBackground(TABLE_HEADER_BG);
    header.setForeground(CHARCOAL);
    header.setOpaque(true);
    header.setReorderingAllowed(false);

    table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
      @Override
      public Component getTableCellRendererComponent(
          JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
        if (isSelected) {
          c.setBackground(SELECTION);
          c.setForeground(TEXT);
        } else {
          c.setBackground(row % 2 == 0 ? PANEL : TABLE_ALT);
          c.setForeground(TEXT);
        }
        setBorder(new EmptyBorder(0, 8, 0, 8));
        return c;
      }
    });
  }

  public static JLabel mutedLabel(String text) {
    JLabel label = new JLabel(text);
    label.setFont(FONT_BODY);
    label.setForeground(MUTED);
    label.setOpaque(false);
    return label;
  }

  public static JLabel bodyLabel(String text) {
    JLabel label = new JLabel(text);
    label.setFont(FONT_BODY);
    label.setForeground(TEXT);
    label.setOpaque(false);
    return label;
  }

  public static JPanel buttonColumn(JButton... buttons) {
    JPanel column = new JPanel(new java.awt.GridLayout(0, 1, 8, 8));
    column.setOpaque(false);
    column.setBorder(new EmptyBorder(0, 6, 0, 4));
    for (JButton button : buttons) {
      column.add(button);
    }
    return column;
  }

  public static JPanel buttonRow(JButton... buttons) {
    JPanel row = new JPanel(new java.awt.GridLayout(1, 0, 8, 8));
    row.setOpaque(false);
    row.setBorder(new EmptyBorder(0, 0, 4, 0));
    for (JButton button : buttons) {
      row.add(button);
    }
    return row;
  }

  public static Insets dialogInsets() {
    return new Insets(4, 4, 4, 4);
  }
}
