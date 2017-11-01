package com.fwsim

import com.fwsim.Color._

import scala.collection.mutable.HashMap
import scala.collection.mutable.ListBuffer

class Simulator {

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


  /* I initially thought that each player would
  have its own copy of the GameState object. The player
  would take an action and then update it's own game state
  and somewhow propagate the updates to all the other players.
  I think it makes more sense to have a single GameState object
  which players check when they want to know about the GameState.
  That way I don't have to worry about registering players with
  each others game states to receive updates. */
  var gameState: GameState = new GameState

}

object Simulator {
  def main(args: Array[String]) {
    println("Hello, World!")
    println("Also")
    val simulator = new Simulator()
    simulator.shuffleDeck(simulator.completeDeck())
  }

}
