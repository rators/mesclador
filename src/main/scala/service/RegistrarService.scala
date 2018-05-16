package service

import java.io._
import java.util.UUID.randomUUID

import io.circe.parser.decode
import io.circe.syntax._
import model.Address

import scala.io.Source


trait RegistrarService {
  def register(outBoxes: Set[Address]): Address

  def getOutBoxes(inBox: Address): Option[Set[Address]]

  def isRegistered(inBox: Address): Boolean

  def getInboxes: Set[Address]

  def registryState: Map[Address, Set[Address]]
}

class LocalRegistrarService(private var localDb: Map[Address, Set[Address]] = Map.empty) extends RegistrarService {

  override def register(outBoxes: Set[Address]): Address = {
    val inboxId: Address = randomUUID.toString
    localDb = localDb + (inboxId -> outBoxes)
    inboxId
  }

  override def getOutBoxes(inBox: Address): Option[Set[Address]] = localDb get inBox

  override def getInboxes: Set[Address] = localDb.keySet

  override def isRegistered(inBox: Address): Boolean = localDb.contains(inBox)

  override def registryState: Map[Address, Set[Address]] = localDb
}

object LocalRegistrarService {
  def apply(localDb: Map[Address, Set[Address]] = Map.empty,
            stateFile: Option[String] = None): LocalRegistrarService =
    new LocalRegistrarService(localDb)

  def parseJson(jsonStr: Address): RegistrarService =
    decode[Map[Address, List[Address]]](jsonStr) match {
      case Right(state) => LocalRegistrarService(state.mapValues(_.toSet))
      case Left(err) => throw err
    }

  def toJson(registrarService: RegistrarService): String =
    registrarService.registryState.asJson.toString

  def loadFromFile(filePath: String): RegistrarService = {
    println(filePath)
    val fileContent = Source.fromFile(filePath).getLines.mkString
    parseJson(fileContent)
  }

  def exportToFile(registrarService: RegistrarService, fileName: String): Unit = {
    val writer = new PrintWriter(new File(fileName))
    val stateJsonStr = toJson(registrarService)
    writer.write(stateJsonStr)
    writer.close()
  }
}