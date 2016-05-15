package unlimited_works.mongodb.model

import com.mongodb.client.result.{DeleteResult, UpdateResult}
import lorance.rxscoket._
import lorance.rxscoket.presentation.json.{IdentityTask, JProtocol}
import net.liftweb.json.{JValue, JField}
import org.mongodb.scala.Completed
import org.mongodb.scala.bson.collection.immutable.Document
import org.mongodb.scala.bson.{BsonString, BsonObjectId, BsonDocument}
import org.mongodb.scala.model.Projections._

import unlimited_works.mongodb.MongoDriver
import unlimited_works.mongodb.start.ServerMongoWithModel
import net.liftweb.json._
import java.util.UUID

/**
  *
  */
object BlogModels {
  val mongoClient = MongoDriver.clients(MongoDriver.defaultClientAddress)
  val hellowroldDb = mongoClient.getDatabase("helloworld")
  val blogCollection = hellowroldDb.getCollection("blogs")
  implicit val formats = DefaultFormats

  val `blog/post` = "blog/post"
  val `blog/post/update` = "blog/post/update"
  val `blog/post/create` = "blog/post/create"
  val `blog/post/delete` = "blog/post/delete"
  val `blog/post/modify/access` = "blog/post/modify/access"
  val models = Vector(`blog/post`, `blog/post/update`, `blog/post/create`, `blog/post/delete`, `blog/post/modify/access`)

