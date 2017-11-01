import scala.collection.mutable.HashMap
import scala.collection.mutable.ListBuffer

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

  class CardAttribute

  case class CardColor(val color: Color) extends CardAttribute

  case class CardNumber(val num: Int) extends CardAttribute {
    require(num < 6 && num > 0)
  }
  // I wonder if I can use unapply in the player method doTurn. Like unwrap the different types
  // of moves until one fits like we do with StreamSources and StreamSinks in Airstream?
  class GameState(hintTokenMax: Int = 5, redTokenMax: Int = 3, playWithTokens: Boolean = true) {
    // todo: implement this config option, so tokens are taken away only if true
    val playingWithTokens = playWithTokens
    var discardPile: List[Card] = List()
    var deck: List[Card] = List()
    var players: HashMap[Int, Player] = HashMap()
    var fireworks: FireworksDisplay = new FireworksDisplay
    var hints: List[Hint] = List()
    var hintTokens: Int = hintTokenMax
    var redTokens: Int = redTokenMax

    def getNextNeededFireworks() = fireworks.getNextNeeded

    // add a given card to the discard pile
    def addDiscardedCard(discarded: Card): Unit = {
      discardPile = discarded :: discardPile
    }

    def drawCard(): Card = {
      val card = deck.head
      deck = deck.tail
      return card
    }
    
    def showAllVisibleHands(id: Int): List[Tuple2[Int, List[Card]]] = {
      return players.values.filter(_.id != id).map(x => (x.id, x.showHand)).toList
    }
    // todo: You have to reject the hint if there aren't enough tokens.
    // todo: How do I reject the hint in a nice way? Boolean return value?
    // Throw exception and make sure players catch it?
    def giveHint(hint: Hint): Unit = {
      hintTokens -= 1
      hints = hint :: hints
    }

    // todo: You have to end the game if the failure causes
    // the red tokens to run out.
    // How do I let the system know the game is over?
    // Maybe use the nice HanabiSimulator context for that?
    def addToFireworksDisplay(card: Card): Boolean = {
      val success = fireworks.addToFireworksDisplay(card)
      if (!success) { redTokens -= 1 }
      return success
    }

    def getFireworksDisplay = fireworks

    def getNumberOfPlayers = players.size

    def getHints = hints

    def getNumberOfHintTokens = hintTokens

    def getNumberOfRedTokens = redTokens

    def getSizeOfDeck = deck.size

    def getDiscardPile = discardPile

    def addPlayer(id: Int, player: Player): Unit = {
      require(!(players contains id))
      players += (id -> player)
    }
  }

  class FireworksDisplay(val colorSet: Set[Color] = Set(Red, Green, Blue, White, Yellow)) {
    require(colorSet.size > 0)
    // todo: will I have problems with the type of this list when I start to use it?
    var display: Map[Color, List[Card]] = colorSet.map(color => (color -> List[Card]())).toMap
    // todo: Can I refactor this to have no special cases by using options somehow?
    // todo: Add tests for this method
    def addToFireworksDisplay(card: Card): Boolean = {
      if (display(card.color).size == 0) {
        if (card.num == 1) {
          display = display + (card.color -> List(card))
          return true
        }
        return false
      } else {
        val currMaxDisplayCard = display(card.color).head
        if (card.num == (currMaxDisplayCard.num + 1)) {
          display = display + (card.color -> (card :: display(card.color)))
          return true
        }
        return false
      }
    }
    // we are assuming the largest item in the list is always the head with this method
    def currentScore: Int = {
      return display.values.map(x => x.head.num).reduce(_ + _)
    }
    // todo: Make this have a better format.
    def printDisplay(): Unit = {
      print(display)
    }

    def getNextNeeded(): Map[Color, Int] = {
      return display.map({
        case (x, y :: ys) => (x -> y.num)
        case (x, Nil) => (x -> 1)
      }).toMap
    }
  }

  /* I initially thought that each player would
  have its own copy of the GameState object. The player
  would take an action and then update it's own game state
  and somewhow propagate the updates to all the other players.
  I think it makes more sense to have a single GameState object
  which players check when they want to know about the GameState.
  That way I don't have to worry about registering players with
  each others game states to receive updates. */
  var gameState: GameState = new GameState

  // For now this class uses a list buffer. Maybe there
  // is a data structure that could be better?
  class Player(val id: Int, val maxCards: Int) {
    var hand: ListBuffer[Card] = ListBuffer()

    // takes index of the card it will discard
    // todo: sometimes tokens need to be subtracted when you discard, how can I tell?
    def discardCard(index: Int): Card = {
      val discarded = hand.remove(index)
      gameState.addDiscardedCard(discarded)
      return discarded
    }

    // returns new hand of the player who drew
    def drawCard(): Unit = {
      require(hand.size < maxCards)
      this.hand += gameState.drawCard()
    }
    // todo: handle when I try to discard a card that doesn't exist in my hand
    def discardCard(card: Card): Unit = {
      this.discardCard(this.hand.zipWithIndex.filter(x => x._1 == card).head._1)
    }

    /*
      1. Discard the card at the given index.
      2. Update the state of the game with that card.
      3. Draw a card.
    */
    def addToFireworksDisplay(index: Int): Unit = {
      val card = discardCard(index)
      drawCard
      gameState.addToFireworksDisplay(card)
    }
    // todo: handle when I try to add a card that doesn't exist in my hand
    def addToFireworksDisplay(card: Card): Unit = {
      this.addToFireworksDisplay(this.hand.zipWithIndex.filter(x => x._1 == card).head._1)
    }

    /*
      1. Examine the state of the game.
      2. Determine the best move out of hinting, discarding, and
        playing.
      3. Do the move.
    */
    def doTurn(): Unit = {
     // Get state of game from this player's perspective.
      val fireworks = gameState.getFireworksDisplay
      val numOfPlayers = gameState.getNumberOfPlayers
      val hints = gameState.getHints
      val numHintTokens = gameState.getNumberOfHintTokens
      val numRedTokens = gameState.getNumberOfRedTokens
      val deckSize = gameState.getSizeOfDeck
      val discardPile = gameState.getDiscardPile
      val shownHands = gameState.showAllVisibleHands(id)

      // determine my own hand to the extent I can.

      var myPossibleHand = ListBuffer.fill(5)(Card(Unknown, -1))

      val hintsToMe = hints.filter(_.player == id)
      // determine hand given hints
      // todo: incrementally update myPossibleHand rather than evaluating it from
      // scratch each time
      hints.foreach({ hint =>
        val indices = hint.indices
        hint.attribute match {
          case CardColor(x) => {
            indices.foreach({ index =>
              val card = myPossibleHand(index)
              val newCard = Card(x, card.num)
              myPossibleHand(index) = newCard
            })
          }
          case CardNumber(x) => {
            indices.foreach({ index =>
              val card = myPossibleHand(index)
              val newCard = Card(card.color, x)
              myPossibleHand(index) = newCard
            })
          }
        }
      })
      println(myPossibleHand)

      // see if I have a card that can be added to the fireworks display
      val neededFireworksByColor = gameState.getNextNeededFireworks
      val perfectlyDeterminedCards = myPossibleHand.filter(c => c.num != -1 && c.color != Unknown)
      val playableCards = perfectlyDeterminedCards.filter(c => neededFireworksByColor(c.color) == c.num)

      // todo: is there a better way to prioritize playing a different color card for any reason? or can i play in arbitrary
      // order without any issues?
      def max(c1: Card, c2: Card): Card = if (c1.num >= c2.num) return c1 else return c2
     
      if (playableCards.size > 0) {
        this.addToFireworksDisplay(playableCards.reduce(max(_, _)))
        // done with this turn
        return
      }

      // determine if there are any discardable cards
      // find cards where the color is complete in the diplay or the number has already been added to the display
      val discardableCards = perfectlyDeterminedCards.filter(c => neededFireworksByColor(c.color) > c.num).union(myPossibleHand.filter(c => neededFireworksByColor(c.color) == 6)).toList
      
      if (discardableCards.size > 0) {
        this.discardCard(discardableCards.head)
        // done with this turn
        return
      }


    }

    def showHand(): List[Card] = {
      return hand.toList
    }

    def addSelfToGame() = {
      gameState.addPlayer(id, this)
    }
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
    val Red, Yellow, Green, Blue, White, Rainbow, Unknown = Value
  }
}
