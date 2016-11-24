package br.usp.ime.fllsouto.dinningActors.chandyMisra

import br.usp.ime.fllsouto.dinningActors._
import akka.actor._
import akka.util.Timeout
import akka.pattern.ask
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._

case class InitializePhilo(lFork: Boolean, lToken: Boolean, rFork: Boolean, rToken: Boolean, lPhilo: ActorRef, rPhilo: ActorRef)
case class Think()
case class FellHungry()
case class AlreadyHungry()

case class RequestFork(philo: ActorRef, fork: ActorRef)
case class ResponseFork(philo: ActorRef, response: Boolean)

case class StartEating()
case class FinishEating()

class Philosopher(name: String, left: ActorRef, right: ActorRef, thinkingTime: FiniteDuration, eatingTime: FiniteDuration, logger: ActorRef) extends Actor {

  import context._
  val transitionTime: FiniteDuration = 1.second

  def receive = {

    case InitializePhilo(lFork: Boolean, lToken: Boolean, rFork: Boolean, rToken: Boolean, lPhilo: ActorRef, rPhilo: ActorRef) =>
      logger ! PhilosopherMessageLogStart("receive", "InitializePhilo", name, System.currentTimeMillis, thinkingTime.toSeconds, eatingTime.toSeconds)
      become(thinking(lFork, lToken, rFork, rToken, lPhilo, rPhilo))

    case undefMessage => println("[%s][%s] Actor: %s, State: %s, Message: %s".format("ChandyMisra", "Philosopher", name, "receive", undefMessage))
  }

  def thinking(lFork: Boolean, lToken: Boolean, rFork: Boolean, rToken: Boolean, lPhilo: ActorRef, rPhilo: ActorRef): Receive = {

    case Think =>
      logger ! PhilosopherMessageLog("thinking", "Think", name, System.currentTimeMillis)
      system.scheduler.scheduleOnce(thinkingTime, self, FellHungry)

    case FellHungry =>
      logger ! PhilosopherMessageLog("thinking", "FellHungry", name, System.currentTimeMillis)
      become(hungry(lFork, lToken, rFork, rToken, lPhilo, rPhilo))
      system.scheduler.scheduleOnce(transitionTime, self, FellHungry)

    case AlreadyHungry =>
      logger ! PhilosopherMessageLog("thinking", "AlreadyHungry", name, System.currentTimeMillis)
      become(hungry(lFork, lToken, rFork, rToken, lPhilo, rPhilo))
      system.scheduler.scheduleOnce(thinkingTime, self, FellHungry)

    case RequestFork(`lPhilo`, `left`) =>
      logger ! PhilosopherMessageLog("thinking", "RequestFork", name, System.currentTimeMillis)
      if (lFork) {
        val _lToken = true
        implicit val timeout = Timeout(5.seconds)

        val futureLeft = left ? IsDirty
        val dirty = Await.result(futureLeft, timeout.duration).asInstanceOf[Boolean]

        val _lFork = if (dirty) {
          left ! Clean
          lPhilo ! ResponseFork(left, true)
          !lFork
        } else { lFork }

        become(thinking(_lFork, _lToken, rFork, rToken, lPhilo, rPhilo))
       
      }


    case RequestFork(`rPhilo`, `right`) =>
      logger ! PhilosopherMessageLog("thinking", "RequestFork", name, System.currentTimeMillis)
      if (rFork) {
        val _rToken = true
        implicit val timeout = Timeout(5.seconds)

        val futureRight = right ? IsDirty
        val dirty = Await.result(futureRight, timeout.duration).asInstanceOf[Boolean]

        val _rFork = if (dirty) {
          right ! Clean
          rPhilo ! ResponseFork(right, true)
          !rFork
        } else { rFork }
        
        become(thinking(lFork, lToken, _rFork, _rToken, lPhilo, rPhilo))
      }

    case ResponseFork(fork, response) =>
      logger ! PhilosopherMessageLog("thinking", "ResponseFork", name, System.currentTimeMillis)
      (fork, response) match {

        case(`left`, true) => 
          val _lFork = true
          become(thinking(_lFork, lToken, rFork, rToken, lPhilo, rPhilo))

        case(`right`, true) => 
          val _rFork = true
          become(thinking(lFork, lToken, _rFork, rToken, lPhilo, rPhilo))

        case(_, _) => // Do nothing
      }


    case undefMessage => println("[%s][%s] Actor: %s, State: %s, Message: %s".format("ChandyMisra", "Philosopher", name, "thinking", undefMessage))
  }

