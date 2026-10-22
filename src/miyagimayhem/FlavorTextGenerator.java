package miyagimayhem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Central home for all the narrated "personality" text of the show.
 * The actual lines live in text files under flavor/ (loaded via
 * FlavorTextLoader), not hardcoded here, so new jokes can be added by
 * editing a text file directly. Call initialize() once at startup before
 * using any of the other methods.
 */
public class FlavorTextGenerator {

    private static final Random RNG = new Random();

    private static Map<String, List<String>> challengeNames = new LinkedHashMap<>();
    private static List<String> loserReactions = new ArrayList<>();
    private static List<String> winnerReactions = new ArrayList<>();
    private static List<String> pairedReactions = new ArrayList<>();
    private static List<String> groupReactions3 = new ArrayList<>();
    private static List<String> groupReactions4 = new ArrayList<>();
    private static List<String> eliminationQuotes = new ArrayList<>();
    private static List<String> tiebreakerReactions = new ArrayList<>();

    /**
     * Loads flavor text from the flavor/ folder. If the files don't exist
     * yet (first run), writes out the built-in defaults first so there's
     * something ready to open and edit afterward.
     */
    public static void initialize() {
        Map<String, List<String>> defaultChallengeNames = new LinkedHashMap<>();
        defaultChallengeNames.put("PHYSICAL", Arrays.asList(
            "Slippery Squid Gauntlet",
            "Bonkers Bootcamp",
            "The Greased Beam of Shame",
            "Mount Wobble Ascent",
            "Sumo Sprint Showdown"
        ));
        defaultChallengeNames.put("MENTAL", Arrays.asList(
            "Trial by Tofu Trivia",
            "The Bewildering Bento Puzzle",
            "Riddle of the Ronin",
            "Panic Panda Logic Grid",
            "The Impossible Origami Test"
        ));
        defaultChallengeNames.put("SOCIAL", Arrays.asList(
            "Whisper Down the Dojo",
            "The Great Alliance Auction",
            "Confession Booth Chaos",
            "Trust Fall Tournament",
            "The Popularity Panel"
        ));

        Map<String, List<String>> defaultReactions = new LinkedHashMap<>();
        defaultReactions.put("LOSER", Arrays.asList(
            "%s slips immediately. Classic %s.",
            "%s just walked straight through it like it owed them money.",
            "%s gives it a valiant effort and fails spectacularly.",
            "%s trips over absolutely nothing.",
            "%s blames the wind. There is no wind."
        ));
        defaultReactions.put("WINNER", Arrays.asList(
            "%s is somehow sprinting sideways and it's working.",
            "%s digs deep and pulls ahead at the last second.",
            "%s makes it look easy. It was not easy.",
            "%s channels pure chaos energy and wins.",
            "%s wins without even breaking a sweat."
        ));
        defaultReactions.put("PAIRED", Arrays.asList(
            "%s and %s collide head-on and both go down in a heap.",
            "%s tries to help %s up, and they both end up falling instead.",
            "%s and %s get into a shoving match and lose track of the actual challenge.",
            "%s trips %s by accident. Or was it an accident?",
            "%s and %s somehow tie for last place, deep in conversation the entire time."
        ));
        defaultReactions.put("GROUP3", Arrays.asList(
            "%s, %s and %s all pile into each other at the worst possible moment.",
            "%s, %s and %s form an accidental human blockade in the middle of the course.",
            "%s, %s and %s spend so long arguing about strategy that the challenge ends without them."
        ));
        defaultReactions.put("GROUP4", Arrays.asList(
            "%s, %s, %s and %s cause a full pileup that takes several minutes to sort out.",
            "%s, %s, %s and %s somehow all end up in the exact same spot, and none of them planned it.",
            "%s, %s, %s and %s get so distracted heckling each other that they forget the challenge is still running."
        ));
        defaultReactions.put("TIEBREAKER", Arrays.asList(
            "The coin lands wrong for %s.",
            "%s loses the rock-paper-scissors showdown.",
            "In a stunning turn of fate, %s draws the short straw.",
            "The tiebreaker duel ends with %s flat on the ground.",
            "Fate decides. %s is out."
        ));

        List<String> defaultQuotes = Arrays.asList(
            "\"I regret nothing. The eel pit regrets ME.\"",
            "\"Tell my alliance... they know what they did.\"",
            "\"Honestly? Fair. That was a bad round for me.\"",
            "\"This is a conspiracy and I will be writing a strongly worded letter.\"",
            "\"I came, I saw, I got voted out. Iconic.\""
        );

        FlavorTextLoader.ensureFilesExist(defaultChallengeNames, defaultReactions, defaultQuotes);

        challengeNames = FlavorTextLoader.loadChallengeNames();
        Map<String, List<String>> reactions = FlavorTextLoader.loadReactions();
        loserReactions = reactions.get("LOSER");
        winnerReactions = reactions.get("WINNER");
        pairedReactions = reactions.get("PAIRED");
        groupReactions3 = reactions.get("GROUP3");
        groupReactions4 = reactions.get("GROUP4");
        tiebreakerReactions = reactions.get("TIEBREAKER");
        eliminationQuotes = FlavorTextLoader.loadQuotes();

        // Defensive fallback: if a file somehow loaded empty (e.g. the user
        // accidentally deleted every line), use the built-in defaults for
        // that category instead of leaving it with nothing to pick from.
        for (String category : defaultChallengeNames.keySet()) {
            if (challengeNames.get(category).isEmpty()) {
                challengeNames.put(category, new ArrayList<>(defaultChallengeNames.get(category)));
            }
        }
        if (loserReactions.isEmpty()) loserReactions = new ArrayList<>(defaultReactions.get("LOSER"));
        if (winnerReactions.isEmpty()) winnerReactions = new ArrayList<>(defaultReactions.get("WINNER"));
        if (pairedReactions.isEmpty()) pairedReactions = new ArrayList<>(defaultReactions.get("PAIRED"));
        if (groupReactions3.isEmpty()) groupReactions3 = new ArrayList<>(defaultReactions.get("GROUP3"));
        if (groupReactions4.isEmpty()) groupReactions4 = new ArrayList<>(defaultReactions.get("GROUP4"));
        if (tiebreakerReactions.isEmpty()) tiebreakerReactions = new ArrayList<>(defaultReactions.get("TIEBREAKER"));
        if (eliminationQuotes.isEmpty()) eliminationQuotes = new ArrayList<>(defaultQuotes);
    }

