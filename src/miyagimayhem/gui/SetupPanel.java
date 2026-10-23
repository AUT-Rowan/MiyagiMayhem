package miyagimayhem.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import miyagimayhem.controller.GameController;
import miyagimayhem.controller.RosterEditor;
import miyagimayhem.logic.RosterValidator;

/**
 * Lets the player build a roster of 3 to 10 contestants and start the game.
 * The list itself is kept by RosterEditor and the saved rosters side is its
 * own panel, so this class mostly arranges components and shows messages.
 */
class SetupPanel extends JPanel {

    private final Navigator navigator;
    private final RosterEditor editor = new RosterEditor();
    private final SavedRostersPanel savedRosters;

    private final DefaultListModel<String> members = new DefaultListModel<>();
    private final JList<String> memberList = new JList<>(members);
    private final JTextField nameField = new JTextField();
    private final JLabel feedback = new JLabel(" ");
    private final JLabel counter = UiTheme.label("");
    private final JButton startButton = new StyledButton("Start Game", true);
    private final JButton removeButton = new StyledButton("Remove", false);

    SetupPanel(GameController controller, Navigator navigator) {
        this.navigator = navigator;
        this.savedRosters = new SavedRostersPanel(controller, editor::getNames, this::loadNames, this::setFeedback);

        setLayout(new BorderLayout(16, 16));
        setBackground(UiTheme.BACKGROUND);
        setBorder(UiTheme.padding(24));

        add(UiTheme.heading("Choose your contestants"), BorderLayout.NORTH);
        add(buildCentre(), BorderLayout.CENTER);
        add(buildBottom(), BorderLayout.SOUTH);
        updateState();
    }

    /** Called each time the screen is shown. */
    void refresh() {
        savedRosters.refresh();
        nameField.requestFocusInWindow();
    }

    private JPanel buildCentre() {
        JPanel centre = new JPanel(new GridLayout(1, 2, 20, 0));
        centre.setOpaque(false);
        centre.add(buildMembersColumn());
        centre.add(savedRosters);
        return centre;
    }

    private JPanel buildMembersColumn() {
        JPanel column = new JPanel(new BorderLayout(8, 8));
        column.setOpaque(false);

        JPanel entry = new JPanel(new BorderLayout(8, 0));
        entry.setOpaque(false);
        nameField.setFont(UiTheme.BODY);
        nameField.setBackground(UiTheme.FIELD);
        nameField.setForeground(UiTheme.TEXT);
        nameField.setCaretColor(UiTheme.TEXT);
        nameField.addActionListener(e -> addName());
        JButton addButton = new StyledButton("Add", false);
        addButton.addActionListener(e -> addName());
        entry.add(nameField, BorderLayout.CENTER);
        entry.add(addButton, BorderLayout.EAST);

        JPanel top = new JPanel(new BorderLayout(0, 4));
        top.setOpaque(false);
        top.add(UiTheme.label("Contestant name"), BorderLayout.NORTH);
        top.add(entry, BorderLayout.CENTER);
        feedback.setFont(UiTheme.BODY);
        feedback.setForeground(UiTheme.DANGER);
        top.add(feedback, BorderLayout.SOUTH);

        styleList(memberList);
        memberList.addListSelectionListener(e -> updateState());

        removeButton.addActionListener(e -> removeSelected());
        JPanel listButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        listButtons.setOpaque(false);
        listButtons.add(removeButton);
        JButton clearButton = new StyledButton("Clear all", false);
        clearButton.addActionListener(e -> {
            editor.clear();
            setFeedback(" ", false);
            updateState();
        });
        listButtons.add(clearButton);
        listButtons.add(counter);

        column.add(top, BorderLayout.NORTH);
        column.add(new JScrollPane(memberList), BorderLayout.CENTER);
        column.add(listButtons, BorderLayout.SOUTH);
        return column;
    }

    private JPanel buildBottom() {
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        JButton back = new StyledButton("Back", false);
        back.addActionListener(e -> navigator.showMenu());
        startButton.addActionListener(e -> startGame());
        bottom.add(back, BorderLayout.WEST);
        bottom.add(startButton, BorderLayout.EAST);
        return bottom;
    }

    private static void styleList(JList<String> list) {
        list.setFont(UiTheme.BODY);
        list.setBackground(UiTheme.PANEL);
        list.setForeground(UiTheme.TEXT);
        list.setSelectionBackground(UiTheme.ACCENT);
        list.setSelectionForeground(UiTheme.ACCENT_TEXT);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setPreferredSize(new Dimension(200, 200));
    }

    private void addName() {
        String error = editor.add(nameField.getText());
        if (error != null) {
            setFeedback(error, true);
            return;
        }
        nameField.setText("");
        setFeedback(" ", false);
        updateState();
    }

    private void removeSelected() {
        editor.removeAt(memberList.getSelectedIndex());
        setFeedback(" ", false);
        updateState();
    }

    private void loadNames(List<String> names) {
        editor.replaceWith(names);
        setFeedback(" ", false);
        updateState();
    }

    private void startGame() {
        String error = editor.checkReady();
        if (error != null) {
            setFeedback(error, true);
            return;
        }
        navigator.showGame(editor.getNames());
    }

    private void setFeedback(String message, boolean isError) {
        feedback.setText(message);
        feedback.setForeground(isError ? UiTheme.DANGER : UiTheme.OK);
    }

    /** Redraws the list, the counter and the buttons from the editor's state. */
    private void updateState() {
        members.clear();
        for (String name : editor.getNames()) {
            members.addElement(name);
        }
        counter.setText(editor.size() + " / " + RosterValidator.MAX_CONTESTANTS
                + " (min " + RosterValidator.MIN_CONTESTANTS + ")");
        startButton.setEnabled(editor.isReady());
        removeButton.setEnabled(memberList.getSelectedIndex() >= 0);
    }
}
