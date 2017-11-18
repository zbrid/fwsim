package com.fwsim.simulator

import com.fwsim.Color._
import com.fwsim.Deck
import com.fwsim.GameState

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

import java.io.{BufferedWriter, File, FileWriter}

import scala.io.Source

class Simulator(val numRuns: Int = 1, val concurrency: Int = 1, val outputFileName: String = "scores.out") {
  require(numRuns > 0, "The number of runs must be greater than zero.")
  require(concurrency == 1, "Concurrency must be 1. Greater concurrency is not supported.")

  private val log: Logger = LogManager.getLogger(Simulator.getClass)
  //private var runs: List[GameState] = List.fill(numRuns)(new GameState)
  private var runsCompleted = false
  /*** public interface ***/
  var data: List[Tuple2[Int, Int]] = List()
  def startSimulation: Unit = {
    if (runsCompleted) {
      log.warn("The simulation is already complete. You have to clear the simulation before you can run it again.")
      return
    }
    val file = new File(outputFileName)
    val bw = new BufferedWriter(new FileWriter(file))
    
    0 until numRuns foreach(_ => {
      val g = new GameState
      g.finishGame
      bw.write(s"${g.getCurrentScore}$DELIMITER${g.numOfTurns}")
      bw.newLine
    })
    runsCompleted = true
    bw.close
  }
  // I'm not 100% sure when this would be called, but I'm guessing it will be useful at some point.
  def stopSimulation = {
    log.info("Not yet implemented.")
  }
  // Would it be better to prevent reuse of the same object
  // for multiple simulations like Spark does with some contexts?
  def clearSimulations = {
    //runs = List.fill(numRuns)(new GameState)
    runsCompleted = false
  }

  // todo: Should this be a different class? This isn't about simulating. It's about
  // analyzing the results of a simulation, right?
  def printScores: Unit = {
    if (!runsCompleted) {
      log.warn("Getting scores when runs aren't completed")
    }
    println(getScores)
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
    if (data.isEmpty) {
      data = readData(outputFileName)
    }
    return data.map(_._1)
  }
  def getAverageScore: Int = {
    if (!runsCompleted) {
      log.warn("Getting scores when runs aren't completed")
    }
    val scores = getScores
    scores.reduce(_ + _) / scores.size
  }

  def printNumOfTurns(): Unit = {
    println(getNumOfTurns)
  }

  /*
    0. See if the val with the data is already populated,
        if no, go to 1, if yes, return the val.
    1. Open file with scores.
    2. Iterate over them.
      2.1 Parse the data point.
      2.2 Add the second value in the list to the list
            of numOfTurns.
    3. Close the file.
    4. Save the list to a val.
  */
  def getNumOfTurns(): List[Int] = {
    //runs.map(_.numOfTurns)
    if (data.isEmpty) {
      data = readData(outputFileName)
    }
    data.map(_._2)
  }

  final val DELIMITER = ", "
  def readData(file: String): List[Tuple2[Int, Int]] = {
    val bufferedSource = Source.fromFile(file)
    var data: List[Tuple2[Int, Int]] = List()
    for (line <- bufferedSource.getLines) {
      val datums = line.split(DELIMITER).map(_.toInt)
      data = (datums(0), datums(1)) :: data
    }
    bufferedSource.close
    data
  }
}

object Simulator {
  val log: Logger = LogManager.getLogger(this.getClass)
  val usage = "Usage: -n [numRuns] -f [outputFile]"
  def main(args: Array[String]) {
    type OptionMap = Map[Symbol, Any]
    // todo: have some way to print the usage string
    
    def nextOption(map : OptionMap, list: List[String]) : OptionMap = {
      def isSwitch(s : String) = (s(0) == '-')
      list match {
        case Nil => map
        case "-n" :: value :: tail =>
                               nextOption(map ++ Map('numRuns -> value.toInt), tail)
        case "-f" :: value :: tail =>
                               nextOption(map ++ Map('outputFile -> value.toString), tail)
        case unknown :: tail => {
            log.info(s"Unknown option: $unknown")
            nextOption(map, tail)
        }
      }
    }
    val options = nextOption(Map(), args.toList)
    log.info(s"options: $options")
    var simulator: Simulator = null
    //1727115
    //1000000000
    //val simulator = new Simulator(numRuns = 1000000000)
    simulator = new Simulator(numRuns = options.getOrElse('numRuns, 100).asInstanceOf[Int], outputFileName = options.getOrElse('outputFile, "scores.out").asInstanceOf[String])
    simulator.startSimulation
    simulator.printScores
    simulator.printNumOfTurns
    println(simulator.getScores.max)
    println(simulator.getNumOfTurns.max)
  }
}
