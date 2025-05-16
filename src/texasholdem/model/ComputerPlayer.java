package texasholdem.model;

import java.util.List;
import java.util.Random;

/**
 * Represents a computer-controlled player in the Texas Holdem game.
 * Extends the Player class and adds AI decision-making capabilities.
 */
public class ComputerPlayer extends Player {
    
    /** Random number generator for decision making */
    private Random random;
    
    /** Aggressiveness factor (0-100): higher means more likely to bet/raise */
    private int aggressiveness;
    
    /** Tightness factor (0-100): higher means more selective about hands */
    private int tightness;
    
    /**
     * Constructs a new computer player with the given name, initial chips, and personality.
     * @param name the player's name
     * @param initialChips the initial number of chips for the player
     * @param aggressiveness 0-100 value indicating how aggressively the AI plays
     * @param tightness 0-100 value indicating how selectively the AI plays
     */
    public ComputerPlayer(String name, int initialChips, int aggressiveness, int tightness) {
        super(name, initialChips);
        this.random = new Random();
        this.aggressiveness = Math.min(100, Math.max(0, aggressiveness));
        this.tightness = Math.min(100, Math.max(0, tightness));
    }
    
    /**
     * Decides what action to take based on the current game state.
     * @param game the current game state
     * @param communityCards the community cards on the table
     * @param maxBet the maximum bet amount from any player
     * @param minimumBet the minimum allowed bet amount
     * @param potSize the total pot size
     * @return the chosen action as a string: "fold", "check", "call", "bet", or "raise"
     */
    public String decideAction(Game game, List<Card> communityCards, int maxBet, int minimumBet, int potSize) {
        // Calculate hand strength (0-1)
        double handStrength = evaluateHandStrength(communityCards);
        
        // Calculate the amount needed to call
        int amountToCall = maxBet - getCurrentBet();
        boolean canCheck = (amountToCall == 0);
        
        // Decision thresholds based on aggressiveness and tightness
        double foldThreshold = 0.2 + (tightness / 200.0); // Higher tightness means more likely to fold
        double callThreshold = 0.4 + (tightness / 400.0) - (aggressiveness / 400.0); // Balance of tight and aggressive
        double raiseThreshold = 0.7 - (aggressiveness / 200.0); // Lower threshold means more likely to raise
        
        // Adjust for pot odds
        if (amountToCall > 0) {
            double potOdds = (double) amountToCall / (potSize + amountToCall);
            // If pot odds are better than hand strength, more likely to call
            if (potOdds < handStrength) {
                callThreshold *= 0.8;
            } else {
                callThreshold *= 1.2;
            }
        }
        
        // Small random factor for unpredictability
        double randomFactor = random.nextDouble() * 0.1;
        
        // Decision making
        if (handStrength + randomFactor < foldThreshold && !canCheck) {
            // Fold with weak hands if we need to call
            return "fold";
        } else if (canCheck || handStrength + randomFactor < callThreshold) {
            // Check if possible, or call with medium hands
            return canCheck ? "check" : "call";
        } else if (handStrength + randomFactor < raiseThreshold) {
            // Bet/call with good hands
            return maxBet > 0 ? "call" : "bet";
        } else {
            // Raise with strong hands
            return maxBet > 0 ? "raise" : "bet";
        }
    }
    
    /**
     * Decides how much to bet or raise based on hand strength and game state.
     * @param game the current game state
     * @param handStrength strength of the hand (0-1)
     * @param potSize the current pot size
     * @param minimumBet the minimum allowed bet
     * @param maxBet the current maximum bet
     * @return the amount to bet or raise
     */
    public int decideBetAmount(Game game, double handStrength, int potSize, int minimumBet, int maxBet) {
        // Base the bet on the pot size and hand strength
        int baseBet = (int) (potSize * (0.1 + (handStrength * 0.4)) * (0.5 + (aggressiveness / 200.0)));
        
        // Ensure minimum bet
        baseBet = Math.max(minimumBet, baseBet);
        
        // Cap by available chips
        baseBet = Math.min(getChips(), baseBet);
        
        // Add randomness
        int randomVariation = (int) (baseBet * (random.nextDouble() * 0.4 - 0.2));
        int finalBet = baseBet + randomVariation;
        
        // Ensure bet is at least minimum and at most available chips
        finalBet = Math.max(minimumBet, Math.min(getChips(), finalBet));
        
        return finalBet;
    }
    
    /**
     * Evaluates the strength of the current hand.
     * @param communityCards the community cards on the table
     * @return a value between 0 and 1 representing hand strength (1 is strongest)
     */
    private double evaluateHandStrength(List<Card> communityCards) {
        // If we don't have community cards yet, evaluate based on hole cards only
        if (communityCards.isEmpty()) {
            return evaluateHoleCards();
        }
        
        // Evaluate based on the best possible hand with community cards
        HandEvaluator.HandResult result = HandEvaluator.evaluateHand(getHoleCards(), communityCards);
        
        // Map hand rank to a strength value (0-1)
        switch (result.getRank()) {
            case ROYAL_FLUSH: return 1.0;
            case STRAIGHT_FLUSH: return 0.95;
            case FOUR_OF_A_KIND: return 0.9;
            case FULL_HOUSE: return 0.8;
            case FLUSH: return 0.7;
            case STRAIGHT: return 0.6;
            case THREE_OF_A_KIND: return 0.5;
            case TWO_PAIR: return 0.4;
            case ONE_PAIR: return 0.3;
            default: return 0.1 + (highCardValue() / 14.0) * 0.1; // HIGH_CARD
        }
    }
    
    /**
     * Evaluates the strength of just the hole cards.
     * @return a value between 0 and 1 representing preflop hand strength
     */
    private double evaluateHoleCards() {
        List<Card> holeCards = getHoleCards();
        if (holeCards.size() < 2) return 0.0;
        
        Card card1 = holeCards.get(0);
        Card card2 = holeCards.get(1);
        
        // Check for pocket pair
        if (card1.getRank() == card2.getRank()) {
            // Higher pairs are stronger
            double rankValue = card1.getRank().getValue() / 14.0;
            return 0.5 + (rankValue * 0.5); // 0.5 - 1.0 for pairs
        }
        
        // Check for suited cards
        boolean suited = card1.getSuit() == card2.getSuit();
        
        // Calculate hand value
        int highRank = Math.max(card1.getRank().getValue(), card2.getRank().getValue());
        int lowRank = Math.min(card1.getRank().getValue(), card2.getRank().getValue());
        int rankDifference = highRank - lowRank;
        
        // High cards close in rank are better, suited is better
        double value = ((highRank / 14.0) * 0.4) +  // High card value
                       ((14 - rankDifference) / 14.0) * 0.3 + // Connected value
                       (suited ? 0.15 : 0); // Suited bonus
        
        // Normalize to 0-0.5 range for non-pairs
        return Math.min(0.5, value);
    }
    
    /**
     * Gets the highest card value in the hole cards.
     * @return the value of the highest card
     */
    private int highCardValue() {
        int highest = 0;
        for (Card card : getHoleCards()) {
            highest = Math.max(highest, card.getValue());
        }
        return highest;
    }
} 