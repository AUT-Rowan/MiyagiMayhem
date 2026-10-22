package miyagimayhem.gui;

import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import miyagimayhem.model.LineStyle;
import miyagimayhem.model.NarratedLine;

/**
 * Scrolling coloured text area that shows narrated lines. Used for the live
 * game and for replaying a saved game from the history.
 */
class TranscriptView extends JScrollPane {

    private final JTextPane pane = new JTextPane();

    TranscriptView() {
        pane.setEditable(false);
        pane.setBackground(UiTheme.PANEL);
        pane.setFont(UiTheme.MONO);
        pane.setBorder(UiTheme.padding(10));
        setViewportView(pane);
        setBorder(BorderFactory.createLineBorder(UiTheme.FIELD));
        getVerticalScrollBar().setUnitIncrement(16);
    }

    void clear() {
        pane.setText("");
    }

    void append(NarratedLine line) {
        StyledDocument document = pane.getStyledDocument();
        try {
            document.insertString(document.getLength(), line.getText() + "\n", attributesFor(line.getStyle()));
        } catch (BadLocationException e) {
            throw new IllegalStateException("Appending at the end of the document can't be out of range", e);
        }
        pane.setCaretPosition(document.getLength());
    }

    private static SimpleAttributeSet attributesFor(LineStyle style) {
        SimpleAttributeSet attributes = new SimpleAttributeSet();
        Color colour = UiTheme.TEXT;
        boolean bold = false;
        int size = 15;
        switch (style) {
            case TITLE:
                colour = UiTheme.ACCENT;
                bold = true;
                size = 20;
                break;
            case ROUND:
                colour = UiTheme.INFO;
                bold = true;
                break;
            case ELIMINATION:
                colour = UiTheme.DANGER;
                bold = true;
                break;
            case WINNER:
                colour = UiTheme.ACCENT;
                bold = true;
                size = 18;
                break;
            case HEADER:
                colour = UiTheme.MUTED;
                bold = true;
                break;
            default:
                break;
        }
        StyleConstants.setForeground(attributes, colour);
        StyleConstants.setBold(attributes, bold);
        StyleConstants.setFontSize(attributes, size);
        StyleConstants.setFontFamily(attributes, UiTheme.MONO.getFamily());
        return attributes;
    }
}
