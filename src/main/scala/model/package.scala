import java.util.Properties

import com.typesafe.config.Config
import io.circe.Decoder.Result
import io.circe.generic.auto._
import io.circe.parser.parse
import io.circe.{Decoder, Encoder, HCursor, Json}

import scala.collection.JavaConverters._
import scala.collection.immutable.Map
import scala.collection.mutable

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

  implicit class RichConfig(val config: Config) extends AnyVal {

    /**
      * Convert Typesafe config to Java `Properties`.
      */
    def toProperties: Properties = {
      val props = new Properties()
      config.entrySet().asScala.foreach(entry => props.put(entry.getKey, entry.getValue.unwrapped().toString))
      props
    }

    /**
      * Convert Typesafe config to a Scala map.
      */
    def toPropertyMap: Map[String, AnyRef] = {
      config.entrySet().asScala.toList.map(entry => entry.getKey -> entry.getValue.unwrapped().toString).toMap
    }
  }

}