# Version 0.2
## Simulator

Class that holds many games. Can configure games to run, run them, then return the results.

## GameInstance

Class that handles a single instance of a game. It can be configured to use partcular rules. It runs a game from start to finish and returns the results.

## GameState

This class is used by the GameInstance. It holds players, token counts, hands, and the fireworks display. It can walk through the game by telling each player to take its turn.

## Player

Class used by the GameState to maintain the state of the game.

## Card

Class that represents a card in the game.

## Hint

Class that represents a hint in the game. It consists of a player id, a card attribute, and a List of ints that are the indexes where the card attribute applies.

## FireworksDisplay

Class that contains a fireworks display. Cards can be added to the display and it will respond about whether the addition was successful or not.

# (Deprecated) Version 0.1

There is a single game state object. The game state object contains all of the players in the game.

The game state will call each player as it becomes their turn. The player will call methods on the
game state that change its state based on what their best move is. Interesting moments are when
each player determines their best move based on the information that the game state can tell it.
