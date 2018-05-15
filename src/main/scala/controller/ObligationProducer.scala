package controller

import java.util.UUID

import cakesolutions.kafka.{KafkaProducer, KafkaProducerRecord}
import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import model.{InsufficientFunds, Obligation, SendJobCoinForm, TransactionSuccess, _}
import monix.execution.{Cancelable, Scheduler}
import org.apache.kafka.common.serialization.StringSerializer
import service.{HouseAccountService, JobCoinApiDao, RegistrarService}

import scala.concurrent.duration._
import scala.util.Try

abstract class ObligationProducer(registrarService: RegistrarService,
                                  houseAccountService: HouseAccountService,
                                  jobCoinApiDao: JobCoinApiDao,
                                  pollRate: FiniteDuration) extends LazyLogging {

  private val scheduler = Scheduler(Scheduler.DefaultScheduledExecutor)

  private val pollTask: Cancelable = scheduler.scheduleWithFixedDelay(0 second, pollRate) {
    logger.info(s"Polling job-coin db")
    registrarService.getInboxes.toIterator.foreach { address =>
      val info = jobCoinApiDao.getAddressInfo(address)
      if (info.balance.value > 0) jobCoinApiDao.sendJobCoins(
        form = SendJobCoinForm(address, houseAccountService.getHouseAccount, info.balance)
      ) match {
        case TransactionSuccess =>
          val obligation = Obligation(address, info.balance)
          send(obligation)
          logger.info(s"Producing obligation $obligation")
        case InsufficientFunds =>
          logger.error(s"Insufficient Funds: Unable to retrieve ${info.balance} from inbox $address")
      }
    }
  }

  def send(obligation: Obligation): Unit

  def close(): Unit = pollTask.cancel()

}

class KafkaObligationProducer(val config: Config,
                              val registrarService: RegistrarService,
                              val houseAccountService: HouseAccountService,
                              val jobCoinApiDao: JobCoinApiDao)
  extends ObligationProducer(
    registrarService,
    houseAccountService,
    jobCoinApiDao,
    Try(config.getInt("pollRate")).toOption.fold(5 seconds)(_.toString.toInt seconds)
  ) with LazyLogging {

  private val producer = KafkaProducer(
    KafkaProducer.Conf(
      config,
      keySerializer = new StringSerializer,
      valueSerializer = new ObligationSerializer))

  override def send(obligation: Obligation): Unit = {
    val record = KafkaProducerRecord[String, Obligation](
      topic = config.getString("topic"),
      key = Option(UUID.randomUUID.toString),
      value = obligation
    )
    producer.send(record)
  }

  override def close(): Unit = {
    super.close()
    producer.close()
  }

}