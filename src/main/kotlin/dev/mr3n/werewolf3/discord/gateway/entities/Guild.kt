package dev.mr3n.werewolf3.discord.gateway.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Guild(
    @SerialName("voice_states")
    val voiceStates: List<VoiceState>,
    val channels: List<Channel>,
    @SerialName("stage_instances")
    val stageInstances: List<StageInstance>
)
