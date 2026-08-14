package boundary;

import java.util.Scanner;

/**
 * Shared console presentation helpers for TARUMT Resorts CLI menus.
 *
 * Uses ASCII frames so Windows cmd/PowerShell stay aligned.
 * Uses basic 16-color ANSI only when the terminal can actually render them
 * (Windows Terminal, Cursor, xterm). Never truecolor, never bright-white.
 */
public final class ConsoleStyle {

  public static final int WIDTH = 64;

  private static final String RESET = "\033[0m";
  private static final String BOLD = "\033[1m";
  private static final String CYAN = "\033[36m";
  private static final String YELLOW = "\033[33m";
  private static final String GREEN = "\033[32m";
  private static final String RED = "\033[31m";
  private static final String GRAY = "\033[90m";

  private static final boolean COLOR = supportsColor();

  private ConsoleStyle() {
  }

  private static boolean supportsColor() {
    if (System.getenv("NO_COLOR") != null) {
      return false;
    }
    if (System.getenv("WT_SESSION") != null
        || System.getenv("ANSICON") != null
        || System.getenv("ConEmuANSI") != null) {
      return true;
    }
    String term = System.getenv("TERM");
    if (term != null) {
      String t = term.toLowerCase();
      if (t.contains("xterm") || t.contains("color") || t.contains("ansi") || t.equals("cygwin")) {
        return true;
      }
    }
    String colorTerm = System.getenv("COLORTERM");
    return colorTerm != null && !colorTerm.isEmpty();
  }

  private static String c(String code, String text) {
    if (!COLOR || text == null) {
      return text == null ? "" : text;
    }
    return code + text + RESET;
  }

  public static void blank() {
    System.out.println();
  }

  /** Clear the terminal so each menu / action starts on a fresh screen. */
  public static void clear() {
    try {
      String os = System.getProperty("os.name", "").toLowerCase();
      if (os.contains("win")) {
        new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
      } else {
        System.out.print("\033[H\033[2J\033[3J");
        System.out.flush();
      }
    } catch (Exception ignored) {
      for (int i = 0; i < 40; i++) {
        System.out.println();
      }
    }
  }

  public static void pause() {
    pause(null);
  }

  public static void pause(Scanner scanner) {
    blank();
    prompt("Press Enter to continue...");
    try {
      if (scanner != null) {
        scanner.nextLine();
      } else {
        System.in.read();
        while (System.in.available() > 0) {
          int ch = System.in.read();
          if (ch == '\n' || ch == -1) {
            break;
          }
        }
      }
    } catch (Exception ignored) {
    }
  }

  public static void rule() {
    System.out.println("  " + c(GRAY, repeat('-', WIDTH)));
  }

  public static void header(String title, String subtitle) {
    clear();
    blank();
    String edge = "+" + repeat('=', WIDTH - 2) + "+";
    String empty = "|" + repeat(' ', WIDTH - 2) + "|";
    System.out.println("  " + c(GRAY, edge));
    System.out.println("  " + c(GRAY, "|") + c(BOLD + CYAN, center(title, WIDTH - 2)) + c(GRAY, "|"));
    if (subtitle != null && !subtitle.isEmpty()) {
      System.out.println("  " + c(GRAY, "|") + c(GRAY, center(subtitle, WIDTH - 2)) + c(GRAY, "|"));
    }
    System.out.println("  " + c(GRAY, empty));
    System.out.println("  " + c(GRAY, edge));
    blank();
  }

  public static void section(String title) {
    blank();
    System.out.println("  " + c(BOLD + CYAN, title));
    System.out.println("  " + c(GRAY, repeat('-', Math.max(24, title.length() + 4))));
  }

  public static void menuItem(int number, String label) {
    String num = String.format("%d.", number);
    System.out.println("    " + c(BOLD + YELLOW, String.format("%-4s", num)) + " " + label);
  }

  public static void menuExit(int number, String label) {
    blank();
    rule();
    String num = String.format("%d.", number);
    System.out.println("    " + c(GRAY, String.format("%-4s", num)) + " " + c(GRAY, label));
  }

  public static void prompt(String label) {
    System.out.print("    " + c(BOLD + CYAN, "> ") + label);
  }

  public static void info(String message) {
    System.out.println("    " + message);
  }

  public static void success(String message) {
    System.out.println("    " + c(GREEN, "[OK]  " + message));
  }

  public static void warn(String message) {
    System.out.println("    " + c(YELLOW, "[!]   " + message));
  }

  public static void error(String message) {
    System.out.println("    " + c(RED, "[X]   " + message));
  }

  public static void keyValue(String key, String value) {
    System.out.println("    " + c(GRAY, String.format("%-14s", key + ":"))
        + (value == null ? "-" : value));
  }

  public static void tableHeader(String row) {
    System.out.println("    " + c(BOLD + CYAN, row));
    System.out.println("    " + c(GRAY, repeat('-', Math.max(row.length(), 48))));
  }

  public static void tableRow(String row) {
    System.out.println("    " + row);
  }

  public static void tableFooter(String summary) {
    System.out.println("    " + c(GRAY, repeat('-', 48)));
    if (summary != null && !summary.isEmpty()) {
      System.out.println("    " + summary);
    }
  }

  private static String center(String text, int width) {
    if (text == null) {
      text = "";
    }
    if (text.length() >= width) {
      return text.substring(0, width);
    }
    int pad = width - text.length();
    int left = pad / 2;
    int right = pad - left;
    return repeat(' ', left) + text + repeat(' ', right);
  }

  private static String repeat(char ch, int count) {
    if (count <= 0) {
      return "";
    }
    StringBuilder sb = new StringBuilder(count);
    for (int i = 0; i < count; i++) {
      sb.append(ch);
    }
    return sb.toString();
  }
}
