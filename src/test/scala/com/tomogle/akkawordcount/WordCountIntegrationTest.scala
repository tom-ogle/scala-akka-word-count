package com.tomogle.akkawordcount

import akka.util.Timeout
import org.scalatest.FlatSpec
import org.scalatest.Matchers
import org.scalatest.concurrent.ScalaFutures

import scala.concurrent.Await
import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._

class WordCountIntegrationTest() extends FlatSpec with Matchers with ScalaFutures {

  val timeoutDuration = 3 seconds
  implicit val timeout: Timeout = Timeout(timeoutDuration)
  implicit val ec: ExecutionContextExecutor = ExecutionContext.global

  "WordCount" should "run end to end" in {
      val wordCountAPI = WordCount()
      try {
        val filePath = getClass.getClassLoader.getResource("simpletestfile.txt").getPath

        val operationID = wordCountAPI.submitFile(filePath)
        val wordProgressReport1 = operationID.flatMap(wordCountAPI.progressReport(_, "the"))
        val wordProgressReport2 = operationID.flatMap(wordCountAPI.progressReport(_, "over"))
        val progressReport = operationID.flatMap(wordCountAPI.progressReport(_))
        val wordResultReport1 = operationID.flatMap(wordCountAPI.result(_, "the"))
        val wordResultReport2 = operationID.flatMap(wordCountAPI.result(_, "fox"))
        val resultReport = operationID.flatMap(wordCountAPI.result(_))


        val uuid = Await.result(operationID, timeoutDuration)

        Await.result(wordProgressReport1, timeoutDuration) match {
          case WordProgressReport(id, "the", _) =>
            assert(id == uuid)
          case _ => fail("Incorrect WordProgressReport")
        }

        Await.result(wordProgressReport2, timeoutDuration) match {
          case WordProgressReport(id, "over", _) =>
            assert(id == uuid)
          case _ => fail("Incorrect WordProgressReport")
        }

        Await.result(progressReport, timeoutDuration) match {
          case ProgressReport(id, _) =>
            assert(id == uuid)
          case _ => fail("Incorrect ProgressReport")
        }

        Await.result(wordResultReport1, timeoutDuration) match {
          case WordResultReport(id, "the", count) =>
            assert(id == uuid)
            assert(count == 2)
          case _ => fail("Incorrect WordResultReport")
        }

        Await.result(wordResultReport2, timeoutDuration) match {
          case WordResultReport(id, "fox", count) =>
            assert(id == uuid)
            assert(count == 1)
          case _ => fail("Incorrect WordResultReport")
        }

        Await.result(resultReport, timeoutDuration) match {
          case ResultReport(id, counts) =>
            assert(id == uuid)
            assert(counts("the") == 2)
            assert(counts("quick") == 1)
            assert(counts("brown") == 1)
            assert(counts("fox") == 1)
            assert(counts("jumps") == 1)
            assert(counts("over") == 1)
            assert(counts("lazy") == 1)
            assert(counts("dog") == 1)
            assert(counts.keys.size == 8)
          case _ => fail("Incorrect ResultReport")
        }
      } finally Await.ready(wordCountAPI.shutDown(), timeoutDuration)
  }
}
