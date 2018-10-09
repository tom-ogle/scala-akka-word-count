package com.tomogle.akkawordcount

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import com.tomogle.akkawordcount.WordCountReducer.ResultsAllIfFinishedQuery
import com.tomogle.akkawordcount.WordCountReducer.ResultsWordIfFinishedQuery

import scala.collection.mutable
import scala.concurrent.duration._

/**
  * An actor that waits for completion of a given operation
  */
object ResultWaiter {
  def props(): Props = Props(new ResultWaiter())

  final case class WaitForWordCommand(operationID: WordCountOperationID, word: String, returnAddress: ActorRef, reducer: ActorRef)
  final case class WaitForOperationCommand(operationID: WordCountOperationID, returnAddress: ActorRef, reducer: ActorRef)
  final case object ProcessWaitingCommand
}

class ResultWaiter() extends Actor with ActorLogging {
  import com.tomogle.akkawordcount.ResultWaiter._

  private val waitingForWords = mutable.Set[(WordCountOperationID, String, ActorRef, ActorRef)]()
  private val waitingForOperations = mutable.Set[(WordCountOperationID, ActorRef, ActorRef)]()

  //   TODO: Make timings configurable
    implicit val executionContext = context.system.dispatcher
    context.system.scheduler.schedule(0 milliseconds, 50 milliseconds, self, ProcessWaitingCommand)

  override def receive: Receive = {
    case WaitForWordCommand(operationID, word, returnAddress, reducer) =>
      waitingForWords.add((operationID, word, returnAddress, reducer))

    case WaitForOperationCommand(operationID, returnAddress, reducer) =>
      waitingForOperations.add(operationID, returnAddress, reducer)

    case ProcessWaitingCommand =>
      for (entry <- waitingForOperations) {
        val (operationId, returnAddress, reducer) = entry
        reducer ! ResultsAllIfFinishedQuery(operationId, returnAddress)
      }
      for (entry <- waitingForWords) {
        val (operationId, word, returnAddress, reducer) = entry
        reducer ! ResultsWordIfFinishedQuery(operationId, word, returnAddress)
      }

  }
}