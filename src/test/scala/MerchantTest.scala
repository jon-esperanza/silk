package jesperan.silk

import nodes.{Merchant, Worker}

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import jesperan.silk.jobs.Job
import org.scalatest.wordspec.AnyWordSpecLike
import jesperan.silk.jobs.ExecutionSummary

class MerchantTest extends ScalaTestWithActorTestKit with AnyWordSpecLike {
  import Merchant._

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
      val probe = createTestProbe[SendExecutionSummary]()
      val merchantActor = spawn(Merchant("orders", "merchant", probe.ref, jobs))
      var duration: Double = System.nanoTime()
      for (i <- 1 to 5) {
        merchantActor ! Merchant.ProcessMessage(20 + i, List("34", "78", "2", "90", "44"))
        val response = probe.receiveMessage()
        response.requestId should === (20 + i)
      }
    }
  }
}
