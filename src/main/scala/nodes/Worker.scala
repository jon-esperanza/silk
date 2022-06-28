package jesperan.silk
package nodes

import akka.actor.typed.{ActorRef, Behavior, PostStop, Signal}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors, LoggerOps}
import jobs.Job

/*
  Functionality
    - Execute message processing
    - Collect execution data

   Protocol
    - Wait for request from Merchant with job to execute then
      sends execution data collected back to Merchant before terminating
 */

object Worker {
  def apply(groupId: String, workerId: String): Behavior[Command] =
    Behaviors.setup(context => new Worker(context, groupId, workerId))

  sealed trait Command
  final case class ExecuteJob(requestId: Long, data: List[String], job: Job, replyTo: ActorRef[RespondExecutionData]) extends Command
  final case class RespondExecutionData(requestId: Long, name: String, data: Double)
}

class Worker(context: ActorContext[Worker.Command], groupId: String, workerId: String)
  extends AbstractBehavior[Worker.Command] (context) {
  import Worker._

  context.log.info2("Worker actor {}-{} started", groupId, workerId)

  override def onMessage(msg: Command): Behavior[Command] = {
    msg match {
      case ExecuteJob(id, data, job, replyTo) =>
        var totalTime: Double = System.nanoTime()
        job.execute(data)
        totalTime = BigDecimal((System.nanoTime - totalTime) / 1e6).setScale(5, BigDecimal.RoundingMode.HALF_UP).toDouble
        replyTo ! RespondExecutionData(id, job.name, totalTime)
        Behaviors.stopped
    }
  }

  override def onSignal: PartialFunction[Signal, Behavior[Command]] = {
    case PostStop =>
      context.log.info2("Worker actor {}-{} stopped", groupId, workerId)
      this
  }
}


