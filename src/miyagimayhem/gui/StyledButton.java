package miyagimayhem.gui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JButton;

/**
 * A flat, rounded button that matches the dark theme. The primary style is
 * gold for the main action on a screen, the secondary style is subdued.
 */
class StyledButton extends JButton {

    private final boolean primary;
    private boolean silent;

    StyledButton(String text, boolean primary) {
        super(text);
        this.primary = primary;
        setFont(UiTheme.BOLD);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setForeground(primary ? UiTheme.ACCENT_TEXT : UiTheme.TEXT);
    }

    /** A silent button doesn't play the click sound when pressed. */
    void setSilent(boolean silent) {
        this.silent = silent;
    }

    boolean isSilent() {
        return silent;
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();
        return new Dimension(size.width + 24, Math.max(size.height + 8, 38));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Color base = primary ? UiTheme.ACCENT : UiTheme.FIELD;
        if (!isEnabled()) {
            base = new Color(0x2C3244);
        } else if (getModel().isPressed()) {
            base = base.darker();
        } else if (getModel().isRollover()) {
            base = base.brighter();
        }
        g2.setColor(base);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
        g2.dispose();
        setForeground(isEnabled() ? (primary ? UiTheme.ACCENT_TEXT : UiTheme.TEXT) : UiTheme.MUTED);
        super.paintComponent(g);
    }
}
