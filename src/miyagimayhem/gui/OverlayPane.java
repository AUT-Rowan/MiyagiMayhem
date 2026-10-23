package miyagimayhem.gui;

import java.awt.Dimension;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;

/**
 * Shows one component filling the window with a second small component
 * floating in its top right corner, so the second one appears on every
 * screen the first one switches between.
 */
class OverlayPane extends JLayeredPane {

    private static final int MARGIN_RIGHT = 12;
    private static final int MARGIN_TOP = 10;

    private final JComponent base;
    private final JComponent overlay;

    OverlayPane(JComponent base, JComponent overlay) {
        this.base = base;
        this.overlay = overlay;
        add(base, DEFAULT_LAYER);
        add(overlay, PALETTE_LAYER);
    }

    @Override
    public Dimension getPreferredSize() {
        return base.getPreferredSize();
    }

    @Override
    public void doLayout() {
        base.setBounds(0, 0, getWidth(), getHeight());
        Dimension size = overlay.getPreferredSize();
        overlay.setBounds(getWidth() - size.width - MARGIN_RIGHT, MARGIN_TOP, size.width, size.height);
    }
}
