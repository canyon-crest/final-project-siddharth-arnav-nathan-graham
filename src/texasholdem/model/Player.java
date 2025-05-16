package texasholdem.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a player in the Texas Holdem game.
 */
public class Player {
    /** The name of the player */
    private String name;
    
    /** The player's current chip count */
    private int chips;
    
    /** The player's hole cards (2 cards in Texas Holdem) */
    private List<Card> holeCards;
    
    /** The amount the player has bet in the current round */
    private int currentBet;
    
    /** Whether the player has folded in the current hand */
    private boolean hasFolded;
    
    /** Whether the player is the dealer for the current hand */
    private boolean isDealer;
    
    /**
     * Constructs a new player with the given name and initial chip count.
     * @param name the player's name
     * @param initialChips the initial number of chips for the player
     */
    public Player(String name, int initialChips) {
        this.name = name;
        this.chips = initialChips;
        this.holeCards = new ArrayList<>();
        this.currentBet = 0;
        this.hasFolded = false;
        this.isDealer = false;
    }
    
    /**
     * Gets the player's name.
     * @return the player's name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Gets the player's current chip count.
     * @return the number of chips the player has
     */
    public int getChips() {
        return chips;
    }
    
    /**
     * Adds chips to the player's stack.
     * @param amount the amount of chips to add
     */
    public void addChips(int amount) {
        if (amount > 0) {
            chips += amount;
        }
    }
    
    /**
     * Removes chips from the player's stack (e.g., for a bet).
     * @param amount the amount of chips to remove
     * @return true if the player had enough chips, false otherwise
     */
    public boolean removeChips(int amount) {
        if (amount <= 0 || amount > chips) {
            return false;
        }
        chips -= amount;
        return true;
    }
    
    /**
     * Gets the player's hole cards.
     * @return a list of the player's hole cards
     */
    public List<Card> getHoleCards() {
        return new ArrayList<>(holeCards);
    }
    
    /**
     * Adds a card to the player's hand.
     * @param card the card to add
     */
    public void addCard(Card card) {
        holeCards.add(card);
    }
    
    /**
     * Clears the player's hand for a new deal.
     */
    public void clearHand() {
        holeCards.clear();
        currentBet = 0;
        hasFolded = false;
    }
    
    /**
     * Gets the amount the player has bet in the current round.
     * @return the current bet amount
     */
    public int getCurrentBet() {
        return currentBet;
    }
    
    /**
     * Sets the player's current bet for this round.
     * @param amount the bet amount
     */
    public void setCurrentBet(int amount) {
        this.currentBet = amount;
    }
    
    /**
     * Checks if the player has folded in the current hand.
     * @return true if the player has folded, false otherwise
     */
    public boolean hasFolded() {
        return hasFolded;
    }
    
    /**
     * Sets whether the player has folded.
     * @param folded true if the player folds, false otherwise
     */
    public void setFolded(boolean folded) {
        this.hasFolded = folded;
    }
    
    /**
     * Checks if the player is the dealer.
     * @return true if the player is the dealer, false otherwise
     */
    public boolean isDealer() {
        return isDealer;
    }
    
    /**
     * Sets whether the player is the dealer.
     * @param dealer true if the player is the dealer, false otherwise
     */
    public void setDealer(boolean dealer) {
        this.isDealer = dealer;
    }
    
    /**
     * Returns a string representation of this player.
     * @return a string with the player's name and chip count
     */
    @Override
    public String toString() {
        return name + " (" + chips + " chips)";
    }
} 