# Texas Holdem Poker Game

A Java-based Texas Holdem poker game with a GUI using Java Swing.

## Game Overview

This game implements a standard Texas Holdem poker game where the player competes against computer-controlled opponents. Each player starts with 1000 chips, and the objective is to win as many chips as possible by winning hands of poker.

## How to Play

1. **Starting the Game**: Run `TexasHoldemLauncher` to start the game.

2. **Game Setup**: The game automatically starts with you and five computer opponents.

3. **Betting Rounds**: Texas Holdem has four betting rounds:
   - **Pre-Flop**: After each player receives two hole cards
   - **Flop**: After the first three community cards are dealt
   - **Turn**: After the fourth community card is dealt
   - **River**: After the fifth and final community card is dealt

4. **Player Actions**:
   - **Fold**: Give up your hand and any bets you've made
   - **Check**: Pass the action to the next player without betting (only available if no one has bet)
   - **Call**: Match the current bet
   - **Bet/Raise**: Increase the amount that other players must match

5. **Bet Slider**: Use the slider to adjust your bet amount when betting or raising.

6. **New Game**: Click the "New Game" button to start a new hand at any time.

## Hand Rankings (from highest to lowest)

1. **Royal Flush**: A, K, Q, J, 10 of the same suit
2. **Straight Flush**: Five consecutive cards of the same suit
3. **Four of a Kind**: Four cards of the same rank
4. **Full House**: Three cards of one rank and two of another
5. **Flush**: Five cards of the same suit, not in sequence
6. **Straight**: Five consecutive cards of mixed suits
7. **Three of a Kind**: Three cards of the same rank
8. **Two Pair**: Two cards of one rank and two of another
9. **One Pair**: Two cards of the same rank
10. **High Card**: Highest card when no other hand is made

## Game Design

The game follows the Model-View-Controller (MVC) design pattern:

- **Model**: Contains the game logic, cards, players, and hand evaluation
- **View**: Provides the graphical interface using Swing components
- **Controller**: Connects the model and view, handling user actions

## Computer AI

The computer players have different playing styles based on "aggressiveness" and "tightness" parameters. They analyze their cards, pot odds, and the current game state to make decisions.

## Compiling and Running

To compile the game:
```
javac -d bin src/TexasHoldemLauncher.java src/texasholdem/*/*.java
```

To run the game:
```
java -cp bin TexasHoldemLauncher
``` 