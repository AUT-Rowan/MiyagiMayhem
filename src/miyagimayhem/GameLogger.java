package miyagimayhem;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles writing narrated game text to both the console and a log file
 * at the same time, so the CUI output and the saved transcript always match.
 *
 * Pacing is entirely player-controlled: every non-blank line waits for the
 * player to press ENTER before the next one appears. A short "..." cue marks
 * where to press ENTER, instead of a repeated instruction banner.
 *
 * Colors use the basic 8-color ANSI codes only (not 24-bit true color,
 * NetBeans' Output console doesn't reliably support that).
 *
 * Contestant names aren't individually colored anymore, they're just bold.
 * Inside a role-colored line (a WINNER line or an elimination line), the bold
 * name naturally inherits that line's color since it's nested inside it;
 * elsewhere names are bold in the console's default color.
 *
 * If colors look wrong at all here, flip COLOR_ENABLED to false below,
 * everything else keeps working exactly the same.
 */
public class GameLogger {

    private static final boolean COLOR_ENABLED = true;
    private static final String RESET = "\u001B[0m";
    private static final String BOLD_ON = "\u001B[1m";
    private static final String BOLD_OFF = "\u001B[22m"; // turns off bold only, keeps any active color

    // Role colors: basic ANSI, no bold.
    private static final String TITLE_RED = "\u001B[31m";        // show title
    private static final String ELIMINATION_RED = "\u001B[31m";  // elimination ceremony header + voted-out lines
    private static final String ROUND_HEADER_COLOR = "\u001B[35m"; // round headers (magenta)
    private static final String WINNER_GREEN = "\u001B[32m";     // round winners, Final Result header, overall winner line
    private static final String HEADER_TEAL = "\u001B[36m";      // other section headers (Twist)

    private PrintWriter fileWriter;
    private final Scanner inputScanner;
    private final Set<String> contestantNames = new HashSet<>();

    public GameLogger(String outputFilePath, Scanner inputScanner) {
        this.inputScanner = inputScanner;
        try {
            fileWriter = new PrintWriter(new FileWriter(outputFilePath));
        } catch (IOException e) {
            System.out.println("Warning: could not open log file. Continuing without file logging.");
            fileWriter = null;
        }
    }

    /**
     * Registers the contestant names so they can be bolded wherever they
     * appear. Call once, right after the final contestant list is known.
     */
    public void registerContestantNames(List<Contestant> contestants) {
        for (Contestant c : contestants) {
            contestantNames.add(c.getName());
        }
    }

    /**
     * Prints a line to console and writes the same clean line (no cue, no
     * color codes) to the log file. Waits for the player to press ENTER
     * before returning, unless the line is blank (spacer lines pass through
     * instantly). Typing 'quit' here ends the game immediately.
     */
    public void log(String message) {
        if (fileWriter != null) {
            fileWriter.println(message);
            fileWriter.flush();
        }

        if (message.isEmpty()) {
            System.out.println();
            return;
        }

        System.out.print(colorize(message) + "  ...");
        String input = inputScanner.nextLine();
        System.out.println();
        System.out.println(); // extra gap so the newest line isn't flush against the bottom
        quitIfRequested(input);
    }

    /**
     * If the player typed 'quit' at a pause, saves what's been logged so
     * far, says goodbye, and ends the program immediately.
     */
    private void quitIfRequested(String input) {
        if (input != null && input.trim().equalsIgnoreCase("quit")) {
            System.out.println("Goodbye! (Transcript saved up to this point.)");
            close();
            System.exit(0);
        }
    }

    /**
     * Prints a section header (e.g. "=== ROUND 1 ==="). Instant, no wait,
     * since headers are structural rather than a comedic beat.
     */
    public void header(String message) {
        if (fileWriter != null) {
            fileWriter.println(message);
            fileWriter.flush();
        }
        System.out.println(colorize(message));
    }

    /**
     * Prints a blank line for spacing, in both places. No wait.
     */
    public void blank() {
        System.out.println();
        if (fileWriter != null) {
            fileWriter.println();
            fileWriter.flush();
        }
    }

    private String colorize(String message) {
        if (!COLOR_ENABLED) {
            return message;
        }

        String roleColor = pickRoleColor(message);
        String withNames = boldNames(message, roleColor);

        if (roleColor != null) {
            return roleColor + withNames + RESET;
        }
        return withNames;
    }

    private String pickRoleColor(String message) {
        if (message.startsWith("=== MIYAGI MAYHEM")) {
            return TITLE_RED;
        }
        if (message.startsWith("=== ROUND")) {
            return ROUND_HEADER_COLOR;
        }
        if (message.startsWith("=== ELIMINATION")) {
            return ELIMINATION_RED;
        }
        if (message.startsWith("=== FINAL RESULT")) {
            return WINNER_GREEN;
        }
        if (message.startsWith("===")) {
            return HEADER_TEAL; // Twist
        }
        if (message.contains("WINNER OF MIYAGI MAYHEM")) {
            return WINNER_GREEN;
        }
        if (message.startsWith("WINNER:")) {
            return WINNER_GREEN;
        }
        if (message.contains("has been voted out")) {
            return ELIMINATION_RED;
        }
        return null;
    }

    /**
     * Bolds any known contestant name in the message. If a role color is
     * active for the whole line, the bold name inherits it naturally (bold
     * only changes weight, not color) since BOLD_OFF only cancels the bold
     * attribute, it doesn't reset the color.
     */
    private String boldNames(String message, String roleColor) {
        String result = message;
        for (String name : contestantNames) {
            if (!result.contains(name)) {
                continue;
            }
            String replacement = BOLD_ON + name + BOLD_OFF;
            Pattern pattern = Pattern.compile("\\b" + Pattern.quote(name) + "\\b");
            result = pattern.matcher(result).replaceAll(Matcher.quoteReplacement(replacement));
        }
        return result;
    }

    public void close() {
        if (fileWriter != null) {
            fileWriter.close();
        }
    }
}
