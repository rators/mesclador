package model

case class Transaction(timestamp: String, fromAddress: Option[Address], toAddress: Address, amount: JobCoin)

case class PendingTransaction(fromAddress: Address, toAddress: Address, amount: JobCoin)

case class Obligation(dropBox: Address, debt: JobCoin)

case class AddressInfo(balance: JobCoin, transactions: List[Transaction])
