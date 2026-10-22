package miyagimayhem;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Owns the contestant list and drives the round-by-round game loop
 * until a single winner remains.
 */
public class Simulation {

    private final List<Contestant> contestants;
    private final GameLogger logger;
    private final Random rng = new Random();
    private final ChallengeNamePool namePool = new ChallengeNamePool();
    private int roundNumber;

    public Simulation(List<Contestant> contestants, GameLogger logger) {
        this.contestants = contestants;
        this.logger = logger;
        this.roundNumber = 0;
    }

    /**
     * Runs the full simulation from start to finish.
     */
    public void run() {
        logger.header("=== MIYAGI MAYHEM ===");
        logger.log(contestants.size() + " contestants begin their journey.");
        logger.blank();

        while (getActiveContestants().size() > 1) {
            roundNumber++;
            runRound();
        }

        announceWinner();
        logger.close();
    }

    private void runRound() {
        logger.header("=== ROUND " + roundNumber + " ===");

        List<Contestant> active = getActiveContestants();

        // Clear immunity from the previous round before the new challenge
        for (Contestant c : active) {
            c.clearImmunity();
        }

        Event challenge = pickChallengeEvent();
        challenge.execute(active);

        // Small chance of a twist after round 1, keeps early rounds predictable
        boolean doubleElimination = false;
        if (roundNumber > 1 && active.size() > 3 && rng.nextInt(5) == 0) { // ~20% chance
            TwistEvent twist = new TwistEvent(logger);
            twist.execute(active);
            doubleElimination = twist.isDoubleElimination();
        }

        // Elimination(s): run twice if a double elimination twist triggered
        active = getActiveContestants();
        if (active.size() > 1) {
            new EliminationEvent(logger).execute(active);
        }
        if (doubleElimination) {
            active = getActiveContestants();
            if (active.size() > 1) {
                new EliminationEvent(logger).execute(active);
            }
        }
    }

    /**
     * Randomly selects which type of challenge runs this round.
     * Expanded in the next version once all Event subclasses exist.
     */
    private Event pickChallengeEvent() {
        int roll = rng.nextInt(3);
        switch (roll) {
            case 0:
                return new PhysicalChallenge(logger, namePool);
            case 1:
                return new MentalChallenge(logger, namePool);
            default:
                return new SocialChallenge(logger, namePool);
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
        List<Contestant> active = getActiveContestants();
        logger.header("=== FINAL RESULT ===");
        if (active.size() == 1) {
            Contestant winner = active.get(0);
            logger.log("WINNER OF MIYAGI MAYHEM: " + winner.getName() + "!");
        } else {
            logger.log("The simulation ended with no clear winner.");
        }
    }
}
