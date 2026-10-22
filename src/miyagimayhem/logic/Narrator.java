package miyagimayhem.logic;

import java.util.ArrayList;
import java.util.List;
import miyagimayhem.model.LineStyle;
import miyagimayhem.model.LineTag;
import miyagimayhem.model.NarratedLine;

/**
 * The Subject of the Observer pattern. Events and the Simulation tell the
 * Narrator what is happening, and the Narrator turns each message into a
 * NarratedLine and passes it to every registered NarrationListener.
 *
 * The Narrator knows nothing about the console or the GUI, which is what
 * keeps the game logic separate from the user interface.
 */
public class Narrator {

    private final List<NarrationListener> listeners = new ArrayList<>();
    private int currentRound = 0;

    public void addListener(NarrationListener listener) {
        listeners.add(listener);
    }

    public void removeListener(NarrationListener listener) {
        listeners.remove(listener);
    }

    public void setRound(int round) {
        this.currentRound = round;
    }

    /** A normal line of narration. The viewer pauses after it. */
    public void log(String message) {
        publish(new NarratedLine(message, styleFor(message), LineTag.NONE, null, currentRound, true));
    }

    /** A line that also changes a contestant's state (eliminated, immunity). */
    public void log(String message, LineTag tag, String subject) {
        publish(new NarratedLine(message, styleFor(message), tag, subject, currentRound, true));
    }

    /** A section header such as "=== ROUND 1 ===". No pause after it. */
    public void header(String message) {
        publish(new NarratedLine(message, styleFor(message), LineTag.NONE, null, currentRound, false));
    }

    /** A blank spacer line. No pause after it. */
    public void blank() {
        publish(new NarratedLine("", LineStyle.BLANK, LineTag.NONE, null, currentRound, false));
    }

    private void publish(NarratedLine line) {
        for (NarrationListener listener : listeners) {
            listener.onLine(line);
        }
    }

    /**
     * Decides how a message should be styled, based on what it says.
     * Package-private so the unit tests can check it directly.
     */
    LineStyle styleFor(String message) {
        if (message.startsWith("=== MIYAGI MAYHEM")) {
            return LineStyle.TITLE;
        }
        if (message.startsWith("=== ROUND")) {
            return LineStyle.ROUND;
        }
        if (message.startsWith("=== ELIMINATION")) {
            return LineStyle.ELIMINATION;
        }
        if (message.startsWith("=== FINAL RESULT")) {
            return LineStyle.WINNER;
        }
        if (message.startsWith("===")) {
            return LineStyle.HEADER;
        }
        if (message.contains("WINNER OF MIYAGI MAYHEM") || message.startsWith("WINNER:")) {
            return LineStyle.WINNER;
        }
        if (message.contains("has been voted out")) {
            return LineStyle.ELIMINATION;
        }
        return LineStyle.NORMAL;
    }
}
