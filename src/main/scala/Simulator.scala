package com.fwsim.simulator

import com.fwsim.Color._
import com.fwsim.Deck
import com.fwsim.GameState

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

import java.io.{BufferedWriter, File, FileWriter}

import scala.io.Source

class Simulator(
    val numRuns: Int = 1,
    val concurrency: Int = 1,
    var outputFilePrefix: String = "scores", 
    val useDiskStorage: Boolean = false,
    val generateRandomFileName: Boolean = true,
    val strategy: String = "Hint, hint, play") {

  require(numRuns > 0, "The number of runs must be greater than zero.")
  require(concurrency == 1, "Concurrency must be 1. Greater concurrency is not supported.")

  private val log: Logger = LogManager.getLogger(Simulator.getClass)
  private var runs: List[GameState] = List()
  private var reporter: SimulationReporter = null
  private var runsCompleted = false
  final val DELIMITER = ", "
  var outputFileName = "shouldNotBeUsedAsAFileName"
  if (useDiskStorage && generateRandomFileName) {
    outputFileName = s"$outputFilePrefix-${scala.util.Random.alphanumeric take 10 mkString("")}.out"
  }
  
  log.info(s"Output file: $outputFileName")
  if (!useDiskStorage) {
    runs = List.fill(numRuns)(new GameState(strategy = strategy))
  }
  if (useDiskStorage) {
    reporter = new FromFileReporter(outputFileName)
  } else {
    reporter = new InMemoryReporter
  }
  /*** public interface ***/
  def startSimulation: Unit = {
    if (runsCompleted) {
      log.warn("The simulation is already complete. You have to clear the simulation before you can run it again.")
      return
    }

    if (useDiskStorage) {
      log.info("Running using disk storage.")
      log.info(s"Output file: $outputFileName")
      val file = new File(outputFileName)
      val bw = new BufferedWriter(new FileWriter(file))
      
      0 until numRuns foreach(_ => {
        val g = new GameState(strategy = strategy)
        g.finishGame
        bw.write(s"${g.getCurrentScore}$DELIMITER${g.numOfTurns}")
        bw.newLine
      })
      bw.close
    } else {
      log.info("Running using in memory storage.")
      //todo: Is asInstanceOf the only way to resolve this issue?
      // Is there a more natural way to do this?
      runs.map(_.finishGame)
      reporter.asInstanceOf[InMemoryReporter].setGames(runs)
    }
    runsCompleted = true
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
  def printScores: Unit = {
    reporter.printScores
  }
  def getScores: List[Int] = {
    reporter.getScores
  }
  def printAverageScore: Unit = {
    reporter.printAverageScore
  }
  def getAverageScore: Int = {
    reporter.getAverageScore
  }
  def printNumOfTurns: Unit = {
    reporter.printNumOfTurns
  }
  def getNumOfTurns: List[Int] = {
    reporter.getNumOfTurns
  }
  abstract class SimulationReporter {
    def printScores: Unit
    def getScores: List[Int]
    def printAverageScore: Unit
    def getAverageScore: Int
    def printNumOfTurns: Unit
    def getNumOfTurns: List[Int]
  }
  class InMemoryReporter extends SimulationReporter {
    var games: List[GameState] = null
    var scores: List[Int] = null
    var numOfTurns: List[Int] = null

    def setGames(games: List[GameState]): Unit = {
      this.games = games
    }
    override def printScores: Unit = {
      assert(games != null)
      if (!runsCompleted) {
        log.warn("Getting scores when runs aren't completed")
      }
      println(getScores)
    }

   override def printAverageScore: Unit = {
      assert(games != null)
      if (!runsCompleted) {
        log.warn("Getting scores when runs aren't completed")
      }
      println(getAverageScore)
    }
    override def getScores: List[Int] = {
      assert(games != null)
      if (!runsCompleted) { 
        log.warn("Getting scores when runs aren't completed")
      }
      if (scores == null) {
        scores = runs.map(_.getCurrentScore)
      }
      scores
    }
    override def getAverageScore: Int = {
      assert(games != null)
      if (!runsCompleted) {
        log.warn("Getting scores when runs aren't completed")
      }
      if (scores == null) {
        getScores
      }
      scores.reduce(_ + _) / scores.size
    }

    override def printNumOfTurns: Unit = {
      assert(games != null)
      println(getNumOfTurns)
    }

    override def getNumOfTurns: List[Int] = {
      assert(games != null)
      if (numOfTurns == null) {
        numOfTurns = runs.map(_.numOfTurns)
      }
      numOfTurns
    }
  }

  class FromFileReporter(val resultFile: String) extends SimulationReporter {
    var data: List[Tuple2[Int, Int]] = List()
    override def printScores: Unit = {
      if (!runsCompleted) {
        log.warn("Getting scores when runs aren't completed")
      }
      println(getScores)
    }

    override def printAverageScore: Unit = {
      if (!runsCompleted) {
        log.warn("Getting scores when runs aren't completed")
      }
      println(getAverageScore)
    }
    override def getScores: List[Int] = {
      if (!runsCompleted) { 
        log.warn("Getting scores when runs aren't completed")
      }
      if (data.isEmpty) {
        data = readData(outputFileName)
      }
      return data.map(_._1)
    }
    override def getAverageScore: Int = {
      if (!runsCompleted) {
        log.warn("Getting scores when runs aren't completed")
      }
      val scores = getScores
      scores.reduce(_ + _) / scores.size
    }

    override def printNumOfTurns(): Unit = {
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
    override def getNumOfTurns(): List[Int] = {
      //runs.map(_.numOfTurns)
      if (data.isEmpty) {
        data = readData(outputFileName)
      }
      data.map(_._2)
    }

    private def readData(file: String): List[Tuple2[Int, Int]] = {
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
}

object Simulator {
  val log: Logger = LogManager.getLogger(this.getClass)
  //todo: better usage method with info about each switch
  val usage = "Usage: -n [numRuns] -f [outputFile] -d [true/false] -rf [true/false]"
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
        case "-d" :: value :: tail =>
                               nextOption(map ++ Map('diskStorage -> value.toBoolean), tail)
        case "-rf" :: value :: tail =>
                               nextOption(map ++ Map('randomlyGenerateFileName-> value.toBoolean), tail)
        case "-s" :: value :: tail =>
                               nextOption(map ++ Map('strategy -> value.toString), tail)
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
    simulator = new Simulator(
      numRuns = options.getOrElse('numRuns, 100).asInstanceOf[Int],
      outputFilePrefix = options.getOrElse('outputFile, "scores").asInstanceOf[String],
      useDiskStorage = options.getOrElse('diskStorage, false).asInstanceOf[Boolean],
      generateRandomFileName = options.getOrElse('randomlyGenerateFileName, true).asInstanceOf[Boolean],
      strategy = options.getOrElse('strategy, "Hint, hint, play").asInstanceOf[String])
    simulator.startSimulation
    simulator.printScores
    simulator.printNumOfTurns
    println(simulator.getScores.max)
    println(simulator.getNumOfTurns.max)
  }
}
