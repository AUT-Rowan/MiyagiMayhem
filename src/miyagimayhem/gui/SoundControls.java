package miyagimayhem.gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import miyagimayhem.audio.AudioManager;

/**
 * Two small icon buttons that switch the music and the sound effects on and
 * off. They sit in the top right corner of every screen.
 */
class SoundControls extends JPanel {

    private final AudioManager audio;
    private final JButton musicButton = new IconButton();
    private final JButton effectsButton = new IconButton();

    SoundControls(AudioManager audio) {
        this.audio = audio;
        setOpaque(false);
        setLayout(new FlowLayout(FlowLayout.RIGHT, 8, 0));

        musicButton.addActionListener(e -> {
            audio.setMusicEnabled(!audio.isMusicEnabled());
            updateIcons();
        });
        effectsButton.addActionListener(e -> {
            audio.setEffectsEnabled(!audio.isEffectsEnabled());
            updateIcons();
        });
        add(musicButton);
        add(effectsButton);

        boolean available = audio.isAvailable();
        musicButton.setEnabled(available);
        effectsButton.setEnabled(available);
        updateIcons();
    }

    private void updateIcons() {
        boolean available = audio.isAvailable();
        musicButton.setIcon(SoundIcons.music(audio.isMusicEnabled()));
        effectsButton.setIcon(SoundIcons.speaker(audio.isEffectsEnabled()));
        musicButton.setToolTipText(available
                ? (audio.isMusicEnabled() ? "Music is on (click to mute)" : "Music is off (click to turn on)")
                : "No audio device found");
        effectsButton.setToolTipText(available
                ? (audio.isEffectsEnabled() ? "Sound effects are on (click to mute)"
                        : "Sound effects are off (click to turn on)")
                : "No audio device found");
    }

    /** A square button that only shows an icon. */
    private static class IconButton extends StyledButton {

        private static final long serialVersionUID = 1L;

        IconButton() {
            super("", false);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(40, 36);
        }
    }
}
