package miyagimayhem.gui;

import java.awt.Color;
import java.awt.Component;
import miyagimayhem.controller.ContestantStatus;
import miyagimayhem.controller.GamePlayback;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

/**
 * Draws each contestant's name with a coloured status next to it.
 */
class ContestantListRenderer extends DefaultListCellRenderer {

    private final GamePlayback playback;
    private final ContestantColors colours;

    ContestantListRenderer(GamePlayback playback, ContestantColors colours) {
        this.playback = playback;
        this.colours = colours;
    }

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index,
            boolean selected, boolean focused) {
        super.getListCellRendererComponent(list, value, index, false, false);
        String name = String.valueOf(value);
        ContestantStatus status = playback.getStatus(name);
        setText(name + "  -  " + status.getLabel());
        setIcon(new ColourDot(colours.get(name)));
        setIconTextGap(8);
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
                return UiTheme.GOLD;
            default:
                return UiTheme.TEXT;
        }
    }
}
