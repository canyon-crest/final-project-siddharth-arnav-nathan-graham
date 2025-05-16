package texasholdem.model;

/**
 * Represents a standard playing card with a suit and rank.
 */
public class Card {
    /** The suit of this card (HEARTS, DIAMONDS, CLUBS, SPADES) */
    private Suit suit;
    
    /** The rank of this card (ACE, TWO, THREE, etc.) */
    private Rank rank;
    
    /**
     * Constructs a card with the specified suit and rank.
     * @param suit the suit of the card
     * @param rank the rank of the card
     */
    public Card(Suit suit, Rank rank) {
        this.suit = suit;
        this.rank = rank;
    }
    
    /**
     * Gets the suit of this card.
     * @return the suit
     */
    public Suit getSuit() {
        return suit;
    }
    
    /**
     * Gets the rank of this card.
     * @return the rank
     */
    public Rank getRank() {
        return rank;
    }
    
    /**
     * Gets the numeric value of this card.
     * @return the numeric value (Ace = 14, King = 13, Queen = 12, Jack = 11, others = face value)
     */
    public int getValue() {
        return rank.getValue();
    }
    
    /**
     * Returns a string representation of this card.
     * @return a string with the rank and suit of the card
     */
    @Override
    public String toString() {
        return rank + " of " + suit;
    }
    
    /**
     * Enumeration of card suits.
     */
    public enum Suit {
        HEARTS, DIAMONDS, CLUBS, SPADES;
        
        @Override
        public String toString() {
            return name().charAt(0) + name().substring(1).toLowerCase();
        }
    }
    
    /**
     * Enumeration of card ranks.
     */
    public enum Rank {
        TWO(2), THREE(3), FOUR(4), FIVE(5), SIX(6), SEVEN(7), EIGHT(8), NINE(9), TEN(10), 
        JACK(11), QUEEN(12), KING(13), ACE(14);
        
        private final int value;
        
        Rank(int value) {
            this.value = value;
        }
        
        /**
         * Gets the numeric value of this rank.
         * @return the numeric value
         */
        public int getValue() {
            return value;
        }
        
        @Override
        public String toString() {
            switch (this) {
                case ACE: return "Ace";
                case KING: return "King";
                case QUEEN: return "Queen";
                case JACK: return "Jack";
                default: return String.valueOf(value);
            }
        }
    }
} 