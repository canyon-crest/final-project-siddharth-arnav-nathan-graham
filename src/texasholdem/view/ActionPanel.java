package texasholdem.view;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/**
 * A panel that displays the action buttons and bet input for the player.
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
    
    /** Text field for entering bet amount */
    private JTextField betTextField;
    
    /** Panel containing the action buttons */
    private JPanel buttonsPanel;
    
    /** Panel containing the bet input */
    private JPanel betPanel;
    
    /** The minimum bet amount */
    private int minBet;
    
    /** The maximum bet amount */
    private int maxBet;
    
    /** Whether the game actions are enabled */
    private boolean actionsEnabled;
    
    private boolean isAwaitingBetAmount = false;
    private ActionListener betListener;
    private String lastBetButtonText = "Bet";
    
    /**
     * Constructs a new action panel.
     */
    public ActionPanel() {
        setLayout(new BorderLayout(5, 5));
        setBorder(new EmptyBorder(5, 5, 5, 5));
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
        betPanel.setVisible(false); // Initially hidden
        
        // Create bet input field
        betTextField = new JTextField();
        betTextField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        betTextField.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Add placeholder text
        betTextField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (betTextField.getText().equals("Enter bet amount...")) {
                    betTextField.setText("");
                    betTextField.setForeground(Color.BLACK);
                }
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                if (betTextField.getText().isEmpty()) {
                    betTextField.setText("Enter bet amount...");
                    betTextField.setForeground(Color.GRAY);
                }
            }
        });
        
        // Set initial placeholder
        betTextField.setText("Enter bet amount...");
        betTextField.setForeground(Color.GRAY);
        
        betPanel.add(betTextField, BorderLayout.CENTER);
        
        add(betPanel, BorderLayout.CENTER);
        
        // Initially disable game actions
        setActionsEnabled(false);
        
        betButton.addActionListener(e -> onBetButtonPressed());
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
     * Sets the minimum and maximum bet amount.
     * @param minBet the minimum bet
     * @param maxBet the maximum bet
     */
    public void setBetLimits(int minBet, int maxBet) {
        this.minBet = Math.max(1, minBet);
        this.maxBet = Math.max(this.minBet, maxBet);
    }
    
    /**
     * Gets the current bet amount from the text field.
     * @return the bet amount, or -1 if invalid
     */
    public int getBetAmount() {
        try {
            String text = betTextField.getText();
            if (text.equals("Enter bet amount...")) {
                return -1;
            }
            int amount = Integer.parseInt(text);
            if (amount >= minBet && amount <= maxBet) {
                return amount;
            }
            return -1;
        } catch (NumberFormatException e) {
            return -1;
        }
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
        betTextField.setEnabled(enabled);
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
        betTextField.setEnabled(actionsEnabled && enabled);
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
     * Sets the text on the bet button and shows/hides the bet input field.
     * @param hasBet true if the bet button should show "Raise", false for "Bet"
     */
    public void setBetButtonText(boolean hasBet) {
        lastBetButtonText = hasBet ? "Raise" : "Bet";
        if (!isAwaitingBetAmount) {
            betButton.setText(lastBetButtonText);
        }
        // Only show the text field if awaiting input
        betPanel.setVisible(isAwaitingBetAmount);
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
        this.betListener = listener;
    }
    
    /**
     * Adds an action listener to the new game button.
     * @param listener the action listener
     */
    public void addNewGameListener(ActionListener listener) {
        newGameButton.addActionListener(listener);
    }
    
    private void onBetButtonPressed() {
        if (!isAwaitingBetAmount) {
            // First press: show text field, change button to Confirm
            isAwaitingBetAmount = true;
            betPanel.setVisible(true);
            betButton.setText("Confirm");
            betTextField.setText("");
            betTextField.requestFocusInWindow();
        } else {
            // Second press: fire bet event, hide text field
            if (betListener != null) {
                betListener.actionPerformed(null);
            }
            isAwaitingBetAmount = false;
            betPanel.setVisible(false);
            betButton.setText(lastBetButtonText);
        }
    }
    
    public boolean isAwaitingBetAmount() {
        return isAwaitingBetAmount;
    }
} 