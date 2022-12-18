package blokd.block.signature

import java.util.*

data class BlockSignature(
    val timestamp: Long,
    val id: String,
    val header: String,
    val height: Long,
    val signature: ByteArray
) {
    override fun equals(other: Any?): Boolean {

        if (other !is BlockSignature) return false

        return (
            this.timestamp.equals(other.timestamp) &&
                    this.id.equals(other.id) &&
                    this.header.equals(other.header) &&
                    this.height.equals(other.height) &&
                    Arrays.equals(signature.toTypedArray(), other.signature.toTypedArray())
            )
    }

    override fun hashCode(): Int {
        var result = timestamp.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + header.hashCode()
        result = 31 * result + height.hashCode()
        result = 31 * result + signature.contentHashCode()
        return result
    }
}