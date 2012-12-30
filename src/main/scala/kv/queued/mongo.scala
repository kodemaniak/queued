package kv.queued

import com.novus.salat._
import com.novus.salat.global._
import com.novus.salat.annotations._
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.conversions.scala.RegisterConversionHelpers
import com.mongodb.casbah.commons.conversions.scala.RegisterJodaTimeConversionHelpers
import scala.reflect.runtime.universe._
import scala.reflect._

class MongoQueue[M <: AnyRef](collection: MongoCollection) extends PersistentQueue[M] {
  RegisterConversionHelpers()
  RegisterJodaTimeConversionHelpers()
    
  def enqueue(elem: M)(implicit ttag: TypeTag[M], ctag: ClassTag[M]) {
    val dbo = grater[M].asDBObject(elem)
    collection.insert(MongoDBObject("payload" -> dbo))
  }
  
  def dequeue(implicit ttag: TypeTag[M], ctag: ClassTag[M]): Option[M] = for {
    dbo <- collection.findAndModify(
        MongoDBObject(), 
        MongoDBObject("payload" -> 1), 
        MongoDBObject("_id" -> 1), 
        true, 
        MongoDBObject(), 
        false, 
        false)
    payload <- dbo.getAs[DBObject]("payload")
  } yield {
    grater[M].asObject(payload)
  }
  
  def length: Int = {
    collection.size
  }
  
}