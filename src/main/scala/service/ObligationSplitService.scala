package service

import model.{Address, JobCoin, Obligation, PendingTransaction}

import scala.util.Random

class ObligationSplitService(registrarService: RegistrarService,
                             houseAccountService: HouseAccountService,
                             splitN: Int = 5) {

  if (splitN <= 0) throw InvalidPropertyError("splitN",
                                              splitN,
                                              "the number of partitions must be greater than or equal to 1")

  def splitObligation(obligation: Obligation): List[PendingTransaction] = {

    val outBoxes = registrarService.getOutBoxes(obligation.dropBox)
                   .getOrElse(throw UnregisteredDropBoxError(obligation.dropBox))

    val circular = shuffleContinually(outBoxes)

    split(obligation.debt, parts = splitN).map(v => PendingTransaction(
      fromAddress = houseAccountService.getHouseAccount,
      toAddress = circular.next,
      amount = JobCoin(v)
    ))
  }

  private def split(coin: JobCoin, parts: Int) = parts match {
    case 1 => List(coin.value)
    case _ =>
      val maxPartitionSize = coin.value / parts
      val ranPartitions = List.fill(parts - 1)(Random.nextFloat * maxPartitionSize)
      val tailPartition = coin.value - ranPartitions.sum
      ranPartitions :+ tailPartition
  }

  private def shuffleContinually(outBoxes: Iterable[Address]) =
    Iterator.continually(Random.shuffle(outBoxes)).flatten
}

object ObligationSplitService {
  def apply(registrarService: RegistrarService,
            houseAccountService: HouseAccountService): ObligationSplitService =
    new ObligationSplitService(registrarService, houseAccountService)
}