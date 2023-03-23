package dev.mr3n.werewolf3.discord.gateway.entities

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class Member(
    val user: User? = null,
    val nick: String? = null,
    val avatar: String? = null,
    val roles: List<String>,
    @SerialName("joined_at")
    val joinedAt: Instant,
    @SerialName("premium_since")
    val premiumSince: Instant? = null,
    val deaf: Boolean,
    val mute: Boolean,
    val flags: Int,
    val pending: Boolean? = null,
    val permissions: String? = null,
    @SerialName("communication_disabled_until")
    val communicationDisabledUntil: Instant? = null
)