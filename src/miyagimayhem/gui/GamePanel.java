package miyagimayhem.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.Timer;
import miyagimayhem.controller.GameController;
import miyagimayhem.db.DataAccessException;
import miyagimayhem.model.GameSession;
import miyagimayhem.model.LineStyle;
import miyagimayhem.model.NarratedLine;

/**
 * Plays one game. The whole game is worked out first, then revealed a few
 * lines at a time: by pressing Next, or automatically at a chosen speed.
 * When the last line is shown the game is saved to the database.
 */
class GamePanel extends JPanel {

    private static final int FASTEST_DELAY_MS = 300;
    private static final int SLOWEST_DELAY_MS = 2500;

    private final GameController controller;
    private final Navigator navigator;

    private final TranscriptView transcript = new TranscriptView();
    private final DefaultListModel<String> sidebarModel = new DefaultListModel<>();
    private final Map<String, ContestantStatus> statuses = new HashMap<>();
    private final JLabel statusLabel = UiTheme.label(" ");
    private final JButton nextButton = new StyledButton("Next", true);
    private final JButton autoButton = new StyledButton("Auto-play", false);
    private final JButton skipButton = new StyledButton("Skip to end", false);
    private final JButton againButton = new StyledButton("Play again", true);
    private final JSlider speedSlider = new JSlider(0, 100, 50);
    private final Timer autoTimer;

    private GameSession session;
    private List<String> names = new ArrayList<>();
    private int nextLine;
    private boolean saved;

    GamePanel(GameController controller, Navigator navigator) {
        this.controller = controller;
        this.navigator = navigator;

        setLayout(new BorderLayout(16, 16));
        setBackground(UiTheme.BACKGROUND);
        setBorder(UiTheme.padding(20));

        autoTimer = new Timer(delayFromSlider(), e -> revealNext());
        autoTimer.setRepeats(true);

        add(buildTop(), BorderLayout.NORTH);
        add(transcript, BorderLayout.CENTER);
        add(buildSidebar(), BorderLayout.EAST);
        add(buildControls(), BorderLayout.SOUTH);
    }

    /** Works out a brand new game for these names and shows its first lines. */
    void startGame(List<String> contestantNames) {
        stopAutoPlay();
        names = new ArrayList<>(contestantNames);
        try {
            session = controller.play(names);
        } catch (DataAccessException e) {
            UiTheme.showDatabaseError(this, e);
            navigator.showMenu();
            return;
        }
        nextLine = 0;
        saved = false;
        transcript.clear();
        statuses.clear();
        sidebarModel.clear();
        for (String name : names) {
            statuses.put(name, ContestantStatus.ACTIVE);
            sidebarModel.addElement(name);
        }
        statusLabel.setText("Press Next to follow the show.");
        updateButtons();
        revealNext();
    }

    private JPanel buildTop() {
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(UiTheme.heading("The Show"), BorderLayout.WEST);
        statusLabel.setForeground(UiTheme.MUTED);
        top.add(statusLabel, BorderLayout.EAST);
        return top;
    }

    private JPanel buildSidebar() {
        JList<String> list = new JList<>(sidebarModel);
        list.setCellRenderer(new ContestantListRenderer(statuses));
        list.setBackground(UiTheme.PANEL);
        list.setFocusable(false);

        JPanel side = new JPanel(new BorderLayout(0, 6));
        side.setOpaque(false);
        side.setPreferredSize(new Dimension(230, 0));
        side.add(UiTheme.label("Contestants"), BorderLayout.NORTH);
        side.add(list, BorderLayout.CENTER);
        return side;
    }

    private JPanel buildControls() {
        JPanel controls = new JPanel(new BorderLayout());
        controls.setOpaque(false);

        JButton menuButton = new StyledButton("Menu", false);
        menuButton.addActionListener(e -> backToMenu());

        nextButton.addActionListener(e -> revealNext());
        autoButton.addActionListener(e -> toggleAutoPlay());
        skipButton.addActionListener(e -> skipToEnd());
        againButton.addActionListener(e -> startGame(names));

        speedSlider.setOpaque(false);
        speedSlider.setPreferredSize(new Dimension(130, 30));
        speedSlider.setToolTipText("Auto-play speed");
        speedSlider.addChangeListener(e -> autoTimer.setDelay(delayFromSlider()));

        JPanel centre = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        centre.setOpaque(false);
        centre.add(nextButton);
        centre.add(autoButton);
        centre.add(UiTheme.label("Fast"));
        centre.add(speedSlider);
        centre.add(UiTheme.label("Slow"));
        centre.add(skipButton);
        centre.add(againButton);

        controls.add(menuButton, BorderLayout.WEST);
        controls.add(centre, BorderLayout.CENTER);
        return controls;
    }

