package com.kelt.akka.mongo

import com.kelt.akka.base.ClientInbox
import com.kelt.akka.base.Message
import scala.concurrent.Promise
import com.kelt.akka.base.ByteSerializer
import com.mongodb.casbah.commons.MongoDBObject
import com.mongodb.WriteConcern
import scala.concurrent._
import ExecutionContext.Implicits.global

object MongoClientInbox extends ClientInbox {
  
  lazy val maxPoll = 500
  private var lastPoll = 0L
  
  var promiseTable: scala.collection.mutable.Map[String, Promise[Any]] = scala.collection.mutable.Map[String, Promise[Any]]()
   
  def queue(msg: Message) {
    addMessageToServerInbox(msg)
  }
   
  def dequeue()  {
    if(!promiseTable.isEmpty && System.currentTimeMillis() - lastPoll >= maxPoll) {
      val itemsToDeliver = MessageDAO.dequeueByCorrelationIds(promiseTable.keys.toList)
      itemsToDeliver.map(a => {
        promiseTable.remove(a.correlationId) match {
          case Some(promise) => {
            a.error.length != 0 match {
              case true => { /*Nothing to do, request should just timeout, maybe log the error*/  }
              case _ => { 
                promise.success(deserialize(a.messageBody))
              }
            }
          }
          case _ => { /*Promise already handled, no action required*/ }
        }
      })
      lastPoll = System.currentTimeMillis
    }
  }
  
  def queue(msg: Message, promise: Promise[Any]) {
    addMessageToServerInbox(msg)
    promiseTable(msg.correlationId) = promise
  }
  
  def addMessageToServerInbox(msg: Message) {
    future {
      MessageDAO.insert(msg, WriteConcern.UNACKNOWLEDGED)
      StatDAO.incrementCount(msg.destinationQueue.get, "queued")
    } onSuccess {
      case _ => { /*Successfully queued message*/ }
    }
  }
  
  def findRecentStats(queue:String, secondsAgo: Long) = {
    StatDAO.findRecentStats(queue, secondsAgo)
  }
    
  def serialize(x: Any) : Array[Byte] = ByteSerializer.serialize(x)
  
  def deserialize(x: Array[Byte]) : Any = ByteSerializer.deserialize(x)
    
}
