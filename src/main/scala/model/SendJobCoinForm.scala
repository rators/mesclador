package model

case class SendJobCoinForm(fromAddress: Address, toAddress: Address, amount: JobCoin)

object SendJobCoinForm {
  def apply(pendingTransaction: PendingTransaction): SendJobCoinForm = SendJobCoinForm(
    pendingTransaction.fromAddress,
    pendingTransaction.toAddress,
    pendingTransaction.amount
  )
}

sealed trait SendJobCoinResponse

case object TransactionSuccess extends SendJobCoinResponse

case object InsufficientFunds extends SendJobCoinResponse