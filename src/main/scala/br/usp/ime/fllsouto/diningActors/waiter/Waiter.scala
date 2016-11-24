package br.usp.ime.fllsouto.dinningActors.waiter

import br.usp.ime.fllsouto.dinningActors._
import akka.actor._
import akka.util.Timeout
import akka.pattern.ask
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.concurrent.duration._

case class RequestForks(philo: ActorRef, left: ActorRef, right: ActorRef)
case class ReleaseForks(philo: ActorRef, left: ActorRef, right: ActorRef)

class Waiter(name: String) extends Actor {
  import context._

  def receive = serving

  def serving: Receive = {

    case RequestForks(philo, left, right) =>
      implicit val timeout = Timeout(5.seconds)

      val futureLeft = left ? ForkStatus
      val leftStatus = Await.result(futureLeft, timeout.duration).asInstanceOf[String]

      val futureRight = right ? ForkStatus
      val rightStatus = Await.result(futureRight, timeout.duration).asInstanceOf[String]

      (leftStatus, rightStatus) match {

        case ("Available", "Available") =>
          left ! Take(philo)
          right ! Take(philo)
          philo ! TakenForks

        case (_, _) =>
          philo ! BlockedToTake
      }

    case ReleaseForks(philo, left, right) =>
      left ! Release(philo)
      right ! Release(philo)

    case undefMessage => println("[%s][%s] Actor: %s, State: %s, Message: %s".format("Waiter", "Waiter", name, "serving", undefMessage))
    case _ => println("[Waiter][Waiter] Error %s at serving".format(name))
  }

  override def postStop { println("Finishing Waiter %s".format(self.path.name)) }
}