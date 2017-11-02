package com.fwsim

import com.fwsim.Color._

import scala.collection.mutable.HashMap
import scala.collection.mutable.ListBuffer

// I wonder if I can use unapply in the player method doTurn. Like unwrap the different types
// of moves until one fits like we do with StreamSources and StreamSinks in Airstream?
class GameState(hintTokenMax: Int = 5, redTokenMax: Int = 3, playWithTokens: Boolean = true) {
  require(hintTokenMax >= 0, "There must be 0 or more hint tokens.")
  require(redTokenMax >= 0, "There must be 0 or more red tokens.")

  /*** public interface ***/
  def doNextRound = ???
  def getState = ???
  def setState = ???
  def getCurrentScore = fireworks.currentScore
  // note that the winning score is different depending on the config of the game state
  // for now we will assume that there are 5 colors that go up to 5
  def isGameOver: Boolean = {
    return fireworks.currentScore == 25 || redTokens == 0 || (deck.size == 0 && finalRound == true)
  }
  def finishGame = ???

  /*** private interface to run a game through its paces ***/
  // todo: implement this config option, so tokens are taken away only if true
  val playingWithTokens = playWithTokens
  var finalRound = false
  var discardPile: List[Card] = List()
  var deck: List[Card] = List()
  // maybe later I can use immutable data structures for this
  var players: HashMap[Int, ListBuffer[Card]] = HashMap()
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
    return players.map({case (x, y) => (x, y.toList)}).filter(_._1 == id).toList
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
  // Maybe use the nice Simulator context for that?
  def addToFireworksDisplay(card: Card): Boolean = {
    val success = fireworks.addToFireworksDisplay(card)
    if (!success) { redTokens -= 1 }
    return success
  }

  def addPlayer(id: Int): Unit = {
    require(!(players contains id))
    players += (id -> ListBuffer[Card]())
  }

  /*
    1. Examine the state of the game.
    2. Determine the best move out of hinting, discarding, and
      playing.
    3. Do the move.
  */
  def doTurn(): Unit = {
   /*// Get state of game from this player's perspective.
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
    */
  }
}
