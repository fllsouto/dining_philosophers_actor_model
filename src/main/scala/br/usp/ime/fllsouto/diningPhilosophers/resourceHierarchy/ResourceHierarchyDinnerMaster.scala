package br.usp.ime.fllsouto.diningPhilosophers.resourceHierarchy

import br.usp.ime.fllsouto.diningPhilosophers._
import akka.actor._
import scala.concurrent.duration._
import akka.pattern.gracefulStop
import scala.concurrent.{Await, ExecutionContext, Future}


case class TerminateDinner(philos: List[ActorRef], forks: IndexedSeq[ActorRef], logger: ActorRef)


class ResourceHierarchyDinnerMaster(duration: FiniteDuration, fakeRandom: Boolean) extends Actor {
  import context._
  val r = scala.util.Random

  def receive  = { 
    case StartDinner() =>
      r.setSeed(System.currentTimeMillis)
      run()

    case TerminateDinner(philos: List[ActorRef], forks: IndexedSeq[ActorRef], logger: ActorRef) =>
      philos.foreach{ _ ! PoisonPill }
      forks.foreach{  _ ! PoisonPill }

      val stopped: Future[Boolean] = gracefulStop(logger, 5.seconds)
      Await.result(stopped, 3.seconds)

      self ! PoisonPill

      case undefMessage => println("[%s][%s] Actor: %s, State: %s, Message: %s".format("ResourceHierarchy", "ResourceHierarchyDinnerMaster", self.path.name, "receive", undefMessage))
  }

  private def run() {
    val fn = getOutputName()
    val philosName = List("Descartes", "Seneca", "Rousseau", "Nietzsche", "Hobbes")

    val logger = system.actorOf(Props(classOf[DinnerLogger], fn, duration.fromNow), "Dinner_Logger")
    val forks = createForks(philosName, logger)
    val philos = createPhilosophers(philosName, forks, logger)

    logger ! WriteProgressBar
    philos.foreach{ _ ! Think }
    system.scheduler.scheduleOnce(duration, self, TerminateDinner(philos, forks, logger))
  }

  private def createForks(philosName: List[String], logger: ActorRef): IndexedSeq[ActorRef] = {
    val forksQ: Int = philosName.length
    return for(position <- 0 until forksQ) yield createFork(position, logger)
  }

  private def createFork(position: Int, logger: ActorRef): ActorRef = {
    return system.actorOf(Props(classOf[Fork], "Fork_" + position, logger), "Fork_" + position)
  }

  private def createPhilosophers(philoNames: List[String], forks: IndexedSeq[ActorRef], logger: ActorRef): List[ActorRef] = {
    val philosQ: Int  = philoNames.length
    return for { (name, position) <- philoNames.zipWithIndex } yield createPhilosopher(
      "%s--%d".format(name, position),
      forks(position),
      forks((position + 1) % philosQ),
      if ((position + 1) == philosQ) "right-handed" else "left-handed",
      logger,
      getRandomSecond(),
      getRandomSecond())
  }

  private def createPhilosopher(name: String, left: ActorRef, right: ActorRef, handedness: String, logger: ActorRef, thinkingTime: FiniteDuration, eatingTime: FiniteDuration): ActorRef = {
    return system.actorOf(Props(classOf[Philosopher], name, left, right, handedness, thinkingTime, eatingTime, logger), name)
  }

  private def getOutputName(): String = {
    val option: String = if(fakeRandom) "Y" else "N"
    return "ResourceHierarchyDinnerMaster-T%s-o%s-%s.json".format(duration.toSeconds, option, System.currentTimeMillis.toString)
  }


  private def getRandomSecond(): FiniteDuration = {
    if (fakeRandom) 5.seconds else (r.nextInt(10) + 1).seconds
  }

  override def postStop {
    println("ResourceHierarchyDinnerMaster Finishing!")
    system.shutdown
  }
}
