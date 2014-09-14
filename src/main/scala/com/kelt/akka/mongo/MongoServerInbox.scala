package com.kelt.akka.mongo

import com.kelt.akka.base.ServerInbox
import com.kelt.akka.base.Message
import com.mongodb.casbah.commons.MongoDBObject
import com.kelt.akka.base.ByteSerializer
import scala.collection.mutable.ListBuffer
import com.mongodb.WriteConcern
import scala.concurrent._
import ExecutionContext.Implicits.global


object MongoServerInbox extends ServerInbox {

  private lazy val maxPoll = 250
  private lazy val bufferSize = 50
  private val buffer = ListBuffer[Message]()

  private var f:Future[Unit] = null
  private var lastPoll = 0L

  def queue(msg: Message) {
    future {
      MessageDAO.insert(msg, WriteConcern.UNACKNOWLEDGED)
    }
  }

  def dequeue(queue: String): Option[Message] = {
    if(System.currentTimeMillis - lastPoll >= maxPoll 
       && buffer.length < bufferSize / 2 
       && (f == null || f.isCompleted)
     ) {
      f = future {
        def addItems() {
          val msg = MessageDAO.dequeue(queue)
          if(msg != None){
          StatDAO.incrementCount(queue, "dequeued")
            buffer.append(msg.get)
            if(buffer.length < bufferSize) {
              addItems()
            }
          }
        }
        addItems()
      } 
      f onComplete {
        case _ => lastPoll = System.currentTimeMillis
      }
      
    }
    if(!buffer.isEmpty) Some(buffer.remove(0)) else None
  }
  
  def incrementDeadMessageCount(queue: String) {
    future {
      StatDAO.incrementCount(queue, "dead")
    }  
  }

  def serialize(x: Any) : Array[Byte] = ByteSerializer.serialize(x)

  def deserialize(x: Array[Byte]) : Any = ByteSerializer.deserialize(x)

}
