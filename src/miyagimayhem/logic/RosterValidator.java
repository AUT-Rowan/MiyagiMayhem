package miyagimayhem.logic;

import java.util.List;

/**
 * Checks and tidies contestant names before a game starts. Returning an
 * error message (instead of throwing) lets the GUI show a friendly hint
 * right next to the field the user is typing in.
 */
public final class RosterValidator {

    public static final int MIN_CONTESTANTS = 3;
    public static final int MAX_CONTESTANTS = 10;
    public static final int MAX_NAME_LENGTH = 20;

    private RosterValidator() {
    }

    /**
     * Trims a name, collapses repeated spaces and capitalises the first
     * letter (the rest is left as typed, so "mcDonald" stays recognisable).
     */
    public static String normalize(String raw) {
        if (raw == null) {
            return "";
        }
        String name = raw.trim().replaceAll("\\s+", " ");
        if (name.isEmpty()) {
            return name;
        }
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    /**
     * Checks one new name against the names already in the roster.
     * Returns an error message, or null if the name is fine.
     */
    public static String validateName(String raw, List<String> existing) {
        String name = normalize(raw);
        if (name.isEmpty()) {
            return "Name can't be empty.";
        }
        if (name.length() > MAX_NAME_LENGTH) {
            return "Name is too long (" + MAX_NAME_LENGTH + " characters at most).";
        }
        if (name.contains(",") || name.contains("|")) {
            return "Names can't contain commas or the | character.";
        }
        if (existing.size() >= MAX_CONTESTANTS) {
            return "The roster is full (" + MAX_CONTESTANTS + " contestants at most).";
        }
        for (String other : existing) {
            if (other.equalsIgnoreCase(name)) {
                return "'" + name + "' is already in, please use a different name.";
            }
        }
        return null;
    }

    /** Checks a whole roster is a legal size. Returns an error message or null. */
    public static String validateRoster(List<String> names) {
        if (names.size() < MIN_CONTESTANTS) {
            return "You need at least " + MIN_CONTESTANTS + " contestants.";
        }
        if (names.size() > MAX_CONTESTANTS) {
            return "You can have at most " + MAX_CONTESTANTS + " contestants.";
        }
        return null;
    }
}
