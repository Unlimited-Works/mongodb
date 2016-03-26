package unlimited_works.mongodb

import lorance.rxscoket.session._
import lorance.rxscoket.log

/**
  * which represent a connect's message queue
  */
object MongoServer extends App {
  val crud = new CRUD("mongodb://localhost:27017")
  //open socket
  val entrance = {
    log("socket start listen")
    new ServerEntrance("127.0.0.1", 10001)
  }
  val listen = entrance.listen
  val reader = listen.flatMap{l =>l.startReading.map{r => (r, l)}}

  val mongoOperationSub = reader.subscribe { _ match {
    case (protos, socket) =>
      protos.foreach { proto =>
        log(s"protocol - $proto")
        val jsonCmd = new String(proto.loaded.array())
        crud.execute(jsonCmd, socket)
      }
    }
  }
  Thread.currentThread().join()
}
