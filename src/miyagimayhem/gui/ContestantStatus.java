package miyagimayhem.gui;

/**
 * What the sidebar shows next to each contestant's name.
 */
enum ContestantStatus {
    ACTIVE("In the game"),
    IMMUNE("Immune"),
    OUT("Voted out"),
    WINNER("Winner!");

    private final String label;

    ContestantStatus(String label) {
        this.label = label;
    }

    String getLabel() {
        return label;
    }
}
