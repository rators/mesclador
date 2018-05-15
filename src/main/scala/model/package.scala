import io.circe.Decoder.Result
import io.circe.generic.auto._
import io.circe.parser.parse
import io.circe.{Decoder, Encoder, HCursor, Json}

import scala.collection.immutable.Map

package object model {
  type Address = String

  implicit val decodeJobCoin: Decoder[JobCoin] = (c: HCursor) => for {
    foo <- if (c.downField("value").failed) c.as[String] else c.downField("value").as[String]
  } yield JobCoin(BigDecimal(foo))

  implicit val encodeJobCoin: Encoder[JobCoin] = (a: JobCoin) => Json.obj(
    ("value", Json.fromString(a.value.toString))
  )

  def decodeHouseAccountsDB(json: String): Result[List[Address]] = {
    val doc: Json = parse(json).getOrElse(Json.Null)
    val cursor: HCursor = doc.hcursor

    val houseAccountsDbDecoder: Decoder.Result[List[Address]] =
      cursor.downField("houseAccounts").as[List[Address]]

    houseAccountsDbDecoder
  }

  def decodeJobCoinDB(json: String): Result[Map[Address, AddressInfo]] = {
    val doc: Json = parse(json).getOrElse(Json.Null)
    val cursor: HCursor = doc.hcursor
    cursor.downField("accounts").as[Map[Address, AddressInfo]]
  }

}