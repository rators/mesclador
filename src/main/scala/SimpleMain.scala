import java.io.File
import java.net.URLDecoder

import com.typesafe.config.{Config, ConfigFactory}
import controller.{ObligationConsumer, ObligationProducer}
import model.Obligation
import service._

import scala.concurrent.duration._

object SimpleMain extends CoreService with App {
  override val registryPath: String = URLDecoder.decode(args(0), "UTF-8")

  lazy val rootConfig = ConfigFactory.parseFile(new File(URLDecoder.decode(args(1), "UTF-8")))

  lazy val consumerConfig: Config = rootConfig.getConfig("consumer")
  lazy val producerConfig: Config = rootConfig.getConfig("producer")

  lazy val obligationConsumer = new ObligationConsumer(
    splitService,
    TransactionSchedulerService(jobCoinApiDao))

  lazy val obligationProducer = new ObligationProducer(registrarService,
                                                  houseAccountService,
                                                  jobCoinApiDao,
                                                  producerConfig.getInt("pollRate").seconds) {
    override def send(obligation: Obligation): Unit =
      obligationConsumer.receiveObligation(obligation)
  }

  StartMixerCLI(registrarService, registryPath)
}
