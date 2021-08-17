package paymentproc

import akka.actor.{ActorLogging, ActorSystem, Props}
import akka.persistence.PersistentActor

import java.util.Date
import scala.util.Random

object PaymentProcDemo extends App {

  //COMMANDS
  case class Payment(paymentId: String, tenantId: String, paymentType: String, date: Date, amount: Double)

  //EVENTS
  case class PaymentRecorded(id: String, paymentId: String, tenantId: String, paymentType: String, date: Date, amount: Double)

  class PaymentDecoratorActor extends PersistentActor with ActorLogging {
    override def receiveRecover: Receive = {
      case PaymentRecorded(id, paymentId, tenantId, paymentType, date, amount) =>
        log.info("Recovered payment persistence ID :: {} for paymentId :: {} for amount :: {}", id, paymentId, amount)
    }

    override def receiveCommand: Receive = {
      case Payment(paymentId, tenantId, paymentType, date, amount) =>
        log.info("Received payment :: {} for amount :: {}", paymentId, amount)
        val event = PaymentRecorded(java.util.UUID.randomUUID.toString, paymentId, tenantId, paymentType, date, amount)
        persist(event) { e =>
          //Update state
          log.info("Persisted {} as payment ## {} for amount :: {}", e, e.paymentId, e.amount)
        }
    }

    override def persistenceId: String = "payment-proc"
  }

  val system = ActorSystem("PaymentProcessor")

  val paymentProc = system.actorOf(Props[PaymentDecoratorActor], "paymentProc")

//  for(i <- 1 to 10) {
//    paymentProc ! Payment("PAY-" + i, "DemoTenant", "SIMPLE", new Date, 2500.00 * Random.nextInt(i))
//  }

}
