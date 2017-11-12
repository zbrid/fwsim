package com.fwsim

import com.fwsim.Color._

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import scala.collection.mutable.HashMap
import scala.collection.mutable.ListBuffer

// I wonder if I can use unapply in the player method doTurn. Like unwrap the different types
// of moves until one fits like we do with StreamSources and StreamSinks in Airstream?
class GameState(hintTokenMax: Int = 5, redTokenMax: Int = 3, playWithTokens: Boolean = true, numPlayers: Int = 4) {
  require(hintTokenMax >= 0, "There must be 0 or more hint tokens.")
  require(redTokenMax >= 0, "There must be 0 or more red tokens.")

  private val log: Logger = LogManager.getLogger(GameState.getClass)
  /*** public interface ***/
  def getCurrentScore = fireworks.currentScore
  def finishGame: GameState = {
    while(!gameOver) {
      doTurn(currPlayer)
      currPlayer = nextPlayer(currPlayer) 
    }
    log.warn("The finishGame method is not properly implemented for the GameState class. Returning a new instance of GameState with the default configuration.")
    new GameState
  }

  /*** private interface to run a game through its paces ***/
  var numOfTurns = 0
  var currPlayer = 0
  var deck: List[Card] = Deck.shuffleDeck(Deck.completeDeck)
  var finalRound = false
  var discardPile: List[Card] = List()
  var players: Array[ListBuffer[Card]] = Array.fill(numPlayers)(drawXCards(numPlayersToInitialCardNum(numPlayers)))
  var fireworks: FireworksDisplay = new FireworksDisplay
  var hints: ListBuffer[Hint] = ListBuffer()
  var hintTokens: Int = hintTokenMax
  var redTokens: Int = redTokenMax
  val numPlayersToInitialCardNum: Map[Int, Int] = Map(3 -> 5, 4 -> 4, 5 -> 4)

  private def drawXCards(x: Int): ListBuffer[Card] = {
    ListBuffer.fill(x)(drawCard)
  }

  private def nextPlayer(curr: Int): Int = {
    (curr + 1) % numPlayers
  }
  private def gameOver: Boolean = redTokens == 0 || fireworks.currentScore == fireworks.maxScore || (deck.size == 0 && finalRound == true)

  /*
    1. Remove from hand of player with given id.
    2. Add to discard pile.
    3. Draw a card from the deck for player with given id.
  */
  private def discardCard(id: Int, index: Int): Unit = {
    discardPile = players(id)(index) :: discardPile
    if (!deck.isEmpty) {
      players(id)(index) = deck.head
      deck = deck.tail
    }
  }
  
  private def drawCard(): Card = {
    val card = deck.head
    deck = deck.tail
    card
  }

  private def addToFireworksDisplay(card: Card): Unit = {
    val success = fireworks.addToFireworksDisplay(card)
    if (!success) { redTokens -= 1 }
  }

  /*
    1. Choose a card from the current player's hand at random.
    2. Try to add it to the fireworks.
    3. Draw a card.
  */
  private def doTurn(id: Int): Unit = {
    numOfTurns += 1
   
    val r = scala.util.Random
    
    val index = r.nextInt % players(id).size
    addToFireworksDisplay(players(id)(index))
    players(id).update(index, drawCard)
  }
}
