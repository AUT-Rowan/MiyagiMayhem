package miyagimayhem.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

public class FlavorTextTest {

    @Test
    public void countsPlaceholders() {
        assertEquals(0, FlavorText.countPlaceholders("no names here"));
        assertEquals(2, FlavorText.countPlaceholders("%s beat %s"));
    }

    @Test
    public void fillReplacesPlaceholdersInOrder() {
        assertEquals("Ann beat Bob", FlavorText.fill("%s beat %s", "Ann", "Bob"));
    }

    @Test
    public void loserLineNeedsOneOrTwoPlaceholders() {
        assertNull(FlavorText.validateLine(FlavorText.LOSER, "%s fell over"));
        assertNull(FlavorText.validateLine(FlavorText.LOSER, "%s said sorry to %s"));
        assertNotNull(FlavorText.validateLine(FlavorText.LOSER, "Nobody fell over"));
    }

    @Test
    public void pairedLineNeedsExactlyTwoPlaceholders() {
        assertNull(FlavorText.validateLine(FlavorText.PAIRED, "%s and %s teamed up"));
        assertNotNull(FlavorText.validateLine(FlavorText.PAIRED, "%s teamed up"));
    }

    @Test
    public void challengeNamesMustNotHavePlaceholders() {
        assertNull(FlavorText.validateLine(FlavorText.PHYSICAL, "Sumo Soup Run"));
        assertNotNull(FlavorText.validateLine(FlavorText.PHYSICAL, "Sumo %s"));
    }

    @Test
    public void rejectsBlankAndOversizedLines() {
        assertNotNull(FlavorText.validateLine(FlavorText.QUOTE, "   "));
        assertNotNull(FlavorText.validateLine(FlavorText.QUOTE, "x".repeat(401)));
    }

    @Test
    public void emptySourceFallsBackToBuiltInText() {
        FlavorText flavor = new FlavorText(new HashMap<String, List<String>>());
        assertFalse(flavor.getChallengeNames(FlavorText.PHYSICAL).isEmpty());
        assertTrue(flavor.loserLine("Ann").contains("Ann"));
        assertNotNull(flavor.eliminationQuote());
    }

    @Test
    public void customLinesAreUsedWhenProvided() {
        Map<String, List<String>> source = new HashMap<>();
        source.put(FlavorText.WINNER, Arrays.asList("%s takes the crown"));
        FlavorText flavor = new FlavorText(source);
        assertEquals("Ann takes the crown", flavor.winnerLine("Ann"));
    }
}
