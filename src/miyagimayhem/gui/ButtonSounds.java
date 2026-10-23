package miyagimayhem.gui;

import java.awt.Component;
import java.awt.Container;
import miyagimayhem.audio.AudioManager;

/**
 * Makes every StyledButton in a window play the click sound.
 */
final class ButtonSounds {

    private ButtonSounds() {
    }

    static void install(Container root, AudioManager audio) {
        for (Component child : root.getComponents()) {
            if (child instanceof StyledButton) {
                ((StyledButton) child).addActionListener(e -> audio.playClick());
            }
            if (child instanceof Container) {
                install((Container) child, audio);
            }
        }
    }
}
