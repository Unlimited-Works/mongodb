package unlimited_works.mongodb

import lorance.rxscoket.session._
import org.mongodb.scala.MongoClient
import net.liftweb.json._

//import
/**
  * which represent a connect's message queue
  */
object MongoDaoMain extends App{
  //mongo
  val mongoClient: MongoClient = MongoClient("mongodb://localhost:27017")
  val mongoDB = mongoClient.getDatabase("helloworld")
  def mongoCollection(collName: String) = mongoDB.getCollection(collName)
  //open socket
  val entrance = new ServerEntrance("127.0.0.1", 10001)
  val listen = entrance.listen
  val reader = listen.flatMap(_.startReading)

  val mongoOperationSub = reader.subscribe{ s =>
    s.map{item =>
      val jStr = new String(item.loaded.array())
      val json = parse(jStr)
      val collectionName = json \ "collection"
      val theMethod = json \ "method"//CRUD
      val theOperators = json \ "operators"
      val theColl = mongoCollection(collectionName.asInstanceOf[String])

      //do search
      theMethod.asInstanceOf[String] match {
        /**
          * method: find
          * params: {
          *   match: {...}
          *   project: {...}
          * }
          */
        case "find" =>
//          theColl.find(compactRender(theOperators))

        /**
          * method: insert
          * params: {
          *   documents: [{...},{...}]//contains one item
          *
          * }
          */
        case "insert" =>

        /**
          * method: update
          * params: {
          *   match: {...},
          *   modify: {...}
          * }
          */
        case "update" =>

        /**
          * method: remove
          * params: {
          *   match: {...}
          * }
          */
        case "remove" =>

        case "count" =>
        case "aggregate" =>
      }
    }
  }
}
