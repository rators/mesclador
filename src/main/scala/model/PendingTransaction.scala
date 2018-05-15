package model

import java.util

import io.circe.generic.auto._
import io.circe.parser.decode
import io.circe.syntax._
import org.apache.kafka.common.serialization.{Deserializer, Serializer, StringDeserializer, StringSerializer}

case class PendingTransaction(fromAddress: Address, toAddress: Address, amount: JobCoin)

class PendingTransactionSerializer extends Serializer[PendingTransaction] {

  private val stringSerializer = new StringSerializer

  override def configure(configs: util.Map[String, _], isKey: Boolean): Unit =
    stringSerializer.configure(configs, isKey)

  override def serialize(topic: String, data: PendingTransaction): Array[Byte] =
    stringSerializer.serialize(topic, data.asJson.toString)

  override def close(): Unit =
    stringSerializer.close()
}

class PendingTransactionDeserializer extends Deserializer[PendingTransaction] {

  private val stringDeserializer = new StringDeserializer

  override def configure(configs: util.Map[String, _], isKey: Boolean): Unit =
    stringDeserializer.configure(configs, isKey)

  override def deserialize(topic: String, data: Array[Byte]): PendingTransaction =
    decode[PendingTransaction](stringDeserializer.deserialize(topic, data)).right.get

  override def close(): Unit =
    stringDeserializer.close()
}