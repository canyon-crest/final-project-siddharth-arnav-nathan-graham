package texasholdem.view;

import texasholdem.model.Card;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.util.List;

/**
 * A component that displays the poker table, community cards, and pot.
 */
public class TableView extends JPanel {
    /** The community cards on the table */
    private CardView[] communityCards;
    
    /** The panel that holds the community cards */
    private JPanel cardsPanel;
    
    /** The label showing the pot amount */
    private JLabel potLabel;
    
    /** The label showing the current round */
    private JLabel roundLabel;
    
    /** The current pot amount */
    private int pot;
    
    /** The current betting round */
    private String roundName;
    
    /** Color of the table */
    private static final Color TABLE_COLOR = new Color(0, 100, 0);
    
    /** Color of the table border */
    private static final Color TABLE_BORDER_COLOR = new Color(50, 30, 0);
    
    /**
     * Constructs a new table view.
     */
    public TableView() {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(600, 300));
        setBackground(Color.DARK_GRAY);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create community cards panel
        cardsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        cardsPanel.setOpaque(false);
        
        communityCards = new CardView[5];
        for (int i = 0; i < 5; i++) {
            communityCards[i] = new CardView(null, true);
            cardsPanel.add(communityCards[i]);
        }
        
        add(cardsPanel, BorderLayout.CENTER);
        
        // Create pot label
        pot = 0;
        potLabel = new JLabel("Pot: $0");
        potLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        potLabel.setForeground(Color.WHITE);
        potLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(potLabel, BorderLayout.SOUTH);
        
        // Create round label
        roundName = "Pre-Flop";
        roundLabel = new JLabel(roundName);
        roundLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        roundLabel.setForeground(Color.WHITE);
        roundLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(roundLabel, BorderLayout.NORTH);
    }
    
    /**
     * Sets the community cards to display.
     * @param cards the list of community cards
     */
    public void setCommunityCards(List<Card> cards) {
        if (cards == null) {
            for (CardView cardView : communityCards) {
                cardView.setCard(null);
            }
        } else {
            for (int i = 0; i < communityCards.length; i++) {
                if (i < cards.size()) {
                    communityCards[i].setCard(cards.get(i));
                    communityCards[i].setFaceUp(true);
                } else {
                    communityCards[i].setCard(null);
                }
            }
        }
        repaint();
    }
    
    /**
     * Sets the pot amount.
     * @param pot the pot amount
     */
    public void setPot(int pot) {
        this.pot = pot;
        potLabel.setText("Pot: $" + pot);
    }
    
    /**
     * Sets the name of the current betting round.
     * @param roundName the round name
     */
    public void setRoundName(String roundName) {
        this.roundName = roundName;
        roundLabel.setText(roundName);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw the table
        int width = getWidth();
        int height = getHeight();
        int tableWidth = width - 40;
        int tableHeight = height - 40;
        
        // Draw the outer edge of the table
        g2.setColor(TABLE_BORDER_COLOR);
        Ellipse2D outerTable = new Ellipse2D.Double(20, 20, tableWidth, tableHeight);
        g2.fill(outerTable);
        
        // Draw the table surface
        g2.setColor(TABLE_COLOR);
        Ellipse2D innerTable = new Ellipse2D.Double(25, 25, tableWidth - 10, tableHeight - 10);
        g2.fill(innerTable);
        
        // Draw a decorative ring around the outer edge
        g2.setColor(Color.DARK_GRAY);
        g2.drawOval(20, 20, tableWidth, tableHeight);
        g2.drawOval(25, 25, tableWidth - 10, tableHeight - 10);
    }
} 