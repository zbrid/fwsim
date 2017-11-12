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
  def doNextRound = ???
  def getState = ???
  def setState = ???
  def getCurrentScore = fireworks.currentScore
  // note that the winning score is different depending on the config of the game state
  // for now we will assume that there are 5 colors that go up to 5
  def isGameOver: Boolean = {
    fireworks.currentScore == 25 || redTokens == 0 || (deck.size == 0 && finalRound == true)
  }
  
  def finishGame: GameState = {
    while(!gameComplete) {
      doTurn(currPlayer)
      currPlayer = nextPlayer(currPlayer) 
    }
    log.warn("The finishGame method is not properly implemented for the GameState class. Returning a new instance of GameState with the default configuration.")
    new GameState
  }

  /*** private interface to run a game through its paces ***/
  var numOfTurns = 0
  var currPlayer = 0
  // todo: implement this config option, so tokens are taken away only if true
  val playingWithTokens = playWithTokens
  // I don't remember what this var is for?
  var finalRound = false
  var discardPile: List[Card] = List()
  var deck: List[Card] = List()
  // maybe later I can use immutable data structures for this
  var players: HashMap[Int, ListBuffer[Card]] = HashMap()
  var fireworks: FireworksDisplay = new FireworksDisplay
  var hints: ListBuffer[Hint] = ListBuffer()
  var hintTokens: Int = hintTokenMax
  var redTokens: Int = redTokenMax

  // add all the players to the game with ids from 0 to numPlayers - 1
  (0).until(numPlayers - 1).foreach(x => addPlayer(x))


  private def nextPlayer(curr: Int): Int = {
    (curr + 1) % numPlayers
  }
  private def gameComplete: Boolean = redTokens > 0 && fireworks.currentScore != fireworks.maxScore

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
    } else {
      players(id)(index) = Card(NotACard, -1)
    }
  }
  
  private def drawCard(): Card = {
    val card = deck.head
    deck = deck.tail
    card
  }

  private def addToFireworksDisplay(card: Card): Boolean = {
    val success = fireworks.addToFireworksDisplay(card)
    if (!success) { redTokens -= 1 }
    success
  }

  private def addPlayer(id: Int): Unit = {
    require(!(players contains id))
    players += (id -> ListBuffer[Card]())
  }

  private def doTurn(id: Int): Unit = {
  }
}