    private int delayFromSlider() {
        // Left end of the slider (0) is the shortest delay, right end (100) the longest.
        int value = speedSlider.getValue();
        return FASTEST_DELAY_MS + (SLOWEST_DELAY_MS - FASTEST_DELAY_MS) * value / 100;
    }

    private boolean isFinished() {
        return session == null || nextLine >= session.getLines().size();
    }

    /** Shows lines up to and including the next one that waits for the viewer. */
    private void revealNext() {
        if (isFinished()) {
            return;
        }
        List<NarratedLine> lines = session.getLines();
        boolean stop = false;
        while (!stop && nextLine < lines.size()) {
            NarratedLine line = lines.get(nextLine++);
            show(line);
            stop = line.isPause();
        }
        if (isFinished()) {
            finishGame();
        }
        updateButtons();
    }

    private void skipToEnd() {
        stopAutoPlay();
        while (!isFinished()) {
            show(session.getLines().get(nextLine++));
        }
        finishGame();
        updateButtons();
    }

    private void show(NarratedLine line) {
        transcript.append(line);
        if (line.getStyle() == LineStyle.ROUND) {
            clearImmunity();
        }
        applyTag(line);
        repaint();
    }

    private void applyTag(NarratedLine line) {
        String subject = line.getSubject();
        if (subject == null || !statuses.containsKey(subject)) {
            return;
        }
        switch (line.getTag()) {
            case ELIMINATED:
                statuses.put(subject, ContestantStatus.OUT);
                break;
            case IMMUNITY_GRANTED:
                if (statuses.get(subject) != ContestantStatus.OUT) {
                    statuses.put(subject, ContestantStatus.IMMUNE);
                }
                break;
            case IMMUNITY_CLEARED:
                if (statuses.get(subject) == ContestantStatus.IMMUNE) {
                    statuses.put(subject, ContestantStatus.ACTIVE);
                }
                break;
            default:
                break;
        }
    }

    private void clearImmunity() {
        for (Map.Entry<String, ContestantStatus> entry : statuses.entrySet()) {
            if (entry.getValue() == ContestantStatus.IMMUNE) {
                entry.setValue(ContestantStatus.ACTIVE);
            }
        }
    }

    private void finishGame() {
        stopAutoPlay();
        String winner = session.getWinnerName();
        if (statuses.containsKey(winner)) {
            statuses.put(winner, ContestantStatus.WINNER);
        }
        saveOnce();
    }

    private void saveOnce() {
        if (saved) {
            return;
        }
        try {
            controller.saveGame(session);
            saved = true;
            statusLabel.setText("Game over - saved to your history.");
        } catch (DataAccessException e) {
            statusLabel.setText("Game over - could not save it.");
            UiTheme.showDatabaseError(this, e);
        }
    }

    private void toggleAutoPlay() {
        if (autoTimer.isRunning()) {
            stopAutoPlay();
        } else {
            autoTimer.start();
        }
        updateButtons();
    }

    private void stopAutoPlay() {
        autoTimer.stop();
    }

    private void updateButtons() {
        boolean finished = isFinished();
        nextButton.setEnabled(!finished);
        autoButton.setEnabled(!finished);
        skipButton.setEnabled(!finished);
        againButton.setVisible(finished);
        autoButton.setText(autoTimer.isRunning() ? "Pause" : "Auto-play");
    }

    private void backToMenu() {
        if (!isFinished()) {
            stopAutoPlay();
            int answer = JOptionPane.showConfirmDialog(this,
                    "Leave this game? It won't be saved because it isn't finished.",
                    "Leave game", JOptionPane.YES_NO_OPTION);
            if (answer != JOptionPane.YES_OPTION) {
                updateButtons();
                return;
            }
        }
        stopAutoPlay();
        navigator.showMenu();
    }
}
