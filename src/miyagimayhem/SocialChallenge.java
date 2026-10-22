package miyagimayhem;

public class SocialChallenge extends ChallengeEvent {

    public SocialChallenge(GameLogger logger, ChallengeNamePool namePool) {
        super(logger, namePool);
    }

    @Override
    protected String getPrimaryStat() {
        return "social";
    }

    @Override
    protected String getFlavorCategory() {
        return "social";
    }

    @Override
    public String getEventName() {
        return "Social Challenge";
    }
}
