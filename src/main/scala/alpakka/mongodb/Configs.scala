package alpakka.mongodb

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

object Configs {

  implicit val actorSystem = ActorSystem("Alpakka")
  implicit val materializer = ActorMaterializer()

  val filePath    = "vehicle_data.log"

}