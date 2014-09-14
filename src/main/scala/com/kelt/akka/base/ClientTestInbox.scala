package com.kelt.akka.base

import scala.concurrent.Promise
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ByteArrayInputStream
import java.io.ObjectOutputStream

object ClientTestInbox extends ClientInbox {

  var promiseTable: scala.collection.mutable.Map[String, Promise[Any]] = scala.collection.mutable.Map[String, Promise[Any]]()
  
  def deliverToInbox(msg: Message) {
    promiseTable.remove(msg.correlationId) match {
      case Some(x) => {
        msg.error.length != 0 match {
          case true => x.failure(deserialize(msg.error).asInstanceOf[Throwable])
          case _ => x.success(deserialize(msg.messageBody))
        }
      }
      case _ => { /* Message already removed or ask pattern was not used */ }
    }
  }
  
  def remove(correlationId: String) {
    promiseTable.remove(correlationId)
  }
    
  def queue(msg: Message) {
	addMessageToServerInbox(msg)
  }
   
  def queue(msg: Message, promise: Promise[Any]) {
    addMessageToServerInbox(msg)
    promiseTable(msg.correlationId) = promise
  }
  
  def addMessageToServerInbox(msg: Message) {
    if(ServerTestInbox.inbox.get(msg.destinationQueue.get) == None){
      ServerTestInbox.inbox(msg.destinationQueue.get) = scala.collection.mutable.ListBuffer[Message]()
      ServerTestInbox.inbox(msg.destinationQueue.get).append(msg)
    }
  }
  
  def findRecentStats(queue:String, secondsAgo: Long) = Stat()
  
  def dequeue() {}
  
  def serialize(x: Any) = {
    val baos = new ByteArrayOutputStream
    val oos = new ObjectOutputStream(baos)
    oos.writeObject(x)
    oos.flush
    oos.close
    baos.toByteArray
  }
   
  def deserialize(data: Array[Byte]) : Any = {
   val in = new ByteArrayInputStream(data)
    val is = new ObjectInputStream(in)
    is.readObject  
  }  
  
}