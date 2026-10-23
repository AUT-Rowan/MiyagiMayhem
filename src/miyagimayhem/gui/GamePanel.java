package miyagimayhem.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.Timer;
import miyagimayhem.controller.GameController;
import miyagimayhem.controller.GamePlayback;
import miyagimayhem.db.DataAccessException;
import miyagimayhem.model.GameSession;
import miyagimayhem.model.NarratedLine;

/**
 * Plays one game. The whole game is worked out first, then revealed a few
 * lines at a time: by pressing Next, or automatically at a chosen speed.
 * When the last line is shown the game is saved to the database.
 *
 * What has been revealed and who is out is tracked by GamePlayback, so this
 * class only has to draw it.
 */
class GamePanel extends JPanel {

    /** Auto-play delay for each slider position, 1 (slowest) to 7 (fastest). */
    private static final int[] DELAY_MS = {2800, 2000, 1500, 1100, 750, 450, 250};
    private static final int DEFAULT_SPEED = 4;

    private final GameController controller;
    private final Navigator navigator;

    private final TranscriptView transcript = new TranscriptView();
    private final DefaultListModel<String> sidebarModel = new DefaultListModel<>();
    private final JList<String> sidebar = new JList<>(sidebarModel);
    private final JLabel statusLabel = UiTheme.label(" ");
    private final StyledButton nextButton = new StyledButton("Next", true);
    private final JButton autoButton = new StyledButton("Auto-play", false);
    private final JButton skipButton = new StyledButton("Skip to end", false);
    private final JButton againButton = new StyledButton("Play again", true);
    private final JSlider speedSlider = new JSlider(1, DELAY_MS.length, DEFAULT_SPEED);
    private final Timer autoTimer;

    private GamePlayback playback;
    private List<String> names = new ArrayList<>();
    private boolean saved;

    GamePanel(GameController controller, Navigator navigator) {
        this.controller = controller;
        this.navigator = navigator;

        setLayout(new BorderLayout(16, 16));
        setBackground(UiTheme.BACKGROUND);
        setBorder(UiTheme.padding(20));

        autoTimer = new Timer(delayForSpeed(), e -> revealNext());
        autoTimer.setRepeats(true);

        add(buildTop(), BorderLayout.NORTH);
        add(transcript, BorderLayout.CENTER);
        add(buildSidebar(), BorderLayout.EAST);
        add(buildControls(), BorderLayout.SOUTH);
    }

    /** Works out a brand new game for these names and shows its first lines. */
    void startGame(List<String> contestantNames) {
        autoTimer.stop();
        names = new ArrayList<>(contestantNames);
        GameSession session;
        try {
            session = controller.play(names);
        } catch (DataAccessException e) {
            UiTheme.showDatabaseError(this, e);
            navigator.showMenu();
            return;
        }
        playback = new GamePlayback(session);
        saved = false;

        transcript.setNames(playback.getContestantNames());
        transcript.clear();
        sidebar.setCellRenderer(new ContestantListRenderer(playback));
        sidebarModel.clear();
        for (String name : playback.getContestantNames()) {
            sidebarModel.addElement(name);
        }
        statusLabel.setText("Press Next to follow the show.");
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
        sidebar.setBackground(UiTheme.PANEL);
        sidebar.setFocusable(false);

        JPanel side = new JPanel(new BorderLayout(0, 6));
        side.setOpaque(false);
        side.setPreferredSize(new Dimension(250, 0));
        side.add(UiTheme.label("Contestants"), BorderLayout.NORTH);
        side.add(sidebar, BorderLayout.CENTER);
        return side;
    }

    private JPanel buildControls() {
        JPanel controls = new JPanel(new BorderLayout());
        controls.setOpaque(false);

        JButton menuButton = new StyledButton("Menu", false);
        menuButton.addActionListener(e -> backToMenu());

        nextButton.setSilent(true);
        nextButton.addActionListener(e -> revealNext());
        autoButton.addActionListener(e -> toggleAutoPlay());
        skipButton.addActionListener(e -> skipToEnd());
        againButton.addActionListener(e -> startGame(names));

        speedSlider.setOpaque(false);
        speedSlider.setForeground(UiTheme.MUTED);
        speedSlider.setMajorTickSpacing(1);
        speedSlider.setPaintTicks(true);
        speedSlider.setSnapToTicks(true);
        speedSlider.setPreferredSize(new Dimension(150, 36));
        speedSlider.setToolTipText("Auto-play speed: 1 is slowest, " + DELAY_MS.length + " is fastest");
        speedSlider.addChangeListener(e -> autoTimer.setDelay(delayForSpeed()));

        JPanel centre = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        centre.setOpaque(false);
        centre.add(nextButton);
        centre.add(autoButton);
        centre.add(UiTheme.label("Slow"));
        centre.add(speedSlider);
        centre.add(UiTheme.label("Fast"));
        centre.add(skipButton);
        centre.add(againButton);

        controls.add(menuButton, BorderLayout.WEST);
        controls.add(centre, BorderLayout.CENTER);
        return controls;
    }

    private int delayForSpeed() {
        return DELAY_MS[speedSlider.getValue() - 1];
    }

    private void revealNext() {
        if (!playback.isFinished()) {
            show(playback.revealNext());
        }
    }

    private void skipToEnd() {
        autoTimer.stop();
        show(playback.revealAll());
    }

    private void show(List<NarratedLine> lines) {
        for (NarratedLine line : lines) {
            transcript.append(line);
        }
        if (playback.isFinished()) {
            autoTimer.stop();
            saveOnce();
        }
        updateButtons();
        sidebar.repaint();
    }

    private void saveOnce() {
        if (saved) {
            return;
        }
        try {
            controller.saveGame(playback.getSession());
            saved = true;
            statusLabel.setText("Game over - saved to your history.");
        } catch (DataAccessException e) {
            statusLabel.setText("Game over - could not save it.");
            UiTheme.showDatabaseError(this, e);
        }
    }

    private void toggleAutoPlay() {
        if (autoTimer.isRunning()) {
            autoTimer.stop();
        } else {
            autoTimer.start();
        }
        updateButtons();
    }

    private void updateButtons() {
        boolean finished = playback.isFinished();
        nextButton.setEnabled(!finished);
        autoButton.setEnabled(!finished);
        skipButton.setEnabled(!finished);
        againButton.setVisible(finished);
        autoButton.setText(autoTimer.isRunning() ? "Pause" : "Auto-play");
    }

    private void backToMenu() {
        autoTimer.stop();
        if (playback != null && !playback.isFinished()) {
            int answer = JOptionPane.showConfirmDialog(this,
                    "Leave this game? It won't be saved because it isn't finished.",
                    "Leave game", JOptionPane.YES_NO_OPTION);
            if (answer != JOptionPane.YES_OPTION) {
                updateButtons();
                return;
            }
        }
        navigator.showMenu();
    }
}
