package com.fwsim

import com.fwsim.Color._
import scala.collection.mutable.ListBuffer

case class Hint(val id: Int, val attribute: CardAttribute, var indices: ListBuffer[Int])

class CardAttribute

case class CardColor(val color: Color) extends CardAttribute

case class CardNumber(val num: Int) extends CardAttribute {
  require(num < 6 && num > 0)
}
