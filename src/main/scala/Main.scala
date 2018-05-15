import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import controller.{KafkaObligationConsumer, KafkaObligationProducer}
import service._

import scala.collection.concurrent.TrieMap
import scala.io.StdIn

object Main extends App {
  val consumerConfig = ConfigFactory.parseString(
    """
      |bootstrap.servers="localhost:9092"
      |topics=["obligations-test-topic"]
      |group.id=mixing
      |pending.minDelay=1
      |pending.maxDelay=4
    """.stripMargin)

  val producerConfig = ConfigFactory.parseString(
    """
      |bootstrap.servers="localhost:9092"
      |topic="obligations-test-topic"
      |client.id="ObligationProducer"
      |pollRate=5
    """.stripMargin)

  val sampleRegistryService = new LocalRegistrarService(TrieMap.empty)

  val jobCoinApiDao = new HttpJobCoinApiDao("jobcoin.projecticeland.net", 80, "unbountifulness")

  val houseAccountService = new SimpleHouseAccountService(jobCoinApiDao, "mixer")

  val obligationProducer = new KafkaObligationProducer(producerConfig,
                                                       sampleRegistryService,
                                                       houseAccountService,
                                                       jobCoinApiDao)

  val splitService = ObligationSplitService(sampleRegistryService,
                                            houseAccountService)

  val actorSystem = ActorSystem("mixer-system")

  val consumerActor = actorSystem.actorOf(
    KafkaObligationConsumer.props(consumerConfig,
                                  splitService,
                                  TransactionSchedulerService(jobCoinApiDao)))

  while (true) {
    val input = StdIn.readLine("Enter addresses you own separated by commas: ").split(",").toSet
    val dropBox = sampleRegistryService.register(input)
    println(s"Your designated Meclador drop box is [$dropBox]")
  }
}
