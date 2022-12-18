package blokd.config

import java.io.FileInputStream
import java.util.Properties

object ChainSettings {

        val properties:Properties

        init {
            this.properties = loadProperties()
        }

        private fun loadProperties() : Properties {
            val props = Properties()
            val defaultConfigPath = "src/main/resources/blokd.properties"
            val configPath = System.getProperty("BLOKD__SETTINGS_PATH", defaultConfigPath)
            FileInputStream(configPath).use { fis ->
                props.load(fis)
            }
            return props
        }
}