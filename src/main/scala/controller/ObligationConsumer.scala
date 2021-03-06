package controller

import akka.actor.{Actor, Props}
import cakesolutions.kafka.KafkaConsumer
import cakesolutions.kafka.akka.KafkaConsumerActor.{Confirm, Subscribe, Unsubscribe}
import cakesolutions.kafka.akka.{ConsumerRecords, KafkaConsumerActor}
import com.typesafe.config.Config
import model.{Obligation, _}
import monix.execution.Cancelable
import org.apache.kafka.common.serialization.StringDeserializer
import service.{ObligationSplitService, TransactionSchedulerService}

import scala.collection.JavaConverters._
import scala.concurrent.duration._

class ObligationConsumer(obligationSplitService: ObligationSplitService,
                         transactionSchedulerService: TransactionSchedulerService,
                         minDelay: FiniteDuration = 2 seconds,
                         maxDelay: FiniteDuration = 10 seconds) {

  def receiveObligation(obligation: Obligation): List[Cancelable] =
    obligationSplitService
      .splitObligation(obligation)
      .map(scheduleOnceRandom)

  private def scheduleOnceRandom: PendingTransaction => Cancelable =
    transactionSchedulerService.scheduleOnceRandom(_, minDelay, maxDelay)

}

class KafkaObligationConsumer(config: Config,
                              obligationSplitService: ObligationSplitService,
                              transactionSchedulerService: TransactionSchedulerService)
  extends ObligationConsumer(obligationSplitService,
                             transactionSchedulerService,
                             config.getInt("pending.minDelay").seconds,
                             config.getInt("pending.maxDelay").seconds) with Actor {

  private val extractor = ConsumerRecords.extractor[String, Obligation]

  private val kafkaConsumerActor = context.actorOf(
    KafkaConsumerActor.props(
      consumerConf = KafkaConsumer.Conf(
        config,
        keyDeserializer = new StringDeserializer,
        valueDeserializer = new ObligationDeserializer
      ),
      actorConf = KafkaConsumerActor.Conf(1.seconds, 3.seconds),
      self
    ), "KafkaObligationConsumer")

  override def preStart(): Unit = {
    val inputTopics = config.getStringList("topics")
    super.preStart()
    kafkaConsumerActor ! Subscribe.AutoPartition(inputTopics.asScala)
  }

  override def postStop(): Unit = {
    kafkaConsumerActor ! Unsubscribe
    super.postStop()
  }

  override def receive: PartialFunction[Any, Unit] = {
    case extractor(consumerRecords) =>
      kafkaConsumerActor ! Confirm(consumerRecords.offsets, commit = true)
      consumerRecords.pairs.foreach {
        case (key, obligation: Obligation) => receiveObligation(obligation)
      }
  }
}

object KafkaObligationConsumer {
  def props(config: Config,
            obligationSplitService: ObligationSplitService,
            transactionSchedulerService: TransactionSchedulerService) =
    Props(new KafkaObligationConsumer(config, obligationSplitService, transactionSchedulerService))
}