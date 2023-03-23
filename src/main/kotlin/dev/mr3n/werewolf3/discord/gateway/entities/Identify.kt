package dev.mr3n.werewolf3.discord.gateway.entities

import dev.mr3n.werewolf3.discord.gateway.Intent
import kotlinx.serialization.Serializable

@Serializable
data class Identify(
    val token: String,
    val intents: Int,
    val properties: Properties) {

    constructor(token: String, intents: Int): this(token, intents, Properties())

    constructor(token: String, vararg intents: Intent): this(token, Intent.createIntents(*intents))

    @Serializable
    data class Properties(
        val os: String = System.getProperty("os.name")?:"UNKNOWN",
        val browser: String = "WereWolf3-DiscordIntegration",
        val device: String = "WereWolf3-DiscordIntegration"
    )
}