package miyagimayhem.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.Icon;

/**
 * A small round swatch of a contestant's colour, drawn next to their name.
 */
class ColourDot implements Icon {

    private static final int SIZE = 14;
    private final Color colour;

    ColourDot(Color colour) {
        this.colour = colour;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(colour);
        g2.fillOval(x, y, SIZE, SIZE);
        g2.setColor(UiTheme.MUTED);
        g2.drawOval(x, y, SIZE, SIZE);
        g2.dispose();
    }

    @Override
    public int getIconWidth() {
        return SIZE;
    }

    @Override
    public int getIconHeight() {
        return SIZE;
    }
}
