package miyagimayhem.gui;

import java.util.List;

/**
 * What a screen can ask the main window to do. Screens depend on this
 * interface rather than on MainFrame itself.
 */
interface Navigator {

    void showMenu();

    void showSetup();

    void showGame(List<String> contestantNames);

    void showHistory();

    void showFlavor();
}
