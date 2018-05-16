import com.typesafe.config.Config
import service._

abstract class CoreService {
  val registryPath: String
  val producerConfig: Config
  val consumerConfig: Config

  lazy val registrarService: RegistrarService = LocalRegistrarService.loadFromFile(registryPath)

  lazy val jobCoinApiDao = new HttpJobCoinApiDao("jobcoin.gemini.com", 80, "polyester")

  lazy val houseAccountService = new SimpleHouseAccountService(jobCoinApiDao, "mixer")

  lazy val splitService = ObligationSplitService(registrarService,
                                            houseAccountService)
}