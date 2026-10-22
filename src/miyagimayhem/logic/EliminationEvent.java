package miyagimayhem.logic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import miyagimayhem.model.Contestant;
import miyagimayhem.model.LineTag;

/**
 * Runs the elimination vote. Every active contestant without immunity is
 * a valid target. Vote weight is inversely related to Social stat (low
 * Social = easier target), and allies never vote for each other.
 */
public class EliminationEvent extends Event {

    private static final int WEIGHT_BASE = Contestant.MAX_STAT + 1;

    private final Random rng = new Random();
    private final FlavorText flavor;

    public EliminationEvent(Narrator narrator, FlavorText flavor) {
        super(narrator);
        this.flavor = flavor;
    }

    @Override
    public void execute(List<Contestant> activeContestants) {
        narrator.header("=== ELIMINATION CEREMONY ===");

        List<Contestant> votable = findVotable(activeContestants);
        narrator.blank();

        if (votable.isEmpty()) {
            narrator.log("Everyone has immunity somehow. No one goes home this round.");
            narrator.blank();
            return;
        }
        if (votable.size() == 1) {
            eliminate(votable.get(0), activeContestants.size());
            return;
        }

        Map<Contestant, Integer> tally = castVotes(activeContestants, votable);
        List<Contestant> mostVoted = announceVotes(votable, tally);

        Contestant eliminated = (mostVoted.size() > 1) ? runTiebreaker(mostVoted) : mostVoted.get(0);
        eliminate(eliminated, activeContestants.size());
    }

    /** Everyone without immunity can be voted for. Immune contestants are announced as safe. */
    private List<Contestant> findVotable(List<Contestant> activeContestants) {
        List<Contestant> votable = new ArrayList<>();
        for (Contestant c : activeContestants) {
            if (c.hasImmunity()) {
                narrator.log(c.getName() + " is safe (immunity).");
            } else {
                votable.add(c);
            }
        }
        return votable;
    }

    /** Each active contestant casts one weighted vote against a votable target. */
    private Map<Contestant, Integer> castVotes(List<Contestant> voters, List<Contestant> votable) {
        Map<Contestant, Integer> tally = new HashMap<>();
        for (Contestant target : votable) {
            tally.put(target, 0);
        }
        for (Contestant voter : voters) {
            Contestant target = pickWeightedTarget(voter, votable);
            if (target != null) {
                tally.put(target, tally.get(target) + 1);
            }
        }
        return tally;
    }

    /** Reads out each contestant's votes and returns everyone tied for the most. */
    private List<Contestant> announceVotes(List<Contestant> votable, Map<Contestant, Integer> tally) {
        narrator.log("The votes are in...");
        narrator.blank();

        int highestVotes = -1;
        List<Contestant> mostVoted = new ArrayList<>();
        for (Contestant c : votable) {
            int votes = tally.get(c);
            narrator.log(c.getName() + ": " + votes + " vote" + (votes == 1 ? "" : "s"));
            if (votes > highestVotes) {
                highestVotes = votes;
                mostVoted.clear();
                mostVoted.add(c);
            } else if (votes == highestVotes) {
                mostVoted.add(c);
            }
        }
        narrator.blank();
        return mostVoted;
    }

    /**
     * Runs when two or more contestants are tied for the most votes.
     * Announces the tie, then randomly picks who actually goes home.
     */
    private Contestant runTiebreaker(List<Contestant> tied) {
        narrator.log("It's a tie between " + formatNames(tied) + "!");
        narrator.log("Time for a quick tiebreaker...");
        Contestant loser = tied.get(rng.nextInt(tied.size()));
        narrator.log(flavor.tiebreakerLine(loser.getName()));
        narrator.blank();
        return loser;
    }

    /**
     * Formats a list of names naturally: "A and B" for two, "A, B and C"
     * for more, no Oxford comma.
     */
    private String formatNames(List<Contestant> contestants) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < contestants.size(); i++) {
            if (i > 0) {
                sb.append(i == contestants.size() - 1 ? " and " : ", ");
            }
            sb.append(contestants.get(i).getName());
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
            if (target == voter || voter.isAlliedWith(target.getName())) {
                continue;
            }
            int weight = Math.max(1, WEIGHT_BASE - target.getSocial()); // low social = high weight
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
        narrator.log(c.getName() + " has been voted out.", LineTag.ELIMINATED, c.getName());
        narrator.log(c.getName() + ": " + flavor.eliminationQuote());
        narrator.blank();

        int remaining = activeCountBeforeElimination - 1;
        narrator.log(remaining + (remaining == 1 ? " contestant remains." : " contestants remain."));
        narrator.blank();
    }

    @Override
    public String getEventName() {
        return "Elimination Ceremony";
    }
}
