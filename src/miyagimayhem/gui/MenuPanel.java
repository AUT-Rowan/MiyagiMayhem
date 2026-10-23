package miyagimayhem.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import miyagimayhem.audio.AudioManager;

/**
 * Main menu: the starting point of the application. It has a bonsai tree
 * in the background and the sound settings along the bottom.
 */
class MenuPanel extends JPanel {

    private final AudioManager audio;
    private final JButton musicButton = new StyledButton("", false);
    private final JButton effectsButton = new StyledButton("", false);

    MenuPanel(Navigator navigator, AudioManager audio, Runnable onQuit) {
        this.audio = audio;
        setLayout(new BorderLayout());
        setBackground(UiTheme.BACKGROUND);

        add(buildMenu(navigator, onQuit), BorderLayout.CENTER);
        add(buildSoundControls(), BorderLayout.SOUTH);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Keep the tree to the right of the buttons so it doesn't hide behind them.
        int size = (int) Math.min(getHeight() * 0.8, getWidth() * 0.36);
        BonsaiPainter.paint((Graphics2D) g, (int) (getWidth() * 0.82 - size / 2.0),
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

    private JPanel buildSoundControls() {
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        controls.setOpaque(false);
        controls.setBorder(UiTheme.padding(16));

        musicButton.addActionListener(e -> {
            audio.setMusicEnabled(!audio.isMusicEnabled());
            updateLabels();
        });
        effectsButton.addActionListener(e -> {
            audio.setEffectsEnabled(!audio.isEffectsEnabled());
            updateLabels();
        });

        JSlider volume = new JSlider(0, 100, audio.getMusicVolume());
        volume.setOpaque(false);
        volume.setPreferredSize(new Dimension(120, 30));
        volume.setToolTipText("Music volume");
        volume.addChangeListener(e -> audio.setMusicVolume(volume.getValue()));

        controls.add(musicButton);
        controls.add(UiTheme.label("Volume"));
        controls.add(volume);
        controls.add(effectsButton);

        boolean available = audio.isAvailable();
        musicButton.setEnabled(available);
        effectsButton.setEnabled(available);
        volume.setEnabled(available);
        if (!available) {
            controls.add(UiTheme.label("No audio device found"));
        }
        updateLabels();
        return controls;
    }

    private void updateLabels() {
        musicButton.setText("Music: " + (audio.isMusicEnabled() ? "On" : "Off"));
        effectsButton.setText("Sound effects: " + (audio.isEffectsEnabled() ? "On" : "Off"));
    }
}
