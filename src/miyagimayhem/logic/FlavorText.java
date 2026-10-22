package miyagimayhem.logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Central home for all the narrated "personality" text of the show.
 *
 * The lines themselves are stored in the database (and edited through the
 * GUI), so this class has no idea where they came from. It is simply given
 * a map of kind to lines. If a kind has no lines at all, it falls back to
 * a small built-in default set so the game can never run out of text.
 *
 * Templates use %s wherever a contestant's name should be inserted.
 */
public class FlavorText {

    // Challenge name kinds
    public static final String PHYSICAL = "PHYSICAL";
    public static final String MENTAL = "MENTAL";
    public static final String SOCIAL = "SOCIAL";
    // Reaction kinds
    public static final String LOSER = "LOSER";
    public static final String WINNER = "WINNER";
    public static final String PAIRED = "PAIRED";
    public static final String GROUP3 = "GROUP3";
    public static final String GROUP4 = "GROUP4";
    public static final String TIEBREAKER = "TIEBREAKER";
    // Elimination quotes
    public static final String QUOTE = "QUOTE";

    /** Every kind, in the order the editor shows them. */
    public static final List<String> KINDS = Collections.unmodifiableList(Arrays.asList(
            PHYSICAL, MENTAL, SOCIAL, LOSER, WINNER, PAIRED, GROUP3, GROUP4, TIEBREAKER, QUOTE));

    private static final String PLACEHOLDER = "%s";
    private static final Map<String, List<String>> DEFAULTS = buildDefaults();

    private final Map<String, List<String>> lines = new LinkedHashMap<>();
    private final Random rng = new Random();

    /**
     * @param source kind to lines, any kind that is missing or empty is
     *               filled from the built-in defaults
     */
    public FlavorText(Map<String, List<String>> source) {
        for (String kind : KINDS) {
            List<String> given = source == null ? null : source.get(kind);
            if (given == null || given.isEmpty()) {
                lines.put(kind, new ArrayList<>(DEFAULTS.get(kind)));
            } else {
                lines.put(kind, new ArrayList<>(given));
            }
        }
    }

    /** A FlavorText that only uses the built-in defaults. */
    public static FlavorText defaults() {
        return new FlavorText(null);
    }

    /** Full name list for a challenge category (PHYSICAL, MENTAL or SOCIAL). */
    public List<String> getChallengeNames(String category) {
        return Collections.unmodifiableList(lines.get(category.toUpperCase()));
    }

    public String loserLine(String name) {
        return fill(pick(LOSER), name);
    }

    public String winnerLine(String name) {
        return fill(pick(WINNER), name);
    }

    public String tiebreakerLine(String name) {
        return fill(pick(TIEBREAKER), name);
    }

    public String pairedLine(String name1, String name2) {
        return fill(pick(PAIRED), name1, name2);
    }

    /** Shared line for a group of 3 or 4 contestants. */
    public String groupLine(List<String> names) {
        String kind = names.size() == 3 ? GROUP3 : GROUP4;
        return fill(pick(kind), names.toArray(new String[0]));
    }

    public String eliminationQuote() {
        return pick(QUOTE);
    }

    private String pick(String kind) {
        List<String> options = lines.get(kind);
        return options.get(rng.nextInt(options.size()));
    }

    /**
     * Replaces each %s in the template with the next name, in order. If the
     * template has more placeholders than names, the last name is reused
     * (so "%s slips. Classic %s." works with a single name). Unlike
     * String.format this never throws, even if a line contains a stray %.
     */
    static String fill(String template, String... names) {
        StringBuilder result = new StringBuilder();
        int searchFrom = 0;
        int used = 0;
        int found;
        while ((found = template.indexOf(PLACEHOLDER, searchFrom)) >= 0) {
            result.append(template, searchFrom, found);
            result.append(names[Math.min(used, names.length - 1)]);
            used++;
            searchFrom = found + PLACEHOLDER.length();
        }
        result.append(template.substring(searchFrom));
        return result.toString();
    }

    /** Counts how many %s placeholders a template contains. */
    static int countPlaceholders(String template) {
        return template.split(PLACEHOLDER, -1).length - 1;
    }

