import texasholdem.view.GameView;

import javax.swing.SwingUtilities;

/**
 * Launcher class for the Texas Holdem game.
 */
public class TexasHoldemLauncher {
    /**
     * Main method to launch the application.
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new GameView();
        });
    }
} 