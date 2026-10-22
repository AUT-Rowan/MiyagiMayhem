package miyagimayhem.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import miyagimayhem.model.GameResult;
import miyagimayhem.model.LineStyle;
import miyagimayhem.model.LineTag;
import miyagimayhem.model.NarratedLine;
import org.junit.Before;
import org.junit.Test;

public class JdbcGameDaoTest extends DatabaseTestCase {

    private GameDao dao;

    @Before
    public void createDao() {
        dao = new JdbcGameDao(database);
    }

    private static List<NarratedLine> sampleTranscript() {
        List<NarratedLine> lines = new ArrayList<>();
        lines.add(new NarratedLine("=== ROUND 1 ===", LineStyle.ROUND, LineTag.NONE, null, 1, false));
        lines.add(new NarratedLine("Ann wins.", LineStyle.NORMAL, LineTag.NONE, null, 1, true));
        lines.add(new NarratedLine("Bob has been voted out.", LineStyle.ELIMINATION, LineTag.ELIMINATED, "Bob", 1, true));
        return lines;
    }

    @Test
    public void savedGameAppearsInHistoryWithItsDetails() {
        int id = dao.save("Ann", "Ann,Bob,Cy", 2, sampleTranscript());
        List<GameResult> games = dao.findAll();
        assertEquals(1, games.size());
        assertEquals(id, games.get(0).getId());
        assertEquals("Ann", games.get(0).getWinner());
        assertEquals(3, games.get(0).getContestantCount());
        assertEquals(2, games.get(0).getRounds());
    }

    @Test
    public void transcriptComesBackInOrderWithStyleAndPause() {
        int id = dao.save("Ann", "Ann,Bob,Cy", 2, sampleTranscript());
        List<NarratedLine> back = dao.findTranscript(id);
        assertEquals(3, back.size());
        assertEquals("=== ROUND 1 ===", back.get(0).getText());
        assertEquals(LineStyle.ROUND, back.get(0).getStyle());
        assertFalse(back.get(0).isPause());
        assertEquals(LineStyle.ELIMINATION, back.get(2).getStyle());
        assertTrue(back.get(2).isPause());
    }

    @Test
    public void newestGameIsListedFirst() {
        dao.save("First", "A,B,C", 1, sampleTranscript());
        int second = dao.save("Second", "A,B,C", 1, sampleTranscript());
        assertEquals(second, dao.findAll().get(0).getId());
    }

    @Test
    public void leaderboardCountsWinsMostFirst() {
        dao.save("Ann", "Ann,Bob,Cy", 1, sampleTranscript());
        dao.save("Bob", "Ann,Bob,Cy", 1, sampleTranscript());
        dao.save("Bob", "Ann,Bob,Cy", 1, sampleTranscript());
        Map<String, Integer> counts = dao.findWinCounts();
        assertEquals(Arrays.asList("Bob", "Ann"), new ArrayList<>(counts.keySet()));
        assertEquals(Integer.valueOf(2), counts.get("Bob"));
    }

    @Test
    public void deleteAllRemovesGamesAndTranscripts() {
        int id = dao.save("Ann", "Ann,Bob,Cy", 1, sampleTranscript());
        dao.deleteAll();
        assertTrue(dao.findAll().isEmpty());
        assertTrue(dao.findTranscript(id).isEmpty());
        assertTrue(dao.findWinCounts().isEmpty());
    }
}
