package com.kelt.akka.base

import scala.concurrent.Promise

trait ClientInbox {

  def queue(msg: Message)
   
  def queue(msg: Message, promise: Promise[Any])
    
  def serialize(x: Any) : Array[Byte]
  
  def deserialize(x: Array[Byte]) : Any
    
  def dequeue()
  
  def findRecentStats (queue:String, secondsAgo: Long): Stat
  
}