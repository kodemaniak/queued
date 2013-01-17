package kv.queued

import scala.reflect.runtime.universe._
import scala.reflect._

trait PersistentQueue[M] {
  def enqueue(elem: M)
  def dequeue: Option[M]
  def length: Int
}
