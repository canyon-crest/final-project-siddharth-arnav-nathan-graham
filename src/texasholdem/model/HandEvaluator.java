package texasholdem.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Evaluates poker hands to determine their rankings.
 */
public class HandEvaluator {
    
    /**
     * Enum representing the different types of poker hands from highest to lowest.
     */
    public enum HandRank {
        ROYAL_FLUSH(9, "Royal Flush"),
        STRAIGHT_FLUSH(8, "Straight Flush"),
        FOUR_OF_A_KIND(7, "Four of a Kind"),
        FULL_HOUSE(6, "Full House"),
        FLUSH(5, "Flush"),
        STRAIGHT(4, "Straight"),
        THREE_OF_A_KIND(3, "Three of a Kind"),
        TWO_PAIR(2, "Two Pair"),
        ONE_PAIR(1, "One Pair"),
        HIGH_CARD(0, "High Card");
        
        private final int value;
        private final String name;
        
        HandRank(int value, String name) {
            this.value = value;
            this.name = name;
        }
        
        public int getValue() {
            return value;
        }
        
        @Override
        public String toString() {
            return name;
        }
    }
    
    /**
     * Represents the result of a hand evaluation, including the hand rank and relevant cards.
     */
    public static class HandResult {
        private HandRank rank;
        private List<Card> relevantCards;
        
        public HandResult(HandRank rank, List<Card> relevantCards) {
            this.rank = rank;
            this.relevantCards = new ArrayList<>(relevantCards);
        }
        
        public HandRank getRank() {
            return rank;
        }
        
        public List<Card> getRelevantCards() {
            return relevantCards;
        }
        
        @Override
        public String toString() {
            return rank.toString();
        }
    }
    
    /**
     * Evaluates the best 5-card poker hand from 7 cards (2 hole cards and 5 community cards).
     * @param holeCards the player's 2 hole cards
     * @param communityCards the 5 community cards
     * @return a HandResult object containing the rank and relevant cards
     */
    public static HandResult evaluateHand(List<Card> holeCards, List<Card> communityCards) {
        List<Card> allCards = new ArrayList<>(holeCards);
        allCards.addAll(communityCards);
        
        // Sort the cards by rank value (highest to lowest)
        Collections.sort(allCards, Comparator.comparing((Card c) -> c.getRank().getValue()).reversed());
        
        // Check for each hand type from highest to lowest
        HandResult result = checkRoyalFlush(allCards);
        if (result != null) return result;
        
        result = checkStraightFlush(allCards);
        if (result != null) return result;
        
        result = checkFourOfAKind(allCards);
        if (result != null) return result;
        
        result = checkFullHouse(allCards);
        if (result != null) return result;
        
        result = checkFlush(allCards);
        if (result != null) return result;
        
        result = checkStraight(allCards);
        if (result != null) return result;
        
        result = checkThreeOfAKind(allCards);
        if (result != null) return result;
        
        result = checkTwoPair(allCards);
        if (result != null) return result;
        
        result = checkOnePair(allCards);
        if (result != null) return result;
        
        // If nothing else, return high card
        int numCards = Math.min(allCards.size(), 5);
        return new HandResult(HandRank.HIGH_CARD, allCards.subList(0, numCards));
    }
    
