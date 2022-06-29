package jesperan.silk

import jesperan.silk.jobs.Job
import jesperan.silk.nodes.Worker
import org.scalatest.wordspec.AnyWordSpecLike
import jesperan.silk.nodes.Merchant
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
  import Worker._

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
      val workerActor = system.actorOf(Props(new Worker("group-1", "worker-1", probe.ref)))
      probe.watch(workerActor)
      workerActor ! Worker.ExecuteJob(41, List("34", "78", "2", "90", "44"), job)
      val response: Merchant.JobExecutionData = probe.expectMsgType[Merchant.JobExecutionData]
      response.requestId should === (41)
      response.name should === (job.name)
    }
  }

}
