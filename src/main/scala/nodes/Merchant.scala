package jesperan.silk
package nodes

import akka.actor.typed.{ActorRef, Behavior, PostStop, Signal}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors, LoggerOps}
import jobs.{ExecutionSummary, Job}

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
  def apply(topic: String, merchantId: String, replyTo: ActorRef[SendExecutionSummary], jobs: List[Job]): Behavior[Command] =
    Behaviors.setup(context => new Merchant(context, topic, merchantId, replyTo, jobs))

  sealed trait Command
  // TODO: object model message
  final case class ProcessMessage(requestId: Long, message: List[String]) extends Command
  final case class SendExecutionSummary(requestId: Long, summary: ExecutionSummary)
  final case class WrappedExecutionData(response: Worker.RespondExecutionData) extends Command
  // TODO: object model adaptData
  final case class StoreAdaptData(requestId: Long, adaptData: String) extends Command
}

class Merchant(context: ActorContext[Merchant.Command], topic: String, merchantId: String, replyTo: ActorRef[Merchant.SendExecutionSummary], jobs: List[Job])
  extends AbstractBehavior[Merchant.Command] (context) {
  import Merchant._

  var executions: Map[Long, ExecutionSummary] = Map().empty
  var duration: Double = 0
  var waitingLists: Map[Long, List[Job]] = Map().empty
  val wrapper = context.messageAdapter(WrappedExecutionData.apply)
  context.log.info2("Merchant actor {}-{} started", topic, merchantId)

  override def onMessage(msg: Command): Behavior[Command] = {
    msg match {
      case ProcessMessage(id, message) =>
        processMessage(id, message)

      case StoreAdaptData(id, adaptData) =>
        this

      case WrappedExecutionData(response) =>
        handleWorkerResponse(response)
    }
  }

  def processMessage(id: Long, message: List[String]): Behavior[Command] = {
    waitingLists += (id -> jobs)
    executions += (id -> new ExecutionSummary(Map().empty, System.nanoTime()))
    jobs.zipWithIndex.foreach({ case (job, i) =>
      val workerActor = context.spawn(Worker("group-" + id, "worker-" + i), id + "-worker-" + i)
      workerActor ! Worker.ExecuteJob(id, message, job, wrapper)
    })
    this
  }

  def handleWorkerResponse(response: Worker.RespondExecutionData): Behavior[Command] = {
    getExecution(response.requestId).executions += (response.name -> response.data)
    val removed = getWaitingList(response.requestId).filter(!_.name.equals(response.name))
    waitingLists -= response.requestId
    waitingLists += (response.requestId -> removed)

    respondWhenAllCollected(response.requestId)
  }

  private def respondWhenAllCollected(id: Long): Behavior[Command] = {
    val list: List[Job] = getWaitingList(id)
    if (list.isEmpty) {
      getExecution(id).totalTime = BigDecimal((System.nanoTime - getExecution(id).totalTime) / 1e6).setScale(5, BigDecimal.RoundingMode.HALF_UP).toDouble
      replyTo ! SendExecutionSummary(id, getExecution(id))
    }
    this
  }

  private def getExecution(id: Long): ExecutionSummary = {
    executions.getOrElse(id, new ExecutionSummary(Map().empty, 0))
  }

  private def getWaitingList(id: Long): List[Job] = {
    waitingLists.getOrElse(id, List())
  }

  override def onSignal: PartialFunction[Signal, Behavior[Command]] = {
    case PostStop =>
      context.log.info2("Merchant actor {}-{} stopped", topic, merchantId)
      this
  }
}
