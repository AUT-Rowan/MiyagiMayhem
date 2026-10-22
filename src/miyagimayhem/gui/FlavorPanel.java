package miyagimayhem.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import miyagimayhem.controller.GameController;
import miyagimayhem.db.DataAccessException;
import miyagimayhem.logic.FlavorText;
import miyagimayhem.model.FlavorLine;

/**
 * Lets the player add and remove the text the show uses: challenge names,
 * reactions to being voted out, winner lines and elimination quotes.
 */
class FlavorPanel extends JPanel {

    private final GameController controller;

    private final JComboBox<String> kindBox = new JComboBox<>();
    private final DefaultListModel<FlavorLine> linesModel = new DefaultListModel<>();
    private final JList<FlavorLine> linesList = new JList<>(linesModel);
    private final JTextField newLineField = new JTextField();
    private final JLabel feedback = new JLabel(" ");
    private final JLabel hint = new JLabel(" ");

    FlavorPanel(GameController controller, Navigator navigator) {
        this.controller = controller;

        setLayout(new BorderLayout(16, 16));
        setBackground(UiTheme.BACKGROUND);
        setBorder(UiTheme.padding(24));

        add(UiTheme.heading("Edit Show Text"), BorderLayout.NORTH);
        add(buildCentre(), BorderLayout.CENTER);

        JButton back = new StyledButton("Back", false);
        back.addActionListener(e -> navigator.showMenu());
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.add(back, BorderLayout.WEST);
        add(bottom, BorderLayout.SOUTH);
    }

    /** Called each time the screen is shown. */
    void refresh() {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        for (String kind : FlavorText.KINDS) {
            model.addElement(FlavorText.label(kind));
        }
        kindBox.setModel(model);
        kindBox.setSelectedIndex(0);
        loadLines();
    }

    private JPanel buildCentre() {
        kindBox.setFont(UiTheme.BODY);
        kindBox.addActionListener(e -> loadLines());

        linesList.setFont(UiTheme.BODY);
        linesList.setBackground(UiTheme.PANEL);
        linesList.setForeground(UiTheme.TEXT);
        linesList.setSelectionBackground(UiTheme.ACCENT);
        linesList.setSelectionForeground(UiTheme.ACCENT_TEXT);
        linesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        hint.setFont(UiTheme.BODY);
        hint.setForeground(UiTheme.MUTED);
        feedback.setFont(UiTheme.BODY);

        JPanel top = new JPanel(new GridLayout(0, 1, 0, 6));
        top.setOpaque(false);
        top.add(UiTheme.label("Type of text"));
        top.add(kindBox);

        newLineField.setFont(UiTheme.BODY);
        newLineField.setBackground(UiTheme.FIELD);
        newLineField.setForeground(UiTheme.TEXT);
        newLineField.setCaretColor(UiTheme.TEXT);
        newLineField.addActionListener(e -> addLine());
        JButton add = new StyledButton("Add line", true);
        add.addActionListener(e -> addLine());
        JButton delete = new StyledButton("Delete selected", false);
        delete.addActionListener(e -> deleteSelected());

        JPanel entry = new JPanel(new BorderLayout(8, 0));
        entry.setOpaque(false);
        entry.add(newLineField, BorderLayout.CENTER);
        entry.add(add, BorderLayout.EAST);

        JPanel south = new JPanel(new GridLayout(0, 1, 0, 6));
        south.setOpaque(false);
        south.add(hint);
        south.add(entry);
        south.add(feedback);
        JPanel deleteRow = new JPanel(new BorderLayout());
        deleteRow.setOpaque(false);
        deleteRow.add(delete, BorderLayout.WEST);
        south.add(deleteRow);

        JPanel centre = new JPanel(new BorderLayout(8, 12));
        centre.setOpaque(false);
        centre.add(top, BorderLayout.NORTH);
        centre.add(new JScrollPane(linesList), BorderLayout.CENTER);
        centre.add(south, BorderLayout.SOUTH);
        return centre;
    }

    private String selectedKind() {
        int index = kindBox.getSelectedIndex();
        return index < 0 ? FlavorText.KINDS.get(0) : FlavorText.KINDS.get(index);
    }

    private void loadLines() {
        try {
            linesModel.clear();
            for (FlavorLine line : controller.getFlavorLines(selectedKind())) {
                linesModel.addElement(line);
            }
            hint.setText(hintFor(selectedKind()));
            setFeedback(" ", false);
        } catch (DataAccessException e) {
            UiTheme.showDatabaseError(this, e);
        }
    }

    private void addLine() {
        try {
            String error = controller.addFlavorLine(selectedKind(), newLineField.getText());
            if (error != null) {
                setFeedback(error, true);
                return;
            }
            newLineField.setText("");
            loadLines();
            setFeedback("Line added.", false);
        } catch (DataAccessException e) {
            UiTheme.showDatabaseError(this, e);
        }
    }

    private void deleteSelected() {
        FlavorLine selected = linesList.getSelectedValue();
        if (selected == null) {
            setFeedback("Pick a line to delete first.", true);
            return;
        }
        try {
            controller.deleteFlavorLine(selected.getId());
            loadLines();
            setFeedback("Line deleted. (If a type has no lines left, the built-in text is used.)", false);
        } catch (DataAccessException e) {
            UiTheme.showDatabaseError(this, e);
        }
    }

    private void setFeedback(String message, boolean isError) {
        feedback.setText(message);
        feedback.setForeground(isError ? UiTheme.DANGER : UiTheme.OK);
    }

    private static String hintFor(String kind) {
        switch (kind) {
            case FlavorText.LOSER:
                return "Use %s for the name of the contestant who lost (once or twice).";
            case FlavorText.WINNER:
            case FlavorText.TIEBREAKER:
                return "Use %s exactly once, where the contestant's name goes.";
            case FlavorText.PAIRED:
                return "Use %s exactly twice, once for each contestant's name.";
            case FlavorText.GROUP3:
                return "Use %s exactly three times, once for each contestant's name.";
            case FlavorText.GROUP4:
                return "Use %s exactly four times, once for each contestant's name.";
            default:
                return "Plain text, no %s needed.";
        }
    }
}
