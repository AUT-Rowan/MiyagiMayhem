package miyagimayhem.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import miyagimayhem.db.DatabaseTestCase;
import miyagimayhem.model.GameSession;
import miyagimayhem.model.NarratedLine;
import org.junit.Before;
import org.junit.Test;

public class GameControllerTest extends DatabaseTestCase {

    private GameController controller;

    @Before
    public void createController() {
        controller = GameController.createDefault();
    }

    @Test
    public void playingAndSavingAGameAddsItToHistoryAndLeaderboard() {
        GameSession session = controller.play(Arrays.asList("Ann", "Bob", "Cy", "Di"));
        int id = controller.saveGame(session);
        assertEquals(1, controller.getHistory().size());
        assertEquals(session.getLines().size(), controller.getTranscript(id).size());
        assertEquals(Integer.valueOf(1), controller.getLeaderboard().get(session.getWinnerName()));
    }

    @Test
    public void savingAnInvalidRosterIsRejectedAndNotStored() {
        List<String> before = controller.getRosterNames();
        assertNotNull(controller.saveRoster("Tiny", Arrays.asList("A", "B")));
        assertEquals(before, controller.getRosterNames());
    }

    @Test
    public void validRosterIsSavedUnderANormalisedName() {
        assertNull(controller.saveRoster("  my crew ", Arrays.asList("A", "B", "C")));
        assertTrue(controller.getRosterNames().contains("My crew"));
    }

    @Test
    public void invalidShowTextIsRejected() {
        assertNotNull(controller.addFlavorLine("PAIRED", "only one %s here"));
        assertNull(controller.addFlavorLine("PAIRED", "%s and %s argue about rice"));
    }

    @Test
    public void newShowTextIsUsedInAGame() {
        for (int i = 0; i < 12; i++) {
            controller.addFlavorLine("WINNER", "%s INSANELY-UNIQUE-WINNER-LINE");
        }
        boolean found = false;
        for (int i = 0; i < 30 && !found; i++) {
            for (NarratedLine line
                    : controller.play(Arrays.asList("Ann", "Bob", "Cy", "Di")).getLines()) {
                found |= line.getText().contains("INSANELY-UNIQUE-WINNER-LINE");
            }
        }
        assertTrue(found);
    }

    @Test
    public void clearingHistoryEmptiesIt() {
        controller.saveGame(controller.play(Arrays.asList("Ann", "Bob", "Cy")));
        controller.clearHistory();
        assertTrue(controller.getHistory().isEmpty());
        assertTrue(controller.getLeaderboard().isEmpty());
    }
}
