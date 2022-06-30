package jesperan.silk
package mdc

import akka.actor.Actor
import com.pirum.kafka.KafkaConsumer
import com.pirum.kafka.akka.KafkaConsumerActor
import akka.actor.ActorLogging
import akka.event.LoggingAdapter
import com.pirum.kafka.akka.ConsumerRecords
import com.pirum.kafka.akka.Extractor
import akka.actor.ActorRef
import akka.actor.Props
import nodes.MerchantNode

import silk.interface.Merchant

import java.util.concurrent.atomic.AtomicLong

/* 
    Functionality:
        - Auto-subscribe to topics on blueprint
        - Consume all kafka messages for subscribed topics
        - Create and manage merchants using user-defined topic blueprint
        - Model data for proper merchant use
        - Distribute messages to merchants
    Protocol:
        - Initialization
            - Store a topic blueprint to help manage merchants
            - Subscribe to topics on blueprint
            - Create merchants using blueprint when needed
        - Wait for kafka messages
            - Check message topic
            - Model data
            - Send to appropriate merchant
 */

trait KafkaConfig {
  def kafkaConfig: KafkaConsumer.Conf[String, String]
  def actorConfig: KafkaConsumerActor.Conf
  def log: LoggingAdapter
}

trait DeltaConsumer extends KafkaConfig {
  this: Actor =>

  val adaptiveAnalyst = context.actorOf(Props(new AdaptiveAnalyst()))

  //for pattern matching in our receive method
  val msgExtractor: Extractor[Any, ConsumerRecords[String, String]] = ConsumerRecords.extractor[String, String]
  val kafkaConsumerActor = context.actorOf(
    KafkaConsumerActor.props(kafkaConfig, actorConfig, self),
    "DeltaKafkaConsumer"
  )
  def subscribe(topics: List[String]) =
     kafkaConsumerActor ! KafkaConsumerActor.Subscribe.AutoPartition(topics)
}

object Distributor {
  sealed trait Command
}

class Distributor(val kafkaConfig: KafkaConsumer.Conf[String, String], val actorConfig: KafkaConsumerActor.Conf, blueprint: List[Merchant]) extends Actor with ActorLogging with DeltaConsumer {
	import Distributor._

  var yellowbook: Map[String, ActorRef] = Map().empty
  var requestCount = new AtomicLong()

	override def preStart(): Unit = {
    super.preStart()
    log.info("Distributor actor started")
    initMerchantNodes()
    subscribe(yellowbook.keys.toList)
	}

	def receive: Receive = {
	// Records from Kafka
    case msgExtractor(records) =>
      processRecords(records.pairs)
      sender() ! KafkaConsumerActor.Confirm(records.offsets, commit = true)
  }

  private def processRecords(records: Seq[(Option[String], String)]): Unit =
    records.foreach { case (key, value) =>
      log.info(s"Received [$key, $value]") // TODO: pull topic from records to distribute to merchants
      yellowbook.getOrElse("ndehlmr6-test", null) ! MerchantNode.ProcessMessage(requestCount.incrementAndGet(), List(value))
    }

  private def initMerchantNodes(): Unit = {
    blueprint.foreach(merchant => {
      val id = java.util.UUID.randomUUID().toString
      val merchantActor = context.actorOf(Props(new MerchantNode(merchant.topic, id, adaptiveAnalyst, merchant.jobs)), "merchant-" + id)
      yellowbook += (merchant.topic -> merchantActor)
    })
  }

}
