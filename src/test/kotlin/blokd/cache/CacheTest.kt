package blokd.cache

import blokd.block.Block
import blokd.block.BlockChain
import blokd.block.cache.Cache
import blokd.block.signature.BlockSignature
import blokd.extensions.id
import blokd.extensions.newKeypair
import org.junit.Before
import org.junit.Test

class CacheTest {

    @Before
    fun before() {
        BlockChain.reset()
        Cache.clear()
    }

    private fun loadBlocks(blocks: List<Block>) {
        blocks.forEach {
            Cache.getOrCreate(it)
        }
    }


    /*
    * Given:
    *  BlockChain: b0x2
    *  Cache:
    *      0: b0x1, b0x2
    *      1: b1x1, b1x2
    *      2: b2x1, b2x2, b2x3
    *
    *  Cache Signatures
    *      0:
    *      1:
    *      2: s2x3
    *
    * When:
    *  We build the BlockChain from the Cache
    *
    * Then:
    *  BlockChain: b0x2 -> b1x2 -> b2x3
    */
    @Test
    fun `Can build consecutive blocks from Cache`() {

        val keypair1 = newKeypair()
        val keypair2 = newKeypair()


        val block0x1 = Block()
        val block0x2 = Block()

        val block1x1 = Block(previousHash = block0x1.header, expectedHeight = 1)
        val block1x2 = Block(previousHash = block0x2.header, expectedHeight = 1)


        val block2x1 = Block(previousHash = block1x1.header, expectedHeight = 2)
        val block2x2 = Block(previousHash = block1x2.header, expectedHeight = 2)
        val block2x3 = Block(previousHash = block1x2.header, expectedHeight = 2)

        val blocks = listOf(block0x1, block0x2, block1x1, block1x2, block2x1, block2x2, block2x3)
        blocks.forEach { block -> block.sign(keypair1)  }
        block2x3.sign(keypair2)

        loadBlocks(blocks)

        BlockChain.add(block0x2)
        Cache.buildToChain()

        assert(BlockChain.blocks.size == 3)
        assert(BlockChain.nextHeight == 3L)
        assert(BlockChain.blocks[0].equals(block0x2))
        assert(BlockChain.blocks[1].equals(block1x2))
        assert(BlockChain.blocks[2].equals(block2x3))
    }
}

