package com.kelt.akka.base

import akka.routing.Router
import akka.actor.ActorRef
import akka.routing.DefaultResizer

trait ServerInbox {
  
  def queue(msg: Message)
   
  def dequeue(queue: String): Option[Message]
  
  def serialize(x: Any) : Array[Byte]
  
  def deserialize(x: Array[Byte]) : Any
   
  def incrementDeadMessageCount(queue: String) 
 
}