package miyagimayhem;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import miyagimayhem.controller.GameController;
import miyagimayhem.db.DataAccessException;
import miyagimayhem.gui.MainFrame;

/**
 * Starts Miyagi Mayhem: opens the database (creating it on the first run)
 * and shows the main window.
 */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::launch);
    }

    private static void launch() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            GameController controller = GameController.createDefault();
            new MainFrame(controller).setVisible(true);
        } catch (DataAccessException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Miyagi Mayhem can't start",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Unexpected problem: " + e, "Miyagi Mayhem can't start",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }
}
