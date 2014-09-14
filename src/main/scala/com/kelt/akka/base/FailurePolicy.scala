package com.kelt.akka.base

object FailurePolicy extends Enumeration {
  type FailurePolicy = Value
  val Drop, RetryTell = Value  
}