package dev.mr3n.werewolf3.discord.gateway

import dev.mr3n.werewolf3.discord.gateway.events.AudioChannelJoinEvent
import dev.mr3n.werewolf3.discord.gateway.events.GuildCreateEvent
import dev.mr3n.werewolf3.discord.gateway.events.StageSpeakRequestEvent
import dev.mr3n.werewolf3.discord.gateway.events.VoiceStateUpdateEvent

abstract class DiscordEventListener {
    open fun onGuildCreate(event: GuildCreateEvent) {}

    open fun onVoiceStateUpdate(event: VoiceStateUpdateEvent) {}

    open fun onStageSpeakRequest(event: StageSpeakRequestEvent) {}

    open fun onAudioChannelJoin(event: AudioChannelJoinEvent) {}
}