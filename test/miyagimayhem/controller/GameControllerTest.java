package miyagimayhem.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import miyagimayhem.db.DatabaseTestCase;
import miyagimayhem.model.GameSession;
import miyagimayhem.model.NarratedLine;
import miyagimayhem.model.PlayerStats;
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
        PlayerStats winner = null;
        for (PlayerStats stats : controller.getLeaderboard()) {
            if (stats.getName().equals(session.getWinnerName())) {
                winner = stats;
            }
        }
        assertNotNull(winner);
        assertEquals(1, winner.getWins());
        assertEquals(1, winner.getGamesPlayed());
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

    @Test
    public void leaderboardCountsGamesPlayedAndWinsForEveryone() {
        for (int i = 0; i < 3; i++) {
            controller.saveGame(controller.play(Arrays.asList("Ann", "Bob", "Cy")));
        }
        List<PlayerStats> leaderboard = controller.getLeaderboard();
        assertEquals(3, leaderboard.size());
        int totalWins = 0;
        for (PlayerStats stats : leaderboard) {
            assertEquals(3, stats.getGamesPlayed());
            totalWins += stats.getWins();
        }
        assertEquals(3, totalWins);
        assertTrue(leaderboard.get(0).getWins() >= leaderboard.get(2).getWins());
    }

    @Test
    public void deletingOneGameLeavesTheOthers() {
        int first = controller.saveGame(controller.play(Arrays.asList("Ann", "Bob", "Cy")));
        int second = controller.saveGame(controller.play(Arrays.asList("Ann", "Bob", "Cy")));
        controller.deleteGame(first);
        assertEquals(1, controller.getHistory().size());
        assertEquals(second, controller.getHistory().get(0).getId());
        assertTrue(controller.getTranscript(first).isEmpty());
        assertFalse(controller.getTranscript(second).isEmpty());
    }
}
