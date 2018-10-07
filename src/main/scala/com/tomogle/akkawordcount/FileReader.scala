package com.tomogle.akkawordcount

import akka.actor.Actor
import akka.actor.ActorLogging
import akka.actor.ActorRef
import akka.actor.Props
import com.tomogle.akkawordcount.LineWordCounter.CountWordsInLine

/**
  *
  */
object FileReader {
  def props(lineWordCounterActor: ActorRef): Props = Props(new FileReader(lineWordCounterActor))
  final case class ReadFile(filePath: String)
}

class FileReader(lineWordCounterActor: ActorRef) extends Actor with ActorLogging {
  import FileReader._

  override def receive: Receive = {
    case ReadFile(filePath) =>
      val source = io.Source.fromFile(filePath)
      for (line <- source.getLines()) {
        lineWordCounterActor ! CountWordsInLine(line)
      }
  }
}
