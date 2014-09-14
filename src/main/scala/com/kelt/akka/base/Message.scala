package com.kelt.akka.base

case class Message(destinationQueue: Option[String] = None, messageBody: Array[Byte], sender: Option[String] = None, recipient: Option[String] = None, isAsk: Boolean = false, error: Array[Byte] = Array(), correlationId: String, clientSent: Boolean, createdAt: Long = System.currentTimeMillis)