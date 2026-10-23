package miyagimayhem.gui;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import miyagimayhem.audio.AudioManager;
import miyagimayhem.controller.GameController;

/**
 * The application window. It owns one panel per screen and switches
 * between them with a CardLayout.
 */
public class MainFrame extends JFrame implements Navigator {

    private static final String MENU = "menu";
    private static final String SETUP = "setup";
    private static final String GAME = "game";
    private static final String HISTORY = "history";
    private static final String FLAVOR = "flavor";

    private final GameController controller;
    private final AudioManager audio;
    private final CardLayout cards = new CardLayout();
    private final JPanel deck = new JPanel(cards);

    private final SetupPanel setupPanel;
    private final GamePanel gamePanel;
    private final HistoryPanel historyPanel;
    private final FlavorPanel flavorPanel;

    /** Sets the look and feel and fonts. Call before creating the window. */
    public static void installLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (ReflectiveOperationException | UnsupportedLookAndFeelException e) {
            // The default look and feel still works, just without the Metal styling.
        }
        UiTheme.installDefaultFonts();
    }

    public MainFrame(GameController controller, AudioManager audio) {
        super("Miyagi Mayhem");
        this.controller = controller;
        this.audio = audio;

        setupPanel = new SetupPanel(controller, this);
        gamePanel = new GamePanel(controller, this);
        historyPanel = new HistoryPanel(controller, this);
        flavorPanel = new FlavorPanel(controller, this);

        deck.add(new MenuPanel(this, audio, this::quit), MENU);
        deck.add(setupPanel, SETUP);
        deck.add(gamePanel, GAME);
        deck.add(historyPanel, HISTORY);
        deck.add(flavorPanel, FLAVOR);
        add(deck);
        ButtonSounds.install(deck, audio);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                quit();
            }
        });
        setMinimumSize(new Dimension(900, 640));
        setSize(1000, 700);
        setLocationRelativeTo(null);
        audio.startMusic();
    }

    @Override
    public void showMenu() {
        cards.show(deck, MENU);
        audio.startMusic();
    }

    @Override
    public void showSetup() {
        audio.stopMusic();
        setupPanel.refresh();
        cards.show(deck, SETUP);
    }

    @Override
    public void showGame(List<String> contestantNames) {
        audio.stopMusic();
        cards.show(deck, GAME);
        gamePanel.startGame(contestantNames);
    }

    @Override
    public void showHistory() {
        audio.stopMusic();
        historyPanel.refresh();
        cards.show(deck, HISTORY);
    }

    @Override
    public void showFlavor() {
        audio.stopMusic();
        flavorPanel.refresh();
        cards.show(deck, FLAVOR);
    }

    private void quit() {
        audio.close();
        controller.shutdown();
        dispose();
        System.exit(0);
    }
}
