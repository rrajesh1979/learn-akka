package akkabasics

import akka.actor.FSM.Shutdown
import akka.actor.{Actor, ActorLogging, ActorSystem, Props, Terminated}
import akka.routing.{ActorRefRoutee, RoundRobinRoutingLogic, Router}
import org.bson.types.ObjectId

import java.io.File
import java.util.UUID.randomUUID
import scala.collection.mutable
import scala.io.Source
import org.mongodb.scala._

import scala.collection.JavaConverters._
import java.util.concurrent.TimeUnit
import scala.concurrent.Await
import scala.concurrent.duration.Duration


object WordCounterApp extends App {
  val VERSION: String = "0.0.1"
  val sourceDirectory: String = "/Users/rrajesh1979/Documents/Learn/gitrepo/word-count/java-wc-thread/src/main/resources/stagefiles"
  val NUM_PERSISTORS = 200

  //STEP 0: get list of files from source directory
  def getListOfFiles(dir: String) : List[File] = {
    val d = new File(dir)
    if (d.exists && d.isDirectory) {
      d.listFiles.filter(_.isFile).toList
    } else {
      List[File]()
    }
  }

  val fileList = getListOfFiles(sourceDirectory)

  val wordCountActorSystem = ActorSystem("WordCountActorSystem")

  import WCMessages._
  fileList.foreach(file =>
    wordCountActorSystem.actorOf(Props(new FileReader(file)), "fileReader" + fileList.indexOf(file)) ! ReadFile
  )

  val aggregator = wordCountActorSystem.actorOf(Props[Aggregator], "aggregator")
  val persistor = wordCountActorSystem.actorOf(Props[ParentPersistor], "persistor")

  //STEP 1: FileReadActor - create one actor to process each file.
  //FileReadActor - reads each file and sends each line to a new WordCount actor.
  object WCMessages {
    case class FileName(fileName: String)
    case object ReadFile
    case object CountWords
    case class AggregateWords(fileName: String, wc: Int)
    case class PrintCount(fileName: String)
    case class InsertLine(collection: MongoCollection[Document], file: String, line: String)
  }
  class FileReader(val file: File) extends Actor with ActorLogging {
    import WCMessages._
    // Use a Connection String
    val mongoClient: MongoClient = MongoClient("mongodb://localhost:27017")
    val database: MongoDatabase = mongoClient.getDatabase("wordcountdb")
    val collection: MongoCollection[Document] = database.getCollection("lines")

    override def receive: Receive = {
      case ReadFile =>
        log.info("Inside ReadFile for file :: {}", file)
        val sourceFile = Source.fromFile(file)
        for (line <- sourceFile.getLines()) {
          context.actorOf(Props(new WordCounter(collection, file, line)), "wordCounter" + randomUUID().toString) ! CountWords
          persistor ! InsertLine(collection, file.toString, line)
        }
        sourceFile.close()
    }
  }

  //STEP 2: Word Count Actor - counts words and sends to AggregatorActor
  class WordCounter(val collection: MongoCollection[Document], val file: File, val line: String) extends Actor with ActorLogging {
    import WCMessages._

    override def receive: Receive = {
      case CountWords =>
//        log.info("Number of words in line :: {} :: is {}",line, line.split(" ").length)
        aggregator ! new AggregateWords(file.toString, line.split(" ").length)
//        context.actorOf(Props[PersistLines], "wordCounter" + randomUUID().toString) ! InsertLine(collection, file.toString, line)
        //TODO: do we create a new actor each time or just have one actor instance doing the work
        //TODO: check how the connection pool for mongo is manged. Should not create multiple connections.

    }
  }

  //STEP 3a : Send message to AggregatorActor to print statistics
  class Aggregator extends Actor with ActorLogging {
    import WCMessages._

    override def receive: Receive = {
      case AggregateWords(file: String, wc: Int) =>
        context.become(updateCount(Map(file -> wc)))
    }

    def updateCount(wordCount: Map[String, Int]): Receive = {
      case AggregateWords(file: String, wc: Int) =>
        var newWordCount: Map[String, Int] = Map()
        if (!wordCount.contains(file)) newWordCount = wordCount.updated(file, wc)
        else newWordCount = wordCount.updated(file, wordCount(file) + wc)
        context.become(updateCount(newWordCount))
//        log.info("File :: {}, line count :: {}", file, newWordCount.get(file))

      case PrintCount(file: String) =>
        log.info("File :: {}, line count :: {}", file, wordCount.get(file))
    }
  }

  //STEP 3b : Insert into MongoDB
  class PersistLines extends Actor with ActorLogging {
    import WCMessages._
    import Helpers._

    override def receive: Receive = {
      case InsertLine(collection: MongoCollection[Document], file: String, line: String) =>
//        log.info("Persisting line :: {} :: in file :: {}", line, file)
        val doc: Document = Document("_id" -> new ObjectId, "file" -> file, "line" -> line)
        collection.insertOne(doc).results() //TODO: asynchronous call
        //self ! Shutdown ???
    }
  }

  class ParentPersistor extends Actor with ActorLogging {
    import WCMessages._
    import Helpers._

    var router: Router = {
      val routees = Vector.fill(NUM_PERSISTORS) {
        val r = context.actorOf(Props[PersistLines]())
        context.watch(r)
        ActorRefRoutee(r)
      }
      Router(RoundRobinRoutingLogic(), routees)
    }

    def receive: Receive = {
      case w: InsertLine =>
        router.route(w, sender())
      case Terminated(a) =>
        router = router.removeRoutee(a)
        val r = context.actorOf(Props[ParentPersistor]())
        context.watch(r)
        router = router.addRoutee(r)
    }

  }

//  fileList.foreach(file =>
//    aggregator ! PrintCount(file.toString)
//  )

  object Helpers {

    implicit class DocumentObservable[C](val observable: Observable[Document]) extends ImplicitObservable[Document] {
      override val converter: Document => String = doc => doc.toJson
    }

    implicit class GenericObservable[C](val observable: Observable[C]) extends ImplicitObservable[C] {
      override val converter: (C) => String = doc => doc.toString
    }

    trait ImplicitObservable[C] {
      val observable: Observable[C]
      val converter: (C) => String

      def results(): Seq[C] = Await.result(observable.toFuture(), Duration(10, TimeUnit.SECONDS))
      def headResult() = Await.result(observable.head(), Duration(10, TimeUnit.SECONDS))
      def printResults(initial: String = ""): Unit = {
        if (initial.length > 0) print(initial)
        results().foreach(res => println(converter(res)))
      }
      def printHeadResult(initial: String = ""): Unit = println(s"${initial}${converter(headResult())}")
    }

  }

}
