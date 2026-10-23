package miyagimayhem.controller;

import java.util.ArrayList;
import java.util.List;
import miyagimayhem.logic.RosterValidator;

/**
 * The list of contestant names the player is building on the setup screen.
 * It checks every change so the list is never in a state the game can't
 * start from by accident, and it has no Swing code so it can be unit tested.
 */
public class RosterEditor {

    private final List<String> names = new ArrayList<>();

    /** A copy of the names, in the order they were added. */
    public List<String> getNames() {
        return new ArrayList<>(names);
    }

    public int size() {
        return names.size();
    }

    /**
     * Adds a name after tidying it up (see RosterValidator.normalize).
     * Returns an error message if it was refused, or null if it was added.
     */
    public String add(String rawName) {
        String error = RosterValidator.validateName(rawName, names);
        if (error == null) {
            names.add(RosterValidator.normalize(rawName));
        }
        return error;
    }

    public void removeAt(int index) {
        if (index >= 0 && index < names.size()) {
            names.remove(index);
        }
    }

    public void clear() {
        names.clear();
    }

    /** Swaps the whole list, for example when a saved roster is loaded. */
    public void replaceWith(List<String> newNames) {
        names.clear();
        names.addAll(newNames);
    }

    /** Error message if the list can't start a game yet, otherwise null. */
    public String checkReady() {
        return RosterValidator.validateRoster(names);
    }

    public boolean isReady() {
        return checkReady() == null;
    }
}
