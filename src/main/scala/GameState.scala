package com.fwsim

import com.fwsim.Color._

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import scala.collection.mutable.HashMap
import scala.collection.mutable.ListBuffer

// I wonder if I can use unapply in the player method doTurn. Like unwrap the different types
// of moves until one fits like we do with StreamSources and StreamSinks in Airstream?
class GameState(hintTokenMax: Int = 5, redTokenMax: Int = 3, playWithTokens: Boolean = true) {
  require(hintTokenMax >= 0, "There must be 0 or more hint tokens.")
  require(redTokenMax >= 0, "There must be 0 or more red tokens.")

  private val log: Logger = LogManager.getLogger(GameState.getClass)
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
  var hints: ListBuffer[Hint] = ListBuffer()
  var hintTokens: Int = hintTokenMax
  var redTokens: Int = redTokenMax

  def getNextNeededFireworks() = fireworks.getNextNeeded

  /*
    1. Remove from hand of player with given id.
    2. Add to discard pile.
    3. Draw a card from the deck for player with given id.
  */
  def discardCard(id: Int, index: Int): Unit = {
    discardPile = players(id)(index) :: discardPile
    if (!deck.isEmpty) {
      players(id)(index) = deck.head
      deck = deck.tail
    } else {
      players(id)(index) = Card(NotACard, -1)
    }
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
    if (hintTokens == 0) log.error("You gave a hint when there were no hint tokens left.")
    hintTokens -= 1
    if (hintTokens == 0) log.warn("No hint tokens remain.")
    hints += hint
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
  // I just realized that these hints may not be about the cards
  // in the hand because I might have discarded or played a card
  // by the time I look at the hints again. I should remove info
  // from the hints as I remove the cards they are about.
  private def determineHand(id: Int): ListBuffer[Card] = {
    var possibleHand = ListBuffer.fill(5)(Card(Unknown, -1))

    val filteredHints = hints.filter(_.id == id)
    // determine hand given hints
    // todo: incrementally update possibleHand rather than evaluating it from
    // scratch each time
    hints.foreach({ hint =>
      val indices = hint.indices
      hint.attribute match {
        case CardColor(x) => {
          indices.foreach({ index =>
            val card = possibleHand(index)
            val newCard = Card(x, card.num)
            possibleHand(index) = newCard
          })
        }
        case CardNumber(x) => {
          indices.foreach({ index =>
            val card = possibleHand(index)
            val newCard = Card(card.color, x)
            possibleHand(index) = newCard
          })
        }
      }
    })
    log.info(s"Player $id has possible hand $possibleHand.")
    return possibleHand
  }

  private def getFullyDeterminedCards(hand: ListBuffer[Card]): ListBuffer[Tuple2[Card, Int]] = {
    return hand.zipWithIndex.filter(c => c._1.num > 0).filter(c._1.color != Unknown).filter(c._1.color != NotACard)
  }
  
  /* 
    Remove hints about a card from the hints repository. This
    is done after discarding or playing a card, so hints aren't
    available for cards no longer in a player's hand.
  */
  // todo: Can I change this to enable using an immutable data structure
  // instead of a list buffer?
  private def updateHints(id: Int, index: Int): Unit = {
    val invalidHints = hints.zipWithIndex.filter(_._1.id == id).filter(_._1.indices.contains(index))
    invalidHints.foreach({ case (hint, i) =>
      hints(i).indices.remove(index)
    })
    hints = hints.filter(!_.indices.isEmpty)
  }

  /*
    1. Examine the state of the game.
    2. Determine the best move out of hinting, discarding, and
      playing.
    3. Do the move.
  */
  private def doTurn(id: Int): Unit = {

    val possibleHand = determineHand(id) 
    val fullyDeterminedCards = getFullyDeterminedCards(possibleHand)
    
    val neededFireworksByColor = getNextNeededFireworks
    val playableCards = fullyDeterminedCards.filter(c => neededFireworksByColor(c._1.color) == c._1.num)

    def max(c1: Tuple2[Card, Int], c2: Tuple2[Card, Int]): Tuple2[Card, Int] = if (c1._1.num >= c2._1.num) return c1 else return c2 
    
    // todo: is there a better way to prioritize playing a different color card for any reason? or can i play in arbitrary
    // order without any issues?  
    if (!playableCards.isEmpty) {
      val toPlay = playableCards.reduce(max(_, _))
      addToFireworksDisplay(toPlay._1) 
      discardCard(id, toPlay._2)
      updateHints(id, toPlay._2)
      return
    }

    // determine if there are any discardable cards
    // find cards where the number has already been added to the display
    val discardableCards = fullyDeterminedCards.filter(c => neededFireworksByColor(c._1.color) > c._1.num)
    
    if (!discardableCards.isEmpty) {
      val toDiscardIndex = discardableCards.head._2
      discardCard(id, toDiscardIndex)
      updateHints(id, toDiscardIndex)
      hintTokens += 1
      return
    }
    // otherwise give a hint, guess at playing, guess at discarding
  }
}
