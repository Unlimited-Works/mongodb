package unlimited_works.mongodb

import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.{Completed, MongoClient}

object TestJStr extends App {
  val mongoClient: MongoClient = MongoClient()
  val mongoDB = mongoClient.getDatabase("blog")
  val mongoCollection = mongoDB.getCollection("test")
  //  val jStr = """{"result":[{"age": 0, "name": "name01"}, {"age":1, "name": "name02"}]}"""
  val jStr = """{"name":"ugl"}"""
  val document: Document = Document("_id" -> 1, "x" -> 1)
  val documentStr: Document = Document(jStr)
  val result = mongoCollection.insertOne(documentStr)
  result.subscribe((observer: Completed) => println(observer))

//  val docorg.mongodb.scala.bson.collection.mutable.Document = Document("name" -> "MongoDB", "type" -> "database",
//    "count" -> 1, "info" -> Document("x" -> 203, "y" -> 102))

  Thread.currentThread().join()
}

object nanoTime extends App {
  val x = System.nanoTime()
  val y = System.nanoTime()
  println(s"x - $x, y - $y")
}