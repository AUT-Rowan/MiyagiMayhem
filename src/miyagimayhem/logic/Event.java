package miyagimayhem.logic;

import java.util.List;
import miyagimayhem.model.Contestant;

/**
 * Abstract base class for anything that happens during a round:
 * challenges, elimination ceremonies, and twists.
 *
 * Each subclass implements execute() differently, this is the core
 * polymorphism point of the simulation. Simulation just calls
 * event.execute(activeContestants) without caring which subtype it is.
 */
public abstract class Event {

    protected final Narrator narrator;

    public Event(Narrator narrator) {
        this.narrator = narrator;
    }

    /**
     * Runs this event against the currently active (non-eliminated) contestants.
     * Subclasses handle their own logic and narration.
     */
    public abstract void execute(List<Contestant> activeContestants);

    /**
     * Short label used in round headers, e.g. "Physical Challenge".
     */
    public abstract String getEventName();
}
