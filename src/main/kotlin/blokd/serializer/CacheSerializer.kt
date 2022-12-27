package blokd.serializer

import blokd.block.cache.Cache
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider

class CacheSerializer : JsonSerializer<Cache>() {

    override fun serialize(value: Cache?, gen: JsonGenerator?, serializers: SerializerProvider?) {
        value?.let {
            gen?.let {
                it.writeStartObject()
                it.writePOJOField("blocks", value.blocks)
            }
        }
    }

}