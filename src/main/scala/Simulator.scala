package com.fwsim

import com.fwsim.Color._

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import scala.collection.mutable.HashMap
import scala.collection.mutable.ListBuffer
 
class Simulator(val numRuns: Int = 1, val concurrency: Int = 1) {
  require(numRuns > 0)
  require(concurrency != 1, "Concurrency must be 1. Greater concurrency is not implemented.")

  private val logger: Logger = LogManager.getLogger(Simulator.getClass)
  private val gameRuns: List[GameState] = List()
  private val runsCompleted = false
  /*** public interface ***/
  
  def startSimulation = ???
  // I'm not 100% sure when this would be called, but I'm guessing it will be useful at some point.
  def stopSimulation = ???
  // Would it be better to prevent reuse of the same object
  // for multiple simulations like Spark does with some contexts?
  def clearSimulations = ???

  // todo: Should this be a different class? This isn't about simulating. It's about
  // analyzing the results of a simulation, right?
  def printReport = ???
  def printScores = ???
  def printAverageScore = ???
  def getScores = ???
  def getAverageScore = ???
}

object Simulator {
  def main(args: Array[String]) {
    println("Hello, World!")
    println("Also")
    Deck.shuffleDeck(Deck.completeDeck())
  }
}
