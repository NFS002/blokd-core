package blokd.integration.threesig

import CONSUMER_IMAGE_NAME
import PRODUCER_IMAGE_NAME
import before3sig
import blokd.block.BlockChain
import blokd.block.cache.Cache
import blokd.extensions.then
import blokd.integration.DOCKER_USER_NAME
import getSingleConsumerWaitDuration
import org.apache.log4j.Logger
import org.junit.Before
import org.junit.Test
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.startupcheck.OneShotStartupCheckStrategy
import org.testcontainers.utility.DockerImageName
import java.lang.IllegalStateException

class P1xC1xC2xC1Test {

    private lateinit var log: Logger

    @Before
    fun before() {
        before3sig()
        log = Logger.getLogger(this::class.java)
    }

    /*
     * Simple 3sig integration test:
     * 1. Load 3sig properties
     * 2. Producer 1 starts and completes
     * 3. Consumer 1 starts and completes
     * 4. Assert:
     *      -the chain is empty
     *      - the cache contains a single block with 2 signatures
     * 5. Consumer 2 starts and completes
     * 6  Consumer 1 restarts and completes
     * 7. Assert
     *      - The chain has a single block with 3 signatures
     *      - The cache contains the same block
     */
    @Test
    fun c1xp1xp2xp1Test() {
        /* TODO:
        * poll for duration specified in env and only wait for that long for the container to exit
        * or wait for "EXITED" log message */

        val singleConsumerWait = getSingleConsumerWaitDuration()

        val producer1 = GenericContainer(DockerImageName.parse("${DOCKER_USER_NAME}/${PRODUCER_IMAGE_NAME}"))
        producer1.withStartupCheckStrategy(OneShotStartupCheckStrategy().withTimeout(singleConsumerWait));


        val consumer1 = GenericContainer(DockerImageName.parse("${DOCKER_USER_NAME}/${CONSUMER_IMAGE_NAME}"))
        consumer1.withStartupCheckStrategy(OneShotStartupCheckStrategy().withTimeout(singleConsumerWait));




        log.info("PRODUCER STARTING")

        producer1.start()

        producer1.isRunning.then {
            throw IllegalStateException("${producer1.containerName} (image=${producer1.getDockerImageName()}) is still running")
        }

        log.info("PRODUCER EXITED")

        blokd.examples.consumer.main()

        assert(Cache.blocks.size == 1)
        assert(Cache.blocks[0].signatures.size == 2)
        assert(BlockChain.nextHeight == 0L)


        log.info("CONSUMER STARTING")

        consumer1.start()

        consumer1.isRunning.then {
            throw IllegalStateException("${consumer1.containerName} (image=${consumer1.getDockerImageName()}) is still running")
        }

        log.info("CONSUMER EXITED")

        blokd.examples.consumer.main()


        assert(BlockChain.nextHeight == 1L)
        assert(BlockChain.blocks[0].signatures.size  == 3)
        assert(Cache.contains(BlockChain.blocks[0]))
    }
}