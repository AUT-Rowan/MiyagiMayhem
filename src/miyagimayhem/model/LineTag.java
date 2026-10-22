package miyagimayhem.model;

/**
 * Marks narrated lines that change the state of a contestant, so the GUI
 * can update its sidebar at the moment the line is revealed.
 */
public enum LineTag {
    NONE,
    ELIMINATED,
    IMMUNITY_GRANTED,
    IMMUNITY_CLEARED
}
