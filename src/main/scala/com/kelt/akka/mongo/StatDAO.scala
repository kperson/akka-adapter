package com.kelt.akka.mongo


import com.novus.salat._
import com.novus.salat.global._
import com.novus.salat.dao.SalatDAO
import org.bson.types.ObjectId
import com.kelt.akka.base.Stat
import com.mongodb.WriteConcern
import com.mongodb.casbah.commons.MongoDBObject


object StatDAO extends SalatDAO[Stat, ObjectId](collection = Connection.db.get("akka_stat")) {

  collection.ensureIndex(MongoDBObject("queue" -> 1, "minute" -> -1), MongoDBObject("unique" -> true)) 
  
  def incrementCount(queue: String, tag: String) {
    val minute = (System.currentTimeMillis/1000L) / 60
    this.update(MongoDBObject("minute" -> minute, "queue" -> queue), MongoDBObject("$inc" -> MongoDBObject(tag -> 1)), true, false, WriteConcern.UNACKNOWLEDGED)
  }
  
  def findRecentStats(queue: String, secondsAgo: Long) = {
    val minute = ((System.currentTimeMillis/1000L  - secondsAgo)) / 60
    findOne(MongoDBObject("minute" -> minute, "queue" -> queue)) match {
      case Some(x) => x
      case _ => {
        val alternativeStat = find(MongoDBObject("minute" -> MongoDBObject("$lt" -> minute) , "queue" -> queue))
         .sort(MongoDBObject("minute" -> -1))
         .limit(1)
         .buffered.toList.headOption
        alternativeStat match {
          case Some(z) => z
          case _ => Stat()
        }
      }
    }
  }  
}