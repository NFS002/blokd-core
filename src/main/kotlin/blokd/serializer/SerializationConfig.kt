package blokd.serializer

import blokd.block.cache.Cache
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.security.PrivateKey
import java.security.PublicKey

private fun getRSAKeySerializerModule(): SimpleModule {
    val module = SimpleModule()
    module.addSerializer(PublicKey::class.java, PublicKeySerializer())
    module.addDeserializer(PublicKey::class.java, PublicKeyDeserializer())
    module.addSerializer(PrivateKey::class.java, PrivateKeySerializer())
    module.addDeserializer(PrivateKey::class.java, PrivateKeyDeserializer())
    return module
}

private fun getCacheSerializerModule(): SimpleModule {
    val module = SimpleModule()
    module.addSerializer(Cache::class.java, CacheSerializer())
    return module
}

fun configureObjectMapper(mapper: ObjectMapper) : ObjectMapper {
    mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
    mapper.configure(SerializationFeature.INDENT_OUTPUT, true)
    mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
    val rsaModule = getRSAKeySerializerModule()
    val cacheModule = getCacheSerializerModule()
    mapper.registerModule(rsaModule)
    mapper.registerModule(cacheModule)
    return mapper
}

fun blokdObjectMapper() : ObjectMapper {
    return configureObjectMapper(
        jacksonObjectMapper()
    )
}
