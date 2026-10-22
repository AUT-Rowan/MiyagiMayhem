package miyagimayhem.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import miyagimayhem.controller.GameController;
import miyagimayhem.db.DataAccessException;
import miyagimayhem.model.GameResult;
import miyagimayhem.model.NarratedLine;

/**
 * Past games with their transcripts, plus a leaderboard of who has won most.
 */
class HistoryPanel extends JPanel {

    private final GameController controller;
    private final Navigator navigator;

    private final DefaultTableModel gamesModel = readOnlyModel("Played", "Winner", "Players", "Rounds");
    private final DefaultTableModel leaderboardModel = readOnlyModel("Rank", "Contestant", "Wins");
    private final JTable gamesTable = new JTable(gamesModel);
    private final TranscriptView transcript = new TranscriptView();
    private List<GameResult> games;

    HistoryPanel(GameController controller, Navigator navigator) {
        this.controller = controller;
        this.navigator = navigator;

        setLayout(new BorderLayout(16, 16));
        setBackground(UiTheme.BACKGROUND);
        setBorder(UiTheme.padding(24));

        add(UiTheme.heading("Game History"), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UiTheme.BOLD);
        tabs.addTab("Past games", buildGamesTab());
        tabs.addTab("Leaderboard", buildLeaderboardTab());
        add(tabs, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        JButton back = new StyledButton("Back", false);
        back.addActionListener(e -> navigator.showMenu());
        JButton clear = new StyledButton("Clear history", false);
        clear.addActionListener(e -> clearHistory());
        bottom.add(back, BorderLayout.WEST);
        bottom.add(clear, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);
    }

    /** Reloads everything from the database each time the screen is shown. */
    void refresh() {
        try {
            games = controller.getHistory();
            gamesModel.setRowCount(0);
            SimpleDateFormat format = new SimpleDateFormat("d MMM yyyy, h:mm a");
            for (GameResult game : games) {
                gamesModel.addRow(new Object[] {format.format(game.getPlayedAt()), game.getWinner(),
                    game.getContestantCount() + " players", game.getRounds()});
            }
            leaderboardModel.setRowCount(0);
            int rank = 1;
            for (Map.Entry<String, Integer> entry : controller.getLeaderboard().entrySet()) {
                leaderboardModel.addRow(new Object[] {rank++, entry.getKey(), entry.getValue()});
            }
            transcript.clear();
            if (!games.isEmpty()) {
                gamesTable.setRowSelectionInterval(0, 0);
            }
        } catch (DataAccessException e) {
            UiTheme.showDatabaseError(this, e);
        }
    }

    private JSplitPane buildGamesTab() {
        styleTable(gamesTable);
        gamesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        gamesTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showSelectedTranscript();
            }
        });
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(gamesTable), transcript);
        split.setResizeWeight(0.35);
        split.setBorder(null);
        return split;
    }

    private JScrollPane buildLeaderboardTab() {
        JTable table = new JTable(leaderboardModel);
        styleTable(table);
        return new JScrollPane(table);
    }

    private void showSelectedTranscript() {
        transcript.clear();
        int row = gamesTable.getSelectedRow();
        if (row < 0 || games == null || row >= games.size()) {
            return;
        }
        try {
            for (NarratedLine line : controller.getTranscript(games.get(row).getId())) {
                transcript.append(line);
            }
        } catch (DataAccessException e) {
            UiTheme.showDatabaseError(this, e);
        }
    }

    private void clearHistory() {
        int answer = JOptionPane.showConfirmDialog(this,
                "Delete every saved game and reset the leaderboard? This can't be undone.",
                "Clear history", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (answer == JOptionPane.YES_OPTION) {
            try {
                controller.clearHistory();
                refresh();
            } catch (DataAccessException e) {
                UiTheme.showDatabaseError(this, e);
            }
        }
    }

    private static DefaultTableModel readOnlyModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private static void styleTable(JTable table) {
        table.setFillsViewportHeight(true);
        table.setFont(UiTheme.BODY);
        table.setRowHeight(28);
        table.setBackground(UiTheme.PANEL);
        table.setForeground(UiTheme.TEXT);
        table.setGridColor(UiTheme.FIELD);
        table.setSelectionBackground(UiTheme.ACCENT);
        table.setSelectionForeground(UiTheme.ACCENT_TEXT);
        table.getTableHeader().setFont(UiTheme.BOLD);
    }
}
