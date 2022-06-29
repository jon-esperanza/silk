package jesperan.silk
package nodes

import jobs.{ExecutionSummary, Job}
import akka.actor.ActorLogging
import akka.actor.Actor
import akka.actor.ActorRef
import akka.event.Logging
import akka.actor.Props

/*
  Functionality
    - Structure message processing
      - Using adaptData stored in mem (MISS -> request from AdaptiveAnalyst)
    - Monitor message processing
    - Summarize execution data and send to AdaptiveAnalyst
    - Receive adaptData from AdaptiveAnalyst

   Protocol
    - Wait for message from Distributor
      - If adaptData not accessible -> skip optimizing structure
      - Generate optimal execution structure using adaptData
      - Using structure, spawn workers for each job
      - Await each worker's response and aggregate execution data
      - Send execution summary to AdaptiveAnalyst
    - Wait for message from Adaptive Analyst
      - Store adaptData in memory
    - On a adaptData in mem MISS -> request adaptData from AdaptiveAnalyst
 */

object Merchant {
  sealed trait Command
  // TODO: object model message
  final case class ProcessMessage(requestId: Long, message: List[String]) extends Command
  final case class SendExecutionSummary(requestId: Long, summary: ExecutionSummary)
  final case class JobExecutionData(requestId: Long, name: String, data: Double)
  // TODO: object model adaptData
  final case class StoreAdaptData(requestId: Long, adaptData: String) extends Command
}

class Merchant(topic: String, merchantId: String, adaptiveAnalyst: ActorRef, jobs: List[Job]) 
  extends Actor with ActorLogging {
  import Merchant._

  var executions: Map[Long, ExecutionSummary] = Map().empty
  var duration: Double = 0
  var waitingLists: Map[Long, List[Job]] = Map().empty
  log.info("Merchant actor {}-{} started", topic, merchantId)

  override def receive: Receive = {
      case ProcessMessage(id, message) => processMessage(id, message)

      case StoreAdaptData(id, adaptData) => this // TODO: Handle requesting adapt data from analyst

      case JobExecutionData(id, name, data) => handleWorkerResponse(id, name, data)
  }

  private def processMessage(id: Long, message: List[String]): Unit = {
    waitingLists += (id -> jobs)
    executions += (id -> new ExecutionSummary(Map().empty, System.nanoTime()))
    jobs.zipWithIndex.foreach({ case (job, i) =>
      val workerActor = context.actorOf(Props(new Worker("group-" + id, "worker-" + i, self)), id + "-worker-" + i)
      workerActor ! Worker.ExecuteJob(id, message, job)
    })
  }

  private def handleWorkerResponse(id: Long, name: String, data: Double): Unit = {
    getExecution(id).executions += (name -> data)
    val removed = getWaitingList(id).filter(!_.name.equals(name))
    waitingLists -= id
    waitingLists += (id -> removed)

    respondWhenAllCollected(id)
  }

  private def respondWhenAllCollected(id: Long): Unit = {
    val list: List[Job] = getWaitingList(id)
    if (list.isEmpty) {
      getExecution(id).totalTime = BigDecimal((System.nanoTime - getExecution(id).totalTime) / 1e6).setScale(5, BigDecimal.RoundingMode.HALF_UP).toDouble
      adaptiveAnalyst ! SendExecutionSummary(id, getExecution(id))
    }
  }

  private def getExecution(id: Long): ExecutionSummary = {
    executions.getOrElse(id, new ExecutionSummary(Map().empty, 0))
  }

  private def getWaitingList(id: Long): List[Job] = {
    waitingLists.getOrElse(id, List())
  }

  override def postStop(): Unit = {
    log.info("Merchant actor {}-{} stopped", topic, merchantId)
  }
}
