package texasholdem.view;

import texasholdem.model.Card;
import texasholdem.model.Player;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;

/**
 * A component that displays a player, their cards, and their current bet.
 */
public class PlayerView extends JPanel {
    /** The player being displayed */
    private Player player;
    
    /** The view for the first card */
    private CardView cardView1;
    
    /** The view for the second card */
    private CardView cardView2;
    
    /** Label for the player's name */
    private JLabel nameLabel;
    
    /** Label for the player's chips */
    private JLabel chipsLabel;
    
    /** Label for the player's current bet */
    private JLabel betLabel;
    
    /** Whether this player is the current player */
    private boolean isCurrentPlayer;
    
    /** The panel for the cards */
    private JPanel cardsPanel;
    
    /** The panel for player info */
    private JPanel infoPanel;
    
    /**
     * Constructs a new player view.
     * @param player the player to display
     */
    public PlayerView(Player player) {
        this.player = player;
        this.isCurrentPlayer = false;
        
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setPreferredSize(new Dimension(200, 180));
        
        // Create cards panel
        cardsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        cardsPanel.setOpaque(false);
        cardView1 = new CardView(null, false);
        cardView2 = new CardView(null, false);
        cardsPanel.add(cardView1);
        cardsPanel.add(cardView2);
        add(cardsPanel, BorderLayout.CENTER);
        
        // Create info panel
        infoPanel = new JPanel();
        infoPanel.setLayout(new BorderLayout());
        infoPanel.setOpaque(false);
        
        // Player name label
        nameLabel = new JLabel(player.getName());
        nameLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        infoPanel.add(nameLabel, BorderLayout.NORTH);
        
        // Player chips label
        chipsLabel = new JLabel(player.getChips() + " chips");
        chipsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        infoPanel.add(chipsLabel, BorderLayout.CENTER);
        
        // Player bet label
        betLabel = new JLabel("Bet: 0");
        betLabel.setHorizontalAlignment(SwingConstants.CENTER);
        infoPanel.add(betLabel, BorderLayout.SOUTH);
        
        add(infoPanel, BorderLayout.SOUTH);
        
        // Initial update
        updateView();
    }
    
    /**
     * Sets the player for this view.
     * @param player the player to display
     */
    public void setPlayer(Player player) {
        this.player = player;
        nameLabel.setText(player.getName());
        updateView();
    }
    
    /**
     * Sets the cards to display.
     * @param cards the player's cards
     * @param faceUp whether to show the cards face up
     */
    public void setCards(List<Card> cards, boolean faceUp) {
        if (cards == null || cards.isEmpty()) {
            cardView1.setCard(null);
            cardView2.setCard(null);
        } else if (cards.size() == 1) {
            cardView1.setCard(cards.get(0));
            cardView1.setFaceUp(faceUp);
            cardView2.setCard(null);
        } else {
            cardView1.setCard(cards.get(0));
            cardView1.setFaceUp(faceUp);
            cardView2.setCard(cards.get(1));
            cardView2.setFaceUp(faceUp);
        }
        repaint();
    }
    
    /**
     * Sets whether this player is the current player.
     * @param isCurrentPlayer true if this player is the current player
     */
    public void setCurrentPlayer(boolean isCurrentPlayer) {
        this.isCurrentPlayer = isCurrentPlayer;
        updateView();
    }
    
    /**
     * Sets whether this player has folded.
     * @param hasFolded true if the player has folded
     */
    public void setFolded(boolean hasFolded) {
        if (hasFolded) {
            cardView1.setFaceUp(false);
            cardView2.setFaceUp(false);
            setEnabled(false);
        } else {
            setEnabled(true);
        }
        updateView();
    }
    
    /**
     * Sets whether this player is the dealer.
     * @param isDealer true if this player is the dealer
     */
    public void setDealer(boolean isDealer) {
        nameLabel.setText(isDealer ? player.getName() + " (D)" : player.getName());
        updateView();
    }
    
    /**
     * Updates the view to match the current player state.
     */
    public void updateView() {
        // Update chip count
        chipsLabel.setText(player.getChips() + " chips");
        
        // Update bet amount
        betLabel.setText("Bet: " + player.getCurrentBet());
        
        // Highlight current player
        if (isCurrentPlayer) {
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.YELLOW, 3),
                BorderFactory.createEmptyBorder(2, 2, 2, 2)));
        } else {
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                BorderFactory.createEmptyBorder(4, 4, 4, 4)));
        }
        
        // Show folded state
        if (player.hasFolded()) {
            setForeground(Color.GRAY);
            nameLabel.setForeground(Color.GRAY);
            chipsLabel.setForeground(Color.GRAY);
            betLabel.setForeground(Color.GRAY);
        } else {
            setForeground(Color.BLACK);
            nameLabel.setForeground(Color.BLACK);
            chipsLabel.setForeground(Color.BLACK);
            betLabel.setForeground(Color.BLACK);
        }
        
        repaint();
    }
    
    /**
     * Gets the player for this view.
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }
} 