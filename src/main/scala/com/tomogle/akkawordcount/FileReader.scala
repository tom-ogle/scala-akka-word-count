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
  def props(lineWordCounterActorPath: String): Props = Props(new FileReader(lineWordCounterActorPath))
  final case class ReadFile(filePath: String)
}

class FileReader(lineWordCounterActorPath: String) extends Actor with ActorLogging {
  import FileReader._

  private val lineWordCounterActor = context.actorSelection(lineWordCounterActorPath)

  override def receive: Receive = {
    case ReadFile(filePath) =>
      val source = io.Source.fromFile(filePath)
      for (line <- source.getLines()) {
        lineWordCounterActor ! CountWordsInLine(line)
      }
  }
}
