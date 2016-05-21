name := "mongodb"

version := "1.0"

scalaVersion := "2.11.7"

lazy val versions = new {
  val mongoScalaDriver = "1.1.0"
  val mongoJavaDriver = "3.2.1"
  val mongoJavaDriverAsync = "3.2.1"
  val liftMongoRecorder = "3.0-M8"
}

libraryDependencies ++= Seq(
  "org.mongodb.scala" %% "mongo-scala-driver" % versions.mongoScalaDriver,
//  "net.liftweb" %% "lift-mongodb-record" % versions.liftMongoRecorder,
//  "org.mongodb" %	"mongodb-driver" % versions.mongoJavaDriver,
//  "org.mongodb" % "mongodb-driver-async" % versions.mongoJavaDriverAsync,

  //  "com.typesafe.akka" %% "akka-actor" % versions.akkaActor,
  "io.reactivex" %% "rxscala" % "0.26.0",

  //json
  "net.liftweb" %% "lift-json" % versions.liftMongoRecorder
)


//手动管理依赖
//val baseDir: File = baseDirectory.value
unmanagedBase :=  baseDirectory.value / "mylib"

//assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)

assemblyJarName in assembly := "ServerMongoWithModel.jar"
mainClass in assembly := Some(" unlimited_works.mongodb.start.ServerMongoWithModel")

//assemblyJarName in assembly := "MongoServerBareCRUD.jar"
//mainClass in assembly :=  Some("unlimited_works.mongodb.start.MongoServerBareCRUD")
