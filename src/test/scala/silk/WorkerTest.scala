package jesperan.silk

import jobs.Job
import nodes.WorkerNode
import org.scalatest.wordspec.AnyWordSpecLike
import nodes.MerchantNode
import akka.testkit.TestKit
import akka.actor.ActorSystem
import akka.testkit.TestProbe
import akka.actor.Props
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers

class WorkerTest extends TestKit(ActorSystem("test-worker"))
    with AnyWordSpecLike 
    with BeforeAndAfterAll 
    with Matchers {
  import WorkerNode._

  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "Worker actor" must {
    "execute a given job and return execution data" in {
      def organize(data: List[String]) = {
        Thread.sleep(15)
      }
      val probe = TestProbe()
      val job = new Job("organizeOrders", organize)
      val workerActor = system.actorOf(Props(new WorkerNode("group-1", "worker-1", probe.ref)))
      probe.watch(workerActor)
      workerActor ! WorkerNode.ExecuteJob(41, List("34", "78", "2", "90", "44"), job)
      val response: MerchantNode.JobExecutionData = probe.expectMsgType[MerchantNode.JobExecutionData]
      response.requestId should === (41)
      response.name should === (job.name)
    }
  }

}
