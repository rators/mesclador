package service

import java.util.UUID.randomUUID

import io.circe.parser.decode
import model.Address


trait RegistrarService {
  def register(outBoxes: Set[Address]): Address

  def getOutBoxes(inBox: Address): Option[Set[Address]]

  def isRegistered(inBox: Address): Boolean

  def getInboxes: Set[Address]
}

class LocalRegistrarService(private var localDb: Map[Address, Set[Address]] = Map.empty) extends RegistrarService {

  override def register(outBoxes: Set[Address]): Address = {
    val inboxId: Address = randomUUID.toString
    localDb = localDb + (inboxId -> outBoxes)
    inboxId
  }

  override def getOutBoxes(inBox: Address): Option[Set[Address]] = localDb get inBox

  override def getInboxes: Set[Address] = localDb.keySet.toSet

  override def isRegistered(inBox: Address): Boolean = localDb.contains(inBox)
}

object LocalRegistrarService {
  def apply(localDb: Map[Address, Set[Address]] = Map.empty): LocalRegistrarService =
    new LocalRegistrarService(localDb)

  def fromJsonState(jsonStr: Address): RegistrarService =
    decode[Map[Address, List[Address]]](jsonStr) match {
      case Right(state) => LocalRegistrarService(state.mapValues(_.toSet))
      case Left(err) => throw err
    }


}