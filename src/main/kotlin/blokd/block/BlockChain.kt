package blokd.block

import blokd.block.actions.Contract
import blokd.block.actions.SignedContract
import blokd.config.Validation
import blokd.extensions.*
import org.apache.log4j.Logger
import scala.Enumeration.Val
import java.security.SignatureException

object BlockChain {

    private val logger: Logger = Logger.getLogger(this::class.java)

    val blocks: MutableList<Block> = mutableListOf()
    private const val difficulty: Int = 2
    private const val prefix: String = "0"

    val contracts: HashMap<String, Contract> = hashMapOf()
    val signedContracts: HashMap<String, SignedContract> = hashMapOf()

    val nextHeight: Long
        get() = blocks.size.toLong()


    fun isValid(): Boolean {
        var result = true
        val windowedBlocks = blocks.windowed(size = 2, step = 1, partialWindows = true)
        windowedBlocks.forEachIndexed { _, wdw ->
            val first = wdw.get(0)
            result = result.and(validateBlock(first).isSuccess).and(first.validateHeader())
            (wdw.size > 1).then {
                val second = wdw.get(1)

                result = result.and(validateBlock(second).isSuccess).and(second.validateHeader()).and(
                    first.header == second.previousHash
                )
            }
        }
        return result
    }

    private fun canAccept(block: Block): Result<Boolean> {
        return kotlin.runCatching {
            val currHash: String = this.getLastBlockHeader()
            (block.expectedHeight == this.nextHeight).ifTrue {
                (block.previousHash == currHash).ifTrue {
                    // TODO("Check new blockchain is valid and this block meets validation rules")
                }.ifFalse {
                    throw IllegalStateException("Previous block hash ${block.previousHash.shorten()} of given block " +
                            "does not match current block header of ${currHash.shorten()}")
                }
            }.ifFalse {
                throw IllegalStateException("Block height ${block.expectedHeight} does not match next height of $nextHeight")
            }
        }
    }

    fun validateBlock(block: Block): Result<Unit> {

        val minSignatures = BASE_PROPERTIES.validation.minSignatures
        return kotlin.runCatching {
            when {
                (block.signatures.size < minSignatures) ->
                    throw IllegalStateException("Block signatures is below required minimum of $minSignatures")
                //TODO("Implement proper validation rules")
                else -> Unit
            }
        }
    }

    fun add(block: Block) : Result<Boolean> {
        return kotlin.runCatching {
            canAccept(block).getOrThrow()
            validateBlock(block).getOrThrow()
            process(block).getOrThrow()
            val res = blocks.add(block)
            res.then {
                logger.info("Added $block, next height is now $nextHeight")
            }
            res
        }
    }

    private fun handleBlockAddResult(blockAddResult: Result<Boolean>) {
        blockAddResult.onFailure {
            logger.error("Failed to add block", it)
        }.onSuccess {
            logger.info("Block successfully added")
        }
    }


    private fun process(block: Block): Result<Unit> {
        return kotlin.runCatching {
            for (blockData in block.blockData) {
                when (blockData) {
                    is Contract -> registerContract(blockData)
                    is SignedContract -> registerSignedContract(blockData)
                }
            }
        }
    }

    private fun isMined(block: Block): Boolean {
        return block.header.startsWith(prefix.repeat(difficulty))
    }


    private fun registerContract(contract: Contract) {

        val contractId = contract.id

        if (hasRegisteredContract(contractId = contractId)) {
            throw java.lang.IllegalArgumentException("Contract '${contractId}' is already registered")
        }

        if (!contract.validateSignature(contract.owner)) {
            throw SignatureException("Contract was not signed by its initial owner")
        }

        contracts[contractId] = contract
    }

    private fun registerSignedContract(signedContract: SignedContract) {
        val contractId = signedContract.contractId

        val contract = contracts.getOrElse(contractId) {
            throw IllegalStateException("Initial Contract not registered")
        }

        (signedContracts.containsKey(contractId)).then {
            throw IllegalStateException("Contract has already been signed")
        }

        (!signedContract.validateSignature(contract.intendedRecipient)).then {
            throw SignatureException("Contract signature is invalid")
        }

        signedContracts[contractId] = signedContract
    }

    private fun hasRegisteredContract(contractId: String): Boolean {
        return contracts.containsKey(contractId)
    }

    fun findSigned(contractId: String): SignedContract? {
        contracts.getOrElse(contractId) { throw IllegalStateException("Initial contract was never registered") }
        return signedContracts.getOrDefault(contractId, null)
    }

    fun getLastBlock(): Block? {
        return blocks.lastOrNull()
    }

    fun getLastBlockHeader(): String {
        return getLastBlock()?.header ?: ""
    }

    fun reset() {
        blocks.clear()
        contracts.clear()
    }
}