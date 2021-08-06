package akkabasics.watcher

import akka.actor.{Actor, ActorLogging, ActorSystem, PoisonPill, Props, Terminated}

object WatcherExample extends App {
  object Parent {
    case class StartChild(name: String)
    case class StopChild(name: String)
    case object Stop
  }

  class Watcher extends Actor with ActorLogging {
    import Parent._

    override def receive: Receive = {
      case StartChild(name) =>
        val child = context.actorOf(Props[Echo], name)
        log.info(s"Started and watching child $name")
        context.watch(child)
      case Terminated(ref) =>
        log.info(s"the reference that I'm watching $ref has been stopped")
    }
  }

  class Echo extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => log.info(message.toString)
    }
  }

  val system = ActorSystem("StoppingActorsDemo")
  val watcher = system.actorOf(Props[Watcher], "watcher")

  import Parent._

  watcher ! StartChild("watchedChild")
  val watchedChild = system.actorSelection("/user/watcher/watchedChild")
  Thread.sleep(500)

  watchedChild ! PoisonPill
}
