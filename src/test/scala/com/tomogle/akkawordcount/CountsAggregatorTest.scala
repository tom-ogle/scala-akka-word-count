package com.tomogle.akkawordcount

import akka.actor.ActorSystem
import akka.testkit.TestActorRef
import akka.testkit.TestKit
import org.scalatest.FlatSpecLike
import org.scalatest.Matchers

/**
  *
  */
class CountsAggregatorTest(_system: ActorSystem) extends TestKit(_system) with FlatSpecLike with Matchers {


  "CountsAggregator" should "aggregate counts" in {
    val actorRef = TestActorRef(new FileReader(testActor))
  }

}
