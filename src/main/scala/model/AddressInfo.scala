package model

case class AddressInfo(balance: JobCoin, transactions: List[Transaction])

case class Transaction(timestamp: String, fromAddress: Option[Address], toAddress: Address, amount: JobCoin)

object InitialTransaction {
  def apply(timestamp: String, toAddress: Address, amount: JobCoin): Transaction =
    Transaction(timestamp, None, toAddress, amount)
}