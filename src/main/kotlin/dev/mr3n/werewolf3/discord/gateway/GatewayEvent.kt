package dev.mr3n.werewolf3.discord.gateway

import kotlinx.serialization.Serializable

@Serializable
data class GatewayEvent<T>(
    val op: OpCode,
    val d: T?
)
