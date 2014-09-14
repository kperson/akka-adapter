package com.kelt.akka.base

import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ByteArrayInputStream
import java.io.ObjectOutputStream
import scala.collection.mutable.ListBuffer

object ServerTestInbox extends ServerInbox {

  var inbox:  scala.collection.mutable.Map[String,ListBuffer[Message]] = scala.collection.mutable.Map[String, ListBuffer[Message]]()

  def queue(msg: Message) {
    ClientTestInbox.deliverToInbox(msg)
  }
   
  def dequeue(queue: String): Option[Message] = {
    if (inbox.get(queue) != None && !inbox(queue).isEmpty) {
      Some(inbox(queue).remove(0))
    }
    else {
      None
    }
  }
  
  def incrementDeadMessageCount(queue: String) {
    
  }
  
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