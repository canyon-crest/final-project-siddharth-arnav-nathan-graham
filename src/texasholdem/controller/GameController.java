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
        Player humanPlayer = new Player("Player 1", DEFAULT_STARTING_CHIPS);
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
        // Update player views
        for (int i = 0; i < playerViews.size() && i < gamePlayers.size(); i++) {
            Player player = gamePlayers.get(i);
            PlayerView playerView = playerViews.get(i);
            // Show cards for human, hide for AI unless showdown
            boolean showCards = (i == 0) || game.getCurrentRound() == Game.BettingRound.SHOWDOWN;
            playerView.setCards(player.getHoleCards(), showCards);
            playerView.setFolded(player.hasFolded());
            playerView.setCurrentPlayer(player == game.getCurrentPlayer());
            playerView.setDealer(player.isDealer());
            playerView.updateView();
        }
        // Start the game
        game.startNewRound();
        updateGameView();
        // If AI's turn, make AI move
        if (game.getCurrentPlayer() instanceof ComputerPlayer) {
            handleAiTurn();
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
            
            // Show cards for human, hide for AI unless showdown
            boolean showCards = (i == 0) || game.getCurrentRound() == Game.BettingRound.SHOWDOWN;
            playerView.setCards(player.getHoleCards(), showCards);
            playerView.setFolded(player.hasFolded());
            playerView.setCurrentPlayer(player == currentPlayer);
            playerView.setDealer(player.isDealer());
            playerView.updateView();
        }
        
        // Update action panel
        int maxBet = game.getMaxBet();
        Player currentHumanPlayer = currentPlayer;
        int callAmount = maxBet - currentHumanPlayer.getCurrentBet();
        
        boolean canCheck = callAmount == 0;
        boolean hasBet = maxBet > 0;
        
        actionPanel.setCallButtonText(callAmount);
        actionPanel.setBetButtonText(hasBet);
        actionPanel.setCheckEnabled(canCheck);
        actionPanel.setCallEnabled(!canCheck);
        
        // Set bet slider limits
        int minBetAmount = Math.max(DEFAULT_MIN_BET, maxBet * 2);
        int maxBetAmount = currentHumanPlayer.getChips();
        actionPanel.setBetLimits(minBetAmount, maxBetAmount);
        
        // Enable actions if it's the current player's turn and they haven't folded
        boolean isCurrentPlayerTurn = !currentHumanPlayer.hasFolded();
        actionPanel.setActionsEnabled(isCurrentPlayerTurn);
        
        // Check for end of round
        if (game.getCurrentRound() == Game.BettingRound.SHOWDOWN) {
            handleShowdown();
        }
        
        // If it's AI's turn, show 'AI is thinking...' and delay before AI acts
        if (game.getCurrentPlayer() instanceof ComputerPlayer && game.getCurrentRound() != Game.BettingRound.SHOWDOWN) {
            if (gameView != null) gameView.setStatusMessage("AI is thinking...");
            javax.swing.Timer timer = new javax.swing.Timer(900, e -> handleAiTurn());
            timer.setRepeats(false);
            timer.start();
            return;
        } else {
            if (gameView != null) gameView.setStatusMessage(""); // Clear status for human
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
    }
    
    /**
     * Handles a check action.
     */
    private void handleCheck() {
        game.check();
        updateGameView();
    }
    
    /**
     * Handles a call action.
     */
    private void handleCall() {
        game.call();
        updateGameView();
    }
    
    /**
     * Handles a bet action.
     */
    private void handleBet() {
        // Only perform bet/raise if ActionPanel is awaiting bet amount (i.e., after Confirm is pressed)
        if (!actionPanel.isAwaitingBetAmount()) {
            // Do nothing; ActionPanel will show the text field
            return;
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
            JOptionPane.showMessageDialog(gameView, "Please enter a valid bet amount between the minimum and maximum.", "Invalid Bet", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Handles the showdown phase.
     */
    private void handleShowdown() {
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

    private void handleAiTurn() {
        ComputerPlayer ai = (ComputerPlayer) game.getCurrentPlayer();
        int maxBet = game.getMaxBet();
        int minBet = DEFAULT_MIN_BET;
        int potSize = game.getPot();
        String action = ai.decideAction(game, game.getCommunityCards(), maxBet, minBet, potSize);
        String message = "";
        switch (action) {
            case "fold":
                game.fold();
                message = ai.getName() + " folds.";
                break;
            case "check":
                game.check();
                message = ai.getName() + " checks.";
                break;
            case "call":
                game.call();
                message = ai.getName() + " calls.";
                break;
            case "bet":
                int betAmount = ai.decideBetAmount(game, 0.5, potSize, minBet, ai.getChips());
                game.bet(betAmount);
                message = ai.getName() + " bets $" + betAmount + ".";
                break;
            case "raise":
                int raiseAmount = ai.decideBetAmount(game, 0.7, potSize, minBet, ai.getChips());
                game.raise(raiseAmount);
                message = ai.getName() + " raises $" + raiseAmount + ".";
                break;
        }
        if (gameView != null) gameView.setStatusMessage(message);
        updateGameView();
        // If still AI's turn, repeat (with delay)
        if (game.getCurrentPlayer() instanceof ComputerPlayer && game.getCurrentRound() != Game.BettingRound.SHOWDOWN) {
            javax.swing.Timer timer = new javax.swing.Timer(900, e -> handleAiTurn());
            timer.setRepeats(false);
            timer.start();
        }
    }
} 