package miyagimayhem.model;

/**
 * One line of narration produced during a game. Immutable, so it is safe
 * to keep in a transcript and to store in the database.
 */
public class NarratedLine {

    private final String text;
    private final LineStyle style;
    private final LineTag tag;
    private final String subject; // contestant the tag applies to, or null
    private final int round;
    private final boolean pause;  // true if the viewer should wait before the next line

    public NarratedLine(String text, LineStyle style, LineTag tag, String subject, int round, boolean pause) {
        this.text = text;
        this.style = style;
        this.tag = tag;
        this.subject = subject;
        this.round = round;
        this.pause = pause;
    }

    public String getText() {
        return text;
    }

    public LineStyle getStyle() {
        return style;
    }

    public LineTag getTag() {
        return tag;
    }

    public String getSubject() {
        return subject;
    }

    public int getRound() {
        return round;
    }

    public boolean isPause() {
        return pause;
    }
}
