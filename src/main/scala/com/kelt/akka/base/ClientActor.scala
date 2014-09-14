package com.kelt.akka.base

import akka.actor.Actor
import akka.pattern.PromiseActorRef
import scala.util.Random
import scala.concurrent.{ future, promise }
import scala.concurrent.Promise
import scala.util.Success
import scala.util.Failure
import akka.actor.ActorRef
import scala.concurrent.Await
import akka.util.Timeout

case class ClientTick()

class ClientActor(inbox: ClientInbox, queue: String)(implicit timeout: Timeout, executor: scala.concurrent.ExecutionContext) extends Actor {
  
   def receive = {
     case msg: ClientTick => {
       inbox.dequeue()
     }
     case msg: Any => {
       if(sender.getClass.getName == "akka.pattern.PromiseActorRef") {
         val promise: Promise[Any] = Promise[Any]
         val originalSender = sender
         val correlationId = Random.alphanumeric.take(100).mkString
         inbox.queue(Message(destinationQueue = Some(queue), messageBody = inbox.serialize(msg), sender = Some(Random.alphanumeric.take(100).mkString), isAsk = true, correlationId = correlationId, clientSent = true), promise)
         promise.future onComplete {
           case Success(x: Any) => {
             originalSender ! x   
           }
           case Failure(t) => {
             //TODO
           }
         }
       }
       else if(sender.getClass.getName == "akka.actor.DeadLetterActorRef") {
         inbox.queue(Message(destinationQueue = Some(queue), messageBody = inbox.serialize(msg), sender = None, isAsk = false, correlationId = Random.alphanumeric.take(100).mkString, clientSent = true))
       }
     }


   }
  
}