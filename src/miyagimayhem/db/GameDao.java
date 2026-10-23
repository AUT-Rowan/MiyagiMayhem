package miyagimayhem.db;

import java.util.List;
import java.util.Map;
import miyagimayhem.model.GameResult;
import miyagimayhem.model.NarratedLine;

/**
 * Data access for finished games and their transcripts.
 */
public interface GameDao {

    /**
     * Stores a finished game together with its full transcript, all or
     * nothing. Returns the id of the new game.
     */
    int save(String winner, String contestants, int rounds, List<NarratedLine> transcript);

    /** All saved games, newest first. */
    List<GameResult> findAll();

    /** The transcript of one saved game, in order. */
    List<NarratedLine> findTranscript(int gameId);

    /** Winner name to number of wins, most wins first. */
    Map<String, Integer> findWinCounts();

    /** Deletes one saved game and its transcript. */
    void delete(int gameId);

    /** Deletes every saved game and transcript. */
    void deleteAll();
}
