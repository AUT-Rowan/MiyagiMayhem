package miyagimayhem.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import miyagimayhem.controller.GameController;
import miyagimayhem.db.DataAccessException;

/**
 * The "saved rosters" side of the setup screen: pick a roster from the
 * database to load, save the current list under a name, or delete one.
 * It doesn't own the list of names. It asks for the current names when
 * saving and hands back the names when one is loaded.
 */
class SavedRostersPanel extends JPanel {

    private final GameController controller;
    private final Supplier<List<String>> currentNames;
    private final Consumer<List<String>> onLoaded;
    private final BiConsumer<String, Boolean> showMessage;

    private final JComboBox<String> rosterBox = new JComboBox<>();

    /**
     * @param currentNames gives the names on the setup screen right now
     * @param onLoaded called with the names of a roster the player loaded
     * @param showMessage shows a message (text, true if it is an error)
     */
    SavedRostersPanel(GameController controller, Supplier<List<String>> currentNames,
            Consumer<List<String>> onLoaded, BiConsumer<String, Boolean> showMessage) {
        this.controller = controller;
        this.currentNames = currentNames;
        this.onLoaded = onLoaded;
        this.showMessage = showMessage;

        setOpaque(false);
        setLayout(new BorderLayout(8, 8));
        add(UiTheme.label("Saved rosters"), BorderLayout.NORTH);

        JPanel box = new JPanel(new GridLayout(0, 1, 0, 8));
        box.setOpaque(false);
        rosterBox.setFont(UiTheme.BODY);
        box.add(rosterBox);
        box.add(button("Load into list", false, this::loadSelected));
        box.add(button("Save current list as...", false, this::saveCurrent));
        box.add(button("Delete saved roster", false, this::deleteSelected));

        JPanel holder = new JPanel(new BorderLayout());
        holder.setOpaque(false);
        holder.add(box, BorderLayout.NORTH);
        add(holder, BorderLayout.CENTER);
    }

    /** Reloads the roster names from the database. */
    void refresh() {
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

    private static JButton button(String text, boolean primary, Runnable action) {
        JButton button = new StyledButton(text, primary);
        button.addActionListener(e -> action.run());
        return button;
    }

    private void loadSelected() {
        String rosterName = (String) rosterBox.getSelectedItem();
        if (rosterName == null) {
            showMessage.accept("There are no saved rosters yet.", true);
            return;
        }
        try {
            onLoaded.accept(controller.loadRoster(rosterName));
        } catch (DataAccessException e) {
            UiTheme.showDatabaseError(this, e);
        }
    }

    private void saveCurrent() {
        List<String> names = currentNames.get();
        String rosterName = JOptionPane.showInputDialog(this, "Name for this roster:", "Save roster",
                JOptionPane.PLAIN_MESSAGE);
        if (rosterName == null) {
            return;
        }
        try {
            String error = controller.saveRoster(rosterName, names);
            if (error != null) {
                showMessage.accept(error, true);
                return;
            }
            refresh();
            rosterBox.setSelectedItem(controller.normalizeName(rosterName));
            showMessage.accept("Roster saved.", false);
        } catch (DataAccessException e) {
            UiTheme.showDatabaseError(this, e);
        }
    }

    private void deleteSelected() {
        String rosterName = (String) rosterBox.getSelectedItem();
        if (rosterName == null) {
            return;
        }
        int answer = JOptionPane.showConfirmDialog(this, "Delete the saved roster '" + rosterName + "'?",
                "Delete roster", JOptionPane.YES_NO_OPTION);
        if (answer == JOptionPane.YES_OPTION) {
            try {
                controller.deleteRoster(rosterName);
                refresh();
                showMessage.accept("Roster deleted.", false);
            } catch (DataAccessException e) {
                UiTheme.showDatabaseError(this, e);
            }
        }
    }
}
