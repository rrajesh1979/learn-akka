package alpakka.mongodb

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.alpakka.mongodb.scaladsl.MongoSink
import akka.stream.scaladsl.Source
import com.mongodb.reactivestreams.client.{MongoClients, MongoCollection}
import com.typesafe.config.ConfigFactory

class MongoConnection(url: String) {

  final val client = MongoClients.create(url)
  final val db = client.getDatabase("vehicle-tracker")

}

object Collections extends App {

  final val conf = ConfigFactory.load()
  val port = conf.getLong("alpakka.mongo.connection.port")
  val ipAddress = conf.getString("alpakka.mongo.connection.ip")
  val connectionUrl = s"mongodb://$ipAddress:$port"

  final val connection = new MongoConnection(connectionUrl)
  val db = connection.db

  val vehicleDataCollection: MongoCollection[VehicleData] = db
    .getCollection(classOf[VehicleData].getSimpleName, classOf[VehicleData])
    .withCodecRegistry(CodecRegistry.vehicleCodec)

  val simpleSource = Source(List(
    "1, 70.23857, 16.239987",
    "1, 70.876, 16.188",
    "2, 17.87, 77.71443",
    "3, 55.7712, 16.9088"
  ))

  implicit val actorSystem = ActorSystem("Alpakka")
  implicit val materializer = ActorMaterializer()
  simpleSource
    .map { data =>
      val v = data.trim.split(",")
      VehicleData(v(0).toLong, GPSLocation(v(1).toDouble, v(2).toDouble))
    }
    .runWith {
      MongoSink.insertOne(vehicleDataCollection)
    }
}