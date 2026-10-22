package miyagimayhem.logic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import miyagimayhem.model.GameSession;
import miyagimayhem.model.LineTag;
import miyagimayhem.model.NarratedLine;
import org.junit.Test;

public class GameRunnerTest {

    private static final List<String> NAMES = Arrays.asList("Aiko", "Ben", "Chika", "Daichi", "Emi");

    private static GameSession play(List<String> names) {
        return new GameRunner(FlavorText.defaults()).play(names);
    }

    @Test
    public void winnerIsOneOfTheContestants() {
        for (int i = 0; i < 100; i++) {
            assertTrue(NAMES.contains(play(NAMES).getWinnerName()));
        }
    }

    @Test
    public void everyoneExceptTheWinnerIsVotedOutExactlyOnce() {
        for (int i = 0; i < 100; i++) {
            GameSession session = play(NAMES);
            Set<String> eliminated = new HashSet<>();
            for (NarratedLine line : session.getLines()) {
                if (line.getTag() == LineTag.ELIMINATED) {
                    assertTrue("voted out twice: " + line.getSubject(), eliminated.add(line.getSubject()));
                }
            }
            assertEquals(NAMES.size() - 1, eliminated.size());
            assertFalse(eliminated.contains(session.getWinnerName()));
        }
    }

    @Test
    public void transcriptIsNotEmptyAndRoundCountIsPlausible() {
        GameSession session = play(NAMES);
        assertFalse(session.getLines().isEmpty());
        assertTrue(session.getRounds() >= 1 && session.getRounds() <= NAMES.size() - 1);
    }

    @Test
    public void minimumSizeGameStillFinishes() {
        GameSession session = play(Arrays.asList("A", "B", "C"));
        assertTrue(Arrays.asList("A", "B", "C").contains(session.getWinnerName()));
    }

    @Test
    public void sessionListsContestantNamesAsCsv() {
        assertEquals("Aiko, Ben, Chika, Daichi, Emi".replace(", ", ","), play(NAMES).getContestantNames().replace(", ", ","));
    }
}
