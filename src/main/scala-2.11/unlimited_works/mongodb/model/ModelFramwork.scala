package unlimited_works.mongodb.model

import lorance.rxscoket._
import lorance.rxscoket.presentation.json.JProtocol
import net.liftweb.json.{JValue, JField}
import org.mongodb.scala.bson.{BsonObjectId, BsonDocument}
import org.mongodb.scala.model.Projections._

import unlimited_works.mongodb.MongoDriver
import unlimited_works.mongodb.start.ServerMongoWithModel
import net.liftweb.json._
import lorance.rxscoket.session.implicitpkg._


/**
  *
  */
object ModelFramwork {
  val mongoClient = MongoDriver.clients(MongoDriver.defaultClientAddress)
  val collection = mongoClient.getDatabase("helloworld").getCollection("blogs")
  implicit val formats = DefaultFormats

  val `blog/post` = "blog/post"

  val models = Vector(`blog/post`)

  def dispatch(tskJValue: JField, tskId: String, load: JValue, jProtocol: JProtocol) = tskId match {
    case `blog/post` =>
      //todo every model have a place to receive parameter.
      // e.g, "id", furthermore, we can create a class to save these information,
      // from raw info - CompletedProto to formatted info - case class.
      try {
        val id = (load \ "id").values.asInstanceOf[String]
        val postObv = collection.find(BsonDocument("_id" -> BsonObjectId(id)))
        val json = postObv.projection(excludeId()).map(_.toJson)
        json.subscribe (
          (s: String) => {
            val rst = JObject(tskJValue, JField("result", parse(s)))
            log(s"blog post merge taskId - ${compactRender(rst)}", 30)
            jProtocol.send(rst)
          },
          (e: Throwable) => {
            e
          },
          () => {
            log(s"blog post completed", 30)
            jProtocol.send(JObject(tskJValue))
          }
        )
      } catch {
        case e : Throwable => None
      }
    case _ => throw new Throwable("Not handle the task - " + tskId)
  }

  //task dispatch
  val o = ServerMongoWithModel.readerJProt.subscribe { s =>
    s.read.subscribe{ r =>
      val jsonProto = r.filter(_.uuid == 1.toByte)
      jsonProto.map { x =>
        try {
          val parsed = parse(x.loaded.array.string)
          val tid = parsed.findField(_.name == "taskId").get
          val tisStr = tid.value.values.asInstanceOf[String]
          if (models.contains(tisStr)) {
            dispatch(tid, tisStr, parsed, s)
          } else None
        } catch {
          case e: Throwable => None
        }
      }
    }
//    Observable.from(taskAndData.filter(_.nonEmpty).map(_.get))
  }

  def ready = {

  }
}
