package texasholdem.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a standard deck of 52 playing cards.
 */
public class Deck {
    /** The list of cards in the deck */
    private List<Card> cards;
    
    /**
     * Constructs a new deck with all 52 cards in order.
     */
    public Deck() {
        cards = new ArrayList<>();
        initializeDeck();
    }
    
    /**
     * Initializes the deck with all 52 cards.
     */
    private void initializeDeck() {
        cards.clear();
        for (Card.Suit suit : Card.Suit.values()) {
            for (Card.Rank rank : Card.Rank.values()) {
                cards.add(new Card(suit, rank));
            }
        }
    }
    
    /**
     * Shuffles the deck, randomizing the order of the cards.
     */
    public void shuffle() {
        Collections.shuffle(cards);
    }
    
    /**
     * Deals a single card from the top of the deck.
     * @return the top card of the deck, or null if the deck is empty
     */
    public Card dealCard() {
        if (cards.isEmpty()) {
            return null;
        }
        return cards.remove(0);
    }
    
    /**
     * Deals a specific number of cards from the deck.
     * @param numCards the number of cards to deal
     * @return a list containing the dealt cards
     */
    public List<Card> dealCards(int numCards) {
        List<Card> dealtCards = new ArrayList<>();
        for (int i = 0; i < numCards && !cards.isEmpty(); i++) {
            dealtCards.add(dealCard());
        }
        return dealtCards;
    }
    
    /**
     * Gets the number of cards remaining in the deck.
     * @return the number of cards remaining
     */
    public int getCardsRemaining() {
        return cards.size();
    }
    
    /**
     * Resets the deck to its initial state with all 52 cards and shuffles them.
     */
    public void reset() {
        initializeDeck();
        shuffle();
    }
} 