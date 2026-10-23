package miyagimayhem.gui;

import java.awt.Component;
import java.awt.Container;
import miyagimayhem.audio.AudioManager;

/**
 * Makes every StyledButton in a window play the click sound, except the
 * ones marked silent (such as Next, which is pressed over and over).
 */
final class ButtonSounds {

    private ButtonSounds() {
    }

    static void install(Container root, AudioManager audio) {
        for (Component child : root.getComponents()) {
            if (child instanceof StyledButton) {
                StyledButton button = (StyledButton) child;
                if (!button.isSilent()) {
                    button.addActionListener(e -> audio.playClick());
                }
            }
            if (child instanceof Container) {
                install((Container) child, audio);
            }
        }
    }
}
