package kv.queued

import scala.reflect.runtime.universe._
import scala.reflect._

trait PersistentQueue[M] {
  def enqueue(elem: M)(implicit ttag: TypeTag[M], ctag: ClassTag[M])
  def dequeue(implicit ttag: TypeTag[M], ctag: ClassTag[M]): Option[M]
  def length: Int
}
