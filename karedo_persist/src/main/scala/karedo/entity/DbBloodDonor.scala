package karedo.entity

import karedo.entity.dao.{DbMongoDAO_Casbah, Keyable}
import salat.annotations.Key

sealed trait TypeCode
case object A extends TypeCode
case object B extends TypeCode
case object O extends TypeCode
case object AB extends TypeCode
case object HH extends TypeCode

sealed trait Rhesus
case object POS extends Rhesus
case object NEG extends Rhesus

case class BloodType
(
  type_code: TypeCode
  , rhesus: Rhesus
)

case class Donor
(
  @Key("_id") id: String
  , name: String
  , mobile: String
  , email: String
  , blood: BloodType
) extends Keyable[String]

trait DbDonor extends DbMongoDAO_Casbah[String,Donor] {}

