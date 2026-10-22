package miyagimayhem.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Main menu: the starting point of the application.
 */
class MenuPanel extends JPanel {

    MenuPanel(Navigator navigator, Runnable onQuit) {
        setLayout(new GridBagLayout());
        setBackground(UiTheme.BACKGROUND);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(4, 0, 4, 0);
        c.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("MIYAGI MAYHEM", SwingConstants.CENTER);
        title.setFont(UiTheme.HEADING.deriveFont(44f));
        title.setForeground(UiTheme.ACCENT);
        add(title, c);

        c.gridy++;
        JLabel subtitle = new JLabel("A reality show elimination simulator", SwingConstants.CENTER);
        subtitle.setFont(UiTheme.BODY);
        subtitle.setForeground(UiTheme.MUTED);
        c.insets = new Insets(0, 0, 30, 0);
        add(subtitle, c);

        c.insets = new Insets(6, 0, 6, 0);
        addButton("New Game", true, navigator::showSetup, c);
        addButton("Game History", false, navigator::showHistory, c);
        addButton("Edit Show Text", false, navigator::showFlavor, c);
        addButton("Quit", false, onQuit, c);
    }

    private void addButton(String text, boolean primary, Runnable action, GridBagConstraints c) {
        c.gridy++;
        JButton button = new StyledButton(text, primary);
        button.addActionListener(e -> action.run());
        add(button, c);
    }
}
