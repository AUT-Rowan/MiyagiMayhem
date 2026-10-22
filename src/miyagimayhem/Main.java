package miyagimayhem;

import java.util.Arrays;
import java.util.List;
import miyagimayhem.logic.FlavorText;
import miyagimayhem.logic.GameRunner;
import miyagimayhem.model.GameSession;
import miyagimayhem.model.NarratedLine;

/**
 * Temporary entry point while the project is being converted from a console
 * game to a GUI: plays one game with the built-in text and prints it.
 * Replaced by the Swing launcher on the GUI branch.
 */
public class Main {

    public static void main(String[] args) {
        List<String> names = Arrays.asList("Aiko", "Ben", "Chika", "Daichi", "Emi");
        GameSession session = new GameRunner(FlavorText.defaults()).play(names);
        for (NarratedLine line : session.getLines()) {
            System.out.println(line.getText());
        }
    }
}
