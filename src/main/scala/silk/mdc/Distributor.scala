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
import models.Merchant
import org.apache.kafka.clients.consumer.ConsumerRecord
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

  val adaptiveAnalyst: ActorRef = context.actorOf(Props(new AdaptiveAnalyst()))

  //for pattern matching in our receive method
  val msgExtractor: Extractor[Any, ConsumerRecords[String, String]] = ConsumerRecords.extractor[String, String]
  val kafkaConsumerActor: ActorRef = context.actorOf(
    KafkaConsumerActor.props(kafkaConfig, actorConfig, self),
    "DeltaKafkaConsumer"
  )
  def subscribe(topics: List[String]): Unit =
    kafkaConsumerActor ! KafkaConsumerActor.Subscribe.AutoPartition(topics)
}

object Distributor {
  sealed trait Command
}

class Distributor(val kafkaConfig: KafkaConsumer.Conf[String, String], val actorConfig: KafkaConsumerActor.Conf, blueprint: List[Merchant])
  extends Actor with ActorLogging with DeltaConsumer {

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
      processRecords(records.recordsList)
      sender() ! KafkaConsumerActor.Confirm(records.offsets, commit = true)
  }

  private def processRecords(records: List[ConsumerRecord[String, String]]): Unit = {
    records.foreach { record =>
      log.info(s"Received [${record.key()}, ${record.value()}] from [${record.topic()}]")
      yellowbook.getOrElse(record.topic(), null) ! MerchantNode.ProcessMessage(requestCount.incrementAndGet(), List(record.value()))
    }
  }

  private def initMerchantNodes(): Unit = {
    blueprint.foreach(merchant => {
      val id = java.util.UUID.randomUUID().toString
      val merchantActor = context.actorOf(Props(new MerchantNode(merchant.topic, id, adaptiveAnalyst, merchant.jobs)), "merchant-" + id)
      yellowbook += (merchant.topic -> merchantActor)
    })
  }

}