    /**
     * Returns the full name list for a challenge category. Used by
     * ChallengeNamePool to build its no-repeat draw pool.
     */
    public static List<String> getChallengeNames(String category) {
        return challengeNames.get(category.toUpperCase());
    }

    public static String loserLine(String name) {
        String template = loserReactions.get(RNG.nextInt(loserReactions.size()));
        // Some templates use the name twice (e.g. "%s slips... Classic %s.")
        int placeholderCount = template.split("%s", -1).length - 1;
        if (placeholderCount == 2) {
            return String.format(template, name, name);
        }
        return String.format(template, name);
    }

    public static String winnerLine(String name) {
        String template = winnerReactions.get(RNG.nextInt(winnerReactions.size()));
        return String.format(template, name);
    }

    public static String eliminationQuote() {
        return eliminationQuotes.get(RNG.nextInt(eliminationQuotes.size()));
    }

    public static String tiebreakerLine(String name) {
        String template = tiebreakerReactions.get(RNG.nextInt(tiebreakerReactions.size()));
        return String.format(template, name);
    }

    public static String pairedLine(String name1, String name2) {
        String template = pairedReactions.get(RNG.nextInt(pairedReactions.size()));
        return String.format(template, name1, name2);
    }

    /**
     * Formats a shared action line for a group of 3 or 4 contestants.
     * names must contain exactly 3 or 4 entries.
     */
    public static String groupLine(List<String> names) {
        String template;
        if (names.size() == 3) {
            template = groupReactions3.get(RNG.nextInt(groupReactions3.size()));
            return String.format(template, names.get(0), names.get(1), names.get(2));
        } else {
            template = groupReactions4.get(RNG.nextInt(groupReactions4.size()));
            return String.format(template, names.get(0), names.get(1), names.get(2), names.get(3));
        }
    }
}
