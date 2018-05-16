import controller.ObligationProducer
import model.{Address, Obligation, _}
import org.scalatest.{MustMatchers, WordSpec}
import service._

import scala.concurrent.{Await, Promise}
import scala.concurrent.duration._

class ObligationProducerTest extends WordSpec with MustMatchers {
  private val HouseAccount = "meclador_house_account"
  private val BobDropBox = "bob_mec_box"
  private val BobBoxA = "bob_acc_a"
  private val BobBoxB = "bob_acc_b"

  private val inputDbJson: Map[Address, AddressInfo] =
    decodeJobCoinDB(
      """
        |{
        |  "accounts": {
        |    "meclador_house_account": {
        |      "balance": "0.01",
        |      "transactions": [
        |        {
        |          "timestamp": "2014-04-22T13:10:01.210Z",
        |          "toAddress": "meclador_house_account_primary",
        |          "amount": "0.01"
        |        }
        |      ]
        |    },
        |    "bob_acc_a": {
        |      "balance": "25",
        |      "transactions": [
        |        {
        |          "timestamp": "2014-04-22T13:10:01.210Z",
        |          "toAddress": "bob_acc_a",
        |          "amount": "25"
        |        }
        |      ]
        |    },
        |    "bob_acc_b": {
        |      "balance": "35",
        |      "transactions": [
        |        {
        |          "timestamp": "2014-04-22T13:10:01.210Z",
        |          "toAddress": "bob_acc_b",
        |          "amount": "35"
        |        }
        |      ]
        |    },
        |    "bob_mec_box": {
        |      "balance": "35",
        |      "transactions": [
        |        {
        |          "timestamp": "2014-04-22T13:10:01.210Z",
        |          "toAddress": "bob_mec_box",
        |          "amount": "35"
        |        }
        |      ]
        |    }
        |  }
        |}
      """.stripMargin).right.get

  "the obligation producer" should {
    "create obligations from withdrawn registered drop boxes" in {
      val jobCoinApi = new LocalJobCoinApiDao(inputDbJson)
      val houseService = new SimpleHouseAccountService(jobCoinApi, HouseAccount)

      val registrar: RegistrarService = new LocalRegistrarService(
        Map[Address, Set[Address]](BobDropBox -> Set(BobBoxA, BobBoxB)))

      val obligationPromise = Promise[Obligation]()

      val obligationProducerStub = new ObligationProducer(registrar,
                                                          houseService,
                                                          jobCoinApi,
                                                          pollRate = 1 second) {
        override def send(obligation: Obligation): Unit = obligationPromise.success(obligation)
      }

      val obligationResult = Await.result(obligationPromise.future, 2 seconds)

      obligationResult.dropBox mustEqual BobDropBox
      obligationResult.debt.value.equals(35) mustBe true

      jobCoinApi.getAddressInfo(BobDropBox).balance.value.equals(0) mustBe true
      jobCoinApi.getAddressInfo(HouseAccount).balance.value.equals(35.01) mustBe true

      obligationProducerStub.close()
    }
  }
}
