package blokd.block

import blokd.contract.registerContract
import blokd.extensions.newKeypair
import org.junit.Before
import org.junit.Test
import java.security.KeyPair
import kotlin.test.assertNotEquals


class BlockTest {

    lateinit var keyPairs: List<KeyPair>

    @Before()
    fun before() {
        keyPairs = (1..10).map { newKeypair() }
    }


    @Test
    fun calculateHash() {
        repeat(2) { registerContract(keyPairs[0], keyPairs[1].public) }
        val block1Header = BlockChain.blocks[0].header
        val block2Header = BlockChain.blocks[1].header
        assertNotEquals(block1Header, block2Header)
    }
}