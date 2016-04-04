package unlimited_works.mongodb.blog

import java.nio.ByteBuffer

import lorance.rxscoket._
import org.mongodb.scala.bson.{BsonObjectId, BsonDocument}
import org.mongodb.scala.model.Projections._
import rx.lang.scala.Observable
import unlimited_works.mongodb.{MongoServer, MongoDriver}
import lorance.rxscoket.session.implicitpkg._
import net.liftweb.json.JsonDSL._
import net.liftweb.json._
/**
  * todo add index for blogs pen_name collection
  * {
  *   taskId: "blog/index/overview"
  *   penName:
  *   skip: Int
  *   limit: Int
  * }
  *
  * {
  *   taskId:
  *   title
  *   issue_time
  *   introduction
  * }
  */
object Overview {
  val mongoClient = MongoDriver.clients(MongoDriver.defaultClientAddress)
  val collection = mongoClient.getDatabase("helloworld").getCollection("blogs")
  implicit val formats = DefaultFormats

  /**
    * stream of the taskId
    */
  def ready = {
    val o = MongoServer.reader.flatMap { s =>
      val jsonProto = s._1.filter(_.uuid == 1.toByte)
      val taskAndData = jsonProto.map { x =>
        try {
          val parsed = parse(x.loaded.array.string)
          val tid = parsed.findField(_.name == "taskId").get
          if (tid.value.values.asInstanceOf[String] == "blog/index/overview") {//modify
            val penName = (parsed \ "penName" values).asInstanceOf[String]//modify
            val skip = (parsed \ "skip" values).asInstanceOf[BigInt].toInt//modify
            val limit = (parsed \ "limit" values).asInstanceOf[BigInt].toInt//modify
            Some((tid, penName, skip, limit, s._2))
          } else None
        } catch {
          case e: Throwable => None
        }
      }

      Observable.from(taskAndData.filter(_.nonEmpty).map(_.get))
    }

    //make is error able, it will be broken if some error occurred
    o.subscribe { sub =>
      val findRst = collection.find(BsonDocument("pen_name" -> sub._2)).limit(sub._4).skip(sub._3)//modify
      val json = findRst.projection(fields(include("title", "issue_time", "introduction"), excludeId())).map(_.toJson)//modify

      json.subscribe(
        (s: String) => {
          val p: JObject = parse(s).asInstanceOf[JObject]
          log(s"blog overview mongo docuemnt - ${prettyRender(p)}", 4)
          val resultJ = JObject(JField("result", p))

          val r = compactRender(resultJ.merge(JObject(sub._1)))
          log(s"blog overview merge taskId - $r", 30)
          sub._5.send(ByteBuffer.wrap(session.enCode(1.toByte, r)))//modify
        },
        (e: Throwable) => {
          log(s"blog/index/overview broken - $e")
          e
        },
        () => {
          val r = compactRender(JObject(sub._1))
          log(s"completed send with $r") //modify
          sub._5.send(ByteBuffer.wrap(session.enCode(1.toByte, r)))
        }
      )
    }
  }
}
