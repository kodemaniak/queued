package kv.queued

trait PersistentQueue[M] {
  def enqueue(elem: M)(implicit manifest: Manifest[M])
  def dequeue(implicit manifest: Manifest[M]): Option[M]
  def length: Int
}
