package br.usp.ime.fllsouto.diningPhilosophers.waiter

import br.usp.ime.fllsouto.diningPhilosophers._
import akka.actor._
import scala.concurrent.duration._
import akka.pattern.gracefulStop
import scala.concurrent.{Await, ExecutionContext, Future}

case class TerminateDinner(philos: List[ActorRef], forks: IndexedSeq[ActorRef], waiter: ActorRef, logger: ActorRef)

class WaiterDinnerMaster(duration: FiniteDuration, fakeRandom: Boolean) extends Actor {
  import context._
  val r = scala.util.Random

  def receive  = { 
    case StartDinner() =>
      r.setSeed(System.currentTimeMillis)
      run()

    case TerminateDinner(philos: List[ActorRef], forks: IndexedSeq[ActorRef], waiter: ActorRef, logger: ActorRef) =>
      philos.foreach{ _ ! PoisonPill }
      forks.foreach{  _ ! PoisonPill }
      waiter ! PoisonPill

      val stopped: Future[Boolean] = gracefulStop(logger, 5.seconds)
      Await.result(stopped, 3.seconds)

      self ! PoisonPill

    case undefMessage => println("[%s][%s] Actor: %s, State: %s, Message: %s".format("Waiter", "WaiterDinnerMaster", self.path.name, "receive", undefMessage))
  }

  private def run() {
    val fn = getOutputName()
    val philosName = List("Descartes", "Seneca", "Rousseau", "Nietzsche", "Hobbes")
    val waiterName = "Alfred"

    val logger = system.actorOf(Props(classOf[DinnerLogger], fn, duration.fromNow), "Dinner_Logger")
    val forks = createForks(philosName, logger)
    val waiter = system.actorOf(Props(classOf[Waiter], waiterName), waiterName)
    val philos = createPhilosophers(philosName, forks, waiter, logger)

    logger ! WriteProgressBar
    philos.foreach{ _ ! Think }
    system.scheduler.scheduleOnce(duration, self, TerminateDinner(philos, forks, waiter, logger))
  }


  private def createForks(philosName: List[String], logger: ActorRef): IndexedSeq[ActorRef] = {
    val forksQ: Int = philosName.length
    return for(position <- 0 until forksQ) yield createFork(position, logger)
  }

  private def createFork(position: Int, logger: ActorRef): ActorRef = {
    return system.actorOf(Props(classOf[Fork], "Fork_" + position, logger), "Fork_" + position)
  }

  private def createPhilosophers(philoNames: List[String], forks: IndexedSeq[ActorRef], waiter: ActorRef, logger: ActorRef): List[ActorRef] = {
    val philosQ: Int  = philoNames.length
    return for { (name, position) <- philoNames.zipWithIndex } yield createPhilosopher( 
      "%s--%d".format(name, position),
      forks(position), 
      forks((position + 1) % philosQ),
      waiter,
      logger,
      getRandomSecond(),
      getRandomSecond())
  }

  private def createPhilosopher(name: String, left: ActorRef, right: ActorRef, waiter: ActorRef, logger: ActorRef, thinkingTime: FiniteDuration, eatingTime: FiniteDuration): ActorRef = {
    return system.actorOf(Props(classOf[Philosopher], name, left, right, waiter, thinkingTime, eatingTime, logger), name)
  }

  private def getRandomSecond(): FiniteDuration = {
    if (fakeRandom) 5.seconds else (r.nextInt(10) + 1).seconds
  }

  private def getOutputName(): String = {
    val option: String = if(fakeRandom) "Y" else "N"
    return "WaiterDinnerMaster-T%s-o%s-%s.json".format(duration.toSeconds, option, System.currentTimeMillis.toString)
  }

  override def postStop { 
    println("WaiterDinnerMaster Finishing!")
    system.shutdown
  }

}