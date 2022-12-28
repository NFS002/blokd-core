package blokd.integration

import blokd.block.BlockChain
import blokd.block.cache.Cache
import blokd.extensions.*
import org.apache.kafka.clients.admin.AdminClient
import org.apache.log4j.PropertyConfigurator

const val DOCKER_USER_NAME = "rockercockerdockerman"

fun beforeAll() {
    PropertyConfigurator.configure("$CONFIG_DIR/log4j.properties")
    BlockChain.reset()
    Cache.clear()
    with (AdminClient.create(loadKafkaConfig())) {
        var allTopicNames = listTopics().names().get()
        val blockTopic = CLIENT_PROPERTIES.kafka.topic
        allTopicNames.contains(blockTopic).then {
            deleteTopics(listOf(blockTopic)).all().get()
        }
    }
}