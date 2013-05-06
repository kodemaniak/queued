package kv.queued

import akka.actor.{Actor, ActorLogging}
import akka.event.LoggingReceive

class QueueSink[T](queue: PersistentQueue[T])(implicit manifest: Manifest[T]) extends Actor with ActorLogging {

	def receive = LoggingReceive {
		case m: T => queue.enqueue(m)
	}

}