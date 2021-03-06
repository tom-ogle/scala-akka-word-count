package com.tomogle.akkawordcount.internal

import java.util.UUID

import akka.actor.ActorSystem
import akka.testkit.TestActorRef
import akka.testkit.TestKit
import com.tomogle.akkawordcount.WordCountOperationID
import com.tomogle.akkawordcount.internal.FullFileReaderMapper.ReadWordsFromFileCommand
import com.tomogle.akkawordcount.internal.WordCountReducer.ReduceWordCommand
import com.tomogle.akkawordcount.internal.WordCountReducer.SetTotalWordsCommand
import org.scalatest.Matchers
import org.scalatest.WordSpecLike


class FullFileReaderMapperTest(_system: ActorSystem) extends TestKit(_system) with WordSpecLike with Matchers {

  def this() = this(ActorSystem())

  // TODO: negative case testing, e.g. file does not exist

  "FileReaderWordCountMapper" should {
    "read all words in a file" in {
      val filePath = getClass.getClassLoader.getResource("simpletestfile.txt").getPath
      val actorRef = TestActorRef(new FullFileReaderMapper(testActor))
      val operationID = WordCountOperationID(UUID.randomUUID())
      actorRef ! ReadWordsFromFileCommand(operationID, filePath)

      expectMsg(ReduceWordCommand(operationID, "the"))
      expectMsg(ReduceWordCommand(operationID, "quick"))
      expectMsg(ReduceWordCommand(operationID, "brown"))
      expectMsg(ReduceWordCommand(operationID, "fox"))
      expectMsg(ReduceWordCommand(operationID, "jumps"))
      expectMsg(ReduceWordCommand(operationID, "over"))
      expectMsg(ReduceWordCommand(operationID, "the"))
      expectMsg(ReduceWordCommand(operationID, "lazy"))
      expectMsg(ReduceWordCommand(operationID, "dog"))

      expectMsg(SetTotalWordsCommand(operationID, 9))
    }
  }
}
