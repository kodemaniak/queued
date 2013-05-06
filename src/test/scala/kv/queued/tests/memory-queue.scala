package kv.queued

import scala.collection.mutable.Queue

class MemoryQueue[M <: AnyRef] extends PersistentQueue[M] {
  val queue = Queue[M]()

  def enqueue(elem: M) {
    queue.enqueue(elem)
  }

  def dequeue = queue.dequeueFirst(_ => true)

  def length = queue.length
}
