package miyagimayhem;

public class MentalChallenge extends ChallengeEvent {

    public MentalChallenge(GameLogger logger, ChallengeNamePool namePool) {
        super(logger, namePool);
    }

    @Override
    protected String getPrimaryStat() {
        return "intelligence";
    }

    @Override
    protected String getFlavorCategory() {
        return "mental";
    }

    @Override
    public String getEventName() {
        return "Mental Challenge";
    }
}
