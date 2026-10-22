package miyagimayhem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Shared base for all challenge types (Physical/Mental/Social).
 * Subclasses only need to specify which stat drives the challenge
 * and which flavor-text category to pull from -- the winner-selection
 * and logging logic lives here once, instead of being copy-pasted three times.
 */
public abstract class ChallengeEvent extends Event {

    private final Random rng = new Random();
    private final ChallengeNamePool namePool;

    public ChallengeEvent(GameLogger logger, ChallengeNamePool namePool) {
        super(logger);
        this.namePool = namePool;
    }

    protected abstract String getPrimaryStat();

    protected abstract String getFlavorCategory();

    @Override
    public void execute(List<Contestant> activeContestants) {
        String challengeName = namePool.draw(getFlavorCategory());
        logger.log("Challenge: " + challengeName);
        logger.blank();

        Contestant winner = null;
        int bestScore = -1;

        for (Contestant c : activeContestants) {
            int score = c.rollScore(getPrimaryStat());
            if (score > bestScore) {
                bestScore = score;
                winner = c;
            }
        }

        logAllActionLines(activeContestants, winner);
        logger.blank();

        winner.grantImmunity();
        logger.log("WINNER: " + winner.getName() + "!");
        logger.log(winner.getName() + " has earned IMMUNITY and cannot be voted out this round.");
        logger.blank();
    }

    /**
     * Prints an action line for every non-winning contestant, sometimes
     * grouping two, three, or four of them into a single shared moment
     * instead of always giving everyone their own solo line. Larger
     * groups are rarer. The winner's own line always prints last, solo.
     */
    private void logAllActionLines(List<Contestant> activeContestants, Contestant winner) {
        List<Contestant> losers = new ArrayList<>();
        for (Contestant c : activeContestants) {
            if (c != winner) {
                losers.add(c);
            }
        }
        Collections.shuffle(losers, rng);

        int i = 0;
        while (i < losers.size()) {
            int remaining = losers.size() - i;
            int groupSize = pickGroupSize(remaining);

            List<String> names = new ArrayList<>();
            for (int j = 0; j < groupSize; j++) {
                names.add(losers.get(i + j).getName());
            }

            switch (groupSize) {
                case 1:
                    logger.log(FlavorTextGenerator.loserLine(names.get(0)));
                    break;
                case 2:
                    logger.log(FlavorTextGenerator.pairedLine(names.get(0), names.get(1)));
                    break;
                default:
                    logger.log(FlavorTextGenerator.groupLine(names));
                    break;
            }
            i += groupSize;
        }

        logger.log(FlavorTextGenerator.winnerLine(winner.getName()));
    }

    /**
     * Decides how many contestants share the next action line.
     * Solo is most common, pairs less common, 3-4 person groups rarest.
     */
    private int pickGroupSize(int remaining) {
        if (remaining >= 4 && rng.nextInt(100) < 10) {
            return 4;
        }
        if (remaining >= 3 && rng.nextInt(100) < 15) {
            return 3;
        }
        if (remaining >= 2 && rng.nextInt(100) < 35) {
            return 2;
        }
        return 1;
    }
}
