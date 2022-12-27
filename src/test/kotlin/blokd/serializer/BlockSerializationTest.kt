package blokd.serializer

import blokd.block.Block
import blokd.block.actions.Contract
import blokd.block.actions.SignedContract
import blokd.extensions.newKeypair
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.Before
import org.junit.Test
import kotlin.test.assertTrue

class BlockSerializationTest {

    lateinit var mapper:ObjectMapper

    @Before
    fun before() {
        mapper = blokdObjectMapper()
    }

    /* Serialization/Deserialisation test:
     * (De)serialize a Block to and from json
     * and check the result is equal to the block we
     * started with.
     */
    @Test
    fun `Block and (De)serialized Block are equal`() {
        val text = "This is a contract! Will you sign it?"
        val keyPair1 = newKeypair()
        val keyPair2 = newKeypair()
        val contract = Contract(text, owner = keyPair1.public, intendedRecipient = keyPair2.public)
        val signedContract = SignedContract(contract.id, keyPair2.private)
        contract.sign(keyPair1.private)
        val block = Block(blockData = listOf(contract, signedContract))
        block.sign(keyPair1)
        val jsonString = mapper.writeValueAsString(block)
        val newBlock = mapper.readValue(jsonString, Block::class.java)
        assertTrue(block.equals(newBlock))
    }
}