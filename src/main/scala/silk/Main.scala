package jesperan.silk

import com.pirum.kafka.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import org.apache.kafka.common.requests.IsolationLevel
import com.pirum.kafka.akka.KafkaConsumerActor

import scala.concurrent.duration._
import mdc.Distributor

import akka.actor.ActorSystem
import akka.actor.Props
import jobs.Job
import silk.interface.Merchant

object Main extends App {
  val jaasTemplate = "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"%s\" password=\"%s\";"
  val jaasCfg = String.format(jaasTemplate, "ndehlmr6", "U7oJMAYEyKgf1DYhHvH1SBXZwey0c5EU")

  val kafkaConfig = KafkaConsumer.Conf(
    keyDeserializer = new StringDeserializer,
    valueDeserializer = new StringDeserializer,
    bootstrapServers = "moped-01.srvs.cloudkafka.com:9094, moped-02.srvs.cloudkafka.com:9094, moped-03.srvs.cloudkafka.com:9094",
    groupId = "silk-delta",
    enableAutoCommit = false,
    sessionTimeoutMs = 30000,
    maxPartitionFetchBytes = ConsumerConfig.DEFAULT_MAX_PARTITION_FETCH_BYTES,
    maxPollRecords = 500,
    maxPollInterval = 300000,
    maxMetaDataAge  = 300000,
    autoOffsetReset = OffsetResetStrategy.EARLIEST,
    isolationLevel = IsolationLevel.READ_UNCOMMITTED
  )
  .withProperty("security.protocol", "SASL_SSL")
  .withProperty("sasl.mechanism", "SCRAM-SHA-256")
  .withProperty("sasl.jaas.config", jaasCfg)

  // Configuration specific to the Async Consumer Actor
  val actorConfig = KafkaConsumerActor.Conf(
    unconfirmedTimeout = 3.seconds, // duration for how long to wait for a confirmation before redelivery
    maxRedeliveries = 3             // maximum number of times a unconfirmed message will be redelivered
  )
  val system: ActorSystem = ActorSystem("silk")
  def test(msg: List[String]): Unit = {
    println("Message received: " + msg.head)
  }
  val testJob: Job = new Job("test-job", test)
  val merchant: Merchant = new Merchant("ndehlmr6-test", List(testJob))
  system.actorOf(Props(new Distributor(kafkaConfig, actorConfig, List(merchant))))
}