    /**
     * Checks a new line before it is saved. Returns an error message that
     * can be shown to the user, or null if the line is fine.
     */
    public static String validateLine(String kind, String text) {
        if (text == null || text.trim().isEmpty()) {
            return "The line can't be empty.";
        }
        if (text.length() > 400) {
            return "The line is too long (400 characters at most).";
        }
        int count = countPlaceholders(text);
        switch (kind) {
            case LOSER:
                return (count == 1 || count == 2) ? null : "A loser line needs %s once or twice.";
            case WINNER:
            case TIEBREAKER:
                return count == 1 ? null : "This kind of line needs exactly one %s.";
            case PAIRED:
                return count == 2 ? null : "A paired line needs exactly two %s.";
            case GROUP3:
                return count == 3 ? null : "A group of 3 line needs exactly three %s.";
            case GROUP4:
                return count == 4 ? null : "A group of 4 line needs exactly four %s.";
            default:
                return count == 0 ? null : "This kind of line shouldn't contain %s.";
        }
    }

    /** Friendly name for the editor's drop-down. */
    public static String label(String kind) {
        switch (kind) {
            case PHYSICAL:
                return "Physical challenge names";
            case MENTAL:
                return "Mental challenge names";
            case SOCIAL:
                return "Social challenge names";
            case LOSER:
                return "Loser reactions (1 name)";
            case WINNER:
                return "Winner reactions (1 name)";
            case PAIRED:
                return "Paired reactions (2 names)";
            case GROUP3:
                return "Group of 3 reactions";
            case GROUP4:
                return "Group of 4 reactions";
            case TIEBREAKER:
                return "Tiebreaker lines (1 name)";
            default:
                return "Elimination quotes";
        }
    }

    private static Map<String, List<String>> buildDefaults() {
        Map<String, List<String>> d = new LinkedHashMap<>();
        d.put(PHYSICAL, Arrays.asList("Slippery Squid Gauntlet", "Bonkers Bootcamp",
                "The Greased Beam of Shame", "Mount Wobble Ascent", "Sumo Sprint Showdown"));
        d.put(MENTAL, Arrays.asList("Trial by Tofu Trivia", "The Bewildering Bento Puzzle",
                "Riddle of the Ronin", "Panic Panda Logic Grid", "The Impossible Origami Test"));
        d.put(SOCIAL, Arrays.asList("Whisper Down the Dojo", "The Great Alliance Auction",
                "Confession Booth Chaos", "Trust Fall Tournament", "The Popularity Panel"));
        d.put(LOSER, Arrays.asList("%s slips immediately. Classic %s.",
                "%s just walked straight through it like it owed them money.",
                "%s gives it a valiant effort and fails spectacularly.",
                "%s trips over absolutely nothing.", "%s blames the wind. There is no wind."));
        d.put(WINNER, Arrays.asList("%s is somehow sprinting sideways and it's working.",
                "%s digs deep and pulls ahead at the last second.",
                "%s makes it look easy. It was not easy.",
                "%s channels pure chaos energy and wins.", "%s wins without even breaking a sweat."));
        d.put(PAIRED, Arrays.asList("%s and %s collide head-on and both go down in a heap.",
                "%s tries to help %s up, and they both end up falling instead.",
                "%s and %s get into a shoving match and lose track of the actual challenge."));
        d.put(GROUP3, Arrays.asList("%s, %s and %s all pile into each other at the worst possible moment.",
                "%s, %s and %s form an accidental human blockade in the middle of the course."));
        d.put(GROUP4, Arrays.asList("%s, %s, %s and %s cause a full pileup that takes several minutes to sort out.",
                "%s, %s, %s and %s somehow all end up in the exact same spot, and none of them planned it."));
        d.put(TIEBREAKER, Arrays.asList("The coin lands wrong for %s.",
                "%s loses the rock-paper-scissors showdown.", "Fate decides. %s is out."));
        d.put(QUOTE, Arrays.asList("\"I regret nothing. The eel pit regrets ME.\"",
                "\"Tell my alliance... they know what they did.\"",
                "\"Honestly? Fair. That was a bad round for me.\""));
        return d;
    }
}
