package dev.mr3n.werewolf3.discord.gateway.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val username: String,
    val discriminator: String,
    val avatar: String? = null,
    val bot: Boolean = false,
    val system: Boolean = false,
    @SerialName("mfa_enabled")
    val mfaEnabled: Boolean? = false,
    val banner: String? = null,
    @SerialName("accent_color")
    val accentColor: Int? = null,
    val locale: String? = null,
    val verified: Boolean = false,
    val email: String? = null,
    val flags: Int? = null,
    @SerialName("premium_type")
    val premiumType: Int? = null,
    @SerialName("public_flags")
    val publicFlags: Int? = null
)
