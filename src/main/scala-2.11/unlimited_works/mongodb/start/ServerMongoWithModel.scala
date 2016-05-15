package unlimited_works.mongodb.start

import lorance.rxscoket.presentation.json.JProtocol
import lorance.rxscoket.session.ServerEntrance
import rx.lang.scala.Observable
import unlimited_works.mongodb.model.{SigninModels, BlogModels}
import unlimited_works.mongodb.model.blog.{Overview, PenName}

/**
  *
  */
object ServerMongoWithModel extends App {
  lorance.rxscoket.logLevel = 100
  lorance.rxscoket.logAim += "ready_pen_name"

  val conntected = new ServerEntrance("127.0.0.1", 10010).listen
  val readX = conntected.map(c => (c, c.startReading))

  val readerJProt = readX.map(cx => new JProtocol(cx._1, cx._2))

  //for Overview and PenName model
  val reader = readX.flatMap{l => l._2.map(x => (x, l._1))}

  Overview.ready
  PenName.ready

  val x = BlogModels.o
  val y = SigninModels.o
  Thread.currentThread().join()
}
