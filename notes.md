  TODO:
  
    Easy tasks
    * add an option to not print the score outcomes in the main method of simulator.
    That way when you do runs with many game states the output you are interested
    won't be hard to find before the score output.
    Longer tasks
    * Add use of the hint tokens to the hint, hint, play strategy
    * is there something I can do with lazy evaluation that would help with the using
    too much memory problem or not? seems like not since I'd eventually end up with all
    the game states in memory in the end even if they start out being instantiated one
    by one. that would help if i could delete a gamestate after being done with it, but
    that's like the write to disk method except the final score data could perhaps be in
    memory instead of writing to disk and reading later?
    * maybe I could implement each of those and have the strategy be pluggable and then
    test them all against each other
    * Note: I guess reading in all the data only works if the data is small enough to fit
    into memory. What's the max I can fit into mem? Whats the difference in size between
    a GameState and a data tuple? (I'm pretty sure I could figure that out.)

    OMG I TOTALLY FIGURED OUT A REALLY INTERESTING THING TO DO:

   I wonder if I could make the data spillover instead of always writing
   to file. Can I use spillover to make sure I don't hit heap limits. This is like
   what Spark does when you enable that option.

  * Would be good to do some testing to determine at what point I should write results
  to a file and when I can have everything in memory.
  * Can do some testing to see the speed of the different strategies as well.

  * Should make a thing to output performance info.
    - need to know: strat used, in mem or disk, num of players, all that config, print at top of file, then list all the perf data in a certain format, then list all the scores and what not in a certain format.
    ******- should start out with just perf data with the relevant configs logged.*****
  /*
    3 1s
    2 2s
    2 3s
    2 4s
    1 5

    Beginning state
    8 info tokens
    3 fuse tokens
    Each player knows the cards of the others, but not their own
    3 players - each person gets five cards
    4 or 5 players - each person gets four cards
    Three moves
    Give info
      Can say X num of Y numbered cards
      Can say X num of Y colored cards
      Two choices - Can or cannot say you have zero of Y type of card
      Removes 1 info token for all above choices
    Discard a card and draw a new one
      Replenishes info token
    Play a card
      Choose a card from your hand an play it
        If can add to a firework display, add it
          If you completed a color, get an info token
        If can't add it, discard the card
          Lose a fuse token
        Always replace card by drawing

     If all fuse tokens are gone, lose immediately
     If all five cards have been played successfully, win immediately
     Otherwise, play until deck is empty and for one full round after that.
     At the end of the game, the values of the highest cards in each suit are added resulting in a total score out of 25 points.

     Variants
       Six suites instead of five
       More info tokens
       More bomb tokens
       Keep playing even when the deck is gone, game ends when a player would start a turn with no cards in hand

  */
