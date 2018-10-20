package com.tomogle.akkawordcount.internal

import java.util.UUID

import akka.actor.ActorSystem
import akka.testkit.TestActorRef
import akka.testkit.TestKit
import akka.testkit.TestProbe
import com.tomogle.akkawordcount.WordCountOperationID
import com.tomogle.akkawordcount.WordProgressReport
import com.tomogle.akkawordcount.internal.WordCountReducer.ReduceWordCommand
import com.tomogle.akkawordcount.internal.WordCountReducer.WordCountWordProgressQuery
import org.scalatest.Matchers
import org.scalatest.WordSpecLike


class WordCountReducerTest(_system: ActorSystem) extends TestKit(_system) with WordSpecLike with Matchers {

  def this() = this(ActorSystem())

  // TODO: Further tests

  "WordCountReducer" should {
    "give correct progress reports" in {
      val actorRef = TestActorRef(new WordCountReducer())
      val operationId1 = WordCountOperationID(UUID.randomUUID())
      val operationId2 = WordCountOperationID(UUID.randomUUID())
      actorRef ! ReduceWordCommand(operationId1, "hello")
      actorRef ! ReduceWordCommand(operationId1, "world")
      actorRef ! ReduceWordCommand(operationId1, "hello")
      actorRef ! ReduceWordCommand(operationId1, "hello")

      actorRef ! ReduceWordCommand(operationId2, "hello")
      actorRef ! ReduceWordCommand(operationId2, "a")
      actorRef ! ReduceWordCommand(operationId2, "b")
      actorRef ! ReduceWordCommand(operationId2, "b")

      val testProbe1 = TestProbe()
      actorRef ! WordCountWordProgressQuery(operationId1, "hello", testProbe1.ref)
      actorRef ! WordCountWordProgressQuery(operationId1, "world", testProbe1.ref)
      actorRef ! WordCountWordProgressQuery(operationId1, "dontexist", testProbe1.ref)

      val testProbe2 = TestProbe()
      actorRef ! WordCountWordProgressQuery(operationId2, "hello", testProbe2.ref)
      actorRef ! WordCountWordProgressQuery(operationId2, "a", testProbe2.ref)
      actorRef ! WordCountWordProgressQuery(operationId2, "b", testProbe2.ref)

      testProbe1.expectMsg(WordProgressReport(operationId1, "hello", 3))
      testProbe1.expectMsg(WordProgressReport(operationId1, "world", 1))
      testProbe1.expectMsg(WordProgressReport(operationId1, "dontexist", 0))

      testProbe2.expectMsg(WordProgressReport(operationId2, "hello", 1))
      testProbe2.expectMsg(WordProgressReport(operationId2, "a", 1))
      testProbe2.expectMsg(WordProgressReport(operationId2, "b", 2))
    }
  }
}
