package texasholdem.view;

import texasholdem.model.Card;

import javax.swing.JComponent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

/**
 * A component that displays a playing card.
 */
public class CardView extends JComponent {
    /** The card to display, or null for a face-down card */
    private Card card;
    
    /** Whether the card is displayed face up */
    private boolean faceUp;
    
    /** The width of the card */
    private static final int CARD_WIDTH = 80;
    
    /** The height of the card */
    private static final int CARD_HEIGHT = 120;
    
    /** The corner radius of the card */
    private static final int CORNER_RADIUS = 8;
    
    /**
     * Constructs a new card view with no card.
     */
    public CardView() {
        this(null, false);
    }
    
    /**
     * Constructs a new card view to display the specified card.
     * @param card the card to display, or null for a face-down card
     * @param faceUp whether to show the card face up
     */
    public CardView(Card card, boolean faceUp) {
        this.card = card;
        this.faceUp = faceUp;
        setPreferredSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
        setMinimumSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
    }
    
    /**
     * Sets the card to display.
     * @param card the card, or null for a face-down card
     */
    public void setCard(Card card) {
        this.card = card;
        repaint();
    }
    
    /**
     * Sets whether the card is face up.
     * @param faceUp true to display the card face up, false for face down
     */
    public void setFaceUp(boolean faceUp) {
        this.faceUp = faceUp;
        repaint();
    }
    
    /**
     * Gets the card displayed by this view.
     * @return the card, or null if no card is displayed
     */
    public Card getCard() {
        return card;
    }
    
    /**
     * Checks if the card is face up.
     * @return true if the card is face up, false if face down
     */
    public boolean isFaceUp() {
        return faceUp;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int width = getWidth();
        int height = getHeight();
        
        // Draw card outline
        RoundRectangle2D cardShape = new RoundRectangle2D.Float(
                1, 1, width - 2, height - 2, CORNER_RADIUS, CORNER_RADIUS);
        
        g2.setColor(Color.WHITE);
        g2.fill(cardShape);
        g2.setColor(Color.BLACK);
        g2.draw(cardShape);
        
        if (!faceUp || card == null) {
            // Draw card back
            drawCardBack(g2, width, height);
        } else {
            // Draw card front
            drawCardFront(g2, width, height);
        }
    }
    
    /**
     * Draws the back of the card.
     */
    private void drawCardBack(Graphics2D g2, int width, int height) {
        // Draw a pattern for the back of the card
        g2.setColor(new Color(30, 100, 200)); // Blue
        
        RoundRectangle2D innerShape = new RoundRectangle2D.Float(
                4, 4, width - 8, height - 8, CORNER_RADIUS - 2, CORNER_RADIUS - 2);
        g2.fill(innerShape);
        
        // Draw a grid pattern
        g2.setColor(new Color(20, 80, 180));
        for (int i = 0; i < width; i += 10) {
            g2.drawLine(i, 0, i, height);
        }
        for (int i = 0; i < height; i += 10) {
            g2.drawLine(0, i, width, i);
        }
    }
    
    /**
     * Draws the front of the card.
     */
    private void drawCardFront(Graphics2D g2, int width, int height) {
        Color cardColor;
        if (card.getSuit() == Card.Suit.HEARTS || card.getSuit() == Card.Suit.DIAMONDS) {
            cardColor = Color.RED;
        } else {
            cardColor = Color.BLACK;
        }
        g2.setColor(cardColor);
        
        // Draw rank and suit in the corners
        Font rankFont = new Font("SansSerif", Font.BOLD, 18);
        g2.setFont(rankFont);
        
        String rankStr = getRankString(card.getRank());
        FontMetrics metrics = g2.getFontMetrics();
        int rankWidth = metrics.stringWidth(rankStr);
        
        // Top left
        g2.drawString(rankStr, 5, 20);
        
        // Draw suit symbol
        drawSuitSymbol(g2, card.getSuit(), 5 + rankWidth / 2, 35, 12);
        
        // Bottom right (upside down)
        g2.rotate(Math.PI, width / 2.0, height / 2.0);
        g2.drawString(rankStr, 5, 20);
        drawSuitSymbol(g2, card.getSuit(), 5 + rankWidth / 2, 35, 12);
        g2.rotate(-Math.PI, width / 2.0, height / 2.0);
        
        // Center symbol (larger)
        drawSuitSymbol(g2, card.getSuit(), width / 2, height / 2, 24);
    }
    
