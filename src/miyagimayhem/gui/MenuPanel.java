package miyagimayhem.gui;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Main menu: the starting point of the application. A bonsai picture
 * (bonsai.png, stored next to this class) sits in the background.
 */
class MenuPanel extends JPanel {

    private static final String BACKGROUND_FILE = "bonsai.png";

    private final BufferedImage background = loadBackground();

    MenuPanel(Navigator navigator, Runnable onQuit) {
        setLayout(new BorderLayout());
        setBackground(UiTheme.BACKGROUND);

        add(buildMenu(navigator, onQuit), BorderLayout.CENTER);
    }

    /** The background picture, or null if the file couldn't be read (the menu still works). */
    private static BufferedImage loadBackground() {
        try (InputStream in = MenuPanel.class.getResourceAsStream(BACKGROUND_FILE)) {
            return in == null ? null : ImageIO.read(in);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (background == null) {
            return;
        }
        // Scale the picture to fit the space to the right of the buttons, keeping its shape.
        double scale = Math.min(getHeight() * 0.8 / background.getHeight(),
                getWidth() * 0.30 / background.getWidth());
        int width = (int) (background.getWidth() * scale);
        int height = (int) (background.getHeight() * scale);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.drawImage(background, (int) (getWidth() * 0.86 - width / 2.0), getHeight() - height - 24,
                width, height, null);
        g2.dispose();
    }

    private JPanel buildMenu(Navigator navigator, Runnable onQuit) {
        JPanel menu = new JPanel(new GridBagLayout());
        menu.setOpaque(false);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(4, 0, 4, 0);
        c.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("- MIYAGI MAYHEM -", SwingConstants.CENTER);
        title.setFont(UiTheme.HEADING.deriveFont(44f));
        title.setForeground(UiTheme.ACCENT);
        menu.add(title, c);

        c.gridy++;
        JLabel subtitle = new JLabel("A reality show simulator", SwingConstants.CENTER);
        subtitle.setFont(UiTheme.BODY);
        subtitle.setForeground(UiTheme.MUTED);
        c.insets = new Insets(0, 0, 30, 0);
        menu.add(subtitle, c);

        c.insets = new Insets(6, 0, 6, 0);
        addButton(menu, "New Game", true, navigator::showSetup, c);
        addButton(menu, "Game History", false, navigator::showHistory, c);
        addButton(menu, "Edit Show Text", false, navigator::showFlavor, c);
        addButton(menu, "Quit", false, onQuit, c);
        return menu;
    }

    private static void addButton(JPanel menu, String text, boolean primary, Runnable action,
            GridBagConstraints c) {
        c.gridy++;
        JButton button = new StyledButton(text, primary);
        button.addActionListener(e -> action.run());
        menu.add(button, c);
    }
}
