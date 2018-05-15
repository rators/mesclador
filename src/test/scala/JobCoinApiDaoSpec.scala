import java.lang.System.currentTimeMillis

import model.{AddressInfo, InsufficientFunds, JobCoin, SendJobCoinForm, Transaction, TransactionSuccess}
import org.scalatest.{MustMatchers, WordSpec}
import service.{JobCoinApiDao, LocalJobCoinApiDao}

class JobCoinApiDaoSpec extends WordSpec with MustMatchers {
  "the jobcoin api" should {
    "execute a transaction between two individuals with sufficient funds" in {
      val jobCoinDao: JobCoinApiDao = new LocalJobCoinApiDao(Map(
        "BobsAddress" -> AddressInfo(JobCoin(50), List(Transaction(currentTimeMillis.toString, None, "BobsAddress", JobCoin(50)))),
        "AlicesAddress" -> AddressInfo(JobCoin(50), List(Transaction(currentTimeMillis.toString, None, "AlicesAddress", JobCoin(50))))
      ))

      val sendAliceReqForm = SendJobCoinForm("BobsAddress", "AlicesAddress", JobCoin(30))

      jobCoinDao.sendJobCoins(sendAliceReqForm) mustBe TransactionSuccess

      jobCoinDao.getAddressInfo("BobsAddress").balance.value mustBe 20
      jobCoinDao.getAddressInfo("AlicesAddress").balance.value mustBe 80
    }

    "fail to execute a transaction when the sender has insufficient funds" in {
      val jobCoinDao: JobCoinApiDao = new LocalJobCoinApiDao(Map(
        "BobsAddress" -> AddressInfo(JobCoin(10), List(Transaction(currentTimeMillis.toString, None, "BobsAddress", JobCoin(10)))),
        "AlicesAddress" -> AddressInfo(JobCoin(50), List(Transaction(currentTimeMillis.toString, None, "AlicesAddress", JobCoin(50))))
      ))

      val sendAliceReqForm = SendJobCoinForm("BobsAddress", "AlicesAddress", JobCoin(30))

      jobCoinDao.sendJobCoins(sendAliceReqForm) mustBe InsufficientFunds
    }
  }
}
