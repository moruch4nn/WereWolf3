package dev.mr3n.werewolf3.discord.gateway.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StageInstance(
    val id: String,
    @SerialName("guild_id")
    val guildId: String,
    @SerialName("channel_id")
    val channelId: String,
    val topic: String,
    @SerialName("privacy_level")
    val privacyLevel: Int,
    @SerialName("discoverable_disabled")
    val discoverableDisabled: Boolean,
    @SerialName("guild_scheduled_event_id")
    val guildScheduledEventId: String? = null
)