    /**
     * Gets the string representation of a card rank.
     */
    private String getRankString(Card.Rank rank) {
        switch (rank) {
            case ACE: return "A";
            case KING: return "K";
            case QUEEN: return "Q";
            case JACK: return "J";
            case TEN: return "10";
            default: return String.valueOf(rank.getValue());
        }
    }
    
    /**
     * Draws a suit symbol at the specified location.
     */
    private void drawSuitSymbol(Graphics2D g2, Card.Suit suit, int x, int y, int size) {
        switch (suit) {
            case HEARTS:
                drawHeart(g2, x, y, size);
                break;
            case DIAMONDS:
                drawDiamond(g2, x, y, size);
                break;
            case CLUBS:
                drawClub(g2, x, y, size);
                break;
            case SPADES:
                drawSpade(g2, x, y, size);
                break;
        }
    }
    
    /**
     * Draws a heart symbol.
     */
    private void drawHeart(Graphics2D g2, int x, int y, int size) {
        int[] xPoints = {
            x, 
            x - size / 2, 
            x, 
            x + size / 2
        };
        int[] yPoints = {
            y, 
            y - size / 2, 
            y - size, 
            y - size / 2
        };
        g2.fillPolygon(xPoints, yPoints, 4);
        
        g2.fillOval(x - size / 2 - size / 4, y - size / 2 - size / 4, size / 2, size / 2);
        g2.fillOval(x + size / 4, y - size / 2 - size / 4, size / 2, size / 2);
    }
    
    /**
     * Draws a diamond symbol.
     */
    private void drawDiamond(Graphics2D g2, int x, int y, int size) {
        int[] xPoints = {
            x, 
            x - size / 2, 
            x, 
            x + size / 2
        };
        int[] yPoints = {
            y, 
            y - size / 2, 
            y - size, 
            y - size / 2
        };
        g2.fillPolygon(xPoints, yPoints, 4);
    }
    
    /**
     * Draws a club symbol.
     */
    private void drawClub(Graphics2D g2, int x, int y, int size) {
        g2.fillOval(x - size / 3, y - size, size / 2, size / 2);
        g2.fillOval(x - size / 3 - size / 3, y - size / 2 - size / 3, size / 2, size / 2);
        g2.fillOval(x - size / 3 + size / 3, y - size / 2 - size / 3, size / 2, size / 2);
        
        int[] xPoints = {
            x - size / 6, 
            x + size / 6, 
            x
        };
        int[] yPoints = {
            y - size / 2, 
            y - size / 2, 
            y
        };
        g2.fillPolygon(xPoints, yPoints, 3);
    }
    
    /**
     * Draws a spade symbol.
     */
    private void drawSpade(Graphics2D g2, int x, int y, int size) {
        int[] xPoints = {
            x, 
            x - size / 2, 
            x, 
            x + size / 2
        };
        int[] yPoints = {
            y, 
            y - size / 2, 
            y - size, 
            y - size / 2
        };
        g2.fillPolygon(xPoints, yPoints, 4);
        
        g2.fillOval(x - size / 4, y - size / 2, size / 2, size / 2);
        g2.fillOval(x - size / 4, y - size / 2, size / 2, size / 2);
        
        int[] stemXPoints = {
            x - size / 6, 
            x + size / 6, 
            x
        };
        int[] stemYPoints = {
            y - size / 3, 
            y - size / 3, 
            y
        };
        g2.fillPolygon(stemXPoints, stemYPoints, 3);
    }
} 