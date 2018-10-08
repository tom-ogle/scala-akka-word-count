package com.tomogle.akkawordcount

import akka.actor.ActorSystem
import akka.testkit.TestActorRef
import akka.testkit.TestKit
import akka.testkit.TestProbe
import akka.util.Timeout
import com.tomogle.akkawordcount.CountsAggregator.CountReport
import com.tomogle.akkawordcount.CountsAggregator.LineResults
import com.tomogle.akkawordcount.CountsAggregator.ReportCount
import org.scalatest.FlatSpecLike
import org.scalatest.Matchers

import scala.concurrent.duration._

class CountsAggregatorTest(_system: ActorSystem) extends TestKit(_system) with FlatSpecLike with Matchers {

  def this() = this(ActorSystem())

  implicit val timeout: Timeout = 3 seconds

  "CountsAggregator" should "aggregate counts" in {
    val aggregatorActorRef = TestActorRef(CountsAggregator.props())
    aggregatorActorRef ! LineResults(Map("hello" -> 1, "world" -> 7))
    aggregatorActorRef ! LineResults(Map("hello" -> 2, "again" -> 1))

    val testProbe = TestProbe()

    aggregatorActorRef.tell(ReportCount("hello"), testProbe.ref)
    aggregatorActorRef.tell(ReportCount("world"), testProbe.ref)
    aggregatorActorRef.tell(ReportCount("again"), testProbe.ref)

    testProbe.expectMsg(500 millis, CountReport("hello", 3))
    testProbe.expectMsg(500 millis, CountReport("world", 7))
    testProbe.expectMsg(500 millis, CountReport("again", 1))
  }

}
