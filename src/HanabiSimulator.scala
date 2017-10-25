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
  case class Card(val color: Color, val num: Int)

  def completeDeck(): List[Card] = {
    // Number of each valued card per color
    val qtyVsValue = List((3, List(1)), (2, List(2, 3, 4)), (1, List(5)))
    val colors = List(Red, Blue, Green, Yellow, White)
    val deck = qtyVsValue.map({ case (quantity, nums) => nums.asInstanceOf[List[Int]].map(n => (quantity, n)) })
      .flatten
      .map({ case (qty, num) => colors.map(color => List.fill(qty.asInstanceOf[Int])(Card(color.asInstanceOf[Color], num.asInstanceOf[Int]))) })
      .flatten
      .flatten
    println(deck)
    return deck
  }

  def shuffleDeck(deck: List[Card]): List[Card] = {
    if (deck.isEmpty || deck.size == 1) {
      return deck;
    }

   // 1. Assign a random number to each card.
   // 2. Sort based on the random number.
   // 3. Return sorted deck.

    val r = scala.util.Random
    r.nextInt
    val shuffled = deck.map(card => (r.nextInt, card))
        .sortWith(_._1 > _._1)
        .map({ case(num, card) => card })
    println(shuffled)
    return shuffled
  }

  def cardsInDeck(deck: List[Card]) = {
    print(deck)
  }

  case class Hint(val player: Int, val attribute: CardAttribute, val indices: List[Int])

  class CardAttribute()

  case class CardColor(val color: Color) extends CardAttribute

  case class CardNumber(val num: Int) extends CardAttribute {
    require(num < 6 && num > 0)
  }

  class GameState

  class Action

  case class GiveHint(hint: Hint) extends Action
  case class DiscardCard(card: Card) extends Action
  case class DrawCard() extends Action

  class Player(val id: Int) {
    var hand: List[Card] = List()
    var gameState: GameState = new GameState

    def drawCard(deck: List[Card]): List[Card] = {
      this.hand = deck.head :: hand
      return deck.tail
    }

    def updateState(action: Action): GameState = ???
  }
}


object HanabiSimulator {
  def main(args: Array[String]) {
    println("Hello, World!")
    println("Also")
    val simulator = new HanabiSimulator()
    simulator.shuffleDeck(simulator.completeDeck())
  }

  object Color extends Enumeration {
    type Color = Value
    val Red, Yellow, Green, Blue, White, Rainbow = Value
  }
}
