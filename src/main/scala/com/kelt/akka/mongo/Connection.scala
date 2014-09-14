package com.kelt.akka.mongo

import com.mongodb.casbah.MongoConnection
import com.mongodb.casbah.MongoDB

case class ConnectionSettings(host: String, port: Int, database: String, userName: Option[String] = None, password: Option[String] = None)


object Connection {

  private var database: MongoDB = null

  def db = database match {
    case null =>{
      None
   }
   case _ =>{
     Some(database)
   }
 }

  def setConnectionSettings(settings: ConnectionSettings) {
    val connection = MongoConnection(settings.host, settings.port)
    database = connection(settings.database)
    if(!database.isAuthenticated) {
      database.authenticate(settings.userName.get, settings.password.get)
    }
  }

}
