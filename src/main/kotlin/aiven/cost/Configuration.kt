package aiven.cost

import com.natpryce.konfig.ConfigurationProperties
import com.natpryce.konfig.ConfigurationProperties.Companion.systemProperties
import com.natpryce.konfig.EnvironmentVariables
import com.natpryce.konfig.Key
import com.natpryce.konfig.overriding
import com.natpryce.konfig.stringType

private fun config() =
    systemProperties() overriding EnvironmentVariables overriding ConfigurationProperties.fromResource("aiven.properties")

data class Configuration(
    val aivenToken: String = config()[Key("secret", stringType)],

    )