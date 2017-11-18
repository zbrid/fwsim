# November 17, 2017
## Results of my test of my napkin math

- Commit: 48cee48721e598be774de3e751a44ef472f7dad5

- Failure! 1,727,115 gamestate instances caused an out of memory error
## Back of the Napkin Math about Max Gamestate size and approximate minimum # of gamestates in memory possible

### Per GameState math
  * I tried to intentionally overestimated the amount of space each of these would take up by
    assuming I had maximum amounts of state for a game.

    Boolean: 1 = 1 bit
    Byte: 0
    Short: 0
    Int: 5 = 80 bits
    Long: 0
    Float: 0
    Double: 0
    Char:
      * (average string length) = 60 chars
      * (number of words) = 5
      - total: 4800 bits for all strings

    Hint: 7 Ints + metadata = 112 bits
    Card: 2 Ints/card = 64 bits
    Max discard pile size: 50 * 76 = 3800 bits
    Max deck size: 50 * 76 = 3800 bits
    Max hintsList size: 25 * 112 bits/hint = 2800 bits
    Max player list size: 5 * 4 cards * 64 bits/card = 1280 bits
    numPlayersToInitialCardNum Map size: 6 Ints, 3 tuple metadatas, map metadata 
        = 6 * 32 = 192 bits
    Fireworks
      - set metadata, five color metadata, five ints
      - five colors * 1 int/color * 5 lists of cards * 2 ints/card * 32 bits
      - 5 colors * 5 cards/color * 2 int/card * 32 bit/int = 1600 bits

    **Memory for one game state**
    TOTAL = 1 + 80 + 4800 + 112 + 64 + 3800 + 3800 + 2800 + 1280 + 192 + 1600 = 18529 bits -- 2316 bytes -- 2kb
    **Approx minimum possible gamestates**
    4gb/2316kb = ~1,727,115 gamestate objects at minimum
    If the heap was 4gb, then about 1.7 million gamestate objects could be in memory at once at minimum.
    To find max heap size: java -XX:+PrintFlagsFinal -version | grep HeapSize
