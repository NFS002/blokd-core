package blokd.service

import blokd.block.Block
import blokd.config.Kafka
import blokd.extensions.CLIENT_PROPERTIES
import blokd.extensions.loadKafkaConfig
import blokd.serializer.BlokdSerializer
import org.apache.kafka.clients.admin.AdminClient
import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig.*
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.apache.kafka.common.errors.TopicExistsException
import org.apache.kafka.common.serialization.StringSerializer
import org.apache.log4j.Level
import org.apache.log4j.Logger
import java.util.*

object Producer {

    private val LOGGER = Logger.getLogger(this::class.java)


    private fun createTopic(properties: Properties, topic: NewTopic): Result<Void> {
        return runCatching {
            with(AdminClient.create(properties)) {
                LOGGER.debug("Attempting creation of topic '${topic.name()}'")
                createTopics(listOf(topic)).all().get()
            }
        }
    }

    private  fun loadBlockProducerConfig(): Properties {
        val props = loadKafkaConfig()
        // Add additional properties.
        props[ACKS_CONFIG] = "all"
        props[KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.qualifiedName
        props[VALUE_SERIALIZER_CLASS_CONFIG] = BlokdSerializer::class.qualifiedName
        props[CLIENT_ID_CONFIG] = CLIENT_PROPERTIES.kafka.clientId
        return props
    }

    fun publish(block: Block) {
        val props = loadBlockProducerConfig()
        val res = createTopic(props, NewTopic(CLIENT_PROPERTIES.kafka.topic, 1, 3))
        this.handleTopicCreationResult(CLIENT_PROPERTIES.kafka.topic, res)

        val producer: KafkaProducer<String, Block> = KafkaProducer<String, Block>(props)

        producer.use {
            val record = ProducerRecord(CLIENT_PROPERTIES.kafka.topic, block.header, block)
                it.send(record) { m: RecordMetadata, e: Exception? ->
                this.handlePublishResult<Block>(record, m, e)
            }
        }

        LOGGER.debug("PREPARING TO EXIT PRODUCER")
        producer.close()
        LOGGER.debug("EXITED PRODUCER")

    }


    private fun handleTopicCreationResult(topicName:String, topicCreationResult: Result<Void>) {
        topicCreationResult.onSuccess {
            LOGGER.info("Topic creation for '$topicName' suceeded")
        }.onFailure { exc ->
            val logLevel: Level = if (exc.cause is TopicExistsException) Level.WARN else Level.ERROR
            LOGGER.log(logLevel, "Topic creation for '${topicName}' failed: $exc")
        }
    }

    private fun <T> handlePublishResult(record: ProducerRecord<String, T>, metadata: RecordMetadata, exception: Exception?) {
        exception?.let {
            LOGGER.error("Publishing '${record.value()}' to'${CLIENT_PROPERTIES.kafka.topic}' failed: $exception")
            throw it
        } ?: run {
            LOGGER.info("Publishing '${record.value()}' to topic '${metadata.topic()}' succeeded.")
        }
    }
}
