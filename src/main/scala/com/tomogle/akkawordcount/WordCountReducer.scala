package com.tomogle.akkawordcount

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props

import scala.collection.mutable


/**
  * An actor that aggregates word counts by operation
  */
object WordCountReducer {
  def props(): Props = Props(new WordCountReducer())

  // Submits a word to be aggregated
  final case class ReduceWordCommand(operationId: WordCountOperationID, word: String)

  // Sets the total words we will count for an operation
  final case class SetTotalWordsCommand(operationId: WordCountOperationID, count: Int)

  // Queries the progress on counts for a given word by operation
  final case class WordCountWordProgressQuery(operationId: WordCountOperationID, word: String, returnAddress: ActorRef)
  // Queries the progress on counts for all words by operation
  final case class WordCountAllProgressQuery(operationId: WordCountOperationID, returnAddress: ActorRef)

  // Queries the final counts for a given word by operation
  final case class ResultsWordIfFinishedQuery(operationId: WordCountOperationID, word: String, returnAddress: ActorRef)
  // Queries the final counts for a given operation
  final case class ResultsAllIfFinishedQuery(operationId: WordCountOperationID, returnAddress: ActorRef)
}

class WordCountReducer() extends Actor with ActorLogging {
  import com.tomogle.akkawordcount.WordCountReducer._

  // TODO Currently WordCountOperationID is unneeded here as WordCountMaster spawns one reducer per operation
  private val wordCounts = mutable.Map[WordCountOperationID, mutable.Map[String, Int]]()
//  private val completedWordCounts = mutable.Map[WordCountOperationID, Boolean]()

  // Counts the total number of words we expect to count for a given operation
  private val expectedTotalWordsPerOperation = mutable.Map[WordCountOperationID, Int]()
  // Counts the total number of words we have counted for a given operation so far
  private val countedWordsPerOperation = mutable.Map[WordCountOperationID, Int]()

  override def receive: Receive = {
    case ReduceWordCommand(operationId, word) =>
      val operationCounts: mutable.Map[String, Int] = wordCounts.getOrElse(operationId, mutable.Map[String, Int]())
      operationCounts(word) = operationCounts.getOrElse(word, 0) + 1
      wordCounts(operationId) = operationCounts
      countedWordsPerOperation(operationId) = countedWordsPerOperation.get(operationId) match {
        case Some(value) => value + 1
        case None => 1
      }

    case WordCountWordProgressQuery(operationId, word, returnAddress) =>
      val count = wordCounts.getOrElse(operationId, mutable.Map[String, Int]()).getOrElse(word, 0)
      returnAddress ! WordProgressReport(operationId, word, count)

    case WordCountAllProgressQuery(operationId, returnAddress) =>
      // Make sure we return an immutable map
      val counts = wordCounts.get(operationId).map(m => m.toMap).getOrElse(Map[String, Int]())
      returnAddress ! ProgressReport(operationId, counts)

    case SetTotalWordsCommand(operationId, count) =>
      expectedTotalWordsPerOperation(operationId) = count

    case ResultsAllIfFinishedQuery(operationId, returnAddress) =>
      val hasBeenSubmitted = expectedTotalWordsPerOperation.keySet.contains(operationId)
      if (hasBeenSubmitted) {
        // this operation has started to be aggregated and we know how to tell when it is finished
        val expectedTotalWords = expectedTotalWordsPerOperation(operationId)
        val totalWordsSoFar = countedWordsPerOperation.getOrElse(operationId, 0)
        if (expectedTotalWords == totalWordsSoFar) {
          val immutableResult = wordCounts.getOrElse(operationId, mutable.Map()).toMap
          returnAddress ! ResultReport(operationId, immutableResult)
        }
      }

    case ResultsWordIfFinishedQuery(operationId, word, returnAddress) =>
      val hasBeenSubmitted = expectedTotalWordsPerOperation.keySet.contains(operationId)
      if (hasBeenSubmitted) {
        // this operation has started to be aggregated and we know how to tell when it is finished
        val expectedTotalWords = expectedTotalWordsPerOperation(operationId)
        val totalWordsSoFar = countedWordsPerOperation.getOrElse(operationId, 0)
        if (expectedTotalWords == totalWordsSoFar) {
          val result = wordCounts.getOrElse(operationId, mutable.Map[String, Int]()).getOrElse(word, 0)
          returnAddress ! WordResultReport(operationId, word, result)
        }
      }


  }
}