package service

import model.Address

trait HouseAccountService {
  val jobCoinApiDao: JobCoinApiDao

  def getHouseAccount: String
}

class SimpleHouseAccountService(val jobCoinApiDao: JobCoinApiDao,
                                houseAccount: Address) extends HouseAccountService {
  assert(jobCoinApiDao.getAddressInfo(houseAccount).transactions.nonEmpty,
         "Fatal initialization error: no house accounts exist for House Account Service (register at gemini.com/hide_your_money)")

  def getHouseAccount: String = houseAccount
}
