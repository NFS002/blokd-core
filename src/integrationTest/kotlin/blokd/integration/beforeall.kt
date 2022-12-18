package blokd.integration

import blokd.block.BlockChain
import blokd.block.cache.Cache
import blokd.extensions.CONFIG_DIR
import blokd.extensions.then
import blokd.extensions.BLOCKS_TOPIC_NAME
import blokd.extensions.loadKafkaConfig
import org.apache.kafka.clients.admin.AdminClient
import org.apache.log4j.PropertyConfigurator

const val DOCKER_USER_NAME = "rockercockerdockerman"

fun beforeAll() {
    PropertyConfigurator.configure("$CONFIG_DIR/log4j.properties")
    BlockChain.reset()
    Cache.blocks.clear()
    with (AdminClient.create(loadKafkaConfig())) {
        var topicNames = listTopics().names().get()
        topicNames.contains(BLOCKS_TOPIC_NAME).then {
            deleteTopics(listOf(BLOCKS_TOPIC_NAME)).all().get()
        }
    }
}