import model.Address

package object service {
  case class UnregisteredDropBoxError(dropBox: Address) extends Throwable {
    override def getMessage: Address = s"Address '$dropBox' has not been registered with Mesclador. No obligation created."
  }

  case class InvalidPropertyError(property: String, value: Any, reason: String) extends Throwable {
    override def getMessage: Address = s"Cannot use $value as input for property '$property': $reason"
  }
}