    /**
     * Checks if the cards contain a royal flush (A, K, Q, J, 10 of the same suit).
     */
    private static HandResult checkRoyalFlush(List<Card> cards) {
        for (Card.Suit suit : Card.Suit.values()) {
            boolean hasAce = false;
            boolean hasKing = false;
            boolean hasQueen = false;
            boolean hasJack = false;
            boolean hasTen = false;
            
            List<Card> relevantCards = new ArrayList<>();
            
            for (Card card : cards) {
                if (card.getSuit() == suit) {
                    if (card.getRank() == Card.Rank.ACE) {
                        hasAce = true;
                        relevantCards.add(card);
                    } else if (card.getRank() == Card.Rank.KING) {
                        hasKing = true;
                        relevantCards.add(card);
                    } else if (card.getRank() == Card.Rank.QUEEN) {
                        hasQueen = true;
                        relevantCards.add(card);
                    } else if (card.getRank() == Card.Rank.JACK) {
                        hasJack = true;
                        relevantCards.add(card);
                    } else if (card.getRank() == Card.Rank.TEN) {
                        hasTen = true;
                        relevantCards.add(card);
                    }
                }
            }
            
            if (hasAce && hasKing && hasQueen && hasJack && hasTen) {
                Collections.sort(relevantCards, Comparator.comparing((Card c) -> c.getRank().getValue()).reversed());
                return new HandResult(HandRank.ROYAL_FLUSH, relevantCards);
            }
        }
        return null;
    }
    
    /**
     * Checks if the cards contain a straight flush (5 consecutive cards of the same suit).
     */
    private static HandResult checkStraightFlush(List<Card> cards) {
        Map<Card.Suit, List<Card>> suitMap = new HashMap<>();
        
        // Group cards by suit
        for (Card card : cards) {
            if (!suitMap.containsKey(card.getSuit())) {
                suitMap.put(card.getSuit(), new ArrayList<>());
            }
            suitMap.get(card.getSuit()).add(card);
        }
        
        // Check each suit group for a straight
        for (List<Card> suitCards : suitMap.values()) {
            if (suitCards.size() >= 5) {
                Collections.sort(suitCards, Comparator.comparing((Card c) -> c.getRank().getValue()).reversed());
                List<Card> straight = findStraight(suitCards);
                if (straight != null) {
                    return new HandResult(HandRank.STRAIGHT_FLUSH, straight);
                }
            }
        }
        return null;
    }
    
    /**
     * Checks if the cards contain four of a kind (4 cards of the same rank).
     */
    private static HandResult checkFourOfAKind(List<Card> cards) {
        Map<Card.Rank, List<Card>> rankMap = groupByRank(cards);
        
        for (Map.Entry<Card.Rank, List<Card>> entry : rankMap.entrySet()) {
            if (entry.getValue().size() == 4) {
                List<Card> relevantCards = new ArrayList<>(entry.getValue());
                
                // Add highest kicker
                for (Card card : cards) {
                    if (card.getRank() != entry.getKey()) {
                        relevantCards.add(card);
                        break;
                    }
                }
                
                return new HandResult(HandRank.FOUR_OF_A_KIND, relevantCards);
            }
        }
        return null;
    }
    
    /**
     * Checks if the cards contain a full house (3 cards of one rank, 2 of another).
     */
    private static HandResult checkFullHouse(List<Card> cards) {
        Map<Card.Rank, List<Card>> rankMap = groupByRank(cards);
        
        List<Card> threeOfAKind = null;
        List<Card> pair = null;
        
        // Find the highest three of a kind
        for (Map.Entry<Card.Rank, List<Card>> entry : rankMap.entrySet()) {
            if (entry.getValue().size() >= 3) {
                if (threeOfAKind == null || entry.getKey().getValue() > threeOfAKind.get(0).getRank().getValue()) {
                    threeOfAKind = entry.getValue();
                }
            }
        }
        
        if (threeOfAKind != null) {
            // Find the highest pair different from the three of a kind
            for (Map.Entry<Card.Rank, List<Card>> entry : rankMap.entrySet()) {
                if (entry.getValue().size() >= 2 && entry.getKey() != threeOfAKind.get(0).getRank()) {
                    if (pair == null || entry.getKey().getValue() > pair.get(0).getRank().getValue()) {
                        pair = entry.getValue();
                    }
                }
            }
            
            if (pair != null) {
                List<Card> relevantCards = new ArrayList<>();
                relevantCards.addAll(threeOfAKind.subList(0, 3));
                relevantCards.addAll(pair.subList(0, 2));
                return new HandResult(HandRank.FULL_HOUSE, relevantCards);
            }
        }
        return null;
    }
    
    /**
     * Checks if the cards contain a flush (5 cards of the same suit).
     */
    private static HandResult checkFlush(List<Card> cards) {
        Map<Card.Suit, List<Card>> suitMap = new HashMap<>();
        
        // Group cards by suit
        for (Card card : cards) {
            if (!suitMap.containsKey(card.getSuit())) {
                suitMap.put(card.getSuit(), new ArrayList<>());
            }
            suitMap.get(card.getSuit()).add(card);
        }
        
        // Check each suit group for a flush
        for (List<Card> suitCards : suitMap.values()) {
            if (suitCards.size() >= 5) {
                Collections.sort(suitCards, Comparator.comparing((Card c) -> c.getRank().getValue()).reversed());
                return new HandResult(HandRank.FLUSH, suitCards.subList(0, 5));
            }
        }
        return null;
    }
    
    /**
     * Checks if the cards contain a straight (5 consecutive cards of any suit).
     */
    private static HandResult checkStraight(List<Card> cards) {
        List<Card> distinctRanks = new ArrayList<>();
        Card.Rank lastRank = null;
        
        // Get one card of each rank
        for (Card card : cards) {
            if (lastRank == null || card.getRank() != lastRank) {
                distinctRanks.add(card);
                lastRank = card.getRank();
            }
        }
        
        if (distinctRanks.size() >= 5) {
            List<Card> straight = findStraight(distinctRanks);
            if (straight != null) {
                return new HandResult(HandRank.STRAIGHT, straight);
            }
        }
        return null;
    }
    
    /**
     * Helper method to find a straight in a list of cards.
     * Returns a list of 5 cards forming a straight, or null if no straight is found.
     */
    private static List<Card> findStraight(List<Card> cards) {
        // Special case for A-5 straight (where Ace counts as 1)
        boolean hasAce = false;
        boolean has5 = false;
        boolean has4 = false;
        boolean has3 = false;
        boolean has2 = false;
        
        for (Card card : cards) {
            if (card.getRank() == Card.Rank.ACE) hasAce = true;
            if (card.getRank() == Card.Rank.FIVE) has5 = true;
            if (card.getRank() == Card.Rank.FOUR) has4 = true;
            if (card.getRank() == Card.Rank.THREE) has3 = true;
            if (card.getRank() == Card.Rank.TWO) has2 = true;
        }
        
        if (hasAce && has2 && has3 && has4 && has5) {
            List<Card> straight = new ArrayList<>();
            // Find the cards that make up this straight
            for (Card card : cards) {
                if (card.getRank() == Card.Rank.FIVE ||
                    card.getRank() == Card.Rank.FOUR ||
                    card.getRank() == Card.Rank.THREE ||
                    card.getRank() == Card.Rank.TWO) {
                    straight.add(card);
                }
                if (card.getRank() == Card.Rank.ACE) {
                    // Add the ace at the end since it's low in this straight
                    straight.add(card);
                }
                if (straight.size() == 5) break;
            }
            return straight;
        }
        
        // Check for standard straights
        for (int i = 0; i <= cards.size() - 5; i++) {
            if (cards.get(i).getRank().getValue() - cards.get(i + 4).getRank().getValue() == 4) {
                return cards.subList(i, i + 5);
            }
        }
        
        return null;
    }
    
    /**
     * Checks if the cards contain three of a kind (3 cards of the same rank).
     */
    private static HandResult checkThreeOfAKind(List<Card> cards) {
        Map<Card.Rank, List<Card>> rankMap = groupByRank(cards);
        
        for (Map.Entry<Card.Rank, List<Card>> entry : rankMap.entrySet()) {
            if (entry.getValue().size() == 3) {
                List<Card> relevantCards = new ArrayList<>(entry.getValue());
                
                // Add highest kickers
                int kickersNeeded = 2;
                for (Card card : cards) {
                    if (card.getRank() != entry.getKey()) {
                        relevantCards.add(card);
                        kickersNeeded--;
                        if (kickersNeeded == 0) break;
                    }
                }
                
                return new HandResult(HandRank.THREE_OF_A_KIND, relevantCards);
            }
        }
        return null;
    }
    
