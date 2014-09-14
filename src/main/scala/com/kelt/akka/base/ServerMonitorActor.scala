package com.kelt.akka.base

import akka.actor.Actor
import com.kelt.akka.base.FailurePolicy._
import scala.util.Random
import akka.actor.ActorRef
import akka.actor.DeadLetter
import akka.actor.Terminated

class ServerMonitorActor(target: ActorRef, queue: String, serverInbox: ServerInbox, clientInbox: Option[ClientInbox] = None, failurePolicy: FailurePolicy = Drop) extends Actor {
  private val targetPath = target.path
  
  override def preStart() {
    context.watch(target)
  }

  def receive: Actor.Receive = {
    case d: DeadLetter => {
      serverInbox.incrementDeadMessageCount(queue)
      if(failurePolicy == RetryTell && clientInbox != None) {
        clientInbox.get.queue(Message(destinationQueue = Some(queue), messageBody = clientInbox.get.serialize(d.message), sender = None, isAsk = false, correlationId = Random.alphanumeric.take(100).mkString, clientSent = true))
      }
    }
    case Terminated(`target`) => {
      context.system.shutdown()
    }
  }
}