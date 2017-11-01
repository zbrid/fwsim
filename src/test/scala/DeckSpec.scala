package com.fwsim

import org.scalatest._

class DeckSpec extends FlatSpec with Matchers {
  "A Deck" should "return a deck with 50 cards when default settings are used" in {
    // 5 colors; 10 cards per color
    Deck.completeDeck.size should be (50)
  }
  
  it should "not remove any cards from a deck after shuffling" in {
    val deck = Deck.completeDeck

    deck.size should be (Deck.shuffleDeck(deck).size)
  }
}
