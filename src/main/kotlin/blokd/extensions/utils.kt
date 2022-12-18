package blokd.extensions

import java.io.FileInputStream

import java.util.*

val KAFKA_CLIENT_PROPERTIES = loadBlokdProperties("client.properties")

val KAFKA_GROUP_ID: String = KAFKA_CLIENT_PROPERTIES.getProperty("kafka.group-id", PRIMARY_KEYPAIR.public.id())

val KAFKA_CLIENT_ID: String = KAFKA_CLIENT_PROPERTIES.getProperty("kafka.client-id", PRIMARY_KEYPAIR.public.id())

val KAFKA_POLL_DURATION: Long = 10000

const val BLOCKS_TOPIC_NAME: String = "blocks_v20"

fun loadKafkaConfig():Properties {
    val props = Properties()
    val kafkaConfigPath = "$CONFIG_DIR/kafka.config"
    FileInputStream(kafkaConfigPath).use {
        props.load(it)
    }
    return props
}