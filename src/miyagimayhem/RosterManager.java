package miyagimayhem;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Saves and loads named contestant rosters to/from the rosters/ folder, so
 * a group of names (e.g. "friends", "family") can be reused across multiple
 * games without retyping them each time.
 */
public class RosterManager {

    private static final String FOLDER = "rosters";

    public RosterManager() {
        new File(FOLDER).mkdirs();
    }

    /**
     * Returns the names of saved rosters (without the .txt extension),
     * or an empty list if none have been saved yet.
     */
    public List<String> listSavedRosters() {
        List<String> names = new ArrayList<>();
        File folder = new File(FOLDER);
        File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt"));
        if (files != null) {
            for (File f : files) {
                String name = f.getName();
                names.add(name.substring(0, name.length() - 4));
            }
        }
        return names;
    }

    /**
     * Reads a saved roster file and returns the contestant names in it.
     */
    public List<String> loadRoster(String rosterName) {
        List<String> names = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(FOLDER + "/" + rosterName + ".txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    names.add(line);
                }
            }
        } catch (IOException e) {
            System.out.println("Warning: could not read roster '" + rosterName + "'.");
        }
        return names;
    }

    /**
     * Saves a list of contestant names as a reusable roster file.
     */
    public void saveRoster(String rosterName, List<String> names) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FOLDER + "/" + rosterName + ".txt"))) {
            for (String name : names) {
                writer.println(name);
            }
        } catch (IOException e) {
            System.out.println("Warning: could not save roster '" + rosterName + "'.");
        }
    }
}
