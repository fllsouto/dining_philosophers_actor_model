package br.usp.ime.fllsouto.diningPhilosophers.waiter

import br.usp.ime.fllsouto.diningPhilosophers._
import akka.actor._
import scala.concurrent.duration._


case class Think()
case class FellHungry()
case class AlreadyHungry()
case class StartEating()
case class FinishEating()

case class TakenForks()
case class BlockedToTake()


class Philosopher(name: String, left: ActorRef, right: ActorRef, waiter: ActorRef, thinkingTime: FiniteDuration, eatingTime: FiniteDuration, logger: ActorRef) extends Actor {

  import context._
  val transitionTime: FiniteDuration = 1.second

  def receive = {

    case Think =>
      logger ! PhilosopherMessageLogStart("receive", "Think", name, System.currentTimeMillis, thinkingTime.toSeconds, eatingTime.toSeconds)
      become(thinking)
      system.scheduler.scheduleOnce(transitionTime, self, Think)

    case undefMessage => println("[%s][%s] Actor: %s, State: %s, Message: %s".format("Waiter", "Philosopher", name, "receive", undefMessage))
  }

  def thinking: Receive = {

    case Think =>
      logger ! PhilosopherMessageLog("thinking", "Think", self.path.name, System.currentTimeMillis)
      system.scheduler.scheduleOnce(thinkingTime, self, FellHungry)

    case FellHungry =>
      logger ! PhilosopherMessageLog("thinking", "FellHungry", self.path.name, System.currentTimeMillis)
      become(hungry)
      system.scheduler.scheduleOnce(transitionTime, self, FellHungry)

    case AlreadyHungry =>
      logger ! PhilosopherMessageLog("thinking", "AlreadyHungry", self.path.name, System.currentTimeMillis)
      become(hungry)
      system.scheduler.scheduleOnce(thinkingTime, self, AlreadyHungry)

    case undefMessage => println("[%s][%s] Actor: %s, State: %s, Message: %s".format("Waiter", "Philosopher", name, "thinking", undefMessage))
  }

  def hungry: Receive = {

    case FellHungry =>
      logger ! PhilosopherMessageLog("hungry", "FellHungry", self.path.name, System.currentTimeMillis)
      waiter ! RequestForks(self, left, right)
      become(requestForks)

    case AlreadyHungry =>
      logger ! PhilosopherMessageLog("hungry", "AlreadyHungry", self.path.name, System.currentTimeMillis)
      waiter ! RequestForks(self, left, right)
      become(requestForks)

    case undefMessage => println("[%s][%s] Actor: %s, State: %s, Message: %s".format("Waiter", "Philosopher", name, "hungry", undefMessage))
  }

  def requestForks: Receive = {

    case TakenForks =>
      logger ! PhilosopherMessageLog("requestForks", "TakenForks", self.path.name, System.currentTimeMillis)
      become(eating)
      system.scheduler.scheduleOnce(transitionTime, self, StartEating)

    case BlockedToTake =>
      logger ! PhilosopherMessageLog("requestForks", "BlockedToTake", self.path.name, System.currentTimeMillis)
      become(thinking)
      system.scheduler.scheduleOnce(transitionTime, self, AlreadyHungry)

    case undefMessage => println("[%s][%s] Actor: %s, State: %s, Message: %s".format("Waiter", "Philosopher", name, "requestForks", undefMessage))
  }

  def eating: Receive = {

    case StartEating =>
      logger ! PhilosopherMessageLog("eating", "StartEating", self.path.name, System.currentTimeMillis)
      system.scheduler.scheduleOnce(eatingTime, self, FinishEating)

    case FinishEating =>
      logger ! PhilosopherMessageLog("eating", "FinishEating", self.path.name, System.currentTimeMillis)
      waiter ! ReleaseForks(self, left, right)
      become(thinking)
      system.scheduler.scheduleOnce(transitionTime, self, Think)

    case undefMessage => println("[%s][%s] Actor: %s, State: %s, Message: %s".format("Waiter", "Philosopher", name, "eating", undefMessage))
  }

  override def postStop { println("Finishing Philo %s".format(self.path.name)) }
}