package miyagimayhem.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import miyagimayhem.controller.GameController;
import miyagimayhem.db.DataAccessException;
import miyagimayhem.logic.RosterValidator;

/**
 * Lets the player build a roster of 3 to 10 contestants, load or save named
 * rosters, and start the game.
 */
class SetupPanel extends JPanel {

    private final GameController controller;
    private final Navigator navigator;

    private final DefaultListModel<String> members = new DefaultListModel<>();
    private final JList<String> memberList = new JList<>(members);
    private final JTextField nameField = new JTextField();
    private final JLabel feedback = new JLabel(" ");
    private final JLabel counter = UiTheme.label("");
    private final JComboBox<String> rosterBox = new JComboBox<>();
    private final JButton startButton = new StyledButton("Start Game", true);
    private final JButton removeButton = new StyledButton("Remove", false);

    SetupPanel(GameController controller, Navigator navigator) {
        this.controller = controller;
        this.navigator = navigator;

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
        loadRosterNames();
        nameField.requestFocusInWindow();
    }

    private JPanel buildCentre() {
        JPanel centre = new JPanel(new GridLayout(1, 2, 20, 0));
        centre.setOpaque(false);
        centre.add(buildMembersColumn());
        centre.add(buildRostersColumn());
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
            members.clear();
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

    private JPanel buildRostersColumn() {
        JPanel column = new JPanel(new BorderLayout(8, 8));
        column.setOpaque(false);
        column.add(UiTheme.label("Saved rosters"), BorderLayout.NORTH);

        JPanel box = new JPanel(new GridLayout(0, 1, 0, 8));
        box.setOpaque(false);
        rosterBox.setFont(UiTheme.BODY);
        box.add(rosterBox);

        JButton load = new StyledButton("Load into list", false);
        load.addActionListener(e -> loadSelectedRoster());
        JButton save = new StyledButton("Save current list as...", false);
        save.addActionListener(e -> saveCurrentRoster());
        JButton delete = new StyledButton("Delete saved roster", false);
        delete.addActionListener(e -> deleteSelectedRoster());
        box.add(load);
        box.add(save);
        box.add(delete);

        JPanel holder = new JPanel(new BorderLayout());
        holder.setOpaque(false);
        holder.add(box, BorderLayout.NORTH);
        column.add(holder, BorderLayout.CENTER);
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

    private List<String> currentNames() {
        return Collections.list(members.elements());
    }

    private void addName() {
        String error = controller.validateName(nameField.getText(), currentNames());
        if (error != null) {
            setFeedback(error, true);
            return;
        }
        members.addElement(controller.normalizeName(nameField.getText()));
        nameField.setText("");
        setFeedback(" ", false);
        updateState();
    }

    private void removeSelected() {
        int index = memberList.getSelectedIndex();
        if (index >= 0) {
            members.remove(index);
            setFeedback(" ", false);
            updateState();
        }
    }

    private void loadRosterNames() {
        try {
            DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
            for (String name : controller.getRosterNames()) {
                model.addElement(name);
            }
            rosterBox.setModel(model);
        } catch (DataAccessException e) {
            UiTheme.showDatabaseError(this, e);
        }
    }

    private void loadSelectedRoster() {
        String rosterName = (String) rosterBox.getSelectedItem();
        if (rosterName == null) {
            setFeedback("There are no saved rosters yet.", true);
            return;
        }
        try {
            members.clear();
            for (String name : controller.loadRoster(rosterName)) {
                members.addElement(name);
            }
            setFeedback(" ", false);
            updateState();
        } catch (DataAccessException e) {
            UiTheme.showDatabaseError(this, e);
        }
    }

    private void saveCurrentRoster() {
        String error = controller.validateRoster(currentNames());
        if (error != null) {
            setFeedback(error, true);
            return;
        }
        String rosterName = JOptionPane.showInputDialog(this, "Name for this roster:", "Save roster",
                JOptionPane.PLAIN_MESSAGE);
        if (rosterName == null) {
            return;
        }
        try {
            error = controller.saveRoster(rosterName, currentNames());
            if (error != null) {
                setFeedback(error, true);
                return;
            }
            loadRosterNames();
            rosterBox.setSelectedItem(controller.normalizeName(rosterName));
            setFeedback("Roster saved.", false);
        } catch (DataAccessException e) {
            UiTheme.showDatabaseError(this, e);
        }
    }

    private void deleteSelectedRoster() {
        String rosterName = (String) rosterBox.getSelectedItem();
        if (rosterName == null) {
            return;
        }
        int answer = JOptionPane.showConfirmDialog(this, "Delete the saved roster '" + rosterName + "'?",
                "Delete roster", JOptionPane.YES_NO_OPTION);
        if (answer == JOptionPane.YES_OPTION) {
            try {
                controller.deleteRoster(rosterName);
                loadRosterNames();
                setFeedback("Roster deleted.", false);
            } catch (DataAccessException e) {
                UiTheme.showDatabaseError(this, e);
            }
        }
    }

    private void startGame() {
        List<String> names = new ArrayList<>(currentNames());
        String error = controller.validateRoster(names);
        if (error != null) {
            setFeedback(error, true);
            return;
        }
        navigator.showGame(names);
    }

    private void setFeedback(String message, boolean isError) {
        feedback.setText(message);
        feedback.setForeground(isError ? UiTheme.DANGER : UiTheme.OK);
    }

    private void updateState() {
        int count = members.size();
        counter.setText(count + " / " + RosterValidator.MAX_CONTESTANTS
                + " (min " + RosterValidator.MIN_CONTESTANTS + ")");
        startButton.setEnabled(controller.validateRoster(currentNames()) == null);
        removeButton.setEnabled(memberList.getSelectedIndex() >= 0);
    }
}
