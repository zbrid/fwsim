package com.fwsim

import com.fwsim.Color._

case class Hint(val player: Int, val attribute: CardAttribute, val indices: List[Int])

class CardAttribute

case class CardColor(val color: Color) extends CardAttribute

case class CardNumber(val num: Int) extends CardAttribute {
  require(num < 6 && num > 0)
}
