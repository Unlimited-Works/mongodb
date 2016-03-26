package unlimited_works.mongodb

import net.liftweb.json._

/**
  *
  */
object TestCRUD extends App {

  //test execute method
  // insert one
//  MongoDaoMain.execute("""{"taskId": "threadname-timestamp" ,"dataBase": "helloworld", "collection": "test", "method": "insert", "params":{"documents":{"name": "insertTest02"}}}""")
//   insert multi
//  MongoDaoMain.execute("""{"dataBase": "helloworld", "collection": "test", "method": "insert", "params":{"documents":[{"name": "insertTest_multi01"},{"name": "insertTest_multi02"}]}}""")
  // find - { "_id" : { "$oid" : "56e4e11be037e008e4ec81ec" }, "name" : "insertTest02" }
//  MongoServer.execute("""{"taskId": "threadname-timestamp" , "dataBase": "helloworld", "collection": "test", "method": "count", "params":{"match":{"name": "insertTest02"}}}""")
//  MongoDaoMain.execute("""{"taskId": "threadname-timestamp" , "dataBase": "helloworld", "collection": "test", "method": "find", "params":{"name": "insertTest02"}}}""")
  // update
//  MongoDaoMain.execute("""{"dataBase": "helloworld", "collection": "test", "method": "update", "params":{"match":{"name": "ugl"}}}""")
  // delete
//  MongoDaoMain.execute("""{"dataBase": "helloworld", "collection": "test", "method": "delete", "params":{"match":{"name": "ugl"}}}""")

  // aggregate
//  MongoDaoMain.execute("""{"dataBase": "helloworld", "collection": "test", "method": "aggregate", "params":{"match":{"name": "ugl"}}}""")

  val newOne = """{"taskId2":"application-akka.actor.default-dispatcher-26784739183884","dataBase":"helloworld","collection":"account","method":"aggregate","params":[{"$match":{"$or":[{"userName":"administrator"},{"phone":"administrator"},{"email":"administrator"},{"pen_name":"administrator"}],"password":"12345_md5"}},{"$limit":1},{"$project":{"_id":1}}]}"""

  val json = parse(newOne)
  val taskIdOpt = json.findField(_.name == "taskId")
  val x = taskIdOpt.get
//  val crud = new CRUD("mongodb://localhost:27017")
//  crud.execute(newOne)

  //  crud.execute("""{"taskId":"application-akka.actor.default-dispatcher-105231490281154","dataBase":"helloworld","collection":"account","method":"aggregate","params":[{"$match":{"$or":[{"username":"administrator"},{"phone":"administrator"},{"email":"administrator"},{"pen_name":"administrator"}],"password":"12345_md5"}]}""")
//  crud.execute("""{"taskId":"application-akka.actor.default-dispatcher-105231490281154","dataBase":"helloworld","collection":"account","method":"aggregate","params":[{"$match":{"username":"administrator"}},{"$limit": 1},{"$project":{"_id": 1}}]}""")
  Thread.currentThread().join()
}
