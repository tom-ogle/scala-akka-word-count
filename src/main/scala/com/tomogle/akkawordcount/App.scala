package com.tomogle.akkawordcount

import akka.actor.ActorRef
import akka.actor.ActorSystem
import com.tomogle.akkawordcount.AggregatePrinter.PrintLatestResults
import com.tomogle.akkawordcount.FileReader.ReadFile

import scala.concurrent.duration._

/**
  *
  */
object App {

  def main(args: Array[String]): Unit = {
    val system = ActorSystem("Word-count")
    val countsAggregator: ActorRef = system.actorOf(CountsAggregator.props(), "countsAggregator")
    val lineWordCounter: ActorRef = system.actorOf(LineWordCounter.props(countsAggregator), "lineWordCounter")
    val fileReader: ActorRef = system.actorOf(FileReader.props(lineWordCounter), "fileReader")
    val resultPrinter = system.actorOf(AggregatePrinter.props(countsAggregator), "printer")

    val filePath = args(0)
    import system.dispatcher
    system.scheduler.schedule(0 milliseconds, 2 seconds, resultPrinter, PrintLatestResults())
    fileReader ! ReadFile(filePath)
  }

}
