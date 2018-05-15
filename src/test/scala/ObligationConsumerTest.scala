import controller.ObligationConsumer
import model.{Address, AddressInfo, JobCoin, Obligation, PendingTransaction, Transaction}
import monix.execution.Cancelable
import org.scalatest.{BeforeAndAfterAll, MustMatchers, WordSpec}
import service._

import scala.collection.concurrent.TrieMap
import scala.concurrent.duration.FiniteDuration

class ObligationConsumerTest extends WordSpec with MustMatchers with BeforeAndAfterAll {
  private val HouseAccount = "meclador_house_account"
  private val BobDropBox = "bob_mec_box"
  private val BobBoxA = "bob_acc_a"
  private val BobBoxB = "bob_acc_b"

  private val registrar: RegistrarService = new LocalRegistrarService(
    TrieMap[Address, Set[Address]](BobDropBox -> Set(BobBoxA, BobBoxB))
  )

  private val initTransaction = Transaction("INIT_DATE", None, HouseAccount, JobCoin(10))
  private val jobCoinState = Map(HouseAccount -> AddressInfo(JobCoin(10), List(initTransaction)))
  private val jobCoinApi = new LocalJobCoinApiDao(jobCoinState)

  private val houseAccountService = new SimpleHouseAccountService(jobCoinApi, HouseAccount)

  private val splitService = ObligationSplitService(registrar, houseAccountService)


  "an obligation consumer" should {
    "send pending obligations to the scheduler" in {
      var scheduledTransactions: List[PendingTransaction] = List.empty

      val transactionSchedulerServiceStub = new TransactionSchedulerService(jobCoinApi) {

        override def scheduleOnceRandom(pendingTransaction: PendingTransaction,
                                        minDelay: FiniteDuration,
                                        maxDelay: FiniteDuration): Cancelable = {
          scheduledTransactions :+= pendingTransaction
          super.scheduleOnceRandom(pendingTransaction, minDelay, maxDelay)
        }
      }

      val consumerStub = new ObligationConsumer(splitService,
                                                transactionSchedulerServiceStub)

      consumerStub.receiveObligation(Obligation(BobDropBox, JobCoin(10)))

      scheduledTransactions.map(_.amount.value).sum mustEqual 10
    }
  }
}
