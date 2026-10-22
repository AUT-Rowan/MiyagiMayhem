package miyagimayhem.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import miyagimayhem.model.Contestant;
import miyagimayhem.model.LineTag;

/**
 * Shared base for all challenge types (Physical/Mental/Social).
 * Subclasses only need to specify which stat drives the challenge
 * and which flavor-text category to pull from. The winner selection
 * and narration logic lives here once, instead of being copy-pasted three times.
 */
public abstract class ChallengeEvent extends Event {

    private static final int FOUR_GROUP_CHANCE = 10;
    private static final int THREE_GROUP_CHANCE = 15;
    private static final int PAIR_CHANCE = 35;

    private final Random rng = new Random();
    private final ChallengeNamePool namePool;
    private final FlavorText flavor;

    public ChallengeEvent(Narrator narrator, ChallengeNamePool namePool, FlavorText flavor) {
        super(narrator);
        this.namePool = namePool;
        this.flavor = flavor;
    }

    protected abstract String getPrimaryStat();

    protected abstract String getFlavorCategory();

    @Override
    public void execute(List<Contestant> activeContestants) {
        String challengeName = namePool.draw(getFlavorCategory());
        narrator.log("Challenge: " + challengeName);
        narrator.blank();

        Contestant winner = pickWinner(activeContestants);

        narrateActionLines(activeContestants, winner);
        narrator.blank();

        winner.grantImmunity();
        narrator.log("WINNER: " + winner.getName() + "!");
        narrator.log(winner.getName() + " has earned IMMUNITY and cannot be voted out this round.",
                LineTag.IMMUNITY_GRANTED, winner.getName());
        narrator.blank();
    }

    /** Everyone rolls against the challenge stat, the highest score wins. */
    private Contestant pickWinner(List<Contestant> activeContestants) {
        Contestant winner = null;
        int bestScore = -1;
        for (Contestant c : activeContestants) {
            int score = c.rollScore(getPrimaryStat());
            if (score > bestScore) {
                bestScore = score;
                winner = c;
            }
        }
        return winner;
    }

    /**
     * Narrates an action line for every non-winning contestant, sometimes
     * grouping two, three, or four of them into a single shared moment
     * instead of always giving everyone their own solo line. Larger
     * groups are rarer. The winner's own line always comes last, solo.
     */
    private void narrateActionLines(List<Contestant> activeContestants, Contestant winner) {
        List<Contestant> losers = new ArrayList<>(activeContestants);
        losers.remove(winner);
        Collections.shuffle(losers, rng);

        int i = 0;
        while (i < losers.size()) {
            int groupSize = pickGroupSize(losers.size() - i);
            List<String> names = new ArrayList<>();
            for (int j = 0; j < groupSize; j++) {
                names.add(losers.get(i + j).getName());
            }
            narrator.log(lineForGroup(names));
            i += groupSize;
        }

        narrator.log(flavor.winnerLine(winner.getName()));
    }

    private String lineForGroup(List<String> names) {
        switch (names.size()) {
            case 1:
                return flavor.loserLine(names.get(0));
            case 2:
                return flavor.pairedLine(names.get(0), names.get(1));
            default:
                return flavor.groupLine(names);
        }
    }

    /**
     * Decides how many contestants share the next action line.
     * Solo is most common, pairs less common, 3-4 person groups rarest.
     */
    private int pickGroupSize(int remaining) {
        if (remaining >= 4 && rng.nextInt(100) < FOUR_GROUP_CHANCE) {
            return 4;
        }
        if (remaining >= 3 && rng.nextInt(100) < THREE_GROUP_CHANCE) {
            return 3;
        }
        if (remaining >= 2 && rng.nextInt(100) < PAIR_CHANCE) {
            return 2;
        }
        return 1;
    }
}
