package miyagimayhem.logic;

public class SocialChallenge extends ChallengeEvent {

    public SocialChallenge(Narrator narrator, ChallengeNamePool namePool, FlavorText flavor) {
        super(narrator, namePool, flavor);
    }

    @Override
    protected String getPrimaryStat() {
        return "social";
    }

    @Override
    protected String getFlavorCategory() {
        return FlavorText.SOCIAL;
    }

    @Override
    public String getEventName() {
        return "Social Challenge";
    }
}
