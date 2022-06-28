package jesperan.silk

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import jesperan.silk.jobs.Job
import jesperan.silk.nodes.Worker
import org.scalatest.wordspec.AnyWordSpecLike

class WorkerTest extends ScalaTestWithActorTestKit with AnyWordSpecLike {
  import Worker._

  "Worker actor" must {
    "execute a given job and return execution data" in {
      val probe = createTestProbe[RespondExecutionData]()
      def organize(data: List[String]) = {
        Thread.sleep(15)
      }
      val job = new Job("organizeOrders", organize)
      val workerActor = spawn(Worker("group-1", "worker-1"))
      workerActor ! Worker.ExecuteJob(41, List("34", "78", "2", "90", "44"), job, probe.ref)
      val response = probe.receiveMessage()
      response.requestId should === (41)
    }
  }

}
