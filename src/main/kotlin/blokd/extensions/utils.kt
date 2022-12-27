package blokd.extensions

import java.io.FileInputStream

import java.util.*


private const val CLIENT_CONFIG_FILENAME = "client.json"

val CLIENT_PROPERTIES = loadBlokdProperties(CLIENT_CONFIG_FILENAME)

val KAFKA_CLIENT_PROPERTIES = CLIENT_PROPERTIES.getJSONObject("kafka")


val KAFKA_CLIENT_ID: String = CLIENT_PROPERTIES.getString("clientId") ?: PRIMARY_KEYPAIR.public.id()

val KAFKA_GROUP_ID: String = KAFKA_CLIENT_PROPERTIES.getString("groupId") ?: PRIMARY_KEYPAIR.public.id()

val BLOCKS_TOPIC_NAME: String = KAFKA_CLIENT_PROPERTIES.getString("topic")

val KAFKA_POLL_DURATION: Long = KAFKA_CLIENT_PROPERTIES.getLong("pollDuration")

fun loadKafkaConfig():Properties {
    val props = Properties()
    val kafkaConfigPath = "$CONFIG_DIR/kafka.config"
    FileInputStream(kafkaConfigPath).use {
        props.load(it)
    }
    return props
}