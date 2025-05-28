package texasholdem.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the game state and rules for a Texas Holdem poker game.
 */
public class Game {
    /** The deck of cards */
    private Deck deck;
    
    /** The list of players */
    private List<Player> players;
    
    /** The index of the current player */
    private int currentPlayerIndex;
    
    /** The index of the dealer */
    private int dealerIndex;
    
    /** The community cards on the table */
    private List<Card> communityCards;
    
    /** The current betting round */
    private BettingRound currentRound;
    
    /** The total amount of chips in the pot */
    private int pot;
    
    /** The minimum bet amount (big blind) */
    private int minBet;
    
    /** The player that bet last (for tracking betting rounds) */
    private Player lastBettor;
    
    /** The amount of the last raise */
    private int lastRaiseAmount;
    
    /** The amount of the last pot won (for display in showdown) */
    private int lastPotWon = 0;
    
    /**
     * Enumeration representing the different betting rounds in Texas Holdem.
     */
    public enum BettingRound {
        PREFLOP, FLOP, TURN, RIVER, SHOWDOWN
    }
    
    /**
     * Constructs a new game with exactly two players and starting chips.
     * @param playerNames the names of the two players
     * @param startingChips the number of chips each player starts with
     * @param minBet the minimum bet amount (big blind)
     * @throws IllegalArgumentException if number of players is not exactly 2
     */
    public Game(String[] playerNames, int startingChips, int minBet) {
        if (playerNames.length != 2) {
            throw new IllegalArgumentException("Game must have exactly 2 players");
        }
        
        this.deck = new Deck();
        this.players = new ArrayList<>();
        this.communityCards = new ArrayList<>();
        this.minBet = minBet;
        
        // Create two players
        for (String name : playerNames) {
            players.add(new Player(name, startingChips));
        }
        
        // Start with a random dealer
        this.dealerIndex = (int) (Math.random() * 2);
        
        // Initialize game state
        resetRound();
    }
    
    /**
     * Starts a new round of Texas Holdem.
     */
    public void startNewRound() {
        // Move the dealer button to the next player
        dealerIndex = (dealerIndex + 1) % players.size();
        
        // Reset game state for new round
        resetRound();
        
        // Deal hole cards to each player
        dealHoleCards();
        
        // Set blinds
        postBlinds();
        
        // Start from the player after the big blind
        currentPlayerIndex = (dealerIndex + 3) % players.size();
        if (currentPlayerIndex >= players.size()) {
            currentPlayerIndex = 0;
        }
    }
    
    /**
     * Resets the game state for a new round.
     */
    private void resetRound() {
        // Reset deck
        deck.reset();
        
        // Clear community cards
        communityCards.clear();
        
        // Reset player hands and bets
        for (Player player : players) {
            player.clearHand();
            player.setFolded(false);
        }
        
        // Reset dealer flag
        players.get(dealerIndex).setDealer(true);
        
        // Reset pot and betting state
        pot = 0;
        currentRound = BettingRound.PREFLOP;
        lastBettor = null;
        lastRaiseAmount = 0;
    }
    
    /**
     * Deals two hole cards to each player.
     */
    private void dealHoleCards() {
        // First card to each player
        for (int i = 0; i < players.size(); i++) {
            int playerIndex = (dealerIndex + i + 1) % players.size();
            players.get(playerIndex).addCard(deck.dealCard());
        }
        
        // Second card to each player
        for (int i = 0; i < players.size(); i++) {
            int playerIndex = (dealerIndex + i + 1) % players.size();
            players.get(playerIndex).addCard(deck.dealCard());
        }
    }
    
    /**
     * Posts the small and big blinds.
     */
    private void postBlinds() {
        // Small blind is the player after the dealer
        int smallBlindIndex = (dealerIndex + 1) % 2;
        
        // Big blind is the player after the small blind
        int bigBlindIndex = (dealerIndex + 2) % 2;
        
        // Post small blind (half of minimum bet)
        Player smallBlindPlayer = players.get(smallBlindIndex);
        int smallBlindAmount = minBet / 2;
        if (smallBlindPlayer.removeChips(smallBlindAmount)) {
            pot += smallBlindAmount;
            smallBlindPlayer.setCurrentBet(smallBlindAmount);
        }
        
        // Post big blind (minimum bet)
        Player bigBlindPlayer = players.get(bigBlindIndex);
        if (bigBlindPlayer.removeChips(minBet)) {
            pot += minBet;
            bigBlindPlayer.setCurrentBet(minBet);
            lastBettor = bigBlindPlayer;
            lastRaiseAmount = minBet;
        }
    }
    
    /**
     * Processes a player's fold action.
     * @return true if the action was successful, false otherwise
     */
    public boolean fold() {
        Player currentPlayer = players.get(currentPlayerIndex);
        currentPlayer.setFolded(true);
        
        // Move to the next player
        advanceToNextPlayer();
        
        // Check if only one player remains
        if (getActivePlayerCount() == 1) {
            // End the hand - the remaining player wins
            for (Player player : players) {
                if (!player.hasFolded()) {
                    player.addChips(pot);
                    pot = 0;
                    return true;
                }
            }
        }
        
        // Check if betting round is over
        checkEndOfBettingRound();
        
        return true;
    }
    
    /**
     * Processes a player's check action.
     * @return true if the action was successful, false otherwise
     */
    public boolean check() {
        Player currentPlayer = players.get(currentPlayerIndex);
        
        // Can only check if no one has bet or the player has matched the current bet
        int maxBet = getMaxBet();
        if (currentPlayer.getCurrentBet() < maxBet) {
            return false;
        }
        
        // Move to the next player
        advanceToNextPlayer();
        
        // Check if betting round is over
        checkEndOfBettingRound();
        
        return true;
    }
    
    /**
     * Processes a player's call action.
     * @return true if the action was successful, false otherwise
     */
    public boolean call() {
        Player currentPlayer = players.get(currentPlayerIndex);
        
        int maxBet = getMaxBet();
        int amountToCall = maxBet - currentPlayer.getCurrentBet();
        
        // If the player doesn't have enough chips, go all-in
        if (amountToCall > currentPlayer.getChips()) {
            amountToCall = currentPlayer.getChips();
        }
        
        // If there's nothing to call, treat as a check
        if (amountToCall == 0) {
            return check();
        }
        
        // Take chips from player and add to pot
        if (currentPlayer.removeChips(amountToCall)) {
            pot += amountToCall;
            currentPlayer.setCurrentBet(currentPlayer.getCurrentBet() + amountToCall);
            
            // Move to next player
            advanceToNextPlayer();
            
            // Check if betting round is over
            checkEndOfBettingRound();
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Processes a player's bet action.
     * @param amount the amount to bet
     * @return true if the action was successful, false otherwise
     */
    public boolean bet(int amount) {
        Player currentPlayer = players.get(currentPlayerIndex);
        
        // Bet must be at least the minimum bet
        if (amount < minBet) {
            amount = minBet;
        }
        
        // If the player doesn't have enough chips, go all-in
        if (amount > currentPlayer.getChips()) {
            amount = currentPlayer.getChips();
        }
        
        // Can only bet if no one else has bet in this round
        int maxBet = getMaxBet();
        if (maxBet > 0) {
            return false;
        }
        
        // Take chips from player and add to pot
        if (currentPlayer.removeChips(amount)) {
            pot += amount;
            currentPlayer.setCurrentBet(amount);
            lastBettor = currentPlayer;
            lastRaiseAmount = amount;
            
            // Move to next player
            advanceToNextPlayer();
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Processes a player's raise action.
     * @param amount the amount to raise
     * @return true if the action was successful, false otherwise
     */
    public boolean raise(int amount) {
        Player currentPlayer = players.get(currentPlayerIndex);
        
        int maxBet = getMaxBet();
        int currentBet = currentPlayer.getCurrentBet();
        int callAmount = maxBet - currentBet;
        
        // Total amount to put in (call + raise)
        int totalAmount = callAmount + amount;
        
        // Raise must be at least the minimum bet
        if (amount < minBet) {
            amount = minBet;
            totalAmount = callAmount + amount;
        }
        
        // If the player doesn't have enough chips, go all-in
        if (totalAmount > currentPlayer.getChips()) {
            totalAmount = currentPlayer.getChips();
        }
        
        // If there's not enough to cover the call, can't raise
        if (totalAmount <= callAmount) {
            return false;
        }
        
        // Take chips from player and add to pot
        if (currentPlayer.removeChips(totalAmount)) {
            pot += totalAmount;
            currentPlayer.setCurrentBet(currentBet + totalAmount);
            lastBettor = currentPlayer;
            lastRaiseAmount = amount;
            
            // Move to next player
            advanceToNextPlayer();
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Advances the game to the next player who hasn't folded.
     */
    private void advanceToNextPlayer() {
        do {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        } while (players.get(currentPlayerIndex).hasFolded() && getActivePlayerCount() > 1);
    }
    
    /**
     * Checks if the current betting round is over and advances to the next round if needed.
     */
    private void checkEndOfBettingRound() {
        // If there's only one player left, the hand is over
        if (getActivePlayerCount() <= 1) {
            currentRound = BettingRound.SHOWDOWN;
            return;
        }
        
        // Check if all active players have had a chance to act and bets are even
        boolean bettingComplete = isBettingComplete();
        
        if (bettingComplete) {
            advanceToNextRound();
        }
    }
    
    /**
     * Determines if the current betting round is complete.
     * @return true if betting is complete, false otherwise
     */
    private boolean isBettingComplete() {
        if (lastBettor == null) {
            // If no bet has been made, check if all players have acted
            return currentPlayerIndex == dealerIndex;
        }
        
        // Check if all active players have matched the highest bet
        int maxBet = getMaxBet();
        for (Player player : players) {
            if (!player.hasFolded() && player.getCurrentBet() != maxBet) {
                return false;
            }
        }
        
        // Check if all players have had a chance to act after the last bettor
        return players.get(currentPlayerIndex) == lastBettor;
    }
    
    /**
     * Advances the game to the next round.
     */
    private void advanceToNextRound() {
        // Reset player bets for the next round
        for (Player player : players) {
            player.setCurrentBet(0);
        }
        
        // Reset betting state
        lastBettor = null;
        lastRaiseAmount = 0;
        
        // Start with player after the dealer
        currentPlayerIndex = (dealerIndex + 1) % players.size();
        
        // Skip folded players
        while (players.get(currentPlayerIndex).hasFolded()) {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        }
        
        // Advance to the next round
        switch (currentRound) {
            case PREFLOP:
                currentRound = BettingRound.FLOP;
                dealFlop();
                break;
            case FLOP:
                currentRound = BettingRound.TURN;
                dealTurn();
                break;
            case TURN:
                currentRound = BettingRound.RIVER;
                dealRiver();
                break;
            case RIVER:
                currentRound = BettingRound.SHOWDOWN;
                determineWinner();
                break;
        }
    }
    
    /**
     * Deals the flop (the first three community cards).
     */
    private void dealFlop() {
        // Burn a card
        deck.dealCard();
        
        // Deal the flop (3 cards)
        for (int i = 0; i < 3; i++) {
            communityCards.add(deck.dealCard());
        }
    }
    
    /**
     * Deals the turn (the fourth community card).
     */
    private void dealTurn() {
        // Burn a card
        deck.dealCard();
        
        // Deal the turn (1 card)
        communityCards.add(deck.dealCard());
    }
    
    /**
     * Deals the river (the fifth community card).
     */
    private void dealRiver() {
        // Burn a card
        deck.dealCard();
        
        // Deal the river (1 card)
        communityCards.add(deck.dealCard());
    }
    
    /**
     * Determines the winner of the hand.
     */
    private void determineWinner() {
        // If only one player is left, they win
        if (getActivePlayerCount() == 1) {
            for (Player player : players) {
                if (!player.hasFolded()) {
                    lastPotWon = pot;
                    player.addChips(pot);
                    pot = 0;
                    return;
                }
            }
        }
        
        // Evaluate each player's hand
        Player winner = null;
        HandEvaluator.HandResult bestHand = null;
        
        for (Player player : players) {
            if (!player.hasFolded()) {
                HandEvaluator.HandResult handResult = HandEvaluator.evaluateHand(
                    player.getHoleCards(), communityCards);
                
                if (bestHand == null || HandEvaluator.compareHands(handResult, bestHand) > 0) {
                    bestHand = handResult;
                    winner = player;
                }
            }
        }
        
        // Award the pot to the winner
        if (winner != null) {
            lastPotWon = pot;
            winner.addChips(pot);
            pot = 0;
        }
    }
    
    /**
     * Gets the player whose turn it is.
     * @return the current player
     */
    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }
    
    /**
     * Gets the number of players who haven't folded.
     * @return the number of active players
     */
    public int getActivePlayerCount() {
        int count = 0;
        for (Player player : players) {
            if (!player.hasFolded()) {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Gets the maximum bet amount among all players.
     * @return the maximum bet amount
     */
    public int getMaxBet() {
        int maxBet = 0;
        for (Player player : players) {
            if (player.getCurrentBet() > maxBet) {
                maxBet = player.getCurrentBet();
            }
        }
        return maxBet;
    }
    
    /**
     * Gets the list of community cards.
     * @return the community cards
     */
    public List<Card> getCommunityCards() {
        return new ArrayList<>(communityCards);
    }
    
    /**
     * Gets the current round of betting.
     * @return the current round
     */
    public BettingRound getCurrentRound() {
        return currentRound;
    }
    
    /**
     * Gets the amount of chips in the pot.
     * @return the pot amount
     */
    public int getPot() {
        return pot;
    }
    
    /**
     * Gets the list of players.
     * @return the players
     */
    public List<Player> getPlayers() {
        return new ArrayList<>(players);
    }
    
    /**
     * Gets the amount of the last pot won (for display in showdown).
     */
    public int getLastPotWon() {
        return lastPotWon;
    }
} 