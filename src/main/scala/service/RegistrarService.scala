package service

import java.util.UUID.randomUUID

import scala.collection.concurrent.{Map => CMap, TrieMap}
import model.Address

trait RegistrarService {
  def register(outBoxes: Set[Address]): Address

  def getOutBoxes(inBox: Address): Option[Set[Address]]

  def isRegistered(inBox: Address): Boolean

  def getInboxes: Set[Address]
}

class LocalRegistrarService(localDb: CMap[Address, Set[Address]] = TrieMap.empty) extends RegistrarService {

  override def register(outBoxes: Set[Address]): Address = {
    val inboxId: Address = randomUUID.toString
    localDb += (inboxId -> outBoxes)
    inboxId
  }

  override def getOutBoxes(inBox: Address): Option[Set[Address]] = localDb get inBox

  override def getInboxes: Set[Address] = localDb.keySet.toSet

  override def isRegistered(inBox: Address): Boolean = localDb.contains(inBox)
}

object LocalRegistrarService {
  def apply(localDb: CMap[Address, Set[Address]] = TrieMap.empty): LocalRegistrarService =
    new LocalRegistrarService(localDb)
}