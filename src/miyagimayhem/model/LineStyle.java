package miyagimayhem.model;

/**
 * How a narrated line should look when it is shown. The logic layer only
 * decides the style, the GUI decides which colour each style gets.
 */
public enum LineStyle {
    NORMAL,
    TITLE,
    ROUND,
    ELIMINATION,
    WINNER,
    HEADER,
    BLANK
}
