package blokd.block.cache

import blokd.block.Block
import blokd.block.BlockChain
import blokd.block.signature.BlockSignature
import blokd.extensions.ifFalse
import blokd.extensions.ifTrue
import blokd.extensions.shorten
import blokd.extensions.then
import org.apache.log4j.Logger

object Cache {

    val blocks: MutableList<Block> = mutableListOf()

    val logger: Logger = Logger.getLogger(this::class.java)

    fun add(signature: BlockSignature): Block? {
        // First find the relevant block
        val blockIdx: Int = this.blocks.indexOfFirst { block -> block.header == signature.header }
        return when {
            blockIdx >= 0 -> {
                logger.info("Found block with given header ${signature.header.shorten()}")
                val block = this.blocks[blockIdx]
                block.signatures.add(signature)
                block
            }
            else -> {
                logger.error("Received block signature for header ${signature.header.shorten()} not previously seen")
                logger.error("Im just gonna pretend this never happened...")
                null
            }
        }
    }


    fun getAll(height: Long): List<Block> {
        val blocks = blocks.filter { block ->
            block.expectedHeight == height
        }
        logger.debug("Looked for block with expected height of ${height}, and found ${blocks.size} blocks")
        return blocks
    }

    fun contains(block: Block) : Boolean {
        return blocks.indexOfFirst { it.header == block.header } >= 0
    }

    fun getOrCreate(block: Block): Block {
        val idx =  this.blocks.indexOf(block)
        return when {
            idx >= 0 -> {
                blocks[idx]
            }
            else -> {
                val newIdx = blocks.size
                blocks.add(newIdx, block)
                blocks[newIdx]
            }
        }
    }

    fun buildToChain() {
        val nextHeight = BlockChain.nextHeight
        logger.debug("Checking cache for blocks with suggested height of ${nextHeight}")
        val nextBlocks: List<Block> = getAll(nextHeight).sortedByDescending { block -> block.signatures.size }
        logger.debug("Found ${nextBlocks.size} blocks")
        for (nextBlock: Block in nextBlocks) {
            val res = BlockChain.add(nextBlock)
            res.onSuccess {
                it.then {
                    buildToChain()
                    return
                }
            }.onFailure {
                logger.error("Cannot add $nextBlock retrieved from cache", it)
            }
        }
    }

    /* Prefer according to validation rules */
    fun preferred(block1: Block, block2: Block): Block {
        val block = block1
        //TODO("Implement validation rules")
        logger.info("Calculated preference between $block1 and $block2, and found $block")
        return block
    }

    fun clear() {
        this.blocks.clear()
    }

    operator fun get(x: Int) : Block {
        return this.blocks[x]
    }
}