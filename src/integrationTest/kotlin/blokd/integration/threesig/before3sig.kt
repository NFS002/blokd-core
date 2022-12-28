import blokd.config.Kafka
import blokd.extensions.CLIENT_PROPERTIES
import blokd.extensions.reloadConfig
import blokd.integration.beforeAll
import java.time.Duration

const val CONSUMER_IMAGE_NAME = "blokd-consumer-3sig:latest"

const val PRODUCER_IMAGE_NAME = "blokd-producer-3sig:latest"

fun before3sig() {
    beforeAll()
    reloadConfig("/Users/noah/projects/blokd/sample-configs/3sig")
}

fun getSingleConsumerWaitDuration() : Duration {
    val kafkaPollDuration = CLIENT_PROPERTIES.kafka.pollDuration
    return Duration.ofMillis(kafkaPollDuration * 2)
}