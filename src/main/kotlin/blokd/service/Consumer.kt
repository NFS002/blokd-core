package blokd.service

import blokd.block.Block
import blokd.block.BlockChain
import blokd.block.cache.Cache
import blokd.extensions.*
import blokd.serializer.BlokdCoreDeserializer
import io.confluent.kafka.serializers.KafkaJsonDeserializerConfig.JSON_VALUE_TYPE
import org.apache.kafka.clients.consumer.ConsumerConfig.*
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.log4j.Logger
import java.time.Duration.ofMillis
import java.util.*

object Consumer {

    private val LOGGER = Logger.getLogger(this::class.java)


    private fun loadBlockConsumerConfig(): Properties {
        val properties = loadKafkaConfig()
        properties[KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java.name
        properties[VALUE_DESERIALIZER_CLASS_CONFIG] = BlokdCoreDeserializer::class.java.name
        properties[JSON_VALUE_TYPE] = Block::class.java
        properties[GROUP_ID_CONFIG] = KAFKA_GROUP_ID
        properties[CLIENT_ID_CONFIG] =  KAFKA_CLIENT_ID
        //properties[AUTO_OFFSET_RESET_CONFIG] = "latest"
        properties[ENABLE_AUTO_COMMIT_CONFIG] = false
        properties[AUTO_OFFSET_RESET_CONFIG] = "earliest"

        return properties
    }

    fun consumeForBlocks() {
        // Load properties from disk.
        val properties = loadBlockConsumerConfig()

        val blockConsumer = KafkaConsumer<String, Block>(properties).apply {
            subscribe(listOf(BLOCKS_TOPIC_NAME))
        }

        val keyPair = PRIMARY_KEYPAIR
        LOGGER.debug("POLLING | topic='${blockConsumer.subscription()}', client-id=$KAFKA_CLIENT_ID, group-id=$KAFKA_GROUP_ID")
        val records: ConsumerRecords<String, Block> = blockConsumer.poll(ofMillis(KAFKA_POLL_DURATION))
        LOGGER.debug("RECEIVED | records=${records.count()}, topic=${blockConsumer.subscription()}")
        val lastRecord = records.last()
        val key = lastRecord.key()
        val block = lastRecord.value()
        val previouslyCached = Cache.contains(block)

        LOGGER.info("CONSUMED | key=${key.shorten()}, value=${block}, cached=$previouslyCached")

        val cachedBlock = Cache.getOrCreate(block.copy())

        /* When the same block we just received has been previously seen and cached, it is possible that some block signature
         * in the block we just processed won't be in the cached block, so we have to transfer them across.
         */
        previouslyCached.then {
            block.signatures.forEach {
                cachedBlock.signatures.add(it, replace = false)
            }
            LOGGER.info("ADDED BLOCK SIGNATURES | signatures=${block.signatures.size}, total-signatures=${cachedBlock.signatures.size}")
        }

        cachedBlock.isSignedBy(keyPair.public).ifTrue {
            LOGGER.info("PREVIOUSLY SIGNED | key-id='${keyPair.public.id().shorten()}'")
        }.ifFalse {
            LOGGER.info("NOT PREVIOUSLY SIGNED key-id='${keyPair.public.id().shorten()}'")
            cachedBlock.sign(keyPair)
            LOGGER.info("SIGNED BLOCK | block=$cachedBlock, key-id=${keyPair.public.id().shorten()}'")
            Producer.publish(cachedBlock)
        }

        LOGGER.debug("ATTEMPTING ADD | block=$cachedBlock")
        BlockChain.add(block = cachedBlock).onSuccess {
            LOGGER.info("CHAINED: block=$cachedBlock, next-height=${BlockChain.nextHeight}")
            Cache.buildToChain()
        }.onFailure {
            LOGGER.warn("NOT CHAINED: block=$cachedBlock, error=${it.message}")
        }

        LOGGER.debug("PREPARING TO EXIT CONSUMER")
        blockConsumer.close()
        LOGGER.debug("EXITED CONSUMER")
    }
}