package unlimited_works.mongodb

import net.liftweb.json._
import scala.io.{Codec, Source}

/**
  *
  */
package object config {
  private implicit val formats = DefaultFormats

  case class Mongo(address: String, port: Int, username: String, password: String, database: String)
  case class AppConfig(mongo: Mongo)

  val datas = Source.fromInputStream(getClass.getResourceAsStream("/unlimited_works/mongodb/config/deploy/common.json"))(Codec.UTF8).mkString
  val json = parse(datas)
  val appConfig = json.extract[AppConfig]
}
