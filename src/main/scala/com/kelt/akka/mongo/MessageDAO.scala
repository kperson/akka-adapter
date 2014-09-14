package com.kelt.akka.mongo

import com.novus.salat._
import com.novus.salat.global._
import com.novus.salat.dao.SalatDAO
import org.bson.types.ObjectId
import com.kelt.akka.base.Message
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.casbah.MongoCursorBase
import com.mongodb.casbah.Imports._
import scala.concurrent.duration._
import scala.util.Random
import scala.concurrent.ExecutionContext
import scala.concurrent._
import ExecutionContext.Implicits.global


object MessageDAO extends SalatDAO[Message, ObjectId](collection = Connection.db.get("akka_queue")) {
  
  collection.ensureIndex(MongoDBObject("destinationQueue" -> 1), MongoDBObject("unique" -> false)) 
  
  collection.ensureIndex(MongoDBObject("correlationId" -> 1), MongoDBObject("unique" -> false)) 

  def dequeue(queue: String) = {
    collection.findAndModify(MongoDBObject("destinationQueue" -> queue), null, MongoDBObject("createdAt" -> 1), true, null, true, false).map(dbo => {
    _grater.asObject(dbo)
  })}
   
  private def markForDequeue(ids: List[String], processorId: String) {
    val query = MongoDBObject("processorId" -> MongoDBObject("$exists" -> false), "clientSent" -> false, "correlationId" -> MongoDBObject("$in" -> MongoDBList(ids.map(a => a):_*)))
    collection.update(query, MongoDBObject("$set" -> MongoDBObject("processorId" -> processorId)), false, true, WriteConcern.Acknowledged)
  }
  
  def dequeueByCorrelationIds(ids: List[String]) = {
    val processorId = Random.alphanumeric.take(100).mkString
    markForDequeue(ids, processorId)
    val messages = find(MongoDBObject("processorId" -> processorId)).buffered.toList
    if(!messages.isEmpty) {
      future {
        remove(MongoDBObject("processorId" -> processorId))
      }
    }
    messages
   }
   
}