package com.tomogle.akkawordcount

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Props

import scala.collection.mutable

/**
  *
  */
object CountsAggregator {
  def props(): Props = Props(new CountsAggregator)
  final case class LineResults(results: Map[String, Int])
  final case class ReportResults()
  final case class ResultsReport(currentAggregateResults: Map[String, Int])
}

class CountsAggregator extends Actor with ActorLogging {
  import CountsAggregator._
  private val aggregatedResults = mutable.Map[String, Int]()
  override def receive: Receive = {
    case LineResults(results) =>
      for (kv <- results) {
        val (k, v) = kv
        aggregatedResults(k) = aggregatedResults.getOrElse(k, 0) + v
      }
    case ReportResults() =>
      sender() ! ResultsReport(aggregatedResults.toMap)
  }
}