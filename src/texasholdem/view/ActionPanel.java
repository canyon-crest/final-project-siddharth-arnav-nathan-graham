package texasholdem.view;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionListener;

/**
 * A panel that displays the action buttons and bet slider for the player.
 */
public class ActionPanel extends JPanel {
    /** Button for checking */
    private JButton checkButton;
    
    /** Button for folding */
    private JButton foldButton;
    
    /** Button for calling */
    private JButton callButton;
    
    /** Button for betting/raising */
    private JButton betButton;
    
    /** Button for starting a new game */
    private JButton newGameButton;
    
    /** Slider for selecting bet amount */
    private JSlider betSlider;
    
    /** Label showing the current bet amount */
    private JLabel betAmountLabel;
    
    /** Panel containing the action buttons */
    private JPanel buttonsPanel;
    
    /** Panel containing the bet controls */
    private JPanel betPanel;
    
    /** The minimum bet amount */
    private int minBet;
    
    /** The maximum bet amount */
    private int maxBet;
    
    /** The current bet amount */
    private int currentBet;
    
    /** Whether the game actions are enabled */
    private boolean actionsEnabled;
    
    /**
     * Constructs a new action panel.
     */
    public ActionPanel() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setPreferredSize(new Dimension(600, 100));
        
        // Create buttons panel
        buttonsPanel = new JPanel(new GridLayout(1, 5, 10, 0));
        
        foldButton = createActionButton("Fold");
        checkButton = createActionButton("Check");
        callButton = createActionButton("Call");
        betButton = createActionButton("Bet/Raise");
        newGameButton = createActionButton("New Game");
        
        buttonsPanel.add(foldButton);
        buttonsPanel.add(checkButton);
        buttonsPanel.add(callButton);
        buttonsPanel.add(betButton);
        buttonsPanel.add(newGameButton);
        
        add(buttonsPanel, BorderLayout.NORTH);
        
        // Create bet panel
        betPanel = new JPanel(new BorderLayout(5, 5));
        
        minBet = 10;
        maxBet = 100;
        currentBet = minBet;
        
        betSlider = new JSlider(JSlider.HORIZONTAL, minBet, maxBet, currentBet);
        betSlider.setMajorTickSpacing(maxBet / 5);
        betSlider.setMinorTickSpacing(maxBet / 10);
        betSlider.setPaintTicks(true);
        betSlider.setPaintLabels(true);
        
        betSlider.addChangeListener(e -> {
            currentBet = betSlider.getValue();
            updateBetAmountLabel();
        });
        
        betAmountLabel = new JLabel("Bet: $" + currentBet);
        betAmountLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        betAmountLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        betPanel.add(betSlider, BorderLayout.CENTER);
        betPanel.add(betAmountLabel, BorderLayout.SOUTH);
        
        add(betPanel, BorderLayout.CENTER);
        
        // Initially disable game actions
        setActionsEnabled(false);
    }
    
    /**
     * Creates a styled action button.
     * @param text the button text
     * @return the created button
     */
    private JButton createActionButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        button.setFocusPainted(false);
        return button;
    }
    
    /**
     * Updates the bet amount label to show the current bet.
     */
    private void updateBetAmountLabel() {
        betAmountLabel.setText("Bet: $" + currentBet);
    }
    
    /**
     * Sets the minimum and maximum bet amount, and updates the slider.
     * @param minBet the minimum bet
     * @param maxBet the maximum bet
     */
    public void setBetLimits(int minBet, int maxBet) {
        this.minBet = Math.max(1, minBet);
        this.maxBet = Math.max(this.minBet, maxBet);
        
        betSlider.setMinimum(this.minBet);
        betSlider.setMaximum(this.maxBet);
        
        // Update tick spacing
        int range = this.maxBet - this.minBet;
        betSlider.setMajorTickSpacing(Math.max(1, range / 5));
        betSlider.setMinorTickSpacing(Math.max(1, range / 10));
        
        // Set current bet to minimum if needed
        if (currentBet < this.minBet) {
            currentBet = this.minBet;
            betSlider.setValue(currentBet);
            updateBetAmountLabel();
        }
    }
    
    /**
     * Gets the current bet amount from the slider.
     * @return the bet amount
     */
    public int getBetAmount() {
        return currentBet;
    }
    
    /**
     * Sets whether the game action buttons are enabled.
     * @param enabled true to enable the buttons, false to disable them
     */
    public void setActionsEnabled(boolean enabled) {
        this.actionsEnabled = enabled;
        foldButton.setEnabled(enabled);
        checkButton.setEnabled(enabled);
        callButton.setEnabled(enabled);
        betButton.setEnabled(enabled);
        betSlider.setEnabled(enabled);
    }
    
    /**
     * Sets whether the check button is enabled.
     * @param enabled true to enable the button, false to disable it
     */
    public void setCheckEnabled(boolean enabled) {
        checkButton.setEnabled(actionsEnabled && enabled);
    }
    
    /**
     * Sets whether the call button is enabled.
     * @param enabled true to enable the button, false to disable it
     */
    public void setCallEnabled(boolean enabled) {
        callButton.setEnabled(actionsEnabled && enabled);
    }
    
    /**
     * Sets whether the bet/raise button is enabled.
     * @param enabled true to enable the button, false to disable it
     */
    public void setBetEnabled(boolean enabled) {
        betButton.setEnabled(actionsEnabled && enabled);
        betSlider.setEnabled(actionsEnabled && enabled);
    }
    
    /**
     * Sets the text on the call button.
     * @param amount the amount to call, or 0 to show "Check"
     */
    public void setCallButtonText(int amount) {
        if (amount <= 0) {
            callButton.setText("Call");
        } else {
            callButton.setText("Call $" + amount);
        }
    }
    
    /**
     * Sets the text on the bet button.
     * @param hasBet true if the bet button should show "Raise", false for "Bet"
     */
    public void setBetButtonText(boolean hasBet) {
        betButton.setText(hasBet ? "Raise" : "Bet");
    }
    
    /**
     * Adds an action listener to the fold button.
     * @param listener the action listener
     */
    public void addFoldListener(ActionListener listener) {
        foldButton.addActionListener(listener);
    }
    
    /**
     * Adds an action listener to the check button.
     * @param listener the action listener
     */
    public void addCheckListener(ActionListener listener) {
        checkButton.addActionListener(listener);
    }
    
    /**
     * Adds an action listener to the call button.
     * @param listener the action listener
     */
    public void addCallListener(ActionListener listener) {
        callButton.addActionListener(listener);
    }
    
    /**
     * Adds an action listener to the bet button.
     * @param listener the action listener
     */
    public void addBetListener(ActionListener listener) {
        betButton.addActionListener(listener);
    }
    
    /**
     * Adds an action listener to the new game button.
     * @param listener the action listener
     */
    public void addNewGameListener(ActionListener listener) {
        newGameButton.addActionListener(listener);
    }
} 