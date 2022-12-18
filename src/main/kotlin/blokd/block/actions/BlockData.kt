package blokd.block.actions

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import java.security.PrivateKey
import java.security.PublicKey

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = Contract::class, name = "Contract"),
    JsonSubTypes.Type(value = SignedContract::class, name = "SignedContract")
)
sealed interface BlockData {

    fun validateSignature(publicKey: PublicKey) : Boolean

    fun sign(privateKey: PrivateKey) : BlockData

    val encoded : String

    val signature: ByteArray
}
