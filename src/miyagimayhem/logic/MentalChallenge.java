package miyagimayhem.logic;

public class MentalChallenge extends ChallengeEvent {

    public MentalChallenge(Narrator narrator, ChallengeNamePool namePool, FlavorText flavor) {
        super(narrator, namePool, flavor);
    }

    @Override
    protected String getPrimaryStat() {
        return "intelligence";
    }

    @Override
    protected String getFlavorCategory() {
        return FlavorText.MENTAL;
    }

    @Override
    public String getEventName() {
        return "Mental Challenge";
    }
}
