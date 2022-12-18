import blokd.extensions.reloadBaseProperties
import blokd.integration.beforeAll
import blokd.extensions.KAFKA_POLL_DURATION
import java.time.Duration

const val CONSUMER_IMAGE_NAME = "blokd-consumer-3sig:latest"

const val PRODUCER_IMAGE_NAME = "blokd-producer-3sig:latest"

fun before3sig() {
    beforeAll()
    reloadBaseProperties("/Users/noah/projects/blokd/sample-configs/3sig")
}

fun getSingleConsumerWaitDuration() : Duration {
    val kafkaPollDuration = KAFKA_POLL_DURATION
    return Duration.ofMillis(kafkaPollDuration * 2)
}