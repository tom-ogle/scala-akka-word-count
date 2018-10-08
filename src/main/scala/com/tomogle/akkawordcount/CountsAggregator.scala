package com.tomogle.akkawordcount

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Props

import scala.collection.mutable

/**
  * Collects aggregate results
  */
object CountsAggregator {
  def props(): Props = Props(new CountsAggregator())
  final case class LineResults(results: Map[String, Int])
  final case class LogLatestResults()
  final case class ReportCount(word: String)

  final case class CountReport(word: String, count: Int)
}

class CountsAggregator extends Actor with ActorLogging {
  import CountsAggregator._

  val aggregatedResults: mutable.Map[String, Int] = mutable.Map[String, Int]()

  override def receive: Receive = {
    case LineResults(results) =>
      for (kv <- results) {
        val (k, v) = kv
        aggregatedResults(k) = aggregatedResults.getOrElse(k, 0) + v
      }
    case ReportCount(word) =>
      sender() ! CountReport(word, aggregatedResults(word))
    case LogLatestResults() =>
      for (kv <- aggregatedResults) {
        val (k, v) = kv
        log.info(k + ": " + v)
      }
  }
}