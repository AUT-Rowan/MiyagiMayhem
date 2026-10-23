package miyagimayhem.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import javax.swing.border.Border;
import miyagimayhem.db.DataAccessException;

/**
 * Colours, fonts and small helpers shared by every screen, so the look of
 * the whole application can be changed in one place.
 */
final class UiTheme {

    static final Color BACKGROUND = new Color(0x1B1F2A);
    static final Color PANEL = new Color(0x252B3A);
    static final Color FIELD = new Color(0x303850);
    static final Color TEXT = new Color(0xECEFF4);
    static final Color MUTED = new Color(0x9AA5B8);
    static final Color ACCENT = new Color(0xCF2A2A);
    static final Color ACCENT_TEXT = Color.WHITE;
    static final Color GOLD = new Color(0xE8B923);
    static final Color DANGER = new Color(0xE05A5A);
    static final Color INFO = new Color(0x62C7E8);
    static final Color OK = new Color(0x6CCB8A);

    private static final String SANS = pickFamily(Font.SANS_SERIF,
            "Segoe UI", "Helvetica Neue", "Arial", "Liberation Sans", "DejaVu Sans");
    private static final String MONOSPACE = pickFamily(Font.MONOSPACED,
            "Consolas", "Menlo", "Courier New", "Liberation Mono", "DejaVu Sans Mono");

    static final Font BODY = new Font(SANS, Font.PLAIN, 15);
    static final Font BOLD = new Font(SANS, Font.BOLD, 15);
    static final Font HEADING = new Font(SANS, Font.BOLD, 26);
    static final Font MONO = new Font(MONOSPACE, Font.PLAIN, 15);

    private UiTheme() {
    }

    /** First of the preferred font families that is installed, else the fallback. */
    private static String pickFamily(String fallback, String... preferred) {
        Set<String> installed = new HashSet<>(Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
        for (String family : preferred) {
            if (installed.contains(family)) {
                return family;
            }
        }
        return fallback;
    }

    /**
     * Makes every standard component (dialogs, tables, combo boxes, tabs and
     * so on) use the theme's sans-serif font instead of whatever the look
     * and feel picks. Call once after the look and feel is set.
     */
    static void installDefaultFonts() {
        UIDefaults defaults = UIManager.getDefaults();
        for (Object key : Collections.list(defaults.keys())) {
            Object value = defaults.get(key);
            if (value instanceof FontUIResource) {
                Font old = (Font) value;
                defaults.put(key, new FontUIResource(SANS, old.getStyle(), old.getSize()));
            }
        }
    }

    static Border padding(int size) {
        return BorderFactory.createEmptyBorder(size, size, size, size);
    }

    static JLabel heading(String text) {
        JLabel label = new JLabel(text);
        label.setFont(HEADING);
        label.setForeground(ACCENT);
        return label;
    }

    static JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setFont(BODY);
        label.setForeground(TEXT);
        return label;
    }

    static void showDatabaseError(Component parent, DataAccessException e) {
        JOptionPane.showMessageDialog(parent, e.getMessage(), "Database problem", JOptionPane.ERROR_MESSAGE);
    }
}
