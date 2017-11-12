package com.fwsim

import com.fwsim.Color._

object Deck {
  def completeDeck(): List[Card] = {
    // Number of each valued card per color
    val qtyVsValue = List((3, List(1)), (2, List(2, 3, 4)), (1, List(5)))
    val colors = List(Red, Blue, Green, Yellow, White)
    val deck = qtyVsValue.map({ case (quantity, nums) => nums.asInstanceOf[List[Int]].map(n => (quantity, n)) })
      .flatten
      .map({ case (qty, num) => colors.map(color => List.fill(qty.asInstanceOf[Int])(Card(color.asInstanceOf[Color], num.asInstanceOf[Int]))) })
      .flatten
      .flatten
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
    val shuffled = deck.map(card => (r.nextInt, card))
        .sortWith(_._1 > _._1)
        .map({ case(num, card) => card })
    return shuffled
  }
}

