package blokd.extensions

import blokd.config.BlokdConfig
import blokd.config.ClientConfig
import blokd.serializer.blokdObjectMapper
import org.apache.log4j.Level
import org.apache.log4j.Logger
import java.io.File
import java.io.FileInputStream
import java.math.BigInteger
import java.security.*
import java.security.spec.EncodedKeySpec
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*
import kotlin.io.path.Path

const val PUBLIC_KEY_FILENAME = "public"

const val PRIVATE_KEY_FILENAME = "private"

const val CONFIG_FILENAME = "blokd.json"

private const val CLIENT_CONFIG_FILENAME = "client.json"

val LOGGER: Logger = Logger.getLogger("utils")

var CONFIG_DIR: String = System.getenv("BLOKD_CONFIG_DIR") ?: System.getProperty("user.dir").plus("/config")

var BASE_PROPERTIES: BlokdConfig = loadConfig(fileName = CONFIG_FILENAME)

var CLIENT_PROPERTIES: ClientConfig = loadConfig(CLIENT_CONFIG_FILENAME)

val PRIMARY_KEYPAIR = getOrLoadKeys()

inline fun <reified T> loadConfig(fileName: String): T {
    val path = Path(CONFIG_DIR).resolve(fileName)
    val f = path.toFile()
    when {
        f.exists() -> {
            val mapper = blokdObjectMapper()
            FileInputStream(f).use {
                return mapper.readValue(it, T::class.java)
            }
        }
        else -> {
            throw IllegalStateException(
                "Configuration file $fileName could not be found in configuration directory $CONFIG_DIR. " +
                        "You can set the configuration directory path to look for manually using the 'BLOKD_CONFIG_DIR' environment variable. " +
                        "Please make sure this point to a directory contain all the required configuration files."
            )
        }
    }
}

fun loadKafkaConfig(): Properties {
    val props = Properties()
    val kafkaConfigPath = "$CONFIG_DIR/kafka.config"
    FileInputStream(kafkaConfigPath).use {
        props.load(it)
    }
    return props
}

fun reloadConfig(configDir: String) {
    CONFIG_DIR = configDir
    BASE_PROPERTIES = loadConfig(CONFIG_FILENAME)
    CLIENT_PROPERTIES = loadConfig(CLIENT_CONFIG_FILENAME)
}

fun String.hash(algorithm: String = "SHA-256"): String {
    val messageDigest = MessageDigest.getInstance(algorithm)
    messageDigest.update(this.toByteArray())
    val base64Hash = String.format("%064x", BigInteger(1, messageDigest.digest()))
    LOGGER.debug("Calculated base64 hash of string '${this.shorten()}' as '${base64Hash.shorten()}")
    return base64Hash
}

fun String.sign(privateKey: PrivateKey, algorithm: String = "SHA256withRSA"): ByteArray {
    val rsa = Signature.getInstance(algorithm)
    rsa.initSign(privateKey)
    rsa.update(this.toByteArray())
    val signed = rsa.sign()
    LOGGER.debug("Signed string '${this.shorten()}' and calculated ${signed.decodeToString().shorten()}")
    return signed
}

fun String.shorten(i: Int = 3): String {
    val l = this.length
    return when (l > i * 2) {
        true -> "${this.subSequence(0, i)}...${this.subSequence(l - i, l)}"
        else -> this
    }
}

fun String.verifySignature(publicKey: PublicKey, signature: ByteArray, algorithm: String = "SHA256withRSA"): Boolean {
    val rsa = Signature.getInstance(algorithm)
    rsa.initVerify(publicKey)
    rsa.update(this.toByteArray())
    val res = rsa.verify(signature)
    val logLevel: Level = if (res) Level.DEBUG else Level.WARN
    LOGGER.log(
        logLevel,
        "Verified signature of ${signature.toString().shorten()} with public key " + "${
            publicKey.encodeToString().shorten()
        } with result=$res"
    )
    return res
}

infix fun String.xor(that: String): String {

    val caller: String = this

    (that.isEmpty()).then {
        return caller
    }

    (this.length == that.length).then {
        return caller.mapIndexed { idx, c ->
            that[idx].code.xor(c.code)
        }.joinToString(separator = "")
    }
    throw IllegalArgumentException("Both strings must be of the same length")
}

fun Key.id(): String {
    return UUID.nameUUIDFromBytes(this.encodeToString().toByteArray()).toString()
}


fun Key.encodeToString(): String {
    return Base64.getEncoder().encodeToString(this.encoded)
}

inline fun Boolean.then(block: Boolean.() -> Unit): Boolean {
    if (this) {
        block()
    }
    return this
}


inline fun Boolean.ifTrue(block: Boolean.() -> Unit): Boolean {
    if (this) block()
    return this@ifTrue
}

inline fun Boolean.ifFalse(block: Boolean.() -> Unit): Boolean {
    if (!this) block()
    return this@ifFalse
}

fun Collection<String>.hashList() {
    this.reduce { acc, v -> (v xor acc).hash() }
}

fun newKeypair(save: Boolean = false): KeyPair {
    val generator = KeyPairGenerator.getInstance("RSA")
    generator.initialize(2048)
    val keyPair = generator.generateKeyPair()
    LOGGER.debug("Generated new keypair with id=${keyPair.public.id()}")
    save.then { saveKeyPair(keyPair) }
    return keyPair
}

fun saveKeyPair(keyPair: KeyPair) {
    val keypath = Path(CONFIG_DIR, "keys").toString()
    val f1 = Path(keypath, PUBLIC_KEY_FILENAME).toFile()
    val f2 = Path(keypath, PRIVATE_KEY_FILENAME).toFile()
    f1.createNewFile().then {
        f2.createNewFile().then {
            f1.writeBytes(keyPair.public.encoded)
            f2.writeBytes(keyPair.private.encoded)
            LOGGER.debug("Saved new keypair in $keypath with id=${keyPair.public.id()}")
        }
    }
}

private fun getOrLoadKeys(): KeyPair {
    return loadKeyPair() ?: newKeypair(save = true)
}

private fun loadKeyPair(): KeyPair? {
    val keypath = Path(CONFIG_DIR, "keys").toString()
    val f1 = Path(keypath, PUBLIC_KEY_FILENAME).toFile()
    val f2 = Path(keypath, PRIVATE_KEY_FILENAME).toFile()
    val canRead = f1.exists().and(f2.exists()).and(f1.canRead()).and(f2.canRead())
    canRead.ifFalse {
        LOGGER.error("Cannot read keypair from $keypath")
    }
    return if (canRead) KeyPair(loadPublic(f1), loadPrivate(f2)) else null
}

private fun loadPublic(f1: File): PublicKey {
    LOGGER.debug("Loading public key from file ${f1.path}")
    val encoded = f1.readBytes()
    val keyFactory = KeyFactory.getInstance("RSA")
    val publicKeySpec: EncodedKeySpec = X509EncodedKeySpec(encoded)
    return keyFactory.generatePublic(publicKeySpec)
}

private fun loadPrivate(f1: File): PrivateKey {
    LOGGER.debug("Loading private key from file ${f1.path}")
    val encoded = f1.readBytes()
    val keyFactory = KeyFactory.getInstance("RSA")
    val privateKeySpec: EncodedKeySpec = PKCS8EncodedKeySpec(encoded)
    return keyFactory.generatePrivate(privateKeySpec)
}