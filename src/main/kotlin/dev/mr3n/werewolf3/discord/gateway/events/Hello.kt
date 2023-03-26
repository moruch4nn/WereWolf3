package dev.mr3n.werewolf3.discord.gateway.events

import dev.mr3n.werewolf3.discord.gateway.Event
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Hello(
    @SerialName("heartbeat_interval")
    val heartbeatInterval: Int
): Event
