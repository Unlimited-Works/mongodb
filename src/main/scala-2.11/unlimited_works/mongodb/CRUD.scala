package unlimited_works.mongodb

import java.nio.ByteBuffer

import com.mongodb.client.result.{DeleteResult, UpdateResult}
import lorance.rxscoket._
import lorance.rxscoket.session.ConnectedSocket
import lorance.rxscoket.presentation.json.JsonParse

import net.liftweb.json._
import org.mongodb.scala.{MongoClient, Completed}
import org.mongodb.scala.bson.collection.immutable.Document

class CRUD(mongoAddress: String) {
  //mongo
  private val mongoClient = MongoDriver.clients(MongoDriver.defaultClientAddress)
  def execute(load: String, socket: ConnectedSocket): Unit = {
    try {
      val json = parse(load)

      val taskIdOpt = json.findField(_.name == "taskId")

      /**
        * todo these instance could be save because of Observable is stream for multi request.
        */
      val dbName = (json \ "dataBase").values.asInstanceOf[String]
      val collectionName = (json \ "collection").values.asInstanceOf[String]
      val theMethod = json \ "method" //CRUD
      val params = json \ "params"
      val theColl = mongoClient.
        getDatabase(dbName).
        getCollection(collectionName)

      theMethod.values.asInstanceOf[String] match {
        /**
          * method: find
          * params: {
          * match: {...}
          * //project: {...} sdk not contains this feature, use aggregate instand
          * }
          * todo add complete state as aggregate
          */
        case "find" =>
          val matcher = compactRender(params \ "match")
          val rst = theColl.find(Document(matcher))
          import org.mongodb.scala.ScalaObservable
          rst.subscribe { s: org.mongodb.scala.bson.collection.immutable.Document =>
            val jStr = s.toJson()
            //          log(s"find result : $s - $jStr")
            val json = parse(jStr)
            val resultJ = JObject(JField("result", json))
            val merged = resultJ.merge(JObject(taskIdOpt.get)) //todo try-catch `NoSuchElementException` from None.get
          val mergedStr = compactRender(merged)
            log(s"find merged result - $mergedStr")
            socket.send(ByteBuffer.wrap(session.enCode(1.toByte, mergedStr)))
          }

        /**
          * method: insert
          * params: {
          * documents: [{...},{...}]//contains one item
          * }
          *
          * response: {
          * taskId:""
          * result:""
          * }
          */
        case "insert" =>
          val documents = params \ "documents"
          val multi = try {
            documents.asInstanceOf[JArray]; true
          } catch {
            case e: ClassCastException => false
          }

          val rst = if (multi) {
            val js = documents.asInstanceOf[JArray]
            val ds = js.arr.map { i => Document(compactRender(i)) }
            theColl.insertMany(ds)
          }
          else {
            val doc = Document(compactRender(documents))
            theColl.insertOne(doc)
          }

          val onNext = (s: Completed) => {
            val json = JObject(JField("result", JString(s.toString)))
            val merged = compactRender(json.merge(JObject(taskIdOpt.get))) //todo try-catch `NoSuchElementException` from None.get
            log("insert completed - " + merged)

            socket.send(ByteBuffer.wrap(JsonParse.enCode(merged)))
          }

          val onError = (e: Throwable) => log(s"insert error - $e")
          val onCompleted = () => {
            val rst = compactRender(JObject(taskIdOpt.get)) //todo try-catch `NoSuchElementException` from None.get

            log(s"insert completed - $rst")
            socket.send(ByteBuffer.wrap(JsonParse.enCode(rst)))
          }
          rst.subscribe(onNext,
            onError,
            onCompleted
          )

        /**
          * Update any matches
          *
          * method: update
          * params: {
          * match: {...},
          * modify: {...}
          * }
          */
        case "update" =>
          val matcher = compactRender(params \ "match")
          val matchDoc = Document(matcher)

          val modify = compactRender(params \ "modify")
          val modifyDoc = Document(modify)

          val rst = theColl.updateMany(matchDoc, modifyDoc)
          rst.subscribe { s: UpdateResult => log(s.toString) }

        /**
          * method: delete
          * params: {
          * match: {...}
          * }
          */
        case "delete" =>
          val matcher = compactRender(params \ "match")
          val matchDoc = Document(matcher)
          val rst = theColl.deleteMany(matchDoc)
          rst.subscribe { s: DeleteResult => log(s.toString) }

        /**
          * method: count
          * params: {
          * match: {...}
          * }
          */
        case "count" =>
          val matcher = compactRender(params \ "match")
          val matchDoc = Document(matcher)
          val rst = theColl.count(matchDoc)
          val x =
            rst.subscribe { s: Long =>
              //todo try-catch `taskIdOpt.get`
              val jString = compactRender(JObject(taskIdOpt.get).merge(JObject(JField("count", JInt(s)))))
              log(s"count result - $jString")
              socket.send(ByteBuffer.wrap(JsonParse.enCode(jString)))
            }

        /**
          * method:aggregate
          * params: [
          * {...},{...}
          * ...
          * ]
          */
        case "aggregate" =>
          val pipes = params.asInstanceOf[JArray]
          val pipesDoc = pipes.arr.map { item => Document(compactRender(item)) }
          val rst = theColl.aggregate(pipesDoc) //aggregate is Future exactly.

          rst.subscribe((s: Document) =>
            taskIdOpt.map { taskId =>
              val jStr = s.toJson()
              val json = parse(jStr)
              val resultJ = JObject(JField("result", json))
              val merged = resultJ.merge(JObject(taskId))
              val mergedStr = compactRender(merged)
              log(s"aggregate onNext - $mergedStr")
              socket.send(ByteBuffer.wrap(JsonParse.enCode(mergedStr)))
            },
            (error: Throwable) => log(s"aggregate onError - $error", -1),
            () => taskIdOpt.map { t =>
              val jsonStr = compactRender(JObject(t))
              log(s"aggregate completed - $jsonStr")
              socket.send(ByteBuffer.wrap(JsonParse.enCode(jsonStr)))
            }
          )

        //todo Q: why custom Observer can't trigge `onNext`?
        //        val rstObserver = new Observer[Document]{
        //          override def onSubscribe(subscription: Subscription): Unit = subscription.request(Long.MaxValue)
        //
        //          override def onNext(value: Document): Unit = {
        //            s: Document => {
        //              taskIdOpt.map { taskId =>
        //                val jStr = s.toJson()
        //                val json = parse(jStr)
        //                val merged = json.merge(JObject(taskId))
        //                val mergedStr = compactRender(merged)
        //                log(s"aggregate onNext - $mergedStr")
        //                socket.send(ByteBuffer.wrap(JsonParse.enCode(mergedStr)))
        //              }
        //            }
        //          }
        //
        //          override def onError(error: Throwable): Unit = log(s"aggregate search Exception - $error", -1)
        //
        //          override def onComplete(): Unit = taskIdOpt.map { t =>
        //            log(s"aggregate completed - ${compactRender(JObject(t))}")
        //            socket.send(ByteBuffer.wrap(JsonParse.enCode(t)))
        //          }
        //        }
        //
        //        //only execute onComplete even through aggregate operates result contains data
        //        rst.subscribe(rstObserver)

      }
    } catch{
      case e: Throwable => log(s"execute exception - $e")
    }
  }
}
