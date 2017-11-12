package com.fwsim

import com.fwsim.Color._

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
  // todo: configure game to have whatever max cards as you want
  def maxScore = colorSet.size * 5 // this assumes max # of cards is 5

  // we are assuming the largest item in the list is always the head with this method
  def currentScore: Int = {
    return display.values.map(x => if(x.isEmpty) 0 else x.head.num).reduce(_ + _)
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

  // todo: refactor to duplicate less code with addToFireworksDisplay
  def isPlayable(card: Card): Boolean = {
    if (display(card.color).size == 0) {
      if (card.num == 1) {
        return true
      }
      return false
    } else {
      val currMaxDisplayCard = display(card.color).head
      if (card.num == (currMaxDisplayCard.num + 1)) {
        return true
      }
      return false
    }
  }
}
