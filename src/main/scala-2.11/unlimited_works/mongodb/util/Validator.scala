package unlimited_works.mongodb.util

/**
  * verify filed does correct
  * by Tec.He
  */
case class Validator(key: String, value: String, validator: String => Option[String]) {
  def validate = validator(value.trim).map(key -> _)
}