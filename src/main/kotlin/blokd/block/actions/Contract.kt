package blokd.block.actions

import blokd.extensions.hash
import blokd.extensions.sign
import blokd.extensions.verifySignature
import com.fasterxml.jackson.annotation.JsonProperty
import java.security.PrivateKey
import java.security.PublicKey
import java.time.Instant
import java.util.*


data class Contract constructor(
    @JsonProperty("text") val text: String,
    @JsonProperty("owner") val owner: PublicKey,
    @JsonProperty("intendedRecipient") val intendedRecipient: PublicKey
) :
    BlockData {

    val createdAt: Long = Instant.now().epochSecond

    override val encoded = "$text$createdAt$owner$intendedRecipient".hash()

    val id = UUID.nameUUIDFromBytes(encoded.toByteArray()).toString()

    override var signature = ByteArray(0)

    override fun sign(privateKey: PrivateKey): Contract {
        signature = encoded.sign(privateKey)
        return this
    }

    override fun validateSignature(publicKey: PublicKey): Boolean {
        return encoded.verifySignature(publicKey, signature)
    }

}