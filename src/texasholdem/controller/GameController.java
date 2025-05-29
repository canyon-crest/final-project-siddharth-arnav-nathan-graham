package texasholdem.controller;

import texasholdem.model.Game;
import texasholdem.model.Player;
import texasholdem.model.ComputerPlayer;
import texasholdem.view.ActionPanel;
import texasholdem.view.PlayerView;
import texasholdem.view.TableView;
import texasholdem.view.GameView;
import texasholdem.model.HandEvaluator;

import javax.swing.JOptionPane;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;

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
    
    /** Default starting chips */
    private static final int DEFAULT_STARTING_CHIPS = 1000;
    
    /** Default minimum bet */
    private static final int DEFAULT_MIN_BET = 10;
    
    /** Game view */
    private GameView gameView;

    
    
    /**
     * Constructs a new game controller.
     * @param tableView the table view
     * @param actionPanel the action panel
     * @param playerViews the player views
     * @param gameView the game view
     */
    public GameController(TableView tableView, ActionPanel actionPanel, List<PlayerView> playerViews, GameView gameView) {
        this.tableView = tableView;
        this.actionPanel = actionPanel;
        this.playerViews = playerViews;
        this.gameView = gameView;
        
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
        // Create players - first is human, second is AI
        List<Player> players = new ArrayList<>();
        Player humanPlayer = new Player("You", DEFAULT_STARTING_CHIPS);
        ComputerPlayer aiPlayer = new ComputerPlayer("AI Bot", DEFAULT_STARTING_CHIPS, 60, 50);
        players.add(humanPlayer);
        players.add(aiPlayer);
        game = new Game(new String[] { humanPlayer.getName(), aiPlayer.getName() }, DEFAULT_STARTING_CHIPS, DEFAULT_MIN_BET);
        
        // Replace the second player in the game with the ComputerPlayer instance
        List<Player> gamePlayers = game.getPlayers();
        gamePlayers.set(1, aiPlayer);
        
        // Reset player views to use the correct Player instances
        if (gameView != null) {
            gameView.resetPlayerViews(gamePlayers);
        }
        
        // Start the game
        game.startNewRound();
        
        // Update the view and trigger AI turn if needed
        SwingUtilities.invokeLater(() -> {
            updateGameView();
            
            // If it's AI's turn after dealing, trigger its move
            Player currentPlayer = game.getCurrentPlayer();
            if (currentPlayer instanceof ComputerPlayer) {
                // Delay AI move slightly after UI has finished initializing
                javax.swing.Timer startAiTimer = new javax.swing.Timer(500, evt -> triggerAiTurn());
                startAiTimer.setRepeats(false);
                startAiTimer.start();
            }
        });
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
        Player humanPlayer = players.get(0);  // First player is always human
        
        for (int i = 0; i < playerViews.size() && i < players.size(); i++) {
            Player player = players.get(i);
            PlayerView playerView = playerViews.get(i);
            
            // Ensure PlayerView references the correct Player instance
            playerView.setPlayer(player);
            
            // Show cards for human, hide for AI unless showdown
            boolean showCards = (i == 0) || game.getCurrentRound() == Game.BettingRound.SHOWDOWN;
            playerView.setCards(player.getHoleCards(), showCards);
            playerView.setFolded(player.hasFolded());
            playerView.setCurrentPlayer(player == currentPlayer);
            if (i==0) {
                playerView.setDealer(true);
            } else {
                playerView.setDealer(false);
            }
            
            // Update chips display
            playerView.updateView();
        }
        
        // Update action panel
        int maxBet = game.getMaxBet();
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
        
        // Only enable actions if it's the human player's turn
        boolean isHumanTurn = currentPlayer == humanPlayer && !humanPlayer.hasFolded();
        actionPanel.setActionsEnabled(isHumanTurn);
        
        // Check for end of round
        if (game.getCurrentRound() == Game.BettingRound.SHOWDOWN) {
            handleShowdown();
            return;
        }
        
        // If it's AI's turn, trigger AI action after a short delay
        if (currentPlayer instanceof ComputerPlayer && game.getCurrentRound() != Game.BettingRound.SHOWDOWN) {
            triggerAiTurn();
        } else if (isHumanTurn) {
            if (gameView != null) {
                gameView.setStatusMessage("Your turn");
            }
        }
    }
    
    /**
     * Gets the display name for a betting round.
     */
    public String getRoundName(Game.BettingRound round) {
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
    public void handleFold() {
        if (!(game.getCurrentPlayer() instanceof ComputerPlayer)) {
            game.fold();
            updateGameView();
        }
    }
    
    /**
     * Handles a check action.
     */
    public void handleCheck() {
        if (!(game.getCurrentPlayer() instanceof ComputerPlayer)) {
            game.check();
            updateGameView();
        }
    }
    
    /**
     * Handles a call action.
     */
    public void handleCall() {
        if (!(game.getCurrentPlayer() instanceof ComputerPlayer)) {
            game.call();
            updateGameView();
        }
    }
    
    /**
     * Handles a bet action.
     */
    public void handleBet() {
        if (game.getCurrentPlayer() instanceof ComputerPlayer) {
            return;  // Don't handle bets for AI
        }
        
        if (!actionPanel.isAwaitingBetAmount()) {
            return;  // Do nothing; ActionPanel will show the text field
        }
        
        int amount = actionPanel.getBetAmount();
        if (amount > 0) {
            if (game.getMaxBet() == 0) {
                game.bet(amount);
            } else {
                game.raise(amount);
            }
            updateGameView();
        } else {
            JOptionPane.showMessageDialog(gameView, 
                "Please enter a valid bet amount between the minimum and maximum.", 
                "Invalid Bet", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Handles the showdown phase.
     */
    public void handleShowdown() {
        // Capture the pot amount BEFORE the winner is awarded the pot and the pot is reset
        int potAmount = game.getLastPotWon();

        // Show all cards
        for (int i = 0; i < playerViews.size() && i < game.getPlayers().size(); i++) {
            Player player = game.getPlayers().get(i);
            PlayerView playerView = playerViews.get(i);
            playerView.setCards(player.getHoleCards(), true);
            playerView.updateView();
        }

        // Evaluate hands and determine winner
        java.util.List<Player> players = game.getPlayers();
        Player player1 = players.get(0);
        Player player2 = players.get(1);
        HandEvaluator.HandResult hand1 = HandEvaluator.evaluateHand(player1.getHoleCards(), game.getCommunityCards());
        HandEvaluator.HandResult hand2 = HandEvaluator.evaluateHand(player2.getHoleCards(), game.getCommunityCards());
        int comparison = HandEvaluator.compareHands(hand1, hand2);
        String winnerName;
        if (player1.hasFolded()) {
            winnerName = player2.getName();
        } else if (player2.hasFolded()) {
            winnerName = player1.getName();
        } else if (comparison > 0) {
            winnerName = player1.getName();
        } else if (comparison < 0) {
            winnerName = player2.getName();
        } else {
            winnerName = "It's a tie!";
        }

        // Build showdown message
        StringBuilder message = new StringBuilder();
        message.append("Showdown!\n\n");
        message.append(player1.getName()).append("'s hand: ").append(hand1.getRank()).append("\n");
        message.append(player2.getName()).append("'s hand: ").append(hand2.getRank()).append("\n\n");
        if (!winnerName.equals("It's a tie!")) {
            message.append("Winner: ").append(winnerName).append("\n");
            message.append("Pot won: $").append(potAmount).append("\n");
        } else {
            message.append("It's a tie! Pot is split.\n");
        }

        JOptionPane.showMessageDialog(gameView, message.toString(), "Showdown", JOptionPane.INFORMATION_MESSAGE);

        // Start new round
        game.startNewRound();
        updateGameView();
    }

    public void triggerAiTurn() {
        if (gameView != null) {
            gameView.setStatusMessage("AI is thinking...");
        }
        
        // Disable action panel during AI turn
        actionPanel.setActionsEnabled(false);
        
        // Schedule AI action with a short delay
        javax.swing.Timer timer = new javax.swing.Timer(1300, e -> {
            // Execute AI's action
            handleAiTurn();
        });
        timer.setRepeats(false);
        timer.start();
    }

    public void handleAiTurn() {
        Player currentPlayer = game.getCurrentPlayer();
        if (!(currentPlayer instanceof ComputerPlayer)) {
            return;  // Not AI's turn
        }
        
        ComputerPlayer ai = (ComputerPlayer) currentPlayer;
        int maxBet = game.getMaxBet();
        int potSize = game.getPot();
        
        // Get AI's decision
        String action = ai.decideAction(
            game,
            game.getCommunityCards(),
            maxBet,
            DEFAULT_MIN_BET,
            potSize
        );
        
        String message = "";
        boolean actionTaken = false;
        
        // Execute AI's action
        switch (action) {
            case "fold":
                actionTaken = game.fold();
                message = ai.getName() + " folds";
                break;
                
            case "check":
                actionTaken = game.check();
                message = ai.getName() + " checks";
                break;
                
            case "call":
                actionTaken = game.call();
                message = ai.getName() + " calls";
                break;
                
            case "bet":
                if (maxBet == 0) {
                    int betAmount = ai.decideBetAmount(game, 0.5, potSize, DEFAULT_MIN_BET, ai.getChips());
                    actionTaken = game.bet(betAmount);
                    message = ai.getName() + " bets $" + betAmount;
                } else {
                    actionTaken = game.call();
                    message = ai.getName() + " calls";
                }
                break;
                
            case "raise":
                if (maxBet > 0) {
                    int raiseAmount = ai.decideBetAmount(game, 0.7, potSize, DEFAULT_MIN_BET, ai.getChips());
                    actionTaken = game.raise(raiseAmount);
                    message = ai.getName() + " raises to $" + raiseAmount;
                } else {
                    int betAmount = ai.decideBetAmount(game, 0.6, potSize, DEFAULT_MIN_BET, ai.getChips());
                    actionTaken = game.bet(betAmount);
                    message = ai.getName() + " bets $" + betAmount;
                }
                break;
        }
        
        // If action failed, try fallback actions
        if (!actionTaken) {
            if (maxBet == 0) {
                actionTaken = game.check();
                message = ai.getName() + " checks";
            } else {
                actionTaken = game.fold();
                message = ai.getName() + " folds";
            }
        }
        
        if (gameView != null) {
            gameView.setStatusMessage(message);
        }
        
        // Update the view after AI's action
        SwingUtilities.invokeLater(this::updateGameView);
    }
} 