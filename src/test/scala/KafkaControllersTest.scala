import java.lang.System.currentTimeMillis

import akka.actor.ActorSystem
import cakesolutions.kafka.testkit.KafkaServer
import com.typesafe.config.ConfigFactory
import controller.{KafkaObligationConsumer, KafkaObligationProducer}
import model.{AddressInfo, JobCoin, Transaction}
import org.scalatest.{BeforeAndAfterAll, MustMatchers, WordSpec}
import service._

import scala.collection.concurrent.TrieMap

class KafkaControllersTest extends WordSpec with MustMatchers with BeforeAndAfterAll {
  val kafkaServer = new KafkaServer()

  val consumerConfig = ConfigFactory.parseString(
    s"""
       |bootstrap.servers="localhost:${kafkaServer.kafkaPort}"
       |topics=["obligations-test-topic"]
       |group.id=mixing
       |obligations.topic="obligations-test-topic"
       |pending.minDelay=1
       |pending.maxDelay=4
    """.stripMargin)

  val producerConfig = ConfigFactory.parseString(
    s"""
       |bootstrap.servers="localhost:${kafkaServer.kafkaPort}"
       |topic="obligations-test-topic"
       |obligations.topic="obligations-test-topic"
       |client.id="ObligationProducer"
    """.stripMargin)

  val sampleRegistryService = new LocalRegistrarService(TrieMap.empty)

  val jobCoinApiDao: JobCoinApiDao = new LocalJobCoinApiDao(Map(
    "BobsAddress" -> AddressInfo(JobCoin(50), List(Transaction(currentTimeMillis.toString, None, "BobsAddress", JobCoin(50)))),
    "AlicesAddress" -> AddressInfo(JobCoin(50), List(Transaction(currentTimeMillis.toString, None, "AlicesAddress", JobCoin(50))))
  ))
  val houseAccountService = new SimpleHouseAccountService(jobCoinApiDao, "BobsAddress")

  val obligationProducer = new KafkaObligationProducer(producerConfig,
    sampleRegistryService,
    houseAccountService,
    jobCoinApiDao)

  val splitService = ObligationSplitService(sampleRegistryService, houseAccountService)

  val actorSystem = ActorSystem("mixer-system")

  override def beforeAll(): Unit = kafkaServer.startup()

  override def afterAll(): Unit = kafkaServer.close()

  "the kafka server" should {
    "work" in {
      val consumerActor = actorSystem.actorOf(
        KafkaObligationConsumer.props(consumerConfig,
          splitService,
          TransactionSchedulerService(jobCoinApiDao)))
      import scala.concurrent.duration._
      Thread.sleep(15.seconds.toMillis)
    }
  }
}
