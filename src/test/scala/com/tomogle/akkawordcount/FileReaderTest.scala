package com.tomogle.akkawordcount

import akka.actor.ActorSystem
import akka.testkit.TestActorRef
import akka.testkit.TestKit
import com.tomogle.akkawordcount.FileReader.ReadFile
import com.tomogle.akkawordcount.LineWordCounter.CountWordsInLine
import org.scalatest.Matchers
import org.scalatest.WordSpecLike


class FileReaderTest(_system: ActorSystem) extends TestKit(_system) with WordSpecLike with Matchers {

  def this() = this(ActorSystem())

  "FileReader" should {
    "request all lines in a file be counted" in {
      val filePath = getClass.getClassLoader.getResource("simpletestfile.txt").getPath
      val actorRef = TestActorRef(new FileReader(testActor))
      actorRef ! ReadFile(filePath)
      expectMsg(CountWordsInLine("The quick brown fox"))
      expectMsg(CountWordsInLine("jumps over the lazy dog"))
    }
  }
}
