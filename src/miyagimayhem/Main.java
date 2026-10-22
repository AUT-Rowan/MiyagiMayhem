package miyagimayhem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Main {

    private static final String ROSTER_FILE = "contestants.txt";
    private static final String LOG_FILE = "miyagi_mayhem_log.txt";
    private static final String LEADERBOARD_FILE = "leaderboard.txt";
    private static final int MAX_CONTESTANTS = 10;

    private static final Random RNG = new Random();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        FlavorTextGenerator.initialize();
        RosterManager rosterManager = new RosterManager();

        System.out.println("=== MIYAGI MAYHEM: Contestant Setup ===");
        System.out.println("(Type 'quit' at any time to exit.)");
        System.out.println();

        List<Contestant> contestants = loadOrEnterContestants(scanner, rosterManager);
        if (contestants == null) {
            System.out.println("Goodbye!");
            scanner.close();
            return;
        }

        formRandomAlliances(contestants);
        writeRosterFile(contestants);

        System.out.println();
        System.out.println("Roster saved to " + ROSTER_FILE);
        System.out.println("Starting simulation...");
        System.out.println("(Press ENTER at each '...' to reveal the next line, or type 'quit' to exit.)");
        System.out.println();

        GameLogger logger = new GameLogger(LOG_FILE, scanner);
        logger.registerContestantNames(contestants);
        Simulation simulation = new Simulation(contestants, logger);
        simulation.run();

        System.out.println();
        System.out.println("Full transcript saved to " + LOG_FILE);

        Contestant winner = findWinner(contestants);
        if (winner != null) {
            updateLeaderboard(winner.getName());
        }

        scanner.close();
    }

    /**
     * Offers to load a saved roster if any exist, otherwise (or if the
     * player declines) falls through to manual entry. Returns null if the
     * player quit instead of finishing setup.
     */
    private static List<Contestant> loadOrEnterContestants(Scanner scanner, RosterManager rosterManager) {
        List<String> savedRosters = rosterManager.listSavedRosters();

        if (!savedRosters.isEmpty()) {
            System.out.println("Saved rosters found:");
            for (int i = 0; i < savedRosters.size(); i++) {
                System.out.println("  " + (i + 1) + ". " + savedRosters.get(i));
            }
            System.out.println("Type a number to load one, or press ENTER to enter names manually.");
            System.out.print("> ");
            String choice = scanner.nextLine().trim();

            if (choice.equalsIgnoreCase("quit")) {
                return null;
            }

            if (!choice.isEmpty()) {
                try {
                    int index = Integer.parseInt(choice) - 1;
                    if (index >= 0 && index < savedRosters.size()) {
                        List<Contestant> loaded = buildFromSavedRoster(rosterManager, savedRosters.get(index));
                        if (loaded != null) {
                            return loaded;
                        }
                        System.out.println("That roster couldn't be used, falling back to manual entry.");
                    } else {
                        System.out.println("Not a valid option, falling back to manual entry.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Not a valid option, falling back to manual entry.");
                }
            }
            System.out.println();
        }

        return enterContestantsManually(scanner, rosterManager);
    }

    /**
     * Loads names from a saved roster file, removing duplicates and
     * capping at MAX_CONTESTANTS. Returns null if fewer than 3 usable
     * names remain after cleanup.
     */
    private static List<Contestant> buildFromSavedRoster(RosterManager rosterManager, String rosterName) {
        List<String> rawNames = rosterManager.loadRoster(rosterName);
        List<Contestant> contestants = new ArrayList<>();

        for (String rawName : rawNames) {
            String name = capitalizeFirst(rawName.trim());
            if (name.isEmpty() || isDuplicateName(contestants, name)) {
                continue;
            }
            contestants.add(new Contestant(name));
            if (contestants.size() == MAX_CONTESTANTS) {
                break;
            }
        }

        if (contestants.size() < 3) {
            return null;
        }

        System.out.println("Loaded " + contestants.size() + " contestants from '" + rosterName + "'.");
        return contestants;
    }

    /**
     * The original manual name-entry loop, now with duplicate checking and
     * a 'quit' command. Returns null if the player quit. Offers to save the
     * finished roster for reuse next time.
     */
    private static List<Contestant> enterContestantsManually(Scanner scanner, RosterManager rosterManager) {
        System.out.println("Enter contestant names one per line.");
        System.out.println("Type 'done' when finished (minimum 3, maximum " + MAX_CONTESTANTS + " contestants).");
        System.out.println();

        List<Contestant> contestants = new ArrayList<>();
        while (true) {
            System.out.print("Contestant " + (contestants.size() + 1) + ": ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("quit")) {
                return null;
            }

            if (input.equalsIgnoreCase("done")) {
                if (contestants.size() < 3) {
                    System.out.println("Need at least 3 contestants before finishing.");
                    continue;
                }
                break;
            }
            if (input.isEmpty()) {
                System.out.println("Name can't be empty.");
                continue;
            }

            String name = capitalizeFirst(input);
            if (isDuplicateName(contestants, name)) {
                System.out.println("'" + name + "' is already in, please use a different name.");
                continue;
            }

            contestants.add(new Contestant(name));

            if (contestants.size() == MAX_CONTESTANTS) {
                System.out.println("Maximum of " + MAX_CONTESTANTS + " contestants reached.");
                break;
            }
        }

        offerToSaveRoster(scanner, rosterManager, contestants);
        return contestants;
    }

    private static boolean isDuplicateName(List<Contestant> contestants, String name) {
        for (Contestant c : contestants) {
            if (c.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    private static void offerToSaveRoster(Scanner scanner, RosterManager rosterManager, List<Contestant> contestants) {
        System.out.print("Save this group as a roster for next time? (y/n): ");
        String answer = scanner.nextLine().trim();
        if (!answer.equalsIgnoreCase("y") && !answer.equalsIgnoreCase("yes")) {
            return;
        }

        System.out.print("Roster name: ");
        String rosterName = scanner.nextLine().trim();
        if (rosterName.isEmpty()) {
            System.out.println("No name given, skipping save.");
            return;
        }

        List<String> names = new ArrayList<>();
        for (Contestant c : contestants) {
            names.add(c.getName());
        }
        rosterManager.saveRoster(rosterName, names);
        System.out.println("Saved as '" + rosterName + "'.");
    }

    /**
     * Randomly pairs some contestants into two-person alliances at the start
     * of the game. Not every contestant gets one.
     */
    private static void formRandomAlliances(List<Contestant> contestants) {
        List<Contestant> unallied = new ArrayList<>(contestants);
        while (unallied.size() >= 2) {
            // ~50% chance any given pair actually forms an alliance
            if (RNG.nextBoolean()) {
                Contestant a = unallied.remove(RNG.nextInt(unallied.size()));
                Contestant b = unallied.remove(RNG.nextInt(unallied.size()));
                a.setAlly(b.getName());
                b.setAlly(a.getName());
            } else {
                // skip pairing this one, still remove to make progress
                unallied.remove(0);
            }
        }
    }

    /**
     * Writes the starting roster (with stats and alliances) to a text file.
     * This is separate from the game log, it's a record of the initial setup.
     */
    private static void writeRosterFile(List<Contestant> contestants) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(ROSTER_FILE))) {
            writer.println("=== MIYAGI MAYHEM ROSTER ===");
            for (Contestant c : contestants) {
                writer.println(c.toString()
                        + (c.getAllyName() != null ? " | Allied with: " + c.getAllyName() : ""));
            }
        } catch (IOException e) {
            System.out.println("Warning: could not write roster file.");
        }
    }

    private static Contestant findWinner(List<Contestant> contestants) {
        for (Contestant c : contestants) {
            if (!c.isEliminated()) {
                return c;
            }
        }
        return null;
    }

    /**
     * Capitalizes just the first letter of a name, leaving the rest as typed
     * (so "mcDonald" stays "McDonald"-ish rather than being fully re-cased).
     */
    private static String capitalizeFirst(String name) {
        if (name.isEmpty()) {
            return name;
        }
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    /**
     * Reads the existing leaderboard file (if any), appends the new winner,
     * writes it back, then prints the updated leaderboard to console.
     * Demonstrates both reading and writing working together on the same file.
     */
    private static void updateLeaderboard(String winnerName) {
        List<String> lines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(LEADERBOARD_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            // File probably doesn't exist yet, that's fine for a first run
        }

        lines.add(winnerName);

        try (PrintWriter writer = new PrintWriter(new FileWriter(LEADERBOARD_FILE))) {
            for (String line : lines) {
                writer.println(line);
            }
        } catch (IOException e) {
            System.out.println("Warning: could not update leaderboard file.");
        }

        System.out.println();
        System.out.println("=== ALL-TIME WINNERS ===");
        for (String line : lines) {
            System.out.println("- " + line);
        }
    }
}
