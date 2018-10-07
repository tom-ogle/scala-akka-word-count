package com.tomogle.akkawordcount

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import com.tomogle.akkawordcount.CountsAggregator.LineResults

import scala.collection.mutable

/**
  *
  */
object LineWordCounter {
  def props(aggregatorActor: ActorRef): Props = Props(new LineWordCounter(aggregatorActor))
  final case class CountWordsInLine(line: String)
  val WhitespaceRegEx = "\\s+"
}

class LineWordCounter(aggregatorActor: ActorRef) extends Actor with ActorLogging {
  import LineWordCounter._

  override def receive: Receive = {
    case CountWordsInLine(line) =>
      val words = line.split(WhitespaceRegEx)
      val results = mutable.Map[String, Int]()
      for (word <- words; lowercaseWord = word.toLowerCase()) {
        results(lowercaseWord) = results.getOrElse(lowercaseWord, 0) + 1
      }
      aggregatorActor ! LineResults(results.toMap)
  }
}