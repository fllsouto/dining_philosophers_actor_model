package br.usp.ime.fllsouto.diningPhilosophers.waiter

import br.usp.ime.fllsouto.diningPhilosophers._
import akka.actor._

case class Take(philo: ActorRef)
case class Release(philo: ActorRef)
case class ForkStatus()

class Fork(name: String, logger: ActorRef) extends Actor {
  import context._

  def receive = available

  def available: Receive = {

    case Take(philo: ActorRef) =>
      logger ! ForkMessageLog("available", "Take", self.path.name, philo.path.name, System.currentTimeMillis)
      become(takenBy(philo))

    case ForkStatus =>
      sender ! "Available"

    case undefMessage => println("[%s][%s] Actor: %s, State: %s, Message: %s".format("Waiter", "Fork", name, "available", undefMessage))

  }

  def takenBy(philo: ActorRef): Receive = {

    case Release(`philo`) =>
      logger ! ForkMessageLog("takenBy", "Release ", self.path.name, philo.path.name, System.currentTimeMillis)
      become(available)

    case ForkStatus =>
      sender ! "Occupied"

    case undefMessage => println("[%s][%s] Actor: %s, State: %s, Message: %s".format("Waiter", "Fork", name, "takenBy", undefMessage))
  }

  override def postStop { println("Finishing Fork %s".format(self.path.name)) }
}