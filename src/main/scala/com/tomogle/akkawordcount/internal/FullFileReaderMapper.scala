package com.tomogle.akkawordcount.internal

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import com.tomogle.akkawordcount.WordCountOperationID
import com.tomogle.akkawordcount.internal.WordCountReducer.ReduceWordCommand
import com.tomogle.akkawordcount.internal.WordCountReducer.SetTotalWordsCommand

/**
  * An actor that reads words from a file and sends them to a provided actor
  */
object FullFileReaderMapper {
  def props(wordConsumer: ActorRef): Props = Props(new FullFileReaderMapper(wordConsumer))
  final case class ReadWordsFromFileCommand(operationId: WordCountOperationID, filePath: String)
  private val WhitespaceRegEx = "\\s+"
}

class FullFileReaderMapper(consumer: ActorRef) extends Actor with ActorLogging {
  import FullFileReaderMapper._

  override def receive: Receive = {
    case ReadWordsFromFileCommand(operationId, filePath) =>
      val source = io.Source.fromFile(filePath)
      // TODO: Fix case of very long lines in very large files
      var countOfAllWords = 0
      for (line <- source.getLines()) {
        val words = line.split(WhitespaceRegEx)
        for (word <- words; lowercaseWord = word.toLowerCase()) {
          // We normalise all words to lowercase
          // TODO: Handle punctuation, etc.
          consumer ! ReduceWordCommand(operationId, lowercaseWord)
          countOfAllWords += 1
        }
      }
      consumer ! SetTotalWordsCommand(operationId, countOfAllWords)
  }
}
