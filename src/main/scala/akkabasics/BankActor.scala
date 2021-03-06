package akkabasics

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akkabasics.BankActor.Person.FinancialLifecycle

object BankActor extends App {
  object BankAccount {
    case class Deposit(amount: Double)
    case class Withdraw(amount: Double)
    case object Statement
    case class TxnSuccess(message: String)
    case class TxnFailure(message: String)
  }
  class BankAccount extends Actor {
    import BankAccount._

    var balance: Double = 0

    override def receive: Receive = {
      case Deposit(amount) =>
        if (amount <= 0)
          sender() ! TxnFailure("Negative or zero deposit amount")
        else {
          balance += amount
          sender() ! TxnSuccess(s"Successfully deposited $amount")
        }

      case Withdraw(amount) =>
        if (amount <= 0)
          sender() ! TxnFailure("Negative or zero withdraw amount")
        else if (amount > balance)
          sender() ! TxnFailure("Not enough funds to withdraw amount")
        else {
          balance -= amount
          sender() ! TxnSuccess(s"Successfully received $amount")
        }

      case Statement =>
        sender() ! s"Your balance is $balance"
    }
  }

  object Person {
    case class FinancialLifecycle(account: ActorRef)
  }
  class Person extends Actor {
    import Person._
    import BankAccount._

    override def receive: Receive = {
      case FinancialLifecycle(account) =>
        account ! Deposit(100000.00)
        account ! Withdraw(500000.00)
        account ! Withdraw(50000.00)
        account ! Statement

      case message =>
        println(message.toString)
    }

  }

  val bankSystem = ActorSystem("BankSystem")
  val customerSystem = ActorSystem("CustomerSystem")
  val accountAlice = bankSystem.actorOf(Props[BankAccount], "alice")
  val alice = customerSystem.actorOf(Props[Person], "alice")

  alice ! FinancialLifecycle(accountAlice)
}
