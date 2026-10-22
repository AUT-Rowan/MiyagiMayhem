package miyagimayhem.logic;

public class PhysicalChallenge extends ChallengeEvent {

    public PhysicalChallenge(Narrator narrator, ChallengeNamePool namePool, FlavorText flavor) {
        super(narrator, namePool, flavor);
    }

    @Override
    protected String getPrimaryStat() {
        return "strength";
    }

    @Override
    protected String getFlavorCategory() {
        return FlavorText.PHYSICAL;
    }

    @Override
    public String getEventName() {
        return "Physical Challenge";
    }
}
