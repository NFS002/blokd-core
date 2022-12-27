package blokd.serializer

import blokd.block.BlockChain
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider

class BlockChainSerializer : JsonSerializer<BlockChain>() {
    override fun serialize(value: BlockChain?, gen: JsonGenerator?, serializers: SerializerProvider?) {
        value?.let {
            gen?.let {
                it.writeStartObject()
                it.writeNumberField("height", value.nextHeight)
                it.writePOJOField("blocks", value.blocks)
                val contractIds = value.contracts.keys
                val signedContractIds = value.signedContracts.keys
                it.writePOJOField("contracts", contractIds)
                it.writePOJOField("signedContracts", signedContractIds)
            }
        }
    }
}