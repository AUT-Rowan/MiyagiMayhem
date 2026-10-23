package miyagimayhem.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

/**
 * Draws a faint bonsai tree with a deep red sun behind it. It is drawn with
 * shapes so there is no image file to ship, and it scales with the window.
 */
final class BonsaiPainter {

    private static final Color SUN = new Color(0xCF, 0x2A, 0x2A, 55);
    private static final Color TRUNK = new Color(0x5A, 0x43, 0x3A, 230);
    private static final Color POT = new Color(0x3B, 0x44, 0x5E, 255);
    private static final Color LEAVES = new Color(0x3E, 0x6B, 0x4F, 215);
    private static final Color LEAVES_LIGHT = new Color(0x4F, 0x82, 0x60, 215);

    /** Centres of the leaf clusters in a 100 x 100 drawing space. */
    private static final double[][] CLUSTERS = {
        {49, 30, 1.15}, {27, 56, 0.95}, {75, 47, 1.0}, {22, 38, 0.6}, {80, 64, 0.65}
    };

    private BonsaiPainter() {
    }

    /**
     * Paints the tree so it fits in a square of the given size whose
     * top-left corner is (x, y).
     */
    static void paint(Graphics2D g, int x, int y, int size) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.translate(x, y);
        g2.scale(size / 100.0, size / 100.0);

        g2.setColor(SUN);
        g2.fill(new Ellipse2D.Double(14, 2, 72, 72));

        drawTrunkAndBranches(g2);
        drawFoliage(g2);
        drawPot(g2);
        g2.dispose();
    }

    private static void drawTrunkAndBranches(Graphics2D g2) {
        g2.setColor(TRUNK);

        Path2D trunk = new Path2D.Double();
        trunk.moveTo(50, 88);
        trunk.curveTo(38, 78, 62, 68, 47, 52);
        trunk.curveTo(44, 46, 50, 40, 49, 32);
        g2.setStroke(new BasicStroke(5.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(trunk);

        g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Path2D left = new Path2D.Double();
        left.moveTo(52, 70);
        left.curveTo(42, 68, 34, 64, 28, 57);
        g2.draw(left);

        Path2D right = new Path2D.Double();
        right.moveTo(48, 58);
        right.curveTo(58, 58, 68, 55, 75, 48);
        g2.draw(right);

        g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Path2D lowRight = new Path2D.Double();
        lowRight.moveTo(56, 74);
        lowRight.curveTo(66, 74, 74, 70, 80, 64);
        g2.draw(lowRight);

        Path2D upLeft = new Path2D.Double();
        upLeft.moveTo(47, 46);
        upLeft.curveTo(38, 44, 28, 42, 22, 38);
        g2.draw(upLeft);
    }

    private static void drawFoliage(Graphics2D g2) {
        for (double[] cluster : CLUSTERS) {
            double cx = cluster[0];
            double cy = cluster[1];
            double scale = cluster[2];
            // Several overlapping ovals make a soft, pad-shaped cloud of leaves.
            g2.setColor(LEAVES);
            g2.fill(oval(cx, cy + 1.5 * scale, 15 * scale, 6.5 * scale));
            g2.setColor(LEAVES_LIGHT);
            g2.fill(oval(cx - 6 * scale, cy - 1, 9 * scale, 5.5 * scale));
            g2.fill(oval(cx + 6 * scale, cy - 1.5 * scale, 9 * scale, 5.5 * scale));
            g2.setColor(LEAVES);
            g2.fill(oval(cx, cy - 3.5 * scale, 10 * scale, 5 * scale));
        }
    }

    private static Ellipse2D oval(double centreX, double centreY, double radiusX, double radiusY) {
        return new Ellipse2D.Double(centreX - radiusX, centreY - radiusY, radiusX * 2, radiusY * 2);
    }

    private static void drawPot(Graphics2D g2) {
        g2.setColor(POT);
        Path2D body = new Path2D.Double();
        body.moveTo(33, 88);
        body.lineTo(67, 88);
        body.lineTo(62, 99);
        body.lineTo(38, 99);
        body.closePath();
        g2.fill(body);
        g2.fill(new RoundRectangle2D.Double(29, 85, 42, 5, 3, 3));
    }
}
