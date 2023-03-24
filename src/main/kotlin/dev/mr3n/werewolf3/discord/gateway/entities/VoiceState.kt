package dev.mr3n.werewolf3.discord.gateway.entities

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VoiceState(
    @SerialName("guild_id")
    val guildId: String? = null,
    @SerialName("channel_id")
    val channelId: String? = null,
    @SerialName("user_id")
    val userId: String,
    val member: Member? = null,
    @SerialName("session_id")
    val sessionId: String,
    val deaf: Boolean,
    val mute: Boolean,
    @SerialName("self_deaf")
    val selfDeaf: Boolean,
    @SerialName("self_mute")
    val selfMute: Boolean,
    @SerialName("self_stream")
    val selfStream: Boolean? = false,
    @SerialName("self_video")
    val selfVideo: Boolean?,
    val suppress: Boolean,
    @SerialName("request_to_speak_timestamp")
    val requestToSpeakTimestamp: Instant?
)