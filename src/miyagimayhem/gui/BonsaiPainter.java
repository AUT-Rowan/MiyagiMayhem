package miyagimayhem.gui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

/**
 * Draws a dark bonsai silhouette in front of a deep red sun. It is drawn
 * with shapes so there is no image file to ship, and it scales with the
 * window.
 */
final class BonsaiPainter {

    private static final Color SUN = new Color(0xCF, 0x2A, 0x2A, 55);
    private static final Color TREE = new Color(0x08, 0x0A, 0x10);

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

        g2.setColor(TREE);
        drawTrunk(g2);
        drawBranches(g2);
        drawFoliage(g2);
        drawPot(g2);
        g2.dispose();
    }

    /** A trunk that is thick at the bottom and thins towards the top. */
    private static void drawTrunk(Graphics2D g2) {
        CubicCurve2D lower = new CubicCurve2D.Double(50, 86, 40, 76, 62, 68, 48, 54);
        CubicCurve2D upper = new CubicCurve2D.Double(48, 54, 44, 48, 56, 44, 50, 36);
        stampAlong(g2, lower, 5.5, 3.6);
        stampAlong(g2, upper, 3.6, 2.2);

        // Roots spreading over the soil.
        Path2D roots = new Path2D.Double();
        roots.moveTo(34, 88);
        roots.curveTo(42, 87, 46, 82, 47, 76);
        roots.lineTo(55, 76);
        roots.curveTo(56, 82, 60, 87, 68, 88);
        roots.closePath();
        g2.fill(roots);
    }

    private static void drawBranches(Graphics2D g2) {
        stampAlong(g2, new CubicCurve2D.Double(49, 66, 42, 66, 36, 62, 30, 56), 2.6, 1.4);
        stampAlong(g2, new CubicCurve2D.Double(51, 58, 60, 58, 68, 56, 74, 51), 2.6, 1.4);
        stampAlong(g2, new CubicCurve2D.Double(51, 44, 58, 42, 64, 38, 70, 33), 2.2, 1.2);
        stampAlong(g2, new CubicCurve2D.Double(50, 38, 46, 34, 42, 30, 40, 26), 2.2, 1.2);
    }

    private static void drawFoliage(Graphics2D g2) {
        cloud(g2, 40, 23, 40, 20);
        cloud(g2, 71, 29, 22, 12);
        cloud(g2, 25, 53, 28, 14);
        cloud(g2, 75, 50, 30, 14);
    }

    /**
     * A rounded cloud of leaves: a row of overlapping bumps on top of a flat
     * oval. All parts use the same solid colour so they merge into one shape.
     */
    private static void cloud(Graphics2D g2, double centreX, double centreY, double width, double height) {
        g2.fill(new Ellipse2D.Double(centreX - width / 2, centreY - height * 0.05, width, height * 0.55));
        int bumps = 5;
        for (int i = 0; i < bumps; i++) {
            double along = (double) i / (bumps - 1);
            double radius = height * (0.34 + 0.1 * Math.sin(i * 1.9 + centreX));
            double bx = centreX - width * 0.34 + along * width * 0.68;
            double by = centreY - height * 0.12 - height * 0.2 * Math.sin(Math.PI * along);
            g2.fill(new Ellipse2D.Double(bx - radius, by - radius, radius * 2, radius * 2));
        }
    }

    private static void drawPot(Graphics2D g2) {
        g2.fill(new Ellipse2D.Double(32, 83, 36, 7));
        g2.fill(new RoundRectangle2D.Double(24, 86, 52, 4, 2, 2));
        Path2D body = new Path2D.Double();
        body.moveTo(27, 89);
        body.lineTo(73, 89);
        body.lineTo(69, 97);
        body.lineTo(31, 97);
        body.closePath();
        g2.fill(body);
        g2.fill(new RoundRectangle2D.Double(33, 96, 8, 3.5, 1, 1));
        g2.fill(new RoundRectangle2D.Double(59, 96, 8, 3.5, 1, 1));
    }

    /** Fills a thick curve by stamping circles along it, shrinking from one width to another. */
    private static void stampAlong(Graphics2D g2, CubicCurve2D curve, double startWidth, double endWidth) {
        int steps = 60;
        for (int i = 0; i <= steps; i++) {
            double t = (double) i / steps;
            double mt = 1 - t;
            double px = mt * mt * mt * curve.getX1() + 3 * mt * mt * t * curve.getCtrlX1()
                    + 3 * mt * t * t * curve.getCtrlX2() + t * t * t * curve.getX2();
            double py = mt * mt * mt * curve.getY1() + 3 * mt * mt * t * curve.getCtrlY1()
                    + 3 * mt * t * t * curve.getCtrlY2() + t * t * t * curve.getY2();
            double radius = (startWidth + (endWidth - startWidth) * t) / 2;
            g2.fill(new Ellipse2D.Double(px - radius, py - radius, radius * 2, radius * 2));
        }
    }
}
