package blokd.blokd.block

import blokd.block.BlockChain
import blokd.extensions.newKeypair
import org.junit.Before
import org.junit.Test
import java.security.KeyPair

class BlockChainTest {

    private lateinit var keyPair: KeyPair

    @Before
    fun beforeTest() {
        keyPair = newKeypair()
    }

    /**
     * Tests the blockchain is initially in a valid state
     */
    @Test
    fun initiallyValid() {
        assert(BlockChain.isValid())
    }
}