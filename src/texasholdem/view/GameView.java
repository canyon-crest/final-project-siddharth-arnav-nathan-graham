package texasholdem.view;

import texasholdem.controller.GameController;
import texasholdem.model.Player;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

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
    
    /** Panel for players on top */
    private JPanel topPlayersPanel;
    
    /** Panel for players on left */
    private JPanel leftPlayersPanel;
    
    /** Panel for players on right */
    private JPanel rightPlayersPanel;
    
    /** Panel for the human player */
    private JPanel bottomPlayerPanel;
    
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
        
        // Create players panel
        setupPlayersLayout();
        
        // Add main components to the frame
        add(tableView, BorderLayout.CENTER);
        add(actionPanel, BorderLayout.SOUTH);
        
        // Create game controller
        controller = new GameController(tableView, actionPanel, playerViews);
        
        // Set default size and make visible
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    /**
     * Sets up the player layout around the table.
     */
    private void setupPlayersLayout() {
        // Create player panels
        topPlayersPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        topPlayersPanel.setOpaque(false);
        
        leftPlayersPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        leftPlayersPanel.setOpaque(false);
        
        rightPlayersPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        rightPlayersPanel.setOpaque(false);
        
        bottomPlayerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPlayerPanel.setOpaque(false);
        
        // Create player views
        playerViews = new ArrayList<>();
        
        // Human player (bottom)
        Player humanPlayer = new Player("Player", 1000);
        PlayerView humanPlayerView = new PlayerView(humanPlayer);
        playerViews.add(humanPlayerView);
        bottomPlayerPanel.add(humanPlayerView);
        
        // AI players (top, left, right)
        for (int i = 1; i <= 5; i++) {
            Player aiPlayer = new Player("Computer " + i, 1000);
            PlayerView aiPlayerView = new PlayerView(aiPlayer);
            playerViews.add(aiPlayerView);
            
            if (i <= 2) {
                topPlayersPanel.add(aiPlayerView);
            } else if (i <= 4) {
                leftPlayersPanel.add(aiPlayerView);
            } else {
                rightPlayersPanel.add(aiPlayerView);
            }
        }
        
        // Add player panels to the main layout
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(topPlayersPanel, BorderLayout.NORTH);
        centerPanel.add(leftPlayersPanel, BorderLayout.WEST);
        centerPanel.add(tableView, BorderLayout.CENTER);
        centerPanel.add(rightPlayersPanel, BorderLayout.EAST);
        centerPanel.add(bottomPlayerPanel, BorderLayout.SOUTH);
        
        add(centerPanel, BorderLayout.CENTER);
    }
    
    /**
     * Entry point for the application.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new GameView();
        });
    }
} 