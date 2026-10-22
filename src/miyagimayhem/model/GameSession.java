package miyagimayhem.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Everything produced by one simulated game: the contestants (with their
 * stats and alliances), the full narration, the winner and how many rounds
 * it took. The GUI replays the narration line by line.
 */
public class GameSession {

    private final List<Contestant> contestants;
    private final List<NarratedLine> lines;
    private final String winnerName;
    private final int rounds;

    public GameSession(List<Contestant> contestants, List<NarratedLine> lines, String winnerName, int rounds) {
        this.contestants = new ArrayList<>(contestants);
        this.lines = new ArrayList<>(lines);
        this.winnerName = winnerName;
        this.rounds = rounds;
    }

    public List<Contestant> getContestants() {
        return Collections.unmodifiableList(contestants);
    }

    public List<NarratedLine> getLines() {
        return Collections.unmodifiableList(lines);
    }

    public String getWinnerName() {
        return winnerName;
    }

    public int getRounds() {
        return rounds;
    }

    /** Comma separated contestant names, in roster order. */
    public String getContestantNames() {
        StringBuilder sb = new StringBuilder();
        for (Contestant c : contestants) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(c.getName());
        }
        return sb.toString();
    }
}
