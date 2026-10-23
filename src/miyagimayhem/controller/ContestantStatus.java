package miyagimayhem.controller;

/**
 * Where a contestant stands while a game is being shown, as the sidebar displays it.
 */
public enum ContestantStatus {
    ACTIVE("In the game"),
    IMMUNE("Immune"),
    OUT("Voted out"),
    WINNER("Winner!");

    private final String label;

    ContestantStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
