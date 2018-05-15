package service

import com.twitter.finagle.{http, Http, Service}
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Await
import io.circe.generic.auto._
import io.circe.parser.decode
import model._

trait JobCoinApiDao {
  def getAddressInfo(address: Address): AddressInfo

  def getTransactions: List[Transaction]

  def sendJobCoins(form: SendJobCoinForm): SendJobCoinResponse
}

class LocalJobCoinApiDao(private var sow: Map[Address, AddressInfo]) extends JobCoinApiDao {
  override def getAddressInfo(address: Address): AddressInfo =
    sow.getOrElse(address, default = AddressInfo(JobCoin(0), List.empty))

  override def getTransactions: List[Transaction] = sow.values.flatMap(_.transactions).toList.distinct

  override def sendJobCoins(form: SendJobCoinForm): SendJobCoinResponse = (for {
    fromAddressInfo <- sow.get(form.fromAddress).filter(_.balance >= form.amount)
    toAddressInfo <- sow.get(form.toAddress)
  } yield {
    val currentTime = System.currentTimeMillis().toString
    val newTransaction = Transaction(currentTime, Option(form.fromAddress), form.toAddress, form.amount)

    val fromBalance = fromAddressInfo.balance - form.amount
    val toBalance = toAddressInfo.balance + form.amount

    val fromTransactions = fromAddressInfo.transactions :+ newTransaction
    val toTransactions = toAddressInfo.transactions :+ newTransaction

    sow = sow + (form.fromAddress -> fromAddressInfo.copy(
      balance = fromBalance,
      transactions = fromTransactions))

    sow = sow + (form.toAddress -> toAddressInfo.copy(
      balance = toBalance,
      transactions = toTransactions))

    TransactionSuccess
  }).getOrElse(InsufficientFunds)

  def SOW: Map[Address, AddressInfo] = sow
}

// TODO: add config for setting route
class HttpJobCoinApiDao extends JobCoinApiDao {
  private lazy val client: Service[Request, Response] = Http.newService("jobcoin.projecticeland.net:80")

  override def getAddressInfo(address: Address): AddressInfo = {
    val request = http.Request(http.Method.Get, s"/unbountifulness/api/addresses/$address")

    request.host = "jobcoin.projecticeland.net"
    decode[AddressInfo](Await.result(client(request)).getContentString()).right.get
  }

  override def getTransactions: List[Transaction] = {
    val request = http.Request(http.Method.Get, s"/unbountifulness/api/transactions")

    request.host = "jobcoin.projecticeland.net"

    decode[List[Transaction]](Await.result(client(request)).getContentString()).right.get
  }

  override def sendJobCoins(form: SendJobCoinForm): SendJobCoinResponse = {
    val request = http.Request(http.Method.Post,
      s"/unbountifulness/api/transactions" +
        s"?fromAddress=${form.fromAddress}&amp" +
        s";toAddress=${form.toAddress}&amp" +
        s";amount=${form.amount.value.toString}")

    request.host = "jobcoin.projecticeland.net"
    Await.result(client(request)).status.code match {
      case 200 => TransactionSuccess
      case 422 => InsufficientFunds
    }
  }
}