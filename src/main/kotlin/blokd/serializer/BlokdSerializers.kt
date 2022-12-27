package blokd.serializer

import blokd.block.Block
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.Serializer

class BlokdSerializer : Serializer<Block> {

    private val mapper = jacksonObjectMapper()

    override fun configure(configs: MutableMap<String, *>?, isKey: Boolean) {
        configs?.also {
            configureObjectMapper(mapper)
        }
    }

    override fun serialize(topic: String?, data: Block?): ByteArray {
        return mapper.writeValueAsBytes(data)
    }
}

class BlokdDeserializer : Deserializer<Block> {

    private val mapper = jacksonObjectMapper()

    override fun configure(configs: MutableMap<String, *>?, isKey: Boolean) {
        configs?.also {
            configureObjectMapper(mapper)
        }
    }

    override fun deserialize(topic: String?, data: ByteArray?): Block? {
        return mapper.readValue(data, Block::class.java)
    }
}
