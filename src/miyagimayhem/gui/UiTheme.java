package miyagimayhem.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
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
    static final Color ACCENT = new Color(0xE8B923);
    static final Color ACCENT_TEXT = new Color(0x1B1F2A);
    static final Color DANGER = new Color(0xE05A5A);
    static final Color INFO = new Color(0x62C7E8);
    static final Color OK = new Color(0x6CCB8A);

    static final Font BODY = new Font(Font.SANS_SERIF, Font.PLAIN, 15);
    static final Font BOLD = new Font(Font.SANS_SERIF, Font.BOLD, 15);
    static final Font HEADING = new Font(Font.SANS_SERIF, Font.BOLD, 26);
    static final Font MONO = new Font(Font.MONOSPACED, Font.PLAIN, 15);

    private UiTheme() {
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
