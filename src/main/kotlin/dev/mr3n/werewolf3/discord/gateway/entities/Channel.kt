package dev.mr3n.werewolf3.discord.gateway.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Channel(
    val id: String,
    val type: ChannelType,
    @SerialName("guild_id")
    val guildId: String? = null,
)