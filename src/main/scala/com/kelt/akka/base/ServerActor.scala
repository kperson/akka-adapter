package com.kelt.akka.base

import akka.actor.Actor
import akka.actor.FSM
import scala.concurrent.duration._
import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import scala.util.{ Success, Failure }
import scala.concurrent.Await
import scala.concurrent.Future

sealed trait State
case object Idle extends State
case object Active extends State
case object Tick
case class ActorTarget(actorRef: ActorRef)


class ServerActor(inbox: ServerInbox, queue: String)(implicit timeout: Timeout, executor: scala.concurrent.ExecutionContext) extends Actor with FSM[State, Option[ActorRef]] {
  startWith(Idle, None)
  
  when (Idle) {
    case Event(ActorTarget(ref), None) => {
      goto(Active) using Some(ref)
    }
    case _ => {
      stay
    }
  }
  
  when (Active) {
    case Event(None, _) => {
      goto(Idle)
    }
    case Event(Tick, Some(ref)) => {
      inbox.dequeue(queue: String) match {
        case Some(x: Message) => {
          if(x.isAsk) {
            val f = ref ? (inbox deserialize x.messageBody)
            f onComplete {
             case Success(s: Any) => {  
               val msg = Message(messageBody = inbox serialize s, recipient = x.sender, correlationId = x.correlationId, clientSent = false)
               inbox queue msg 
             }
             case Failure(t) => {
               val msg = Message(messageBody = Array(), recipient = x.sender, error = inbox serialize t, correlationId = x.correlationId, clientSent = false)
               inbox queue msg 
             }
            }
          }
          else {
            ref ! (inbox deserialize x.messageBody)
          }
        }
        case _ => { /*There is nothing to do*/  }
      }
      stay
    }
  }
  
  onTransition {
    case Idle -> Active => setTimer("moreRequests", Tick, 10.milli, true)
    case Active -> Idle => cancelTimer("moreRequests")
  }
 
  initialize
  
}