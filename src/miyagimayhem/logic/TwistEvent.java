package miyagimayhem.logic;

import java.util.List;
import java.util.Random;
import miyagimayhem.model.Contestant;
import miyagimayhem.model.LineTag;

/**
 * A rare, unpredictable event that shakes up a round. Two variants for now:
 * an immunity steal, or a double elimination warning (Simulation checks
 * isDoubleElimination() afterwards and runs the elimination event twice
 * if true). Easy to extend with more twist types later.
 */
public class TwistEvent extends Event {

    private static final int MIN_CONTESTANTS_FOR_TWIST = 3;

    private final Random rng = new Random();
    private boolean doubleEliminationTriggered = false;

    public TwistEvent(Narrator narrator) {
        super(narrator);
    }

    @Override
    public void execute(List<Contestant> activeContestants) {
        narrator.header("=== TWIST! ===");

        if (activeContestants.size() < MIN_CONTESTANTS_FOR_TWIST) {
            narrator.log("No twist this round, not enough contestants left for chaos.");
            narrator.blank();
            return;
        }

        if (rng.nextBoolean()) {
            stealImmunity(activeContestants);
        } else {
            doubleEliminationTriggered = true;
            narrator.log("DOUBLE ELIMINATION this round! Two contestants will be going home.");
        }
        narrator.blank();
    }

    private void stealImmunity(List<Contestant> activeContestants) {
        Contestant victim = findImmune(activeContestants);
        if (victim == null) {
            narrator.log("A twist loomed... but nothing happened. Anticlimactic.");
            return;
        }
        Contestant thief = randomOther(activeContestants, victim);
        victim.clearImmunity();
        thief.grantImmunity();
        narrator.log("IMMUNITY STEAL! " + thief.getName()
                + " has stolen immunity from " + victim.getName() + "!",
                LineTag.IMMUNITY_GRANTED, thief.getName());
        narrator.log(victim.getName() + " is no longer safe from the vote.",
                LineTag.IMMUNITY_CLEARED, victim.getName());
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
