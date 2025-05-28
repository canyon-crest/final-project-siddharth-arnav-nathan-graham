package texasholdem.view;

import texasholdem.controller.GameController;
import texasholdem.model.Player;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingConstants;

/**
 * The main game view that ties all GUI components together.
 */
public class GameView extends JFrame {
    /** The table view */
    private TableView tableView;
    
    /** The action panel */
    private ActionPanel actionPanel;
    
    /** The player views */
    private List<PlayerView> playerViews;
    
    /** The game controller */
    private GameController controller;
    
    /** Panel for player 1 */
    private JPanel player1Panel;
    
    /** Panel for player 2 */
    private JPanel player2Panel;
    
    /** Status label */
    private JLabel statusLabel;
    
    /**
     * Constructs a new game view.
     */
    public GameView() {
        super("Texas Holdem Poker");
        setupUI();
    }
    
    /**
     * Sets up the user interface.
     */
    private void setupUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.DARK_GRAY);
        
        // Create main components
        tableView = new TableView();
        actionPanel = new ActionPanel();
        playerViews = new ArrayList<>();
        
        // Create players panel and get the center panel
        JPanel centerPanel = setupPlayersLayout();
        
        // Add main components to the frame
        add(centerPanel, BorderLayout.CENTER);
        add(actionPanel, BorderLayout.SOUTH);
        
        // Add status label at the bottom
        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setFont(new Font("SansSerif", Font.ITALIC, 14));
        add(statusLabel, BorderLayout.NORTH);
        
        // Create game controller
        controller = new GameController(tableView, actionPanel, playerViews, this);
        
        // Set default size and make visible
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    /**
     * Sets up the player layout around the table.
     */
    private JPanel setupPlayersLayout() {
        // Create player panels
        player1Panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        player1Panel.setOpaque(false);
        
        player2Panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        player2Panel.setOpaque(false);
        
        // Create player views
        playerViews = new ArrayList<>();
        
        // Player 1 (top)
        Player player1 = new Player("Player 1", 1000);
        PlayerView player1View = new PlayerView(player1);
        playerViews.add(player1View);
        player1Panel.add(player1View);
        
        // Player 2 (bottom)
        Player player2 = new Player("Player 2", 1000);
        PlayerView player2View = new PlayerView(player2);
        playerViews.add(player2View);
        player2Panel.add(player2View);
        
        // Add player panels to the main layout
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(player1Panel, BorderLayout.NORTH);
        centerPanel.add(tableView, BorderLayout.CENTER);
        centerPanel.add(player2Panel, BorderLayout.SOUTH);
        
        return centerPanel;
    }
    
    /**
     * Entry point for the application.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new GameView();
        });
    }

    /**
     * Resets the player views with new player instances.
     * @param players the list of players
     */
    public void resetPlayerViews(List<Player> players) {
        // Remove all player views from panels
        player1Panel.removeAll();
        player2Panel.removeAll();
        playerViews.clear();

        // Player 1 (top)
        PlayerView player1View = new PlayerView(players.get(0));
        playerViews.add(player1View);
        player1Panel.add(player1View);

        // Player 2 (bottom)
        PlayerView player2View = new PlayerView(players.get(1));
        playerViews.add(player2View);
        player2Panel.add(player2View);

        // Refresh the layout
        player1Panel.revalidate();
        player2Panel.revalidate();
        player1Panel.repaint();
        player2Panel.repaint();
    }

    /**
     * Updates the status message.
     * @param msg the new status message
     */
    public void setStatusMessage(String msg) {
        statusLabel.setText(msg);
    }
} 