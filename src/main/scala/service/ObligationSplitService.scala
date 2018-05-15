package service

import model.{JobCoin, Obligation, PendingTransaction}

import scala.util.Random

// TODO: add config properties. i.e. parts split
class ObligationSplitService(registrarService: RegistrarService,
                             houseAccountService: HouseAccountService) {

  def splitObligation(obligation: Obligation): List[PendingTransaction] = {
    // TODO remove get call and handle missing link
    val outBoxes = registrarService.getOutBoxes(obligation.dropBox).get
    val circular = Iterator.continually(Random.shuffle(outBoxes)).flatten
    println(Random.shuffle(outBoxes))
    split(obligation.debt, parts = 5).map(v => PendingTransaction(
      fromAddress = houseAccountService.getHouseAccount,
      toAddress = circular.next,
      amount = JobCoin(v)
    ))
  }

  private def split(coin: JobCoin, parts: Int): List[BigDecimal] = parts match {
    case 1 => List(coin.value)
    case _ =>
      val maxPartitionSize = coin.value / parts
      val ranPartitions = List.fill(parts - 1)(Random.nextFloat * maxPartitionSize)
      val tailPartition = coin.value - ranPartitions.sum
      ranPartitions :+ tailPartition
  }
}

object ObligationSplitService {
  def apply(registrarService: RegistrarService,
            houseAccountService: HouseAccountService): ObligationSplitService =
    new ObligationSplitService(registrarService, houseAccountService)
}