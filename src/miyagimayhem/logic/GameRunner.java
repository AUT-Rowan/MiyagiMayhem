package miyagimayhem.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import miyagimayhem.model.Contestant;
import miyagimayhem.model.GameSession;

/**
 * Plays one complete game from a list of names and hands back the finished
 * GameSession. This is the single entry point the controller uses, so it
 * hides how contestants, alliances, the narrator and the simulation are
 * put together.
 */
public class GameRunner {

    private final FlavorText flavor;
    private final Random rng = new Random();

    public GameRunner(FlavorText flavor) {
        this.flavor = flavor;
    }

    /**
     * @param names contestant names, already validated (3 to 10, no duplicates)
     * @throws IllegalArgumentException if the roster is not a legal size
     */
    public GameSession play(List<String> names) {
        String problem = RosterValidator.validateRoster(names);
        if (problem != null) {
            throw new IllegalArgumentException(problem);
        }

        List<Contestant> contestants = new ArrayList<>();
        for (String name : names) {
            contestants.add(new Contestant(name));
        }
        AllianceMaker.formRandomAlliances(contestants, rng);

        Narrator narrator = new Narrator();
        TranscriptRecorder recorder = new TranscriptRecorder();
        narrator.addListener(recorder);

        Simulation simulation = new Simulation(contestants, narrator, new EventFactory(narrator, flavor));
        simulation.run();

        Contestant winner = simulation.getWinner();
        String winnerName = winner == null ? "No winner" : winner.getName();
        return new GameSession(contestants, recorder.getLines(), winnerName, simulation.getRounds());
    }
}
