package dev.mr3n.werewolf3.discord.gateway.entities

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateMemberRequest(
    val deaf: Boolean?,
    @SerialName("channel_id")
    val channelId: String?,
    val suppress: Boolean?,
    @SerialName("request_to_speak_timestamp")
    val requestToSpeakTimestamp: Instant?,
    val mute: Boolean?
)