package miyagimayhem;

public class PhysicalChallenge extends ChallengeEvent {

    public PhysicalChallenge(GameLogger logger, ChallengeNamePool namePool) {
        super(logger, namePool);
    }

    @Override
    protected String getPrimaryStat() {
        return "strength";
    }

    @Override
    protected String getFlavorCategory() {
        return "physical";
    }

    @Override
    public String getEventName() {
        return "Physical Challenge";
    }
}
