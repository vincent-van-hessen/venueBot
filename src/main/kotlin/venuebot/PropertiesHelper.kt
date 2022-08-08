package venuebot

import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.InputStream
import java.util.Properties

object PropertiesHelper {
    const val PROPERTIY_FILE = "user.cfg"
    const val PROPERTIY_FILE_DEFAULT = "user.default.cfg"
    const val VERSION_FILE = "version.properties"
    val version: String?
        get() {
            val inputStream: InputStream?
            var result: String? = null
            try {
                val prop = Properties()
                inputStream = PropertiesHelper::class.java.classLoader.getResourceAsStream(VERSION_FILE)
                result = if (inputStream != null) {
                    prop.load(inputStream)
                    prop.getProperty("version")
                } else {
                    "version-missing"
                }
                inputStream.close()
            } catch (e: Exception) {
                println("Exception: $e")
            }
            return result
        }

    fun getProperty(propname: String?): String? {
        val inputStream: InputStream
        var result: String? = null

        // check if property exists in Environment, then skip
        val envValue = System.getenv(propname)
        if (envValue != null) {
            return envValue
        }
        if (propertyFile != null) {
            try {
                val prop = Properties()
                inputStream = FileInputStream(propertyFile)
                if (inputStream != null) {
                    prop.load(inputStream)
                } else {
                    throw FileNotFoundException("property file '" + propertyFile + "' not found in the classpath")
                }
                result = prop.getProperty(propname)
                inputStream.close()
            } catch (e: Exception) {
                println("Exception: $e")
            }
        }
        return result
    }

    private val propertyFile: String?
        private get() {
            if (File(PROPERTIY_FILE).isFile) {
                return PROPERTIY_FILE
            } else if (File(PROPERTIY_FILE_DEFAULT).isFile) {
                return PROPERTIY_FILE_DEFAULT
            }
            return null
        }
}