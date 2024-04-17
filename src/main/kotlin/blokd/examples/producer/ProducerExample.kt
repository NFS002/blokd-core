@file:JvmName("ProducerExample")


package blokd.examples.producer


import blokd.block.Block
import blokd.block.actions.Contract
import blokd.extensions.CONFIG_DIR
import blokd.extensions.PRIMARY_KEYPAIR
import blokd.extensions.newKeypair
import blokd.service.Producer
import org.apache.log4j.PropertyConfigurator

fun main() {
    PropertyConfigurator.configure("$CONFIG_DIR/log4j.properties")
    val text = "This is a contract (v2)"
    val keyPair1 = PRIMARY_KEYPAIR
    val keyPair2 = newKeypair()
    val contract = Contract(text, owner = keyPair1.public, intendedRecipient = keyPair2.public)
    contract.sign(keyPair1.private)
    val block = Block(blockData = listOf(contract))
    block.sign(keyPair1)
    Producer.publish(block)
}