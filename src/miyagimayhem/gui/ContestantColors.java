package miyagimayhem.gui;

import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Gives every contestant in a game their own colour, in roster order:
 * red, orange, yellow, green, blue, purple, pink, black, white, brown.
 */
final class ContestantColors {

    static final Color BLACK = Color.BLACK;

    private static final Color[] PALETTE = {
        new Color(0xE53935), // red
        new Color(0xFB8C00), // orange
        new Color(0xFDD835), // yellow
        new Color(0x43A047), // green
        new Color(0x2196F3), // blue
        new Color(0x9C27B0), // purple
        new Color(0xF06EAA), // pink
        BLACK,
        Color.WHITE,
        new Color(0xA0693F)  // brown
    };

    private final Map<String, Color> colours = new LinkedHashMap<>();

    ContestantColors(List<String> names) {
        for (int i = 0; i < names.size(); i++) {
            colours.put(names.get(i), PALETTE[i % PALETTE.length]);
        }
    }

    /** The contestant's colour, or null if the name isn't in this game. */
    Color get(String name) {
        return colours.get(name);
    }

    Set<String> names() {
        return colours.keySet();
    }

    /** Black text is invisible on the dark theme, so it is drawn on a light highlight. */
    static boolean needsHighlight(Color colour) {
        return BLACK.equals(colour);
    }
}
