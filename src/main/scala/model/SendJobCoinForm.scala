package model

case class SendJobCoinForm(fromAddress: Address, toAddress: Address, amount: JobCoin)

sealed trait SendJobCoinResponse

case object TransactionSuccess extends SendJobCoinResponse
case object InsufficientFunds extends SendJobCoinResponse