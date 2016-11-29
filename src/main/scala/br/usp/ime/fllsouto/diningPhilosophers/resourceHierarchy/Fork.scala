package br.usp.ime.fllsouto.diningPhilosophers.resourceHierarchy

import br.usp.ime.fllsouto.diningPhilosophers._
import akka.actor._

case class Take(philo: ActorRef)
case class Release(philo: ActorRef)
case class GiveUp(philo: ActorRef)

class Fork(name: String, logger: ActorRef) extends Actor {

  import context._

  def receive = available

  def available: Receive = {

    case Take(philo: ActorRef) =>
      logger ! ForkMessageLog("available", "Take", self.path.name, philo.path.name, System.currentTimeMillis)
      become(takenBy(philo))
      philo ! TakenFork(self)

    case undefMessage => println("[%s][%s] Actor: %s, State: %s, Message: %s".format("ResourceHierarchy", "Fork", name, "available", undefMessage))
  }

  def takenBy(philo: ActorRef): Receive = {

    case Take(otherPhilo) =>
      logger ! ForkMessageLog("takenBy", "Take", self.path.name, otherPhilo.path.name, System.currentTimeMillis)
      otherPhilo ! BlockedToTake(self)

    case Release(`philo`) =>
      logger ! ForkMessageLog("takenBy", "Release", self.path.name, philo.path.name, System.currentTimeMillis)
      become(available)

    case GiveUp(`philo`) =>
      logger ! ForkMessageLog("takenBy", "GiveUp", self.path.name, philo.path.name, System.currentTimeMillis)
      become(available)

    case undefMessage => println("[%s][%s] Actor: %s, State: %s, Message: %s".format("ResourceHierarchy", "Fork", name, "takenBy", undefMessage))
  }

  override def postStop { println("Finishing Fork %s".format(self.path.name)) }
}