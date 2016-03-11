name := "mongodb"

version := "1.0"

scalaVersion := "2.11.8"

lazy val versions = new {
  val mongoScalaDriver = "1.1.0"
  val mongoJavaDriver = "3.2.1"
  val mongoJavaDriverAsync = "3.2.1"
  val liftMongoRecorder = "3.0-M7"
}

libraryDependencies ++= Seq(
  "org.mongodb.scala" %% "mongo-scala-driver" % versions.mongoScalaDriver,
  "net.liftweb" %% "lift-mongodb-record" % versions.liftMongoRecorder,
  "org.mongodb" %	"mongodb-driver" % versions.mongoJavaDriver,
//  "com.typesafe.akka" %% "akka-actor" % versions.akkaActor,
  "org.mongodb" % "mongodb-driver-async" % versions.mongoJavaDriverAsync
)
