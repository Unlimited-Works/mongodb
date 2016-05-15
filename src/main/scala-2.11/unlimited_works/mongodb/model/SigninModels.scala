package unlimited_works.mongodb.model

import com.mongodb.client.result.UpdateResult
import lorance.rxscoket.presentation.json.JProtocol
import net.liftweb.json._
import org.bson.{BsonString, BsonBoolean}
import org.mongodb.scala.Completed
import org.mongodb.scala.bson.BsonArray
import org.mongodb.scala.bson.collection.immutable.Document
import unlimited_works.mongodb.MongoDriver
import unlimited_works.mongodb.start.ServerMongoWithModel
import unlimited_works.mongodb.util.Validator

import scala.util.{Success, Try}

/**
  * make very action smaller than security.
  * db operation not as a service for every one.
  */
object SigninModels {
  val mongoClient = MongoDriver.clients(MongoDriver.defaultClientAddress)
  val hellowroldDb = mongoClient.getDatabase("helloworld")
  val registerInvitationColl = hellowroldDb.getCollection("register_invitation")
  val accountColl = hellowroldDb.getCollection("account")
  implicit val formats = DefaultFormats

  val `account/exist` = "account/exist"
  val `signin/register/invitCodeStatus` = "signin/register/invitCodeStatus"
  val `signin/register/addAccount` = "signin/register/addAccount"
  val `signin/register/invitCode` = "signin/register/invitCode" //invitcode is a part of register
  val models = Vector(`account/exist`, `signin/register/invitCodeStatus`, `signin/register/addAccount`, `signin/register/invitCode`)

  def dispatch(modelStr: String, load: JValue, jProtocol: JProtocol) {
    val taskFieldOpt = load.findField(_.name == "taskId")
    modelStr match {
      case `account/exist` =>
        case class ExistAccount(
                              email: String,
                              username: String,
                              penName: String,
                              model: String,
                              taskId: String)
        load.extractOpt[ExistAccount].foreach{ account =>
          val obv = accountColl.find(Document("$or" -> BsonArray(
                            Document("username" -> account.username),
                            Document("email" -> account.email),
                            Document("pen_name" -> account.penName))))
          obv.subscribe(
            (s: Document) => {
              jProtocol.send(JObject(JField("accountData", JObject(
                JField("email", JString(s.get("email").get.asString().getValue)),
                JField("username", JString(s.get("username").get.asString().getValue)),
                JField("penName", JString(s.get("pen_name").get.asString().getValue)))),
                taskFieldOpt.get))
            },
            (e: Throwable) => e.printStackTrace(),
            () => jProtocol.send(JObject(JField("isCompleted", JBool(true)), taskFieldOpt.get))
          )
        }
      case `signin/register/invitCodeStatus` =>
        case class InvitCode( invitCode: String,
                              model: String,
                              taskId: String)
        load.extractOpt[InvitCode] match {
          case Some(invitCode) =>
            registerInvitationColl.find(Document("invitation_code" -> invitCode.invitCode, "used" -> false)).subscribe(
              (s: Document) => {
                jProtocol.send(JObject(JField("canUse", JBool(true)), taskFieldOpt.get))
              },
              (e: Throwable) => e.printStackTrace(),
              () => // 如果只收到完成消息,表示失败
                jProtocol.send(JObject(JField("canUse", JBool(false)), taskFieldOpt.get))
            )
          case None => Unit //todo should send to kafka
        }
      case `signin/register/invitCode` =>
        //email: String, username: String,
        //        penName: String, password: String,
        //    model: String,
        case class InvitCode( invitCode: String,
                              model: String,
                              taskId: String)
        load.extractOpt[InvitCode] match {
          case Some(invitCode) =>
            val useInvitCodeObv = registerInvitationColl.updateOne(
              Document("invitation_code" -> invitCode.invitCode),
              Document("$set" -> Document("used" -> true)))
            useInvitCodeObv.subscribe(
              (s: UpdateResult) => {
                if(s.wasAcknowledged) {
                  if(s.getMatchedCount == 1) {
                    if(s.getModifiedCount == 1) {
                      jProtocol.send(JObject(taskFieldOpt.get))
                    } else {
                      //邀请码已被使用或不存在
                      jProtocol.send(JObject(JField("error", JString("邀请码已被使用或不存在")), taskFieldOpt.get))
                    }
                  } else {
                    //邀请码不存在
                    jProtocol.send(JObject(JField("error", JString("邀请码已被使用或不存在")), taskFieldOpt.get))
                  }
                } else {
                  //db error
                  jProtocol.send(JObject(JField("error", JString("数据库操作错误")), taskFieldOpt.get))
                }
              }
            )
           case None =>
           //todo make framework avoid do this boring case
           taskFieldOpt.map(i => jProtocol.send(JObject(JField("error", JString("signin/register/invitCode 参数不匹配")), i)))
        }
        //todo 根据语义,这是一个注册的功能————包括验证邀请码,判断账号是否存在,最后再创建一个account
        //但是现在的逻辑没有任何验证
      case `signin/register/addAccount` =>
        case class Account(
                              email: String, username: String,
                              penName: String, password: String,
                              model: String, taskId: String)
        load.extractOpt[Account] match {
          case Some(account) =>
            val acctObv = accountColl.insertOne(
              Document("email" -> account.email,
              "username" -> account.username,
              "pen_name" -> account.penName,
              "password" -> account.password)
            )
            acctObv.subscribe(
              (s:Completed) => jProtocol.send(taskFieldOpt.get),
              (e: Throwable) => e.printStackTrace()
            )
          case None => Unit
        }

      case _ => throw new Exception("signin not contains this service")
    }
  }

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
