package miyagimayhem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;

/**
 * Runs the elimination vote. Every active contestant without immunity is
 * a valid target. Vote weight is inversely related to Social stat (low
 * Social = easier target), and allies never vote for each other.
 */
public class EliminationEvent extends Event {

    private final Random rng = new Random();

    public EliminationEvent(GameLogger logger) {
        super(logger);
    }

    @Override
    public void execute(List<Contestant> activeContestants) {
        logger.header("=== ELIMINATION CEREMONY ===");

        List<Contestant> votable = new ArrayList<>();
        for (Contestant c : activeContestants) {
            if (!c.hasImmunity()) {
                votable.add(c);
            } else {
                logger.log(c.getName() + " is safe (immunity).");
            }
        }
        logger.blank();

        if (votable.isEmpty()) {
            logger.log("Everyone has immunity somehow. No one goes home this round.");
            logger.blank();
            return;
        }

        if (votable.size() == 1) {
            eliminate(votable.get(0), activeContestants.size());
            return;
        }

        // Each active contestant casts one weighted vote against a votable target
        Map<Contestant, Integer> voteTally = new HashMap<>();
        for (Contestant target : votable) {
            voteTally.put(target, 0);
        }

        for (Contestant voter : activeContestants) {
            Contestant target = pickWeightedTarget(voter, votable);
            if (target != null) {
                voteTally.put(target, voteTally.get(target) + 1);
            }
        }

        logger.log("The votes are in...");
        logger.blank();

        Contestant mostVoted = null;
        int highestVotes = -1;
        List<Contestant> tied = new ArrayList<>();
        for (Contestant c : votable) {
            int votes = voteTally.get(c);
            logger.log(c.getName() + ": " + votes + " vote" + (votes == 1 ? "" : "s"));
            if (votes > highestVotes) {
                highestVotes = votes;
                tied.clear();
                tied.add(c);
            } else if (votes == highestVotes) {
                tied.add(c);
            }
        }
        logger.blank();

        Contestant eliminated = (tied.size() > 1) ? runTiebreaker(tied) : tied.get(0);
        eliminate(eliminated, activeContestants.size());
    }

    /**
     * Runs when two or more contestants are tied for the most votes.
     * Announces the tie, then randomly picks who actually goes home.
     */
    private Contestant runTiebreaker(List<Contestant> tied) {
        logger.log("It's a tie between " + formatNames(tied) + "!");
        logger.log("Time for a quick tiebreaker...");
        Contestant loser = tied.get(rng.nextInt(tied.size()));
        logger.log(FlavorTextGenerator.tiebreakerLine(loser.getName()));
        logger.blank();
        return loser;
    }

    /**
     * Formats a list of names naturally: "A and B" for two, "A, B and C"
     * for more, no Oxford comma.
     */
    private String formatNames(List<Contestant> contestants) {
        List<String> names = new ArrayList<>();
        for (Contestant c : contestants) {
            names.add(c.getName());
        }
        if (names.size() == 2) {
            return names.get(0) + " and " + names.get(1);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < names.size(); i++) {
            if (i > 0) {
                sb.append(i == names.size() - 1 ? " and " : ", ");
            }
            sb.append(names.get(i));
        }
        return sb.toString();
    }

    /**
     * Picks who a given voter votes for, weighted so low-Social targets are
     * more likely to be picked. Allies never vote for each other.
     */
    private Contestant pickWeightedTarget(Contestant voter, List<Contestant> votable) {
        List<Contestant> eligible = new ArrayList<>();
        List<Integer> weights = new ArrayList<>();
        int totalWeight = 0;

        for (Contestant target : votable) {
            if (target == voter) continue;
            if (voter.isAlliedWith(target.getName())) continue; // won't vote for an ally

            int weight = Math.max(1, 11 - target.getSocial()); // low social = high weight
            eligible.add(target);
            weights.add(weight);
            totalWeight += weight;
        }

        if (eligible.isEmpty()) {
            return null;
        }

        int roll = rng.nextInt(totalWeight);
        int cumulative = 0;
        for (int i = 0; i < eligible.size(); i++) {
            cumulative += weights.get(i);
            if (roll < cumulative) {
                return eligible.get(i);
            }
        }
        return eligible.get(eligible.size() - 1); // fallback
    }

    private void eliminate(Contestant c, int activeCountBeforeElimination) {
        c.eliminate();
        logger.log(c.getName() + " has been voted out.");
        logger.log(c.getName() + ": " + FlavorTextGenerator.eliminationQuote());
        logger.blank();

        int remaining = activeCountBeforeElimination - 1;
        logger.log(remaining + (remaining == 1 ? " contestant remains." : " contestants remain."));
        logger.blank();
    }

    @Override
    public String getEventName() {
        return "Elimination Ceremony";
    }
}
