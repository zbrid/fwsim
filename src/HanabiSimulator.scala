class HanabiSimulator {
  import HanabiSimulator.Color._
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
  case class HanabiCard(val color: Color, val num: Int)

  def completeDeck(): HanabiDeck = {
    // Number of each valued card per color
    val qtyVsValue = List((3, List(1)), (2, List(2, 3, 4)), (1, List(5)))
    val colors = List(Red, Blue, Green, Yellow, White)
    val deck = qtyVsValue.map({ case (quantity, nums) => nums.asInstanceOf[List[Int]].map(n => (quantity, n)) })
      .flatten
      .map({ case (qty, num) => colors.map(color => List.fill(qty.asInstanceOf[Int])(HanabiCard(color.asInstanceOf[Color], num.asInstanceOf[Int]))) })
      .flatten
      .flatten
    return new HanabiDeck(deck)
  }

  def shuffleDeck(deck: HanabiDeck): HanabiDeck = {
    if (deck.deck.isEmpty || deck.deck.size == 1) {
      return deck
    }
    
    return deck
  }

  def cardsInDeck(deck: HanabiDeck) = {
    print(deck.deck)
  }

  class HanabiDeck(var deck: List[HanabiCard]) {
    def this() {
      // unshuffled
      this(completeDeck.deck)
    }
    def this(deck: HanabiDeck) {
      this(deck.deck)
    }

    override def toString(): String = {
      return deck.toString()
    }
  }
}


object HanabiSimulator {
  def main(args: Array[String]) {
    println("Hello, World!")
    println("Also")
    val simulator = new HanabiSimulator()
    println(simulator.shuffleDeck(new simulator.HanabiDeck(simulator.completeDeck())))
  }

  object Color extends Enumeration {
    type Color = Value
    val Red, Yellow, Green, Blue, White, Rainbow = Value
  }
}
