package unlimited_works.mongodb

import org.mongodb.scala.bson.collection.immutable.Document

/**
  *
  */
object ObvTest extends App {
  val mongoClient = MongoDriver.clients(MongoDriver.defaultClientAddress)
  val hellowroldDb = mongoClient.getDatabase("helloworld")
  val registerInvitationColl = hellowroldDb.getCollection("register_invitation")
  val accountColl = hellowroldDb.getCollection("account")

  registerInvitationColl.find(Document("used" -> "")).subscribe(
    (s: Document) => {
      println("out - " + s.toJson())
      s.get("username")
    },
    (e: Throwable) => Unit,//e.printStackTrace(),
    () => println("complete")
  )

  Thread.currentThread().join()
}
