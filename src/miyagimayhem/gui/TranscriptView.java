package miyagimayhem.gui;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    private Pattern namePattern;

    TranscriptView() {
        pane.setEditable(false);
        pane.setBackground(UiTheme.PANEL);
        pane.setFont(UiTheme.MONO);
        pane.setBorder(UiTheme.padding(10));
        setViewportView(pane);
        setBorder(BorderFactory.createLineBorder(UiTheme.FIELD));
        getVerticalScrollBar().setUnitIncrement(16);
    }

    /** Sets which names are shown in bold (call before appending lines). */
    void setNames(Collection<String> names) {
        namePattern = buildNamePattern(names);
    }

    void clear() {
        pane.setText("");
    }

    void append(NarratedLine line) {
        StyledDocument document = pane.getStyledDocument();
        SimpleAttributeSet lineAttributes = attributesFor(line.getStyle());
        String text = line.getText();
        try {
            int written = 0;
            if (namePattern != null) {
                Matcher matcher = namePattern.matcher(text);
                while (matcher.find()) {
                    document.insertString(document.getLength(), text.substring(written, matcher.start()), lineAttributes);
                    document.insertString(document.getLength(), matcher.group(), boldVersionOf(lineAttributes));
                    written = matcher.end();
                }
            }
            document.insertString(document.getLength(), text.substring(written) + "\n", lineAttributes);
        } catch (BadLocationException e) {
            throw new IllegalStateException("Appending at the end of the document can't be out of range", e);
        }
        pane.setCaretPosition(document.getLength());
    }

    /** Matches any contestant name as a whole word, longest names first. */
    private static Pattern buildNamePattern(Collection<String> contestantNames) {
        if (contestantNames.isEmpty()) {
            return null;
        }
        List<String> names = new ArrayList<>(contestantNames);
        names.sort(Comparator.comparingInt(String::length).reversed());
        StringBuilder regex = new StringBuilder("(?<![\\p{L}\\p{N}])(?:");
        for (int i = 0; i < names.size(); i++) {
            if (i > 0) {
                regex.append('|');
            }
            regex.append(Pattern.quote(names.get(i)));
        }
        regex.append(")(?![\\p{L}\\p{N}])");
        return Pattern.compile(regex.toString());
    }

    private static SimpleAttributeSet boldVersionOf(SimpleAttributeSet base) {
        SimpleAttributeSet attributes = new SimpleAttributeSet(base);
        StyleConstants.setBold(attributes, true);
        return attributes;
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
                colour = UiTheme.GOLD;
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
