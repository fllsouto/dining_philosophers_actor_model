package br.usp.ime.fllsouto.diningPhilosophers

import br.usp.ime.fllsouto.diningPhilosophers.resourceHierarchy._
import br.usp.ime.fllsouto.diningPhilosophers.waiter._
import br.usp.ime.fllsouto.diningPhilosophers.chandyMisra._
import akka.actor._
import scala.concurrent.duration._

case class StartDinner()
//sbt "run-main br.usp.ime.fllsouto.diningPhilosophers.DinningPhilosophers foo"
object DiningPhilosophers {

  val system = ActorSystem()

  def main(args: Array[String]): Unit = {

    val (eTime, option, fakeRandom) = readFromInput(args)
    try {
      val duration: FiniteDuration = getDuration(eTime)

      printf("Total of duration : %s\n".format(duration))
      printf("Fake Random Option: %s\n", fakeRandom)

      val actorMaster: ActorRef = chooseAlgorithm(option, duration, fakeRandom)

      if(actorMaster == null) closeProgram else actorMaster ! StartDinner()

    } catch {
        case nfe: java.lang.NumberFormatException =>
          printf("\nERROR: %s\n\n".format(nfe))
          closeProgram

        case iae: IllegalArgumentException =>
          printf("\nERROR: %s\n\n".format(iae))
          closeProgram
    }
  }

  private def readFromInput(args: Array[String]): Tuple3[Int, String, Boolean] = {
    try {
      val eTime: Int = args(0).toInt
      val option: String = args(1)
      val fakeRandom: Boolean = if (args(2) == "Y") true else false

      return (eTime, option, fakeRandom)
    } catch {
      case aiob: java.lang.ArrayIndexOutOfBoundsException =>
        print("Insert the simulation time in integer seconds: ")
        val eTime: Int = readInt()
        
        printf("Choose the algorithm for the simulation\n")
        showAlgorithms
        val option: String = readLine()

        print("Run with fake random thinking and eating time (Y/N): ")
        val fakeRandom: Boolean = if (readLine() == "Y") true else false

        return (eTime, option, fakeRandom)
    }
  }

  private def getDuration(eTime: Int): FiniteDuration = {
    return new FiniteDuration(eTime, SECONDS)
  }

  private def chooseAlgorithm(option: String, duration: FiniteDuration, fakeRandom: Boolean): ActorRef = {
    option match {
      case "R" =>
        printf("Running simulation using Resource Hierarchy algorithm\n\n")
        return system.actorOf(Props(classOf[ResourceHierarchyDinnerMaster], duration, fakeRandom), "RH_DinnerMaster")
      case "W" =>
        printf("Running simulation using Waiter algorithm\n\n")
        return system.actorOf(Props(classOf[WaiterDinnerMaster], duration, fakeRandom), "W_DinnerMaster")
      case "C" =>
        return system.actorOf(Props(classOf[ChandyMisraDinnerMaster], duration, fakeRandom), "CM_DinnerMaster")
      case "Q" =>
        return null

      case _ =>
        throw new IllegalArgumentException("Undefined Option: %s".format(option))
        return null
    }

  }

  private def showAlgorithms(): Unit = {
    printf("[R]esource Hierarchy\n")
    printf("[W]aiter\n")
    printf("[C]handyMisra\n")
    printf("\n[Q]uit the program\n")
    print("\nOption: ")
  }

  def closeProgram(): Unit = {
    printf("\nClosing the simulation...\n\n")
    system.shutdown
  }
}
