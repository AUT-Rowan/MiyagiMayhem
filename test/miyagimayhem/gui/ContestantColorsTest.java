package miyagimayhem.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;

public class ContestantColorsTest {

    private static List<String> names(int count) {
        List<String> names = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            names.add("P" + i);
        }
        return names;
    }

    @Test
    public void firstTenContestantsAllGetDifferentColours() {
        ContestantColors colours = new ContestantColors(names(10));
        Set<Color> seen = new HashSet<>();
        for (String name : colours.names()) {
            assertTrue(seen.add(colours.get(name)));
        }
        assertEquals(10, seen.size());
    }

    @Test
    public void coloursFollowRosterOrderStartingWithRed() {
        ContestantColors colours = new ContestantColors(Arrays.asList("Ann", "Bob"));
        assertEquals(new Color(0xE53935), colours.get("Ann"));
        assertNotNull(colours.get("Bob"));
    }

    @Test
    public void unknownNameHasNoColour() {
        assertNull(new ContestantColors(Arrays.asList("Ann")).get("Zed"));
    }

    @Test
    public void onlyBlackNeedsAHighlightOnTheDarkTheme() {
        assertTrue(ContestantColors.needsHighlight(Color.BLACK));
        assertFalse(ContestantColors.needsHighlight(Color.WHITE));
    }
}