  def hungry(lFork: Boolean, lToken: Boolean, rFork: Boolean, rToken: Boolean, lPhilo: ActorRef, rPhilo: ActorRef): Receive = {

    case FellHungry =>
      logger ! PhilosopherMessageLog("hungry", "FellHungry", name, System.currentTimeMillis)
      (lFork, rFork) match {

        case(true, true) =>
          left ! Take(self)
          right ! Take(self)

          become(eating(lFork, lToken, rFork, rToken, lPhilo, rPhilo))
          system.scheduler.scheduleOnce(transitionTime, self, StartEating)

        case(false, true) =>
          val _lToken = if (lToken) {
            lPhilo ! RequestFork(self, left)
            !lToken
          } else { lToken }

          become(thinking(lFork, _lToken, rFork, rToken, lPhilo, rPhilo))
          system.scheduler.scheduleOnce(transitionTime, self, AlreadyHungry)

        case(true, false) =>
          val _rToken = if (rToken) {
            rPhilo ! RequestFork(self, right)
            !rToken
          } else { rToken }

          become(thinking(lFork, lToken, rFork, _rToken, lPhilo, rPhilo))
          system.scheduler.scheduleOnce(transitionTime, self, AlreadyHungry)

        case(false, false) =>
          val (_lToken, _rToken) = if (lToken && rToken) { 
            lPhilo ! RequestFork(self, left)
            rPhilo ! RequestFork(self, right)
            (!lToken, !rToken)
          } else { (lToken, rToken) }

          become(thinking(lFork, _lToken, rFork, _rToken, lPhilo, rPhilo))
          system.scheduler.scheduleOnce(transitionTime, self, AlreadyHungry)

      }

    case RequestFork(`lPhilo`, `left`) =>
      logger ! PhilosopherMessageLog("hungry", "RequestFork", name, System.currentTimeMillis)
      if (lFork) {
        val _lToken = true
        lPhilo ! ResponseFork(left, false)
        become(hungry(lFork, _lToken, rFork, rToken, lPhilo, rPhilo))
      }

    case RequestFork(`rPhilo`, `right`) =>
      logger ! PhilosopherMessageLog("hungry", "RequestFork", name, System.currentTimeMillis)
      if (rFork) {
        val _rToken = true
        rPhilo ! ResponseFork(right, false)
        become(hungry(lFork, lToken, rFork, _rToken, lPhilo, rPhilo))
      }

    case ResponseFork(fork, response) =>
      logger ! PhilosopherMessageLog("hungry", "ResponseFork", name, System.currentTimeMillis)
      (fork, response) match {

        case(`left`, true) => 
          val _lFork = true
          become(hungry(_lFork, lToken, rFork, rToken, lPhilo, rPhilo))

        case(`right`, true) =>
          val _rFork = true
          become(hungry(lFork, lToken, _rFork, rToken, lPhilo, rPhilo))

        case(_, _) => // Do nothing
      }

    case undefMessage => println("[%s][%s] Actor: %s, State: %s, Message: %s".format("ChandyMisra", "Philosopher", name, "hungry", undefMessage))
  }

  def eating(lFork: Boolean, lToken: Boolean, rFork: Boolean, rToken: Boolean, lPhilo: ActorRef, rPhilo: ActorRef): Receive = {

    case StartEating =>
      logger ! PhilosopherMessageLog("eating", "StartEating", name, System.currentTimeMillis)
      system.scheduler.scheduleOnce(eatingTime, self, FinishEating)

    case FinishEating =>
      logger ! PhilosopherMessageLog("eating", "FinishEating", name, System.currentTimeMillis)
      left ! Release(self)
      right ! Release(self)

      val (_lFork, _rFork) = (lToken, rToken) match {

        case (true, true) =>
          left ! Clean
          lPhilo ! ResponseFork(left, true)

          right ! Clean
          rPhilo ! ResponseFork(right, true)

          (!lFork, !rFork)
        
        case (true, false) =>
          left ! Clean
          lPhilo ! ResponseFork(left, true)

          (!lFork, rFork)

        case (false, true) =>
          right ! Clean
          rPhilo ! ResponseFork(right, true)

          (lFork, !rFork)
        
        case (false, false) => (lFork, rFork)
      }

      become(thinking(_lFork, lToken, _rFork, rToken, lPhilo, rPhilo))
      system.scheduler.scheduleOnce(transitionTime, self, Think)

    case RequestFork(`lPhilo`, `left`) =>
      logger ! PhilosopherMessageLog("eating", "RequestFork", name, System.currentTimeMillis)
      if (lFork) {
        val _lToken = true
        lPhilo ! ResponseFork(left, false)
        become(eating(lFork, _lToken, rFork, rToken, lPhilo, rPhilo))
      }

    case RequestFork(`rPhilo`, `right`) =>
      logger ! PhilosopherMessageLog("eating", "RequestFork", name, System.currentTimeMillis)
      if (rFork) {
        val _rToken = true
        rPhilo ! ResponseFork(right, false)
        become(eating(lFork, lToken, rFork, _rToken, lPhilo, rPhilo))
      }

    case undefMessage => println("[%s][%s] Actor: %s, State: %s, Message: %s".format("ChandyMisra", "Philosopher", name, "eating", undefMessage))
  }

 

  override def postStop { println("Finishing Philo %s".format(name)) }
}
