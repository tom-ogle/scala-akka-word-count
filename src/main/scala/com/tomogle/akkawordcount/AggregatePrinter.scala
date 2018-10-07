package com.tomogle.akkawordcount

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import com.tomogle.akkawordcount.CountsAggregator.ReportResults
import com.tomogle.akkawordcount.CountsAggregator.ResultsReport

/**
  *
  */
object AggregatePrinter {
  def props(aggregatorActor: ActorRef): Props = Props(new AggregatePrinter(aggregatorActor))
  final case class PrintLatestResults()
}

class AggregatePrinter(aggregatorActor: ActorRef) extends Actor with ActorLogging {
  import AggregatePrinter._

  override def receive: Receive = {
    case PrintLatestResults() =>
      aggregatorActor ! ReportResults()
    case ResultsReport(currentAggregateResults) =>
      for (kv <- currentAggregateResults) {
        val (k, v) = kv
        log.info(k + ": " + v)
      }
  }
}