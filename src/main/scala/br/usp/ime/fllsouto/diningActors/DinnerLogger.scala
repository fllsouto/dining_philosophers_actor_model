package br.usp.ime.fllsouto.dinningActors

import java.io._
import akka.actor._
import scala.concurrent.duration._

import net.liftweb.json._
import net.liftweb.json.JsonDSL._

case class PhilosopherMessageLog(state: String, message: String, philo: String, tAction: Long)
case class PhilosopherMessageLogStart(state: String, message: String, philo: String, tAction: Long, eTime: Long, tTime: Long)
case class ForkMessageLog(state: String, message: String, fork: String, philo: String, tAction: Long)
case class WriteProgressBar()


//TODO: buffer the messages and sent in the end of the simulation
class DinnerLogger(filename: String, duration: Deadline) extends Actor{
  import context._

  //All type of events in the same file
  val file: java.io.FileWriter = new FileWriter(filename, true)

  def receive = {
    case PhilosopherMessageLogStart(state, message, philo, tAction, eTime, tTime) =>
      val json = ("actor" -> "Philosopher") ~ ("state" -> state) ~ ("message" -> message) ~ ("philo" -> philo) ~ ("timestamp" -> tAction) ~ ("eTime" -> eTime) ~ ("tTime" -> tTime)
      printToFile(compact(render(json)))

    case PhilosopherMessageLog(state, message, philo, tAction) =>
      val json = ("actor" -> "Philosopher") ~ ("state" -> state) ~ ("message" -> message) ~ ("philo" -> philo) ~ ("timestamp" -> tAction)
      printToFile(compact(render(json)))
   
    case ForkMessageLog(state, message, fork, philo, tAction) =>
      val json = ("actor" -> "Fork") ~ ("state" -> state) ~ ("message" -> message) ~ ("fork" -> fork) ~ ("philo" -> philo) ~ ("timestamp" -> tAction)
      printToFile(compact(render(json)))

    case WriteProgressBar =>
      printf("Time left: %s s\n".format(duration.timeLeft.toSeconds))
      system.scheduler.scheduleOnce(2.seconds, self, WriteProgressBar)

  }

  private def printToFile(data: String) {
    try { file.write(data + ","); file.flush; } catch { 
      case ioe: IOException =>
        printf("Error : " + ioe) 
    }
  }

  override def postStop { 
    println("Finishing logger actor, output file saved in : " + filename)
    file.close()
  }
}