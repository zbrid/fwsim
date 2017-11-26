package com.fwsim

import com.fwsim.Color._

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import scala.collection.mutable.HashMap
import scala.collection.mutable.ListBuffer

// I wonder if I can use unapply in the player method doTurn. Like unwrap the different types
// of moves until one fits like we do with StreamSources and StreamSinks in Airstream?
class GameState(hintTokenMax: Int = 5,
    redTokenMax: Int = 3,
    playWithTokens: Boolean = true, 
    numPlayers: Int = 4,
    strategy: String = "Hint, hint, play") {

  require(hintTokenMax >= 0, "There must be 0 or more hint tokens.")
  require(redTokenMax >= 0, "There must be 0 or more red tokens.")
  require(numPlayers >= 3 && numPlayers <= 5, "There must be three to five players.")
  private val log: Logger = LogManager.getLogger(GameState.getClass)

  /*** private interface to run a game through its paces ***/
  var numOfTurns = 0
  var currPlayer = 0
  var deck: List[Card] = Deck.shuffleDeck(Deck.completeDeck)
  var finalRound = false
  var discardPile: List[Card] = List()
  val numPlayersToInitialCardNum: Map[Int, Int] = Map(3 -> 5, 4 -> 4, 5 -> 4)
  var players: List[ListBuffer[Card]] =
    List.fill(numPlayers)(drawXCards(numPlayersToInitialCardNum(numPlayers)))
  var fireworks: FireworksDisplay = new FireworksDisplay
  var hints: ListBuffer[Hint] = ListBuffer()
  var hintTokens: Int = hintTokenMax
  var redTokens: Int = redTokenMax
// val strategy = Strategy("Randomly play a card")
 val strategyClass = Strategy(strategy)

  /* hint hint play strategy variables */
  var hintedIndex = 0
  var hintCounter = 0
  var hinteeId = 0

  case class Strategy(name: String)
  /*** public interface ***/
  def getCurrentScore = fireworks.currentScore
  def finishGame: GameState = {
    while(!gameOver) {
      numOfTurns += 1
      strategyClass match {
        case Strategy("Randomly play a card") => randomlyPlayACard(currPlayer)
        case Strategy("Hint, hint, play") => hintHintPlay(currPlayer)
        case unknown => { 
          log.error(s"Strategy named $unknown was not found.")
          return this
        }
      }
      currPlayer = nextPlayer(currPlayer) 
    }
    return this
  }


  private def drawXCards(x: Int): ListBuffer[Card] = {
    require(x >= 0)
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
  private def discardCard(id: Int, index: Int, discardPlay: Boolean = false): Unit = {
    discardPile = players(id)(index) :: discardPile
    players(id).update(index, drawCard)
    if (discardPlay && hintTokens < hintTokenMax) {
      hintTokens += 1
    }
  }
  
  private def drawCard(): Card = {
    val card = deck.head
    deck = deck.tail
    card
  }

  private def addToFireworksDisplay(card: Card): Unit = {
    val success = fireworks.addToFireworksDisplay(card)
    if (!success) {
      redTokens -= 1
    } else {
      hintTokens += 1
    }
  }

  /*
    1. Choose a card from the current player's hand at random.
    2. Try to add it to the fireworks.
    3. Draw a card.
  */
  private def randomlyPlayACard(id: Int): Unit = {
    val index = randomCardIndex(id)
    addToFireworksDisplay(players(id)(index))
    players(id).update(index, drawCard)
  }

  private def randomCardIndex(id: Int): Int = {
    val r = scala.util.Random
    Math.abs(r.nextInt) % players(id).size
  }

  private def hintHintPlay(id: Int): Unit = {
    // if we have enough hint tokens to complete a full hinting
    if ((hintCounter == 0 && hintTokens >= 2)
          || (hintCounter == 1 && hintTokens >= 1)
          || (hintCounter == 2)
        ) {
      hintCounter match {
        case 0 => {
          hinteeId = nextPlayer(nextPlayer(id)) 
          hintedIndex = getBestCard(hinteeId)
          hintCounter += 1
        }
        case 1 => { 
          hintCounter += 1
        }
        case 2 => {
          addToFireworksDisplay(players(id)(hintedIndex))
          players(id).update(hintedIndex, drawCard)
          hintCounter = 0
        }
      }
    } else {
      val index = randomCardIndex(id)
      discardCard(id, index, true)
    }
    def getBestCard(id: Int): Int = {
      def max(c1: Tuple2[Card, Int], c2: Tuple2[Card, Int]): Tuple2[Card, Int] = if (c1._1.num >= c2._1.num) c1 else c2
      val playables = players(id).zipWithIndex
                 .filter(x => fireworks.isPlayable(x._1))
      if (playables.isEmpty) {
        randomCardIndex(id)
      } else {
        playables.reduce(max(_,_))._2
      }
    }
  }
  // next strat: get the best playable card of player
  // two people ahead, if none, randomly play a card,
  // else give that hint
  private def sophisticatedHintHintPlay(id: Int) = {

    def getBestCard(id: Int): Option[Int] = {
      def max(c1: Tuple2[Card, Int], c2: Tuple2[Card, Int]): Tuple2[Card, Int] = if (c1._1.num >= c2._1.num) c1 else c2
      val playables = players(id).zipWithIndex
                 .filter(x => fireworks.isPlayable(x._1))
      if (playables.isEmpty) {
        None
      } else {
        Some(playables.reduce(max(_,_))._2)
      }
    }
  }
}
