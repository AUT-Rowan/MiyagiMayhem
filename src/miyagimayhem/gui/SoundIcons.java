package miyagimayhem.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import javax.swing.Icon;

/**
 * Small drawn icons for the sound buttons (a music note and a speaker). A
 * red slash is drawn through the icon when that sound is switched off.
 * They are drawn with shapes so they look the same on every computer, with
 * no dependence on which symbols a font happens to have.
 */
final class SoundIcons {

    private static final int SIZE = 22;

    private SoundIcons() {
    }

    /** A music note, with a slash through it when music is off. */
    static Icon music(boolean on) {
        return new DrawnIcon(on) {
            @Override
            void draw(Graphics2D g2) {
                g2.fill(new Ellipse2D.Double(3, 14, 7, 5.5));
                g2.fill(new Ellipse2D.Double(12, 12, 7, 5.5));
                g2.setStroke(new BasicStroke(1.8f));
                g2.draw(new Line2D.Double(9.2, 16, 9.2, 4));
                g2.draw(new Line2D.Double(18.2, 14, 18.2, 2));
                Path2D beam = new Path2D.Double();
                beam.moveTo(9.2, 3);
                beam.lineTo(18.2, 1);
                beam.lineTo(18.2, 4.5);
                beam.lineTo(9.2, 6.5);
                beam.closePath();
                g2.fill(beam);
            }
        };
    }

    /** A speaker with sound waves, with a slash through it when sound effects are off. */
    static Icon speaker(boolean on) {
        return new DrawnIcon(on) {
            @Override
            void draw(Graphics2D g2) {
                Path2D body = new Path2D.Double();
                body.moveTo(2, 8);
                body.lineTo(6, 8);
                body.lineTo(11, 3.5);
                body.lineTo(11, 18.5);
                body.lineTo(6, 14);
                body.lineTo(2, 14);
                body.closePath();
                g2.fill(body);
                g2.setStroke(new BasicStroke(1.7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.draw(new Arc2D.Double(8, 6, 7, 10, -60, 120, Arc2D.OPEN));
                g2.draw(new Arc2D.Double(7, 3, 12, 16, -60, 120, Arc2D.OPEN));
            }
        };
    }

    private abstract static class DrawnIcon implements Icon {
        private final boolean on;

        DrawnIcon(boolean on) {
            this.on = on;
        }

        abstract void draw(Graphics2D g2);

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.translate(x, y);
            boolean usable = c == null || c.isEnabled();
            g2.setColor(!usable ? UiTheme.MUTED : (on ? UiTheme.TEXT : UiTheme.MUTED));
            draw(g2);
            if (!on) {
                g2.setColor(UiTheme.DANGER);
                g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.draw(new Line2D.Double(2, 20, 20, 2));
            }
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
}
