package com.tomogle.akkawordcount

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.Props
import com.tomogle.akkawordcount.CountsAggregator.LineResults

import scala.collection.mutable

object LineWordCounter {
  def props(aggregatorActorPath: String): Props = Props(new LineWordCounter(aggregatorActorPath))
  final case class CountWordsInLine(line: String)
  val WhitespaceRegEx = "\\s+"
}

class LineWordCounter(aggregatorActorPath: String) extends Actor with ActorLogging {
  import LineWordCounter._

  private val aggregatorActor = context.actorSelection(aggregatorActorPath)

  override def receive: Receive = {
    case CountWordsInLine(line) =>
      val words = line.split(WhitespaceRegEx)
      val results = mutable.Map[String, Int]()
      for (word <- words; lowercaseWord = word.toLowerCase()) {
        results(lowercaseWord) = results.getOrElse(lowercaseWord, 0) + 1
      }
      val immutableResult = results.toMap
      aggregatorActor ! LineResults(immutableResult)
  }
}