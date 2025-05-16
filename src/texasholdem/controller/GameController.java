package texasholdem.controller;

import texasholdem.model.ComputerPlayer;
import texasholdem.model.Game;
import texasholdem.model.Player;
import texasholdem.view.ActionPanel;
import texasholdem.view.PlayerView;
import texasholdem.view.TableView;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for the Texas Holdem game.
 * Handles game logic and connects the model and view.
 */
public class GameController {
    /** The game model */
    private Game game;
    
    /** The table view */
    private TableView tableView;
    
    /** The action panel */
    private ActionPanel actionPanel;
    
    /** The player views */
    private List<PlayerView> playerViews;
    
    /** Timer for AI player actions */
    private Timer aiTimer;
    
    /** Default starting chips */
    private static final int DEFAULT_STARTING_CHIPS = 1000;
    
    /** Default minimum bet */
    private static final int DEFAULT_MIN_BET = 10;
    
    /** Delay before AI actions (milliseconds) */
    private static final int AI_ACTION_DELAY = 1000;
    
    /**
     * Constructs a new game controller.
     * @param tableView the table view
     * @param actionPanel the action panel
     * @param playerViews the player views
     */
    public GameController(TableView tableView, ActionPanel actionPanel, List<PlayerView> playerViews) {
        this.tableView = tableView;
        this.actionPanel = actionPanel;
        this.playerViews = playerViews;
        
        // Set up AI timer
        aiTimer = new Timer(AI_ACTION_DELAY, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleAiAction();
            }
        });
        aiTimer.setRepeats(false);
        
        // Add action listeners
        setupActionListeners();
        
        // Create initial game
        createNewGame();
    }
    
    /**
     * Sets up the action listeners for the action panel.
     */
    private void setupActionListeners() {
        actionPanel.addFoldListener(e -> handleFold());
        actionPanel.addCheckListener(e -> handleCheck());
        actionPanel.addCallListener(e -> handleCall());
        actionPanel.addBetListener(e -> handleBet());
        actionPanel.addNewGameListener(e -> createNewGame());
    }
    
    /**
     * Creates a new game with the default settings.
     */
    public void createNewGame() {
        // Stop any pending AI actions
        aiTimer.stop();
        
        // Create players - first player is human, others are AI
        List<String> playerNames = new ArrayList<>();
        for (PlayerView playerView : playerViews) {
            Player player = playerView.getPlayer();
            if (player != null) {
                playerNames.add(player.getName());
            }
        }
        
        // If no existing players, create default names
        if (playerNames.isEmpty()) {
            playerNames.add("Player");
            playerNames.add("Computer 1");
            playerNames.add("Computer 2");
            playerNames.add("Computer 3");
        }
        
        // Create new game
        String[] nameArray = playerNames.toArray(new String[0]);
        game = new Game(nameArray, DEFAULT_STARTING_CHIPS, DEFAULT_MIN_BET);
        
        // Replace computer players with ComputerPlayer instances
        List<Player> players = game.getPlayers();
        for (int i = 1; i < players.size(); i++) {
            Player oldPlayer = players.get(i);
            // Create computer player with random personality
            int aggressiveness = 40 + (int)(Math.random() * 40); // 40-80
            int tightness = 30 + (int)(Math.random() * 40); // 30-70
            ComputerPlayer computerPlayer = new ComputerPlayer(
                oldPlayer.getName(), 
                oldPlayer.getChips(),
                aggressiveness,
                tightness
            );
            // Copy state from old player
            computerPlayer.setCurrentBet(oldPlayer.getCurrentBet());
            computerPlayer.setFolded(oldPlayer.hasFolded());
            computerPlayer.setDealer(oldPlayer.isDealer());
            players.set(i, computerPlayer);
        }
        
        // Update player views
        for (int i = 0; i < playerViews.size() && i < players.size(); i++) {
            playerViews.get(i).setPlayer(players.get(i));
            playerViews.get(i).setCards(players.get(i).getHoleCards(), i == 0);
            playerViews.get(i).setFolded(false);
            playerViews.get(i).setCurrentPlayer(false);
            playerViews.get(i).setDealer(players.get(i).isDealer());
            playerViews.get(i).updateView();
        }
        
        // Start the game
        game.startNewRound();
        updateGameView();
        
        // If the current player is AI, trigger their action
        if (isCurrentPlayerAi()) {
            aiTimer.start();
        }
    }
    
    /**
     * Updates the game view to match the current game state.
     */
    public void updateGameView() {
        // Update table view
        tableView.setCommunityCards(game.getCommunityCards());
        tableView.setPot(game.getPot());
        
        // Update round display
        String roundName = getRoundName(game.getCurrentRound());
        tableView.setRoundName(roundName);
        
        // Update player views
        List<Player> players = game.getPlayers();
        Player currentPlayer = game.getCurrentPlayer();
        
        for (int i = 0; i < playerViews.size() && i < players.size(); i++) {
            Player player = players.get(i);
            PlayerView playerView = playerViews.get(i);
            
            // Update player state
            playerView.setCards(player.getHoleCards(), i == 0 || game.getCurrentRound() == Game.BettingRound.SHOWDOWN);
            playerView.setFolded(player.hasFolded());
            playerView.setCurrentPlayer(player == currentPlayer);
            playerView.setDealer(player.isDealer());
            playerView.updateView();
        }
        
        // Update action panel
        int maxBet = game.getMaxBet();
        Player humanPlayer = players.get(0);
        int callAmount = maxBet - humanPlayer.getCurrentBet();
        
        boolean canCheck = callAmount == 0;
        boolean hasBet = maxBet > 0;
        
        actionPanel.setCallButtonText(callAmount);
        actionPanel.setBetButtonText(hasBet);
        actionPanel.setCheckEnabled(canCheck);
        actionPanel.setCallEnabled(!canCheck);
        
        // Set bet slider limits
        int minBetAmount = Math.max(DEFAULT_MIN_BET, maxBet * 2);
        int maxBetAmount = humanPlayer.getChips();
        actionPanel.setBetLimits(minBetAmount, maxBetAmount);
        
        // Enable actions if it's the human player's turn
        boolean isHumanTurn = currentPlayer == humanPlayer && !humanPlayer.hasFolded();
        actionPanel.setActionsEnabled(isHumanTurn);
        
        // Check for end of round
        if (game.getCurrentRound() == Game.BettingRound.SHOWDOWN) {
            handleShowdown();
        }
    }
    
    /**
     * Gets the display name for a betting round.
     */
    private String getRoundName(Game.BettingRound round) {
        switch (round) {
            case PREFLOP: return "Pre-Flop";
            case FLOP: return "Flop";
            case TURN: return "Turn";
            case RIVER: return "River";
            case SHOWDOWN: return "Showdown";
            default: return "Unknown";
        }
    }
    
    /**
     * Handles a fold action.
     */
    private void handleFold() {
        game.fold();
        updateGameView();
        
        // If the next player is AI, trigger their action
        if (isCurrentPlayerAi()) {
            aiTimer.start();
        }
    }
    
    /**
     * Handles a check action.
     */
    private void handleCheck() {
        game.check();
        updateGameView();
        
        // If the next player is AI, trigger their action
        if (isCurrentPlayerAi()) {
            aiTimer.start();
        }
    }
    
    /**
     * Handles a call action.
     */
    private void handleCall() {
        game.call();
        updateGameView();
        
        // If the next player is AI, trigger their action
        if (isCurrentPlayerAi()) {
            aiTimer.start();
        }
    }
    
    /**
     * Handles a bet/raise action.
     */
    private void handleBet() {
        int betAmount = actionPanel.getBetAmount();
        int maxBet = game.getMaxBet();
        
        if (maxBet > 0) {
            // Raise
            game.raise(betAmount);
        } else {
            // Bet
            game.bet(betAmount);
        }
        
        updateGameView();
        
        // If the next player is AI, trigger their action
        if (isCurrentPlayerAi()) {
            aiTimer.start();
        }
    }
    
    /**
     * Handles an AI player's action.
     */
    private void handleAiAction() {
        Player currentPlayer = game.getCurrentPlayer();
        
        if (currentPlayer instanceof ComputerPlayer) {
            ComputerPlayer ai = (ComputerPlayer) currentPlayer;
            
            // Get game state
            int maxBet = game.getMaxBet();
            int potSize = game.getPot();
            int callAmount = maxBet - ai.getCurrentBet();
            
            // Decide action
            String action = ai.decideAction(
                game, 
                game.getCommunityCards(), 
                maxBet, 
                DEFAULT_MIN_BET, 
                potSize
            );
            
            // Execute action
            switch (action) {
                case "fold":
                    game.fold();
                    break;
                case "check":
                    game.check();
                    break;
                case "call":
                    game.call();
                    break;
                case "bet":
                    int betAmount = ai.decideBetAmount(
                        game, 
                        0.5, // Medium hand strength 
                        potSize, 
                        DEFAULT_MIN_BET, 
                        maxBet
                    );
                    game.bet(betAmount);
                    break;
                case "raise":
                    int raiseAmount = ai.decideBetAmount(
                        game, 
                        0.7, // Stronger hand strength 
                        potSize, 
                        DEFAULT_MIN_BET, 
                        maxBet
                    );
                    game.raise(raiseAmount);
                    break;
            }
            
            // Update view
            updateGameView();
            
            // If still AI turn, schedule next action
            if (isCurrentPlayerAi()) {
                aiTimer.start();
            }
        }
    }
    
    /**
     * Handles the showdown (end of round).
     */
    private void handleShowdown() {
        // Disable actions during showdown
        actionPanel.setActionsEnabled(false);
        
        // Schedule game reset after a delay
        Timer showdownTimer = new Timer(3000, e -> {
            game.startNewRound();
            updateGameView();
            
            // If the next player is AI, trigger their action
            if (isCurrentPlayerAi()) {
                aiTimer.start();
            }
        });
        showdownTimer.setRepeats(false);
        showdownTimer.start();
    }
    
    /**
     * Checks if the current player is an AI player.
     * @return true if the current player is AI, false otherwise
     */
    private boolean isCurrentPlayerAi() {
        Player currentPlayer = game.getCurrentPlayer();
        return currentPlayer instanceof ComputerPlayer;
    }
} 