import model.{Address, AddressInfo, JobCoin, Obligation, Transaction}
import org.scalatest.{MustMatchers, WordSpec}
import service._

import scala.collection.concurrent.Map

class ObligationSplitServiceSpec extends WordSpec with MustMatchers {
  private val HouseAccount = "meclador_house_account"
  private val BobDropBox = "bob_mec_box"
  private val BobBoxA = "bob_acc_a"
  private val BobBoxB = "bob_acc_b"

  "the obligation split service" should {
    "split an obligation into n parts" in {
      val registrar: RegistrarService = new LocalRegistrarService(
        Map[Address, Set[Address]](BobDropBox -> Set(BobBoxA, BobBoxB))
      )

      val initTransaction = Transaction("INIT_DATE", None, HouseAccount, JobCoin(10))
      val jobCoinState = Map(HouseAccount -> AddressInfo(JobCoin(10), List(initTransaction)))
      val jobCoinApi = new LocalJobCoinApiDao(jobCoinState)

      val houseAccountService = new SimpleHouseAccountService(jobCoinApi, HouseAccount)

      val splitService = ObligationSplitService(registrar, houseAccountService)

      val obligation = Obligation(BobDropBox, JobCoin(10))
      val obligationSplit = splitService.splitObligation(obligation)

      val total = obligationSplit.map(_.amount.value).sum
      total.equals(obligation.debt.value) mustBe true
    }
  }
}
