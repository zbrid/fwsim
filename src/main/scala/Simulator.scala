package com.fwsim.simulator

import com.fwsim.GameState
import com.fwsim.Deck
import com.fwsim.Color._

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
 
class Simulator(val numRuns: Int = 1, val concurrency: Int = 1) {
  require(numRuns > 0, "The number of runs must be greater than zero.")
  require(concurrency == 1, "Concurrency must be 1. Greater concurrency is not supported.")

  private val log: Logger = LogManager.getLogger(Simulator.getClass)
  private var runs: List[GameState] = List.fill(numRuns)(new GameState)
  private var runsCompleted = false
  /*** public interface ***/
  
  def startSimulation: Unit = {
    if (runsCompleted) {
      log.warn("The simulation is already complete. You have to clear the simulation before you can run it again.")
      return
    }
    runs = runs.map(_.finishGame)
    runsCompleted = true
  }
  // I'm not 100% sure when this would be called, but I'm guessing it will be useful at some point.
  def stopSimulation = {
    log.info("Not yet implemented.")
  }
  // Would it be better to prevent reuse of the same object
  // for multiple simulations like Spark does with some contexts?
  def clearSimulations = {
    runs = List.fill(numRuns)(new GameState)
    runsCompleted = false
  }

  // todo: Should this be a different class? This isn't about simulating. It's about
  // analyzing the results of a simulation, right?
  def printScores: Unit = {
    if (!runsCompleted) {
      log.warn("Getting scores when runs aren't completed")
    }
    println(runs.map(_.getCurrentScore))
  }

  def printAverageScore: Unit = {
    if (!runsCompleted) {
      log.warn("Getting scores when runs aren't completed")
    }
    println(getAverageScore)
  }
  def getScores: List[Int] = {
    if (!runsCompleted) { 
      log.warn("Getting scores when runs aren't completed")
    }
    runs.map(_.getCurrentScore)
  }
  def getAverageScore: Int = {
    if (!runsCompleted) {
      log.warn("Getting scores when runs aren't completed")
    }
    runs.map(_.getCurrentScore).reduce(_ + _) / runs.size
  }

  def printNumOfTurns(): Unit = {
    println(getNumOfTurns)
  }

  def getNumOfTurns(): List[Int] = {
    runs.map(_.numOfTurns)
  }
}

object Simulator {
  def main(args: Array[String]) {
    val simulator = new Simulator(numRuns = 1000000000)
    simulator.startSimulation
    println(simulator.getScores.max)
    println(simulator.getNumOfTurns.max)
  }
}
