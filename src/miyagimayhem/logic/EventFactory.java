package miyagimayhem.logic;

import java.util.Random;

/**
 * Factory for the events a Simulation needs. Simulation asks for "a random
 * challenge", "a twist" or "an elimination" and never has to know which
 * concrete classes exist or how they are wired up. Adding a new challenge
 * type only means changing this one class.
 */
public class EventFactory {

    private static final int CHALLENGE_TYPES = 3;

    private final Narrator narrator;
    private final FlavorText flavor;
    private final ChallengeNamePool namePool;
    private final Random rng = new Random();

    public EventFactory(Narrator narrator, FlavorText flavor) {
        this.narrator = narrator;
        this.flavor = flavor;
        this.namePool = new ChallengeNamePool(flavor);
    }

    /** Randomly selects which type of challenge runs this round. */
    public ChallengeEvent createRandomChallenge() {
        switch (rng.nextInt(CHALLENGE_TYPES)) {
            case 0:
                return new PhysicalChallenge(narrator, namePool, flavor);
            case 1:
                return new MentalChallenge(narrator, namePool, flavor);
            default:
                return new SocialChallenge(narrator, namePool, flavor);
        }
    }

    public TwistEvent createTwist() {
        return new TwistEvent(narrator);
    }

    public EliminationEvent createElimination() {
        return new EliminationEvent(narrator, flavor);
    }
}
