package kv.queued

import akka.actor.ActorContext
import akka.actor.ActorRef
import akka.util.Duration
import akka.util.duration._
import akka.actor.Actor
import akka.actor.FSM
import akka.actor.OneForOneStrategy
import akka.util.Timeout
import akka.actor.SupervisorStrategy._
import akka.actor.Terminated
import akka.actor.LoggingFSM

sealed trait PollingMessages
case object Poll extends PollingMessages
case object StopPolling extends PollingMessages
case object WorkFinished extends PollingMessages
case object Acknowledge extends PollingMessages
case object RequeueJob extends PollingMessages
sealed trait PollingState
case object Polling extends PollingState
case object AwaitingAck extends PollingState
case object Working extends PollingState
case object Stopped extends PollingState

class QueuePollingActor[M <: AnyRef](queue: PersistentQueue[M], workerBuilder: ActorContext => ActorRef, initialDelay: Duration = 0 seconds, frequency: Duration = 1 second)(implicit manifest: Manifest[M]) extends Actor with FSM[PollingState, Option[M]] {

  var worker: ActorRef = _

  override val supervisorStrategy = OneForOneStrategy() {
    case e: Exception =>
      log.error("Requeueing job.")
      log.error(e.getMessage)
      self ! RequeueJob
      Restart
  }
  
  override def preStart {
    worker = workerBuilder(context)
  }

  startWith(Polling, None)
  setTimer("polling", Poll, frequency, true)

  import akka.pattern.ask
  import context.dispatcher
  implicit val timeout = Timeout(1 second)

  when(Polling) {
    case Event(Poll, _) =>
      if (workStarting()) {
        queue.dequeue match {
          case Some(job) =>
            worker ! job
            goto(AwaitingAck) using (Some(job))
          case None =>
            workFinished()
            stay
        }
      } else {
        goto(Stopped)
      }
    case Event(StopPolling, _) =>
      goto(Stopped)
  }

  when(AwaitingAck, stateTimeout = 1 second) {
    case Event(Acknowledge, Some(job)) =>
      goto(Working)
    case Event(FSM.StateTimeout, Some(job)) =>
      queue.enqueue(job)
      goto(Stopped) using (None)
    case Event(StopPolling, Some(job)) =>
      queue.enqueue(job)
      goto(Stopped) using (None)
    case Event(RequeueJob, Some(job)) =>
      queue.enqueue(job)
      goto(Polling) using (None)
  }

  when(Working) {
    case Event(StopPolling, _) => goto(Stopped)
    case Event(WorkFinished, _) =>
      if (workFinished()) {
        goto(Polling) using (None)
      } else {
        goto(Stopped) using (None)
      }
    case Event(Terminated(child), Some(job)) =>
      queue.enqueue(job)
      goto(Polling)
    case Event(RequeueJob, Some(job)) =>
      queue.enqueue(job)
      goto(Polling) using (None)
  }

  when(Stopped) {
    case Event(WorkFinished, _) => stay using (None)
  }

  whenUnhandled {
    case Event(Poll, _) => stay
  }

  onTransition {
    case _ -> Stopped => log.debug("stopping queue polling")
  }

  def workStarting(): Boolean = true

  def workFinished(): Boolean = true
}
