package kv.damsimpl.tests.boot

import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FlatSpec
import akka.testkit.TestKit
import akka.testkit.TestFSMRef
import org.mockito.Mockito._
import akka.testkit.TestProbe
import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import scala.collection.mutable.Queue
import org.scalatest.BeforeAndAfterAll
import akka.event.LoggingReceive
import akka.actor.Actor
import akka.testkit.TestActor
import akka.actor.ActorRef
import akka.util.duration._
import akka.actor.ActorContext
import akka.actor.Props
import kv.queued._

class QueuePollingTest(_system: ActorSystem) extends TestKit(_system) with FlatSpec with ShouldMatchers with BeforeAndAfterEach with BeforeAndAfterAll {

  def this() {
    this(ActorSystem("test", ConfigFactory.load("test")))
  }

  override def afterAll {
    system.shutdown()
  }
  
  case class TestMessage(msg: String)

  behavior of "The Queue Polling Actor"

  it should "start in waiting" in {
    val mockQueue = new MemoryQueue[TestMessage]()
    val probe = new TestProbe(system)
    val q = TestFSMRef(new QueuePollingActor(mockQueue, context => probe.ref))
    q.stateName should be(Polling)
    q.stateData should be(None)
  }

  it should "stay in waiting when no job available" in {
    val mockQueue = new MemoryQueue[TestMessage]()
    val probe = new TestProbe(system)
    val q = TestFSMRef(new QueuePollingActor(mockQueue, context => probe.ref))
    Thread.sleep(2000)
    q.stateName should be(Polling)
    q.stateData should be(None)
  }

  it should "go to working when job available and return to waiting when job finished" in {
    val mockQueue = new MemoryQueue[TestMessage]()
    val msg = mock(classOf[TestMessage])
    mockQueue.enqueue(msg)
    val probe = TestProbe()
    val q = TestFSMRef(new QueuePollingActor(mockQueue, context => probe.ref))
    within(2 seconds) {
      probe.expectMsg(msg)
      Thread.sleep(250)
      q.stateName should be(AwaitingAck)
      q.stateData should be(Some(probe.ref, msg))
	    probe.sender ! Acknowledge
	    q.stateName should be(Working)
	    q.stateData should be(Some(probe.ref, msg))
    }
    mockQueue.length should be(0)
    probe.sender ! WorkFinished
    q.stateName should be(Polling)
    q.stateData should be(None)
  }

  it should "go to stoppped when polling is stopped" in {
    val mockQueue = new MemoryQueue[TestMessage]()
    val probe = TestProbe()
    val q = TestFSMRef(new QueuePollingActor(mockQueue, context => probe.ref))
    q ! StopPolling
    q.stateName should be(Stopped)
    q.stateData should be(None)
    val r = TestFSMRef(new QueuePollingActor(mockQueue, context => probe.ref))
    val msg = mock(classOf[TestMessage])
    mockQueue.enqueue(msg)
    within(2 seconds) {
      probe.expectMsg(msg)
    }
    probe.sender ! Acknowledge
    r ! StopPolling
    r.stateName should be(Stopped)
    r.stateData should be(Some(probe.ref, msg))
    probe.sender ! WorkFinished
    r.stateName should be(Stopped)
    r.stateData should be(None)
    val s = TestFSMRef(new QueuePollingActor(mockQueue, context => probe.ref))
    mockQueue.enqueue(msg)
    within(2 seconds) {
      probe.expectMsg(msg)
    }
    s ! StopPolling
    s.stateName should be(Stopped)
    s.stateData should be(None)
    mockQueue.length should be (1)
  }

  it should "go to stopped when the worker does not handle jobs" in {
    val mockQueue = new MemoryQueue[TestMessage]()
    val msg = mock(classOf[TestMessage])
    mockQueue.enqueue(msg)
    val probe = TestProbe()
    val q = TestFSMRef(new QueuePollingActor(mockQueue, (context => probe.ref), 0 seconds, 10 seconds))
    q ! Poll
    probe.expectMsg(msg)
    Thread.sleep(10000)
    q.stateName should be(Stopped)
    q.stateData should be(None)
    mockQueue.length should be(1)
  }

  it should "handle worker errors gracefully" in {
    val mockQueue = new MemoryQueue[TestMessage]()
    val msg = mock(classOf[TestMessage])
    mockQueue.enqueue(msg)
    val q = TestFSMRef(new QueuePollingActor(mockQueue, context => context.actorOf(Props(new Actor {
      def receive = LoggingReceive {
        case u: TestMessage =>
          throw new Exception
      }
    }))))
    Thread.sleep(1000)
    q.stateName should be(Polling)
    mockQueue.length should be(1)
  }
}

class MemoryQueue[M <: AnyRef] extends PersistentQueue[M] {
  val queue = Queue[M]()

  def enqueue(elem: M)(implicit manifest: Manifest[M]) {
    queue.enqueue(elem)
  }

  def dequeue(implicit manifest: Manifest[M]) = queue.dequeueFirst(_ => true)

  def length = queue.length
}