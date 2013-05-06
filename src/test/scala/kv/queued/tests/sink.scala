package kv.queued

import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FlatSpec
import akka.testkit.TestKit
import akka.testkit.TestFSMRef
import org.mockito.Mockito._
import org.mockito.Matchers._
import akka.testkit.TestProbe
import akka.actor.ActorSystem
import akka.actor.Props
import com.typesafe.config.ConfigFactory
import org.scalatest.BeforeAndAfterAll

case class SinkMessage(msg: String)
case class UnsupportedSinkMessage(msg: String)

class QueueSinkTest(_system: ActorSystem) extends TestKit(_system) with FlatSpec with ShouldMatchers with BeforeAndAfterEach with BeforeAndAfterAll {

  def this() {
    this(ActorSystem("test", ConfigFactory.load("test")))
  }


  override def afterAll {
    system.shutdown()
  }

  behavior of "The Queue Sink"

  it should "persist messages of the correct type" in {
  	val queue = spy(new MemoryQueue[SinkMessage]())
  	val sink = system.actorOf(Props(new QueueSink(queue)))
  	val msg = SinkMessage("test")
  	sink ! msg
  	verify(queue).enqueue(msg)
  }

  it should "not persist message of unsupported type" in {
  	val queue = spy(new MemoryQueue[SinkMessage]())
  	val sink = system.actorOf(Props(new QueueSink(queue)))
  	val msg = UnsupportedSinkMessage("test")
  	sink ! msg
  	verify(queue, never()).enqueue(anyObject())
  }
  
}