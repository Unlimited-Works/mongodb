package unlimited_works.mongodb.start

import java.util.concurrent.Executors

import lorance.rxscoket.log
import lorance.rxscoket.session._
import rx.lang.scala.schedulers.ComputationScheduler
import unlimited_works.mongodb.CRUD

/**
  * which represent a connect's message queue
  */
object MongoServerBareCRUD extends App {

  lorance.rxscoket.logLevel =10

  val crud = new CRUD("mongodb://localhost:27017")
  //open socket
  val entrance = {
    log("socket start listen")
    new ServerEntrance("127.0.0.1", 10001)
  }
  val listen = entrance.listen

  val executors = Executors.newSingleThreadExecutor()

  val reader = listen.flatMap{l =>l.startReading.map{r => (r, l)}}.
//    subscribeOn(ComputationScheduler()).//needn't use ThreaPool for subscribe event beacuse of data cimputation works on `stratReading` loop which is scala Future affect.
    observeOn(ComputationScheduler())

  //handle common method
  val mongoOperationSub = reader.subscribe { _ match {
    case (protos, socket) =>
      try {
        protos.foreach { proto =>
          log(s"protocol - $proto")
          val jsonCmd = new String(proto.loaded.array())
          crud.execute(jsonCmd, socket)
        }
      } catch {
        case e: Throwable => log(s"execute CRUD - $e")
      }
    }
  }

  Thread.currentThread().join()
}
