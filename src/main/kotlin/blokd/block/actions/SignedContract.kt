package blokd.block.actions

import blokd.extensions.sign
import blokd.extensions.verifySignature
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.security.PrivateKey
import java.security.PublicKey

data class SignedContract constructor(@JsonProperty("contractId") val contractId: String, @JsonProperty("signedBy") val signedBy:PrivateKey) :
    BlockData {

    override val encoded: String = "${contractId}-signed"

    override var signature = encoded.sign(signedBy)


    override fun validateSignature(publicKey: PublicKey): Boolean {
        return encoded.verifySignature(publicKey, signature)
    }

    override fun sign(privateKey: PrivateKey) : SignedContract {
        this.signature = encoded.sign(privateKey)
        return this
    }
}