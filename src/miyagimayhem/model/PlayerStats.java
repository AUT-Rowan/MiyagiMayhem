package miyagimayhem.model;

/**
 * How one contestant name has done across all saved games.
 */
public class PlayerStats {

    private final String name;
    private final int gamesPlayed;
    private final int wins;

    public PlayerStats(String name, int gamesPlayed, int wins) {
        this.name = name;
        this.gamesPlayed = gamesPlayed;
        this.wins = wins;
    }

    public String getName() {
        return name;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public int getWins() {
        return wins;
    }

    /** Wins as a percentage of games played (0 if they haven't played). */
    public int getWinPercent() {
        return gamesPlayed == 0 ? 0 : Math.round(100f * wins / gamesPlayed);
    }
}
