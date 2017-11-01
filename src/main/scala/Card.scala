package com.fwsim

import com.fwsim.Color._

// Note this card needs to accept num = -1 for partially determined cards
case class Card(val color: Color, val num: Int)
