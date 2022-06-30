package jesperan.silk

import nodes.MerchantNode

import jobs.Job
import org.scalatest.wordspec.AnyWordSpecLike
import akka.testkit.TestKit
import akka.actor.ActorSystem
import akka.testkit.TestProbe
import akka.actor.Props
import org.scalatest.BeforeAndAfterAll
import org.scalatest.matchers.should.Matchers

class MerchantTest extends TestKit(ActorSystem("test-merchant"))
    with AnyWordSpecLike 
    with BeforeAndAfterAll 
    with Matchers {
  import MerchantNode._


  override def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  "Merchant actor" must {
    "execute multiple jobs using workers and return execution summary" in {
      def organize(data: List[String]) = {
        Thread.sleep(15)
      }
      def format(data: List[String]) = {
        Thread.sleep(30)
      }
      def send(data: List[String]) = {
        Thread.sleep(20)
      }
      val jobs: List[Job] = List(new Job("organizeOrders", organize), new Job("formatOrders", format), new Job("sendOrders", send))
      val probe = TestProbe()
      val merchantActor = system.actorOf(Props(new MerchantNode("orders", "merchant", probe.ref, jobs)))
      var duration: Double = System.nanoTime()
      for (i <- 1 to 5) {
        merchantActor ! MerchantNode.ProcessMessage(20 + i, List("34", "78", "2", "90", "44"))
        val response: SendExecutionSummary = probe.expectMsgType[SendExecutionSummary]
        response.requestId should === (20 + i)
      }
    }
  }
}