    /**
     * Checks if the cards contain two pair (2 cards of one rank, 2 of another).
     */
    private static HandResult checkTwoPair(List<Card> cards) {
        Map<Card.Rank, List<Card>> rankMap = groupByRank(cards);
        
        List<List<Card>> pairs = new ArrayList<>();
        
        for (List<Card> rankCards : rankMap.values()) {
            if (rankCards.size() >= 2) {
                pairs.add(rankCards);
            }
        }
        
        if (pairs.size() >= 2) {
            // Sort pairs by rank
            Collections.sort(pairs, Comparator.comparing((List<Card> p) -> p.get(0).getRank().getValue()).reversed());
            
            List<Card> relevantCards = new ArrayList<>();
            relevantCards.addAll(pairs.get(0).subList(0, 2));
            relevantCards.addAll(pairs.get(1).subList(0, 2));
            
            // Add highest kicker
            for (Card card : cards) {
                if (card.getRank() != pairs.get(0).get(0).getRank() && 
                    card.getRank() != pairs.get(1).get(0).getRank()) {
                    relevantCards.add(card);
                    break;
                }
            }
            
            return new HandResult(HandRank.TWO_PAIR, relevantCards);
        }
        return null;
    }
    
    /**
     * Checks if the cards contain one pair (2 cards of the same rank).
     */
    private static HandResult checkOnePair(List<Card> cards) {
        Map<Card.Rank, List<Card>> rankMap = groupByRank(cards);
        
        for (Map.Entry<Card.Rank, List<Card>> entry : rankMap.entrySet()) {
            if (entry.getValue().size() == 2) {
                List<Card> relevantCards = new ArrayList<>(entry.getValue());
                
                // Add highest kickers
                int kickersNeeded = 3;
                for (Card card : cards) {
                    if (card.getRank() != entry.getKey()) {
                        relevantCards.add(card);
                        kickersNeeded--;
                        if (kickersNeeded == 0) break;
                    }
                }
                
                return new HandResult(HandRank.ONE_PAIR, relevantCards);
            }
        }
        return null;
    }
    
    /**
     * Helper method to group cards by rank.
     */
    private static Map<Card.Rank, List<Card>> groupByRank(List<Card> cards) {
        Map<Card.Rank, List<Card>> rankMap = new HashMap<>();
        
        for (Card card : cards) {
            if (!rankMap.containsKey(card.getRank())) {
                rankMap.put(card.getRank(), new ArrayList<>());
            }
            rankMap.get(card.getRank()).add(card);
        }
        
        return rankMap;
    }
    
    /**
     * Compares two hands to determine the winner.
     * @param hand1 the first hand result
     * @param hand2 the second hand result
     * @return a positive number if hand1 wins, negative if hand2 wins, 0 if tie
     */
    public static int compareHands(HandResult hand1, HandResult hand2) {
        // First compare hand ranks
        int rankComparison = hand1.getRank().getValue() - hand2.getRank().getValue();
        if (rankComparison != 0) {
            return rankComparison;
        }
        
        // If ranks are the same, compare cards in order of importance
        List<Card> cards1 = hand1.getRelevantCards();
        List<Card> cards2 = hand2.getRelevantCards();
        
        int minSize = Math.min(cards1.size(), cards2.size());
        for (int i = 0; i < minSize; i++) {
            int valueComparison = cards1.get(i).getValue() - cards2.get(i).getValue();
            if (valueComparison != 0) {
                return valueComparison;
            }
        }
        
        // If all cards match, it's a tie
        return 0;
    }
} 