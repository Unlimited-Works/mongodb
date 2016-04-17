package unlimited_works.mongodb

import lorance.rxscoket._
import org.mongodb.scala.MongoClient
import scala.collection.mutable
/**
  *
  */
object MongoDriver {
  val clients = mutable.Map[String, MongoClient]()

  val defaultClientAddress = "mongodb://localhost:27017"
  clients += (defaultClientAddress -> mongoClient(defaultClientAddress))
  log(s"${getClass} - " + clients)

  def mongoClient(address: String): MongoClient = {
    log("mongo client link at " + address)
    MongoClient(address)
  }
}
