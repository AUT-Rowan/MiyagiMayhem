package miyagimayhem.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import miyagimayhem.model.Contestant;

/**
 * Owns the contestant list and drives the round-by-round game loop
 * until a single winner remains. All narration goes through the
 * Narrator, so this class has no knowledge of any user interface.
 */
public class Simulation {

    private static final int TWIST_ODDS = 5;        // 1 in 5 rounds (~20%)
    private static final int MIN_ACTIVE_FOR_TWIST = 4;

    private final List<Contestant> contestants;
    private final Narrator narrator;
    private final EventFactory events;
    private final Random rng = new Random();
    private int roundNumber = 0;

    public Simulation(List<Contestant> contestants, Narrator narrator, EventFactory events) {
        this.contestants = contestants;
        this.narrator = narrator;
        this.events = events;
    }

    /** Runs the full simulation from start to finish. */
    public void run() {
        narrator.header("=== MIYAGI MAYHEM ===");
        narrator.log(contestants.size() + " contestants begin their journey.");
        narrator.blank();

        while (getActiveContestants().size() > 1) {
            roundNumber++;
            runRound();
        }

        announceWinner();
    }

    public int getRounds() {
        return roundNumber;
    }

    /** The last contestant standing, or null if the game hasn't finished. */
    public Contestant getWinner() {
        List<Contestant> active = getActiveContestants();
        return active.size() == 1 ? active.get(0) : null;
    }

    private void runRound() {
        narrator.setRound(roundNumber);
        narrator.header("=== ROUND " + roundNumber + " ===");

        List<Contestant> active = getActiveContestants();

        // Clear immunity from the previous round before the new challenge
        for (Contestant c : active) {
            c.clearImmunity();
        }

        events.createRandomChallenge().execute(active);

        boolean doubleElimination = maybeRunTwist(active);

        runElimination();
        if (doubleElimination) {
            runElimination();
        }
    }

    /**
     * Small chance of a twist after round 1, keeps early rounds predictable.
     * Returns true if the twist was a double elimination.
     */
    private boolean maybeRunTwist(List<Contestant> active) {
        if (roundNumber > 1 && active.size() >= MIN_ACTIVE_FOR_TWIST && rng.nextInt(TWIST_ODDS) == 0) {
            TwistEvent twist = events.createTwist();
            twist.execute(active);
            return twist.isDoubleElimination();
        }
        return false;
    }

    private void runElimination() {
        List<Contestant> active = getActiveContestants();
        if (active.size() > 1) {
            events.createElimination().execute(active);
        }
    }

    private List<Contestant> getActiveContestants() {
        List<Contestant> active = new ArrayList<>();
        for (Contestant c : contestants) {
            if (!c.isEliminated()) {
                active.add(c);
            }
        }
        return active;
    }

    private void announceWinner() {
        narrator.header("=== FINAL RESULT ===");
        Contestant winner = getWinner();
        if (winner != null) {
            narrator.log("WINNER OF MIYAGI MAYHEM: " + winner.getName() + "!");
        } else {
            narrator.log("The simulation ended with no clear winner.");
        }
    }
}
