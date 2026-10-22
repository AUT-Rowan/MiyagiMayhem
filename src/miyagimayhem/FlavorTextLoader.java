package miyagimayhem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads and writes the show's flavor text (challenge names, reaction lines,
 * elimination quotes) from plain text files in the flavor/ folder, so new
 * lines can be added by editing a text file directly, no recompiling needed.
 *
 * On first run, since these files won't exist yet, ensureFilesExist() writes
 * out the built-in defaults so there's something ready to open and edit.
 */
public class FlavorTextLoader {

    private static final String FOLDER = "flavor";
    private static final String CHALLENGE_NAMES_FILE = FOLDER + "/challenge_names.txt";
    private static final String REACTIONS_FILE = FOLDER + "/reactions.txt";
    private static final String QUOTES_FILE = FOLDER + "/elimination_quotes.txt";

    public static void ensureFilesExist(Map<String, List<String>> defaultChallengeNames,
                                         Map<String, List<String>> defaultReactions,
                                         List<String> defaultQuotes) {
        new File(FOLDER).mkdirs();

        if (!new File(CHALLENGE_NAMES_FILE).exists()) {
            writeChallengeNames(defaultChallengeNames);
        }
        if (!new File(REACTIONS_FILE).exists()) {
            writeReactions(defaultReactions);
        }
        if (!new File(QUOTES_FILE).exists()) {
            writeQuotes(defaultQuotes);
        }
    }

    private static void writeChallengeNames(Map<String, List<String>> data) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(CHALLENGE_NAMES_FILE))) {
            writer.println("# One challenge name per line. Format: CATEGORY|Name");
            writer.println("# CATEGORY must be PHYSICAL, MENTAL, or SOCIAL");
            writer.println("# Add as many lines as you like, they'll all be used.");
            writer.println();
            for (Map.Entry<String, List<String>> entry : data.entrySet()) {
                for (String name : entry.getValue()) {
                    writer.println(entry.getKey() + "|" + name);
                }
            }
        } catch (IOException e) {
            System.out.println("Warning: could not create default challenge names file.");
        }
    }

    private static void writeReactions(Map<String, List<String>> data) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(REACTIONS_FILE))) {
            writer.println("# One reaction line per line. Format: TYPE|template text");
            writer.println("# TYPE is one of: LOSER, WINNER, PAIRED, GROUP3, GROUP4, TIEBREAKER");
            writer.println("# Use %s wherever a contestant's name should be inserted.");
            writer.println("# LOSER lines may use %s twice, PAIRED needs exactly 2, GROUP3 needs 3, GROUP4 needs 4.");
            writer.println();
            for (Map.Entry<String, List<String>> entry : data.entrySet()) {
                for (String line : entry.getValue()) {
                    writer.println(entry.getKey() + "|" + line);
                }
            }
        } catch (IOException e) {
            System.out.println("Warning: could not create default reactions file.");
        }
    }

    private static void writeQuotes(List<String> quotes) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(QUOTES_FILE))) {
            writer.println("# One elimination quote per line.");
            writer.println();
            for (String quote : quotes) {
                writer.println(quote);
            }
        } catch (IOException e) {
            System.out.println("Warning: could not create default elimination quotes file.");
        }
    }

    public static Map<String, List<String>> loadChallengeNames() {
        Map<String, List<String>> result = new HashMap<>();
        result.put("PHYSICAL", new ArrayList<>());
        result.put("MENTAL", new ArrayList<>());
        result.put("SOCIAL", new ArrayList<>());

        for (String line : readContentLines(CHALLENGE_NAMES_FILE)) {
            String[] parts = line.split("\\|", 2);
            if (parts.length == 2) {
                String category = parts[0].trim().toUpperCase();
                if (result.containsKey(category)) {
                    result.get(category).add(parts[1].trim());
                }
            }
        }
        return result;
    }

    public static Map<String, List<String>> loadReactions() {
        Map<String, List<String>> result = new HashMap<>();
        result.put("LOSER", new ArrayList<>());
        result.put("WINNER", new ArrayList<>());
        result.put("PAIRED", new ArrayList<>());
        result.put("GROUP3", new ArrayList<>());
        result.put("GROUP4", new ArrayList<>());
        result.put("TIEBREAKER", new ArrayList<>());

        for (String line : readContentLines(REACTIONS_FILE)) {
            String[] parts = line.split("\\|", 2);
            if (parts.length == 2) {
                String type = parts[0].trim().toUpperCase();
                if (result.containsKey(type)) {
                    result.get(type).add(parts[1].trim());
                }
            }
        }
        return result;
    }

    public static List<String> loadQuotes() {
        return readContentLines(QUOTES_FILE);
    }

    /**
     * Reads a text file, skipping blank lines and comment lines (starting
     * with #). Used by all three loaders above.
     */
    private static List<String> readContentLines(String path) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            System.out.println("Warning: could not read " + path + ".");
        }
        return lines;
    }
}
