package miyagimayhem.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import miyagimayhem.model.Contestant;
import miyagimayhem.model.GameSession;
import miyagimayhem.model.LineStyle;
import miyagimayhem.model.LineTag;
import miyagimayhem.model.NarratedLine;
import org.junit.Before;
import org.junit.Test;

public class GamePlaybackTest {

    private GamePlayback playback;

    @Before
    public void createPlayback() {
        List<Contestant> contestants = Arrays.asList(
                new Contestant("Ann", 5, 5, 5, 5), new Contestant("Bob", 5, 5, 5, 5), new Contestant("Cy", 5, 5, 5, 5));
        List<NarratedLine> lines = new ArrayList<>();
        lines.add(new NarratedLine("=== ROUND 1 ===", LineStyle.ROUND, LineTag.NONE, null, 1, false));
        lines.add(new NarratedLine("Challenge: Sumo", LineStyle.NORMAL, LineTag.NONE, null, 1, true));
        lines.add(new NarratedLine("Ann earns immunity.", LineStyle.NORMAL, LineTag.IMMUNITY_GRANTED, "Ann", 1, true));
        lines.add(new NarratedLine("Bob has been voted out.", LineStyle.ELIMINATION, LineTag.ELIMINATED, "Bob", 1, true));
        lines.add(new NarratedLine("=== ROUND 2 ===", LineStyle.ROUND, LineTag.NONE, null, 2, false));
        lines.add(new NarratedLine("Cy looks nervous.", LineStyle.NORMAL, LineTag.NONE, null, 2, true));
        lines.add(new NarratedLine("Cy has been voted out.", LineStyle.ELIMINATION, LineTag.ELIMINATED, "Cy", 2, true));
        playback = new GamePlayback(new GameSession(contestants, lines, "Ann", 2));
    }

    @Test
    public void everyoneStartsActive() {
        assertEquals(Arrays.asList("Ann", "Bob", "Cy"), playback.getContestantNames());
        assertEquals(ContestantStatus.ACTIVE, playback.getStatus("Ann"));
        assertFalse(playback.isFinished());
    }

    @Test
    public void revealNextIncludesUnpausedLinesWithTheNextPausedOne() {
        List<NarratedLine> first = playback.revealNext();
        assertEquals(2, first.size());
        assertEquals("=== ROUND 1 ===", first.get(0).getText());
        assertEquals("Challenge: Sumo", first.get(1).getText());
    }

    @Test
    public void taggedLinesChangeStatuses() {
        playback.revealNext();
        playback.revealNext();
        assertEquals(ContestantStatus.IMMUNE, playback.getStatus("Ann"));
        playback.revealNext();
        assertEquals(ContestantStatus.OUT, playback.getStatus("Bob"));
    }

    @Test
    public void immunityEndsWhenTheNextRoundStarts() {
        for (int i = 0; i < 3; i++) {
            playback.revealNext();
        }
        assertEquals(ContestantStatus.IMMUNE, playback.getStatus("Ann"));
        playback.revealNext();
        assertFalse(playback.isFinished());
        assertEquals(ContestantStatus.ACTIVE, playback.getStatus("Ann"));
    }

    @Test
    public void winnerIsMarkedOnlyWhenTheGameIsFinished() {
        playback.revealNext();
        assertFalse(playback.getStatus("Ann") == ContestantStatus.WINNER);
        playback.revealAll();
        assertTrue(playback.isFinished());
        assertEquals(ContestantStatus.WINNER, playback.getStatus("Ann"));
        assertEquals(ContestantStatus.OUT, playback.getStatus("Cy"));
    }

    @Test
    public void revealAllReturnsEveryRemainingLineOnce() {
        playback.revealNext();
        assertEquals(5, playback.revealAll().size());
        assertTrue(playback.revealNext().isEmpty());
    }
}
