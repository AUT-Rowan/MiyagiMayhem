package miyagimayhem.model;

/**
 * One piece of editable show text (a challenge name, reaction line or
 * elimination quote) together with its database id.
 */
public class FlavorLine {

    private final int id;
    private final String kind;
    private final String text;

    public FlavorLine(int id, String kind, String text) {
        this.id = id;
        this.kind = kind;
        this.text = text;
    }

    public int getId() {
        return id;
    }

    public String getKind() {
        return kind;
    }

    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return text;
    }
}
