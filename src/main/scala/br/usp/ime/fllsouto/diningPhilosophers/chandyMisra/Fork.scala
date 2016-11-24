package br.usp.ime.fllsouto.diningPhilosophers.chandyMisra

import br.usp.ime.fllsouto.diningPhilosophers._
import akka.actor._

// Fork Messages

case class InitializeFork(dirty: Boolean)
case class IsDirty()
case class Clean()
case class Take(philo: ActorRef)
case class Release(philo: ActorRef)

class Fork(name: String, logger: ActorRef) extends Actor {

  import context._

  def receive = {

    case InitializeFork(dirty: Boolean) => become(available(dirty))
  }
 
  def available(dirty: Boolean): Receive = {

    case IsDirty => sender ! dirty

    case Clean => if (dirty) { become(available(!dirty)) }

    case Take(philo: ActorRef) =>
      logger ! ForkMessageLog("available", "Take", name, philo.path.name, System.currentTimeMillis)
      become(takenBy(philo, dirty))

    case undefMessage => println("[%s][%s] Actor: %s, State: %s, Message: %s".format("ChandyMisra", "Fork", name, "available", undefMessage))
  }

  def takenBy(philo: ActorRef, dirty: Boolean): Receive = {

    case Release(`philo`) =>
      logger ! ForkMessageLog("takenBy", "Release", name, philo.path.name, System.currentTimeMillis)
      become(available(true))

    case IsDirty => sender ! dirty

    case undefMessage => println("[%s][%s] Actor: %s, State: %s, Message: %s".format("ChandyMisra", "Fork", name, "takenBy", undefMessage))
  }


  override def postStop { println("Finishing Fork %s".format(name)) }
}