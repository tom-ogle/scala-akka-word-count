package com.tomogle.akkawordcount

import java.util.UUID

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import com.tomogle.akkawordcount.ResultWaiter.WaitForOperationCommand
import com.tomogle.akkawordcount.ResultWaiter.WaitForWordCommand
import com.tomogle.akkawordcount.WordCountReducer.WordCountAllProgressQuery
import com.tomogle.akkawordcount.WordCountReducer.WordCountWordProgressQuery

import scala.collection.mutable

/**
  * The control node in the WordCount actor system
  */
object WordCountMaster {
  def props(): Props = Props(new WordCountMaster())

  // Submit a file for word counting
  final case class SubmitFileCommand(filePath: String)
  // Cleans up resources in the system which are no longer needed
  final case class CleanupOperationCommand(operationId: WordCountOperationID)

  // Request a progress report for a particular operation and word
  final case class WordProgressReportQuery(operationID: WordCountOperationID, word: String)
  // Request a progress report for a particular operation
  final case class ProgressReportQuery(operationID: WordCountOperationID)

  // Request the result for a particular operation and word
  final case class WordResultQuery(operationID: WordCountOperationID, word: String)
  // Request the result for a particular operation
  final case class ResultQuery(operationID: WordCountOperationID)
}

class WordCountMaster() extends Actor with ActorLogging {
  import com.tomogle.akkawordcount.WordCountMaster._



  private val mappers = mutable.Map[WordCountOperationID, ActorRef]()
  private val reducers = mutable.Map[WordCountOperationID, ActorRef]()

  override def receive: Receive = {
    case SubmitFileCommand(filePath) =>
      import com.tomogle.akkawordcount.FileReaderWordCountMapper.ReadWordsFromFileCommand
      val operationID = WordCountOperationID(UUID.randomUUID())
      // TODO: Distribute work over more than one reducer, route using hash of word / ID?
      val reducer = context.actorOf(WordCountReducer.props())
      // TODO: Distribute work over multiple mappers
      val mapper = context.actorOf(FileReaderWordCountMapper.props(reducer))
      reducers(operationID) = reducer
      mappers(operationID) = mapper
      mapper ! ReadWordsFromFileCommand(operationID, filePath)
      sender() ! operationID

    case WordProgressReportQuery(operationID, word) =>
      val reducer = reducers(operationID)
      reducer ! WordCountWordProgressQuery(operationID, word, sender())

    case ProgressReportQuery(operationID) =>
      val reducer = reducers(operationID)
      reducer ! WordCountAllProgressQuery(operationID, sender())

    case WordResultQuery(operationID, word) =>
      val reducer = reducers(operationID)
      val resultWaiter = context.actorOf(ResultWaiter.props(), s"wait-${operationID.id}")
      resultWaiter ! WaitForWordCommand(operationID, word, sender(), reducer)

    case ResultQuery(operationID) =>
      val reducer = reducers(operationID)
      val resultWaiter = context.actorOf(ResultWaiter.props(), s"wait-${operationID.id}")
      resultWaiter ! WaitForOperationCommand(operationID, sender(), reducer)
    // TODO CleanupOperation to reclaim memory in system for completed operations
  }
}
