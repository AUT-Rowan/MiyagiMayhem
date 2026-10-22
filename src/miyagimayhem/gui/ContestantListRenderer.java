package miyagimayhem.gui;

import java.awt.Color;
import java.awt.Component;
import java.util.Map;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

/**
 * Draws each contestant's name with a coloured status next to it.
 */
class ContestantListRenderer extends DefaultListCellRenderer {

    private final Map<String, ContestantStatus> statuses;

    ContestantListRenderer(Map<String, ContestantStatus> statuses) {
        this.statuses = statuses;
    }

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index,
            boolean selected, boolean focused) {
        super.getListCellRendererComponent(list, value, index, false, false);
        ContestantStatus status = statuses.getOrDefault(String.valueOf(value), ContestantStatus.ACTIVE);
        setText(value + "  -  " + status.getLabel());
        setForeground(colourFor(status));
        setFont(status == ContestantStatus.WINNER ? UiTheme.BOLD : UiTheme.BODY);
        setBackground(UiTheme.PANEL);
        setBorder(UiTheme.padding(6));
        return this;
    }

    private static Color colourFor(ContestantStatus status) {
        switch (status) {
            case IMMUNE:
                return UiTheme.INFO;
            case OUT:
                return UiTheme.MUTED;
            case WINNER:
                return UiTheme.ACCENT;
            default:
                return UiTheme.TEXT;
        }
    }
}
