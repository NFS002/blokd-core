package blokd.block

import blokd.block.actions.BlockData
import blokd.block.signature.BlockSignature
import blokd.block.signature.BlockSignatures
import blokd.extensions.*
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import org.apache.log4j.Level
import org.apache.log4j.Logger
import java.security.KeyPair
import java.security.PublicKey
import java.time.Instant

data class Block @JsonCreator constructor(
    @JsonProperty val previousHash: String = BlockChain.getPreviousBlock()?.header ?: "",
    @JsonProperty val expectedHeight: Long = BlockChain.nextHeight,
    @JsonProperty val blockData: List<BlockData> = listOf(),
    @JsonProperty val nonce: Long = 0,
    @JsonProperty val timestamp: Long = Instant.now().toEpochMilli(),
    @JsonProperty val header: String = calculateHeader(blockData, previousHash, expectedHeight, timestamp, nonce),
    @JsonProperty val signatures: BlockSignatures = BlockSignatures()
) {

    @JsonIgnore
    private val logger = Logger.getLogger(this::class.java)

    fun validateHeader(): Boolean {
        return this.header == calculateHeader(this)
    }

    fun isSignedBy(publicKey: PublicKey, id: () -> String = { publicKey.id() }): Boolean {
        val signature = signatures.find { sig -> sig.id == id() } ?: return false
        val res = this.header.verifySignature(publicKey, signature.signature)
        val logLevel: Level = if (res) Level.INFO else Level.WARN
        logger.log(logLevel, "$this is ${if (res) "correctly" else "incorrectly"} signed by ${id()}")
        return res
    }

    fun sign(keypair: KeyPair): BlockSignature {
        val signature = BlockSignature(Instant.now().toEpochMilli(), keypair.public.id(), this.header, this.expectedHeight, this.header.sign(keypair.private))
        signatures.add(signature)
        return signature
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false
        other as Block
        return (
                other.blockData.equals(this.blockData) &&
                other.header.equals(this.header) &&
                other.timestamp.equals(this.timestamp) &&
                other.nonce.equals(this.nonce) &&
                other.signatures.equals(this.signatures)
        )
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }

    override fun toString(): String {
        return "Block[header=${header.shorten()}, signatures=${signatures.size}, data=${blockData.size}, expectedHeight=${expectedHeight}, timestamp=$timestamp, nonce=$nonce]"
    }
}
