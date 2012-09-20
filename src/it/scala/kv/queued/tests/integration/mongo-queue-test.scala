package kv.queued.tests.integration

import org.scalatest.FlatSpec
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.ShouldMatchers
import com.mongodb.casbah.MongoCollection
import com.mongodb.casbah.commons.MongoDBObject
import kv.queued.MongoQueue
import com.mongodb.casbah.MongoDB
import com.mongodb.casbah.MongoConnection
import java.util.UUID

case class ExampleMessage(name: String, value: Int, properties: Map[String, String])

class MongoQueueIntegrationTest extends FlatSpec with BeforeAndAfterEach with ShouldMatchers {

  var db: MongoDB = _
  var collection: MongoCollection = _

  override def beforeEach {
    db = MongoConnection()(UUID.randomUUID.toString)
    collection = db(UUID.randomUUID.toString)
  }
  
  override def afterEach {
    db.dropDatabase()
  }
  
  behavior of "mongo queue"

  it should "enqueue new messages" in {
    val mongoQueue = new MongoQueue[ExampleMessage](collection)
    val m1 = ExampleMessage("test", 5, Map("key1" -> "value1", "key2" -> "value2"))
    val m2 = ExampleMessage("test", 15, Map("key1" -> "eek"))
    mongoQueue.length should equal(0)
    mongoQueue.enqueue(m1)
    mongoQueue.length should equal (1)
    mongoQueue.enqueue(m2)
    mongoQueue.length should equal (2)
    mongoQueue.dequeue should equal (Some(m1))
    mongoQueue.length should equal (1)
    mongoQueue.dequeue should equal (Some(m2))
    mongoQueue.length should equal(0)
    mongoQueue.dequeue should equal (None)
  }
    
}