  def dispatch(modelStr: String, load: JValue, jProtocol: JProtocol) = {
    val taskFieldOpt = load.findField(_.name == "taskId")

    modelStr match {
      case `blog/post` =>
        //todo every model have a place to receive parameter.
        // e.g, "id", furthermore, we can create a class to save these information,
        // from raw info - CompletedProto to formatted info - case class.
        try {
          val id = (load \ "id").values.asInstanceOf[String]
          val postObv = blogCollection.find(BsonDocument("_id" -> BsonObjectId(id)))
          val json = postObv.projection(excludeId()).map(_.toJson)
          val taskField = taskFieldOpt.get
          json.subscribe (
            (s: String) => {
              val rst = JObject(taskField, JField("result", parse(s)))
              log(s"blog post merge taskId - ${compactRender(rst)}", 30)
              jProtocol.send(rst)
            },
            (e: Throwable) => {
              e.printStackTrace()
            },
            () => {
              log(s"blog post completed", 30)
              jProtocol.send(JObject(taskField))
            }
          )
        } catch {
          case e : Throwable => None
        }
      case `blog/post/update` =>
        try {
          val taskField = taskFieldOpt.get
          val _id = (load \ "_id").values.asInstanceOf[String]
          val introduce = (load \ "introduce").values.asInstanceOf[String]
          val title = (load \ "title").values.asInstanceOf[String]
          val body = (load \ "body").values.asInstanceOf[String]
          val postObv = blogCollection.updateOne(Document("_id" -> BsonObjectId(_id)), Document( "$set" -> BsonDocument( "introduction" -> introduce,  "title" -> title, "body" -> body)))
          postObv.subscribe (
            (s: UpdateResult) => {
              val rst = JObject(taskField)
              log(s"blog post merge taskId - ${compactRender(rst)}", 30)
              jProtocol.send(rst)
            },
            (e: Throwable) => {
              e.printStackTrace()
            },
            () => {
              log(s"blog post completed", 30)
              jProtocol.send(JObject(taskField))
            }
          )
        } catch {
          case e : Throwable => None
        }
      case `blog/post/create` =>
        try {
          val taskField = taskFieldOpt.get
          val penName = (load \ "pen_name").values.asInstanceOf[String]
          val introduce = (load \ "introduce").values.asInstanceOf[String]
          val title = (load \ "title").values.asInstanceOf[String]
          val body = (load \ "body").values.asInstanceOf[String]
          val time = (load \ "time").values.asInstanceOf[String]
          val postObv = blogCollection.insertOne(Document("introduction" -> introduce,  "title" -> title, "body" -> body, "pen_name" -> penName, "issue_time" -> time))
          postObv.subscribe (
            (s: Completed) => {
              val rst = JObject(taskField)
              log(s"blog post merge taskId - ${compactRender(rst)}", 30)
              jProtocol.send(rst)
            },
            (e: Throwable) => {
              e.printStackTrace()
            },
            () => {
              log(s"blog post completed", 30)
              jProtocol.send(JObject(taskField))
            }
          )
        } catch {
          case e : Throwable => None
        }
      case `blog/post/delete` =>
        try {
          val taskField = taskFieldOpt.get
          val id = (load \ "id").values.asInstanceOf[String]
          val deleteObv = blogCollection.deleteOne(Document("_id" -> BsonObjectId(id)))
          deleteObv.subscribe (
            (s: DeleteResult) => {
              if (s.wasAcknowledged && s.getDeletedCount == 1){
                val rst = JObject(taskField)
                log(s"blog delete post merge taskId - ${compactRender(rst)}", 30)
                jProtocol.send(rst)
              } else {
                val rst = JObject(taskField, JField("result", JString("delete failed")))
                log(s"blog delete post merge taskId failed - ${compactRender(rst)}", 30)
                jProtocol.send(rst)
              }
            },
            (e: Throwable) => {
              e.printStackTrace()
            },
            () => {
              log(s"blog delete completed", 30)
              jProtocol.send(JObject(taskField, JField("result", JString("completed"))))
            }
          )
        } catch {
          case e : Throwable => None
        }
      case `blog/post/modify/access` =>
        val taskField = taskFieldOpt.get
        case class PostAccessReq(postId: String, toPublic: Boolean, model: String, taskId: String)
        load.extractOpt[PostAccessReq].foreach { req =>
          if(req.toPublic) {
            val uuid = UUID.randomUUID().toString
            val addShaObv = blogCollection.updateOne(Document("_id" -> BsonObjectId(req.postId)), Document("$set" -> Document("share_sha" -> uuid)))
            addShaObv.subscribe(
              (s: UpdateResult) =>
                if(s.wasAcknowledged && s.getMatchedCount == 1) {
                  val rst = JObject(JField("share_sha", JString(uuid)), taskField)
                  jProtocol.send(rst)
                } else {
                  val rst = JObject(JField("error", JString("更新数据库失败或者匹配了多个文档")), taskField)
                  jProtocol.send(rst)
                },
              (e: Throwable) => {
                e.printStackTrace()
              },
              () => {
                log(s"blog delete completed", 30)
                jProtocol.send(JObject(taskField, JField("error", JString("这只是一个结束信号"))))
              }
            )
          } else {
            val addShaObv = blogCollection.updateOne(Document("_id" -> BsonObjectId(req.postId)), Document("$unset" -> Document("share_sha" -> "")))
            addShaObv.subscribe(
              (s: UpdateResult) =>
                if(s.wasAcknowledged && s.getMatchedCount == 1) {
                  val rst = JObject(taskField)
                  jProtocol.send(rst)
                } else {
                  val rst = JObject(JField("error", JString("更新数据库失败或者匹配了多个文档")), taskField)
                  jProtocol.send(rst)
                },
              (e: Throwable) => {
                e.printStackTrace()
              },
              () => {
                log(s"blog delete completed", 30)
                jProtocol.send(JObject(taskField, JField("error", JString("这只是一个结束信号"))))
              }
            )
          }
        }
      case _ =>
        //todo handle the exception
//        throw new Throwable("Not handle the model - " + modelStr)
        log("Not handle the model - " + modelStr)
    }
  }

  //model dispatch
  val o = ServerMongoWithModel.readerJProt.subscribe { s =>
    s.jRead.subscribe{ j =>
      try {
        val model = j.findField(_.name == "model").get
        val modelStr = model.value.values.asInstanceOf[String]
        if (models.contains(modelStr)) {
          dispatch(modelStr, j, s)
        } else None
      } catch {
        case e: Throwable => None
      }
    }
  }
}
