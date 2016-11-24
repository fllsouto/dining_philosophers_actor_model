package br.usp.ime.fllsouto.dinningActors.resourceHierarchy

import br.usp.ime.fllsouto.dinningActors._
import akka.actor._
import scala.concurrent.duration._


case class Think()
case class FellHungry()
case class AlreadyHungry()
case class StartEating()
case class FinishEating()

// Fork -> Philo
case class TakenFork(firstFork: ActorRef)
case class BlockedToTake(blockedFork: ActorRef)

class Philosopher(name: String, left: ActorRef, right: ActorRef, handedness: String, thinkingTime: FiniteDuration, eatingTime: FiniteDuration, logger: ActorRef) extends Actor {

  import context._
  val transitionTime: FiniteDuration = 1.second

  def receive = {

    case Think =>
      logger ! PhilosopherMessageLogStart("receive", "Think", name, System.currentTimeMillis, thinkingTime.toSeconds, eatingTime.toSeconds)
      become(thinking)
      system.scheduler.scheduleOnce(transitionTime, self, Think)

    case undefMessage => println("[%s][%s] Actor: %s, State: %s, Message: %s".format("ResourceHierarchy", "Philosopher", name, "receive", undefMessage))
  }

  def thinking: Receive = {

    case Think =>
      logger ! PhilosopherMessageLog("thinking", "Think", name, System.currentTimeMillis)
      system.scheduler.scheduleOnce(thinkingTime, self, FellHungry)

    case FellHungry =>
      logger ! PhilosopherMessageLog("thinking", "FellHungry", name, System.currentTimeMillis)
      become(hungry)
      system.scheduler.scheduleOnce(transitionTime, self, FellHungry)

    case AlreadyHungry =>
      logger ! PhilosopherMessageLog("thinking", "AlreadyHungry", name, System.currentTimeMillis)
      become(hungry)
      system.scheduler.scheduleOnce(thinkingTime, self, AlreadyHungry)

    case undefMessage => println("[%s][%s] Actor: %s, State: %s, Message: %s".format("ResourceHierarchy", "Philosopher", name, "thinking", undefMessage))
  }

  def hungry: Receive = {

    case FellHungry =>
      logger ! PhilosopherMessageLog("hungry", "FellHungry", name, System.currentTimeMillis)
      handedness match {
        case "left-handed" => left ! Take(self)
        case "right-handed" => right ! Take(self)
      }
      become(takeFirstFork)

    case AlreadyHungry =>
      logger ! PhilosopherMessageLog("hungry", "AlreadyHungry", name, System.currentTimeMillis)
      handedness match {
        case "left-handed" => left ! Take(self)
        case "right-handed" => right ! Take(self)
      }
      become(takeFirstFork)

    case undefMessage => println("[%s][%s] Actor: %s, State: %s, Message: %s".format("ResourceHierarchy", "Philosopher", name, "hungry", undefMessage))
  }

  def takeFirstFork: Receive = {

    case TakenFork(`left`) =>
      logger ! PhilosopherMessageLog("takeFirstFork", "TakenFork", name, System.currentTimeMillis)
      right ! Take(self)
      become(takeSecondFork(left))

    case TakenFork(`right`) =>
      logger ! PhilosopherMessageLog("takeFirstFork", "TakenFork", name, System.currentTimeMillis)
      left ! Take(self)
      become(takeSecondFork(right))

    case BlockedToTake(blockedFork) =>
      logger ! PhilosopherMessageLog("takeFirstFork", "BlockedToTake", name, System.currentTimeMillis)
      become(thinking)
      system.scheduler.scheduleOnce(transitionTime, self, AlreadyHungry)

    case undefMessage => println("[%s][%s] Actor: %s, State: %s, Message: %s".format("ResourceHierarchy", "Philosopher", name, "takeFirstFork", undefMessage))
  }

  def takeSecondFork(firstFork: ActorRef): Receive = {

    case TakenFork(`left`) =>
      logger ! PhilosopherMessageLog("takeSecondFork", "TakenFork", name, System.currentTimeMillis)
      become(eating)
      system.scheduler.scheduleOnce(transitionTime, self, StartEating)

    case TakenFork(`right`) =>
      logger ! PhilosopherMessageLog("TakenFork", "TakenFork", name, System.currentTimeMillis)
      become(eating)
      system.scheduler.scheduleOnce(transitionTime, self, StartEating)

    case BlockedToTake(blockedFork) =>
      logger ! PhilosopherMessageLog("takeSecondFork", "BlockedToTake", name, System.currentTimeMillis)
      firstFork ! GiveUp(self)
      become(thinking)
      system.scheduler.scheduleOnce(transitionTime, self, AlreadyHungry)

    case undefMessage => println("[%s][%s] Actor: %s, State: %s, Message: %s".format("ResourceHierarchy", "Philosopher", name, "takeSecondFork", undefMessage))
  }

  def eating: Receive = {

    case StartEating =>
      logger ! PhilosopherMessageLog("eating", "StartEating", name, System.currentTimeMillis)
      system.scheduler.scheduleOnce(eatingTime, self, FinishEating)

    case FinishEating =>
      logger ! PhilosopherMessageLog("eating", "FinishEating", name, System.currentTimeMillis)
      handedness match {

        case "left-handed" =>
          left ! Release(self)
          right ! Release(self)

        case "right-handed" =>
          right ! Release(self)
          left ! Release(self)
      }
      become(thinking)
      system.scheduler.scheduleOnce(transitionTime, self, Think)

    case undefMessage => println("[%s][%s] Actor: %s, State: %s, Message: %s".format("ResourceHierarchy", "Philosopher", name, "eating", undefMessage))
  }

  override def postStop { println("Finishing Philo %s".format(name)) }
}