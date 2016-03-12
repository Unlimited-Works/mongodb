package unlimited_works.mongodb

import lorance.rxscoket.session._
import org.mongodb.scala.MongoClient
import net.liftweb.json._
import org.mongodb.scala.bson.collection.immutable.Document
import com.mongodb.util.JSON
//import
/**
  * which represent a connect's message queue
  */
object MongoDaoMain extends App{
  //mongo
  val mongoClient: MongoClient = MongoClient("mongodb://localhost:27017")
//  val mongoDB = mongoClient.getDatabase("helloworld")
//  def mongoCollection(collName: String) = mongoDB.getCollection(collName)
  //open socket
  val entrance = new ServerEntrance("127.0.0.1", 10001)
  val listen = entrance.listen
  val reader = listen.flatMap(_.startReading)

  val mongoOperationSub = reader.subscribe{ s =>
    s.map{item =>
      val jsonCmd = new String(item.loaded.array())
      execute(jsonCmd)
    }
  }

  //test execute method
  val findTestResult = execute("""{"dataBase": "helloworld", "collection": "test", "method": "find", "params":{"match":{"name": "ugl"}}}""")
  val docs = findTestResult.map{o => o}
  docs.subscribe{s: Document => println(s)}

  Thread.currentThread().join()
  private def execute(load: String) = {
    val json = parse(load)
//    val test = (json \ "dataBase")
    val dbName = (json \ "dataBase").values.asInstanceOf[String]
    val collectionName = (json \ "collection").values.asInstanceOf[String]
    val theMethod = json \ "method"//CRUD
    val params = json \ "params"
    val theColl = mongoClient.
      getDatabase(dbName).
      getCollection(collectionName)

    theMethod.values.asInstanceOf[String] match {
      /**
        * method: find
        * params: {
        *   match: {...}
        *   //project: {...}
        * }
        */
      case "find" =>
        val matcher = compactRender(params \ "match")
        theColl.find(Document(matcher))

      /**
        * method: insert
        * params: {
        *   documents: [{...},{...}]//contains one item
        *   multi: "boolean"
        * }
        */
      case "insert" => throw new Exception
//        val multi = (params \ "multi").asInstanceOf[String] == "true"
//        val documents = params \ "documents"
//
//        if (multi) {
//          val js = documents.asInstanceOf[JArray]
//          val ds = js.arr.map{ i => Document(compactRender(i))}
//          theColl.insertMany(ds)
//        }
//        else {
//          val doc = Document(compactRender(documents))
//          theColl.insertOne(doc)
//        }
      /**
        * method: update
        * params: {
        *   match: {...},
        *   modify: {...}
        * }
        */
      case "update" =>throw new Exception
      /**
        * method: remove
        * params: {
        *   match: {...}
        * }
        */
      case "remove" =>throw new Exception

      /**
        * method: count
        * params: {
        *   match: {...}
        * }
        */
      case "count" =>throw new Exception

      /**
        * method:aggregate
        * params: [
        *   {...},{...}
        *   ...
        * ]
        */
      case "aggregate" =>throw new Exception
    }
  }
}
