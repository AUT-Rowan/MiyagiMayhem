package miyagimayhem.model;

import java.sql.Timestamp;

/**
 * Summary of one finished game, as shown in the history screen.
 */
public class GameResult {

    private final int id;
    private final Timestamp playedAt;
    private final String winner;
    private final String contestants; // comma separated names
    private final int rounds;

    public GameResult(int id, Timestamp playedAt, String winner, String contestants, int rounds) {
        this.id = id;
        this.playedAt = playedAt;
        this.winner = winner;
        this.contestants = contestants;
        this.rounds = rounds;
    }

    public int getId() {
        return id;
    }

    public Timestamp getPlayedAt() {
        return playedAt;
    }

    public String getWinner() {
        return winner;
    }

    public String getContestants() {
        return contestants;
    }

    public int getRounds() {
        return rounds;
    }

    public int getContestantCount() {
        return contestants.isEmpty() ? 0 : contestants.split(",").length;
    }
}
