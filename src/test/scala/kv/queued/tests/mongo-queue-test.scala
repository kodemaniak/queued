package kv.queued.tests

import org.scalatest.FlatSpec
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.ShouldMatchers
import org.mockito.Mockito._
import com.novus.salat._
import com.novus.salat.global._
import com.mongodb.casbah.MongoCollection
import com.mongodb.casbah.commons.MongoDBObject
import kv.queued.MongoQueue

case class ExampleMessage(name: String, value: Int, properties: Map[String, String])

class MongoQueueTest extends FlatSpec with BeforeAndAfterEach with ShouldMatchers {

  var collection: MongoCollection = _

  override def beforeEach {
    collection = mock(classOf[MongoCollection])
  }
  
  behavior of "mongo queue"

  it should "enqueue new messages" in {
    val mongoQueue = new MongoQueue[ExampleMessage](collection)
    val m1 = ExampleMessage("test", 5, Map("key1" -> "value1", "key2" -> "value2"))
    val m1dbo = grater[ExampleMessage].asDBObject(m1)
    mongoQueue.enqueue(m1)
    verify(collection).insert(MongoDBObject("payload" -> m1dbo))
  }
  
  it should "dequeue new messages" in {
    val mongoQueue = new MongoQueue[ExampleMessage](collection)
    val m1 = ExampleMessage("test", 5, Map("key1" -> "value1", "key2" -> "value2"))
    val m1dbo = grater[ExampleMessage].asDBObject(m1)
    when(collection.findAndModify(
        MongoDBObject(), 
        MongoDBObject("payload" -> 1), 
        MongoDBObject("_id" -> 1), 
        true, 
        MongoDBObject(), 
        false, 
        false)).thenReturn(Some(MongoDBObject("_id" -> "123", "payload" -> m1dbo)))
    mongoQueue.dequeue should equal (Some(m1))
    when(collection.findAndModify(
        MongoDBObject(), 
        MongoDBObject("payload" -> 1), 
        MongoDBObject("_id" -> 1), 
        true, 
        MongoDBObject(), 
        false, 
        false)).thenReturn(None)
    mongoQueue.dequeue should equal (None)
  }
  
}
