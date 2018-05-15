import org.scalatest.{MustMatchers, WordSpec}
import service.{LocalRegistrarService, RegistrarService}

class RegistrarSpec extends WordSpec with MustMatchers {
  "the registrar service" should {
    "register a user in the meclador registry" in {
      val registrar: RegistrarService = LocalRegistrarService()
      val bobsAddresses = Set("BobAddress1", "BobAddress2", "BobAddress3")
      val bobDepositAddress = registrar.register(bobsAddresses)

      registrar.isRegistered(bobDepositAddress) mustBe true
      registrar.getOutBoxes(bobDepositAddress).contains(bobsAddresses) mustBe true
    }
  }
}