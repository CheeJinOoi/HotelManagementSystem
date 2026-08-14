package boundary;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

/**
 * Short intro animation shown before the operations GUI appears.
 */
public final class IntroSplash {

  private IntroSplash() {
  }

  public static void showThen(Runnable onDone) {
    JWindow splash = new JWindow();
    SplashPanel panel = new SplashPanel();
    splash.setContentPane(panel);
    splash.setSize(560, 340);
    splash.setLocationRelativeTo(null);
    splash.setAlwaysOnTop(true);
    splash.setVisible(true);

    Timer timer = new Timer(16, null);
    timer.addActionListener((ActionEvent e) -> {
      if (!panel.tick()) {
        timer.stop();
        splash.setVisible(false);
        splash.dispose();
        onDone.run();
      }
    });
    timer.start();
  }

  private static final class SplashPanel extends JPanel {
    private float titleAlpha = 0f;
    private float subAlpha = 0f;
    private float bar = 0f;
    private int frame = 0;

    private SplashPanel() {
      setPreferredSize(new Dimension(560, 340));
      setBackground(UiTheme.CHARCOAL);
      setLayout(null);
      setBorder(new EmptyBorder(0, 0, 0, 0));
    }

    boolean tick() {
      frame++;
      if (frame <= 28) {
        titleAlpha = Math.min(1f, frame / 28f);
      }
      if (frame > 18 && frame <= 50) {
        subAlpha = Math.min(1f, (frame - 18) / 32f);
      }
      if (frame > 24) {
        bar = Math.min(1f, (frame - 24) / 70f);
      }
      repaint();
      return frame < 110;
    }

    @Override
    protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

      int w = getWidth();
      int h = getHeight();
      g2.setColor(UiTheme.CHARCOAL);
      g2.fillRect(0, 0, w, h);

      g2.setColor(UiTheme.TEAL);
      g2.fillRect(0, 0, w, 4);

      g2.setComposite(java.awt.AlphaComposite.SrcOver.derive(titleAlpha));
      g2.setColor(Color.WHITE);
      g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 28));
      String brand = "TARUMT RESORTS";
      int bw = g2.getFontMetrics().stringWidth(brand);
      g2.drawString(brand, (w - bw) / 2, h / 2 - 18);

      g2.setComposite(java.awt.AlphaComposite.SrcOver.derive(subAlpha));
      g2.setColor(new Color(0x99, 0xF6, 0xE4));
      g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
      String sub = "Operations Console";
      int sw = g2.getFontMetrics().stringWidth(sub);
      g2.drawString(sub, (w - sw) / 2, h / 2 + 12);

      int barW = 280;
      int barH = 6;
      int bx = (w - barW) / 2;
      int by = h / 2 + 52;
      g2.setComposite(java.awt.AlphaComposite.SrcOver);
      g2.setColor(new Color(0x2A, 0x33, 0x3C));
      g2.fillRoundRect(bx, by, barW, barH, 8, 8);
      g2.setColor(UiTheme.TEAL);
      g2.fillRoundRect(bx, by, Math.max(8, (int) (barW * bar)), barH, 8, 8);

      g2.setColor(new Color(0x71, 0x71, 0x7A));
      g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
      String hint = "Loading workspace...";
      int hw = g2.getFontMetrics().stringWidth(hint);
      g2.drawString(hint, (w - hw) / 2, by + 28);

      g2.dispose();
    }
  }
}
