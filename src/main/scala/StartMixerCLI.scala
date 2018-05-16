import service.{LocalRegistrarService, RegistrarService}

import scala.io.StdIn

object StartMixerCLI {
  def apply(registry: RegistrarService, exportPath: String): Unit = {
    while (true) {
      StdIn.readLine("mesclador> Enter addresses you own separated by commas: ") match {
        case "exit" =>
          LocalRegistrarService.exportToFile(registry, exportPath)
          println(s"mesclador> Saved registry state to: $exportPath")
          sys.exit(0)
        case input =>
          val targetBoxSet = input.split(",").toSet
          val dropBox = registry.register(targetBoxSet)
          println(s"mesclador> Your designated Meclador drop box is [$dropBox]")
      }
    }
  }
}
