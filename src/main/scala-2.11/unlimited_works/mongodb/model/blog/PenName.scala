package unlimited_works.mongodb.model.blog

import java.nio.ByteBuffer

import org.mongodb.scala.bson.{BsonDocument, BsonObjectId}
import rx.lang.scala.Observable
import unlimited_works.mongodb.MongoDriver
import lorance.rxscoket.session.implicitpkg._
import net.liftweb.json._
import org.mongodb.scala.model.Projections._
import lorance.rxscoket.log
import lorance.rxscoket.session
import unlimited_works.mongodb.start.ServerMongoWithModel

import scala.util.{Success, Failure, Try}

/**
  * get
  * {
  *   taskId: "pen_name"
  *   accountId:
  * }
  *
  * rsp
  * {
  *   taskId:
  *   pen_name:
  * }
  */
object PenName {
  val mongoClient = MongoDriver.clients(MongoDriver.defaultClientAddress)
  val collection = mongoClient.getDatabase("helloworld").getCollection("account")
  implicit val formats = DefaultFormats

  /**
    * stream of the taskId
    */
  def ready = {
    val o = ServerMongoWithModel.reader.flatMap{ s =>
      log("ready pen_name - ", 70, Some("ready_pen_name"))
      val jsonProto = s._1.filter(_.uuid == 1.toByte)
      val taskAndData = jsonProto.map{x =>
        try {
          val parsed = parse(x.loaded.array.string)
          val tid = parsed.findField(_.name == "taskId").get
          val model = parsed.findField(_.name == "model").get
          if (model.value.values.asInstanceOf[String] == "pen_name") {
            val accountId = (parsed \ "accountId" values).asInstanceOf[String]
            Some((tid, accountId, s._2))
          } else None
        } catch {
          case e : Throwable => None
        }
      }

      Observable.from(taskAndData.filter(_.nonEmpty).map(_.get)).doOnError((e: Throwable) => e.printStackTrace())
    }

    o.subscribe { sub =>
      Try(BsonObjectId(sub._2)) match {
        case Failure(e) => log("can't transfer to mongo's BsonObjectId")
        case Success(objId) =>
          val findRst = collection.find(BsonDocument("_id" -> objId))
          val penNameJson = findRst.projection(fields(include("pen_name"), excludeId())).map(_.toJson)

          penNameJson.subscribe (
            (s: String) => {
              val p: JObject = parse(s).asInstanceOf[JObject]
              log(s"penName mongo docuemnt - ${prettyRender(p)}",4)
              val r = compactRender(p.merge(JObject(sub._1)))
              log(s"panName - $r", 30)
              sub._3.send(ByteBuffer.wrap(session.enCode(1.toByte, r)))
            },
            (e: Throwable) => e.printStackTrace(),
            () => log(s"completed")
          )
      }

    }
  }
}
