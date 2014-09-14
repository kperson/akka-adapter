package com.kelt.akka.base

case class Stat(dead: Long = 0, dequeued: Long = 0, queued: Long = 0) {

  def unsuccessfullyQueued = math.min(dead, queued)
  
  def successfullyQueued = queued - unsuccessfullyQueued
   
  def balancePercentage:Double = if (queued == 0) 0.0 else successfullyQueued.toDouble / queued.toDouble
   
  def failureRate = 1.0 - balancePercentage
 
}