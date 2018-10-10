package com.tomogle.akkawordcount

import akka.util.Timeout

import scala.concurrent.Await
import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.Future
import scala.concurrent.duration._

/**
  * An example app
  * Provide a filepath to the
  */
object ExampleApp {

  implicit val timeout: Timeout = Timeout(5 seconds)
  implicit val ec: ExecutionContextExecutor = ExecutionContext.global

  def main(args: Array[String]): Unit = {
    val filePath = args(0)
    val wordCount = WordCount()
    try {
      val operationID: Future[WordCountOperationID] = wordCount.submitFile(filePath)
      val results: Future[ResultReport] = operationID.flatMap(id => wordCount.awaitResult(id))
      val resultReport: ResultReport = Await.result(results, 5 seconds)
      for (entry <- resultReport.counts) {
        val (key, value) = entry
        println(s"$key: $value")
      }
    } finally Await.result(wordCount.shutDown(), 5 seconds)
  }

}
