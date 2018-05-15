package service

import com.typesafe.scalalogging.StrictLogging
import model.{PendingTransaction, SendJobCoinForm}
import monix.execution.Cancelable

import scala.concurrent.duration.FiniteDuration
import monix.execution.Scheduler.{global => scheduler}

import scala.util.Random

class TransactionSchedulerService(jobCoinApiDao: JobCoinApiDao) extends StrictLogging {

  def scheduleOnceRandom(pendingTransaction: PendingTransaction,
                         minDelay: FiniteDuration,
                         maxDelay: FiniteDuration): Cancelable = {
    val sendJobCoinForm = SendJobCoinForm(
      pendingTransaction.fromAddress,
      pendingTransaction.toAddress,
      pendingTransaction.amount
    )

    val intervalSlack = (maxDelay - minDelay) * Random.nextFloat
    val randomFiniteDelay = minDelay + intervalSlack
    val scheduleFn: Runnable = () => jobCoinApiDao.sendJobCoins(sendJobCoinForm)
    scheduler.scheduleOnce(randomFiniteDelay._1,
                           randomFiniteDelay._2,
                           scheduleFn)
  }
}

object TransactionSchedulerService {
  def apply(dao: JobCoinApiDao): TransactionSchedulerService =
    new TransactionSchedulerService(dao)
}


