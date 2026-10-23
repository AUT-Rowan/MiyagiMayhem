package miyagimayhem.gui;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Main menu: the starting point of the application. It has a bonsai tree
 * in the background.
 */
class MenuPanel extends JPanel {

    MenuPanel(Navigator navigator, Runnable onQuit) {
        setLayout(new BorderLayout());
        setBackground(UiTheme.BACKGROUND);

        add(buildMenu(navigator, onQuit), BorderLayout.CENTER);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Keep the tree to the right of the buttons so it doesn't hide behind them.
        int size = (int) Math.min(getHeight() * 0.8, getWidth() * 0.34);
        BonsaiPainter.paint((Graphics2D) g, (int) (getWidth() * 0.835 - size / 2.0),
                getHeight() - size - 24, size);
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
