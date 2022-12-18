@file:JvmName("ConsumerExample")

package blokd.examples.consumer

import blokd.extensions.CONFIG_DIR
import blokd.service.Consumer
import org.apache.log4j.PropertyConfigurator


fun main() {
    PropertyConfigurator.configure("$CONFIG_DIR/log4j.properties")
    Consumer.consumeForBlocks()
}