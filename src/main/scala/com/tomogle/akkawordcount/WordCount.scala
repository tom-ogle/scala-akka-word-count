package com.tomogle.akkawordcount

import java.util.UUID

import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.Future

/**
  * The WordCount API.
  */
trait WordCount {

  // Submit a file to be word counted
  def submitFile(filePath: String)(implicit timeout: Timeout): Future[WordCountOperationID]
  // Get a progress report on the word counts for a single word count operation
  def progressReport(operationID: WordCountOperationID)(implicit timeout: Timeout): Future[ProgressReport]
  // Get a progress report on word counts for a specific word for a single word count operation
  def progressReport(operationID: WordCountOperationID, word: String)(implicit timeout: Timeout): Future[WordProgressReport]
  // Await the final results of a word count for a single word count operation
  def awaitResult(operationID: WordCountOperationID)(implicit timeout: Timeout): Future[ResultReport]
  // Await the final results of a word count for a specific word count for a single word count operation
  def awaitResult(operationID: WordCountOperationID, word: String)(implicit timeout: Timeout): Future[WordResultReport]
  // Shutdown the WordCount system, when the future completes the WordCount system is shutdown
  def shutDown(): Future[Boolean]
}

object WordCount {
  def apply(): WordCount = new WordCountImpl()
}

// Represents a unique identifier for a word count operation
final case class WordCountOperationID(id: UUID)
// Represents a report on counts so far
final case class ProgressReport(operationID: WordCountOperationID, counts: Map[String, Int])
// Represents a report on the count so far for a given word
case class WordProgressReport(operationID: WordCountOperationID, word: String, count: Int)
// Represents a report on final counts
final case class ResultReport(operationID: WordCountOperationID, counts: Map[String, Int])
// Represents a report on the final count for a given word
final case class WordResultReport(operationID: WordCountOperationID, word: String, count: Int)

class WordCountImpl extends WordCount {

  private val system = ActorSystem("Word-count")
  import WordCountMaster._
  private val master = system.actorOf(props(), "word-count-master")


  override def submitFile(filePath: String)(implicit timeout: Timeout): Future[WordCountOperationID] =
    (master ? SubmitFileCommand(filePath)).mapTo[WordCountOperationID]

  override def progressReport(operationID: WordCountOperationID)(implicit timeout: Timeout): Future[ProgressReport] =
    (master ? ProgressReportQuery(operationID)).mapTo[ProgressReport]

  override def progressReport(operationID: WordCountOperationID, word: String)(implicit timeout: Timeout): Future[WordProgressReport] =
    (master ? WordProgressReportQuery(operationID, word)).mapTo[WordProgressReport]

  override def awaitResult(operationID: WordCountOperationID)(implicit timeout: Timeout): Future[ResultReport] =
    (master ? ResultQuery(operationID)).mapTo[ResultReport]

  override def awaitResult(operationID: WordCountOperationID, word: String)(implicit timeout: Timeout): Future[WordResultReport] =
    (master ? WordResultQuery(operationID, word)).mapTo[WordResultReport]

  override def shutDown(): Future[Boolean] = {
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher
    system.terminate().map(_ => true)
  }
}