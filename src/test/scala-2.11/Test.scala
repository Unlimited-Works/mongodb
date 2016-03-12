import org.mongodb.scala.{Completed, MongoClient}
import org.mongodb.scala.bson.collection.immutable.Document

object Test extends App {
  val mongoClient: MongoClient = MongoClient()
  val mongoDB = mongoClient.getDatabase("helloworld")
  val mongoCollection = mongoDB.getCollection("test")
  //  val jStr = """{"result":[{"age": 0, "name": "name01"}, {"age":1, "name": "name02"}]}"""
  val jStr = """{"name":"ugl"}"""
  val document: Document = Document("_id" -> 1, "x" -> 1)
  val documentStr: Document = Document(jStr)
  val result = mongoCollection.insertOne(documentStr)
  result.subscribe((observer: Completed) => println(observer))
  Thread.currentThread().join()
  Thread.sleep(3000)
}
