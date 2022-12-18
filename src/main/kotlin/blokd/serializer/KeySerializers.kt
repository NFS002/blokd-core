package blokd.serializer

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.EncodedKeySpec
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec

class PublicKeySerializer: JsonSerializer<PublicKey>(){
    override fun serialize(value: PublicKey?, gen: JsonGenerator?, serializers: SerializerProvider?) {
        value?.let { key ->
            gen?.writeBinary(key.encoded)
        }
    }
}

class PublicKeyDeserializer : JsonDeserializer<PublicKey>() {


    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): PublicKey? {
        return p?.let {parser ->
            val keyFactory = KeyFactory.getInstance("RSA")
            val bytes = parser.binaryValue
            val publicKeySpec: EncodedKeySpec = X509EncodedKeySpec(bytes)
            keyFactory.generatePublic(publicKeySpec)
        }
    }

}

class PrivateKeySerializer: JsonSerializer<PrivateKey>(){
    override fun serialize(value: PrivateKey?, gen: JsonGenerator?, serializers: SerializerProvider?) {
        value?.let { key ->
            gen?.writeBinary(key.encoded)
        }
    }
}

class PrivateKeyDeserializer : JsonDeserializer<PrivateKey>() {

    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): PrivateKey? {
        return p?.let {parser ->
            val keyFactory = KeyFactory.getInstance("RSA")
            val bytes = parser.binaryValue
            val privateKeySpec: EncodedKeySpec = PKCS8EncodedKeySpec(bytes)
            keyFactory.generatePrivate(privateKeySpec)
        }
    }

}