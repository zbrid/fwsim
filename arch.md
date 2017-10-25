There is a single game state object. The game state object contains all of the players in the game.

The game state will call each player as it becomes their turn. The player will call methods on the
game state that change its state based on what their best move is. Interesting moments are when
each player determines their best move based on the information that the game state can tell it.
