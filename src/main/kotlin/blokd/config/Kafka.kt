package blokd.config

data class Kafka(
    val topic: String = "blocks",
    val pollDuration: Long = 10000,
    val clientId: String? = null,
    val groupId: String? = null
)