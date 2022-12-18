package blokd.block

import blokd.block.actions.BlockData
import blokd.extensions.hash

fun calculateHeader(blockData: List<BlockData>, previousHash: String, expectedHeight: Long, timestamp: Long, nonce: Long): String {
    val dt = blockData.joinToString(separator = "") { it.encoded }
    return "$previousHash$timestamp$dt$expectedHeight$nonce".hash()
}

fun calculateHeader(block:Block): String {
    return calculateHeader(
        block.blockData,
        block.previousHash,
        block.expectedHeight,
        block.timestamp,
        block.nonce
    )
}

fun Map<String, ByteArray>.decode() : List<Pair<String, String>> {
    return this.map { entry -> entry.decode() }
}

private fun Map.Entry<String, ByteArray>.decode() : Pair<String, String> {
    return this.key to this.value.decodeToString()
}