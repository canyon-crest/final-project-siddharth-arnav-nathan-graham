package texasholdem.model;

import java.util.List;
import java.util.Random;

/**
 * Represents a computer-controlled player (AI) for Texas Holdem.
 * Not yet integrated into the game loop or UI.
 */
public class ComputerPlayer extends Player {
    private int aggressiveness; // 0-100, higher = more likely to bet/raise
    private int tightness;      // 0-100, higher = more likely to fold weak hands
    private Random random;

    public ComputerPlayer(String name, int initialChips, int aggressiveness, int tightness) {
        super(name, initialChips);
        this.aggressiveness = aggressiveness;
        this.tightness = tightness;
        this.random = new Random();
    }

    /**
     * Decides the action for this AI player.
     * @param game The game state
     * @param communityCards The community cards
     * @param maxBet The current max bet
     * @param minBet The minimum bet
     * @param potSize The current pot size
     * @return "fold", "check", "call", "bet", or "raise"
     */
    public String decideAction(Game game, List<Card> communityCards, int maxBet, int minBet, int potSize) {
        // Simple logic: fold if hand is weak and tight, otherwise call/check, sometimes raise if aggressive
        double handStrength = evaluateHandStrength(game, communityCards);
        if (handStrength < 0.2 && random.nextInt(100) < tightness) {
            return "fold";
        }
        if (maxBet == 0) {
            if (random.nextInt(100) < aggressiveness) {
                return "bet";
            } else {
                return "check";
            }
        } else {
            if (random.nextInt(100) < aggressiveness && handStrength > 0.5) {
                return "raise";
            } else {
                return "call";
            }
        }
    }

    /**
     * Decides the bet or raise amount for this AI player.
     * @param game The game state
     * @param handStrength The evaluated hand strength (0-1)
     * @param potSize The current pot size
     * @param minBet The minimum bet
     * @param maxBet The maximum bet
     * @return The amount to bet or raise
     */
    public int decideBetAmount(Game game, double handStrength, int potSize, int minBet, int maxBet) {
        // Simple logic: bet more with stronger hands, less with weaker hands
        int range = maxBet - minBet;
        int bet = minBet + (int)(range * handStrength * (aggressiveness / 100.0));
        bet = Math.max(minBet, Math.min(bet, maxBet));
        return bet;
    }

    /**
     * Evaluates the hand strength (0 = worst, 1 = best) for the AI's current hand.
     * @param game The game state
     * @param communityCards The community cards
     * @return A value between 0 and 1
     */
    private double evaluateHandStrength(Game game, List<Card> communityCards) {
        // Very basic: use hand rank as a proxy for strength
        HandEvaluator.HandResult result = HandEvaluator.evaluateHand(getHoleCards(), communityCards);
        return result.getRank().getValue() / 9.0; // 9 = Royal Flush
    }

    // Getters and setters for AI personality
    public int getAggressiveness() { return aggressiveness; }
    public void setAggressiveness(int aggressiveness) { this.aggressiveness = aggressiveness; }
    public int getTightness() { return tightness; }
    public void setTightness(int tightness) { this.tightness = tightness; }
} 