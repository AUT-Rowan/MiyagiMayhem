package miyagimayhem;

import java.util.List;
import java.util.Random;

/**
 * A rare, unpredictable event that shakes up a round. Two variants for now:
 * an immunity steal, or a double elimination warning (Simulation checks
 * isDoubleElimination() afterwards and runs the elimination event twice
 * if true). Easy to extend with more twist types later.
 */
public class TwistEvent extends Event {

    private final Random rng = new Random();
    private boolean doubleEliminationTriggered = false;

    public TwistEvent(GameLogger logger) {
        super(logger);
    }

    @Override
    public void execute(List<Contestant> activeContestants) {
        logger.header("=== TWIST! ===");

        if (activeContestants.size() < 3) {
            logger.log("No twist this round, not enough contestants left for chaos.");
            logger.blank();
            return;
        }

        boolean stealImmunity = rng.nextBoolean();

        if (stealImmunity) {
            Contestant immuneContestant = findImmune(activeContestants);
            if (immuneContestant != null) {
                Contestant thief = randomOther(activeContestants, immuneContestant);
                immuneContestant.clearImmunity();
                thief.grantImmunity();
                logger.log("IMMUNITY STEAL! " + thief.getName()
                        + " has stolen immunity from " + immuneContestant.getName() + "!");
            } else {
                logger.log("A twist loomed... but nothing happened. Anticlimactic.");
            }
        } else {
            doubleEliminationTriggered = true;
            logger.log("DOUBLE ELIMINATION this round! Two contestants will be going home.");
        }
        logger.blank();
    }

    /**
     * Whether this twist triggered a double elimination. Simulation checks this
     * after calling execute() to decide whether to run EliminationEvent twice.
     */
    public boolean isDoubleElimination() {
        return doubleEliminationTriggered;
    }

    private Contestant findImmune(List<Contestant> contestants) {
        for (Contestant c : contestants) {
            if (c.hasImmunity()) {
                return c;
            }
        }
        return null;
    }

    private Contestant randomOther(List<Contestant> contestants, Contestant exclude) {
        Contestant pick;
        do {
            pick = contestants.get(rng.nextInt(contestants.size()));
        } while (pick == exclude);
        return pick;
    }

    @Override
    public String getEventName() {
        return "Twist";
    }
}
