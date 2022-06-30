package jesperan.silk
package nodes

import jobs.Job
import akka.actor.ActorRef
import akka.actor.ActorLogging
import akka.actor.Actor

/*
  Functionality
    - Execute message processing
    - Collect execution data

   Protocol
    - Wait for request from Merchant with job to execute then
      sends execution data collected back to Merchant before terminating
 */

object WorkerNode {
  final case class ExecuteJob(requestId: Long, data: List[String], job: Job)
}

class WorkerNode(groupId: String, workerId: String, merchant: ActorRef)
  extends Actor with ActorLogging {
  import WorkerNode._

  override def preStart(): Unit = {
    log.info("Worker actor {}-{} started", groupId, workerId)
  }

  override def receive: Receive = {
      case ExecuteJob(id, data, job) =>
        var totalTime: Double = System.nanoTime()
        job.execute(data)
        totalTime = BigDecimal((System.nanoTime - totalTime) / 1e6).setScale(5, BigDecimal.RoundingMode.HALF_UP).toDouble
        merchant ! MerchantNode.JobExecutionData(id, job.name, totalTime)
        
  }

  override def postStop(): Unit = {
    log.info("Worker actor {}-{} stopped", groupId, workerId)
  }
}


