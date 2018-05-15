import io.circe.generic.auto._
import io.circe.syntax._
import model._
import org.scalatest.{MustMatchers, WordSpec}

class DecodeEncodeSpec extends WordSpec with MustMatchers {
  "PendingTransaction" should {
    "be encoded/decoded" in {
      val pendingJson = PendingTransaction("from", "to", JobCoin(BigDecimal(1))).asJson
      pendingJson.as[PendingTransaction].right.toOption.contains(pendingJson)
    }
  }

  "Obligation" should {
    "be encoded/decoded" in {
      val obligationJson = Obligation("dropBox", JobCoin(BigDecimal(1))).asJson
      obligationJson.as[Obligation].right.toOption.contains(obligationJson)
    }
  }

  "AddressInfo and Transaction" should {
    "be encoded/decoded" in {
      val addressJson = AddressInfo(JobCoin(BigDecimal(1)), List(
        Transaction(System.currentTimeMillis().toString, None, toAddress = "bob_address", JobCoin(BigDecimal(1)))
      )).asJson

      addressJson.as[AddressInfo].right.toOption.contains(addressJson)
    }
  }
}