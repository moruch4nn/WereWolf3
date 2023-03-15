package dev.mr3n.werewolf3.discord

import dev.mr3n.werewolf3.WereWolf3
import net.dv8tion.jda.api.entities.channel.concrete.StageChannel
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceRequestToSpeakEvent
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

object JDAEventListener: ListenerAdapter() {

    override fun onReady(event: ReadyEvent) {
        VOICE_CHANNEL = when(WereWolf3.CONFIG.getString("voice_chat.discord.voice_channel.type")) {
            "VOICE", "VOICE_CHANNEL" -> DiscordManager.voiceChannelId?.let { event.jda.getVoiceChannelById(it) }
            "STAGE", "STAGE_CHANNEL" -> DiscordManager.voiceChannelId?.let { event.jda.getStageChannelById(it) }
            else -> null
        }
        SPECTATORS_VOICE_CHANNEL = when(WereWolf3.CONFIG.getString("voice_chat.discord.spectators_voice_chat.type")) {
            "VOICE", "VOICE_CHANNEL" -> DiscordManager.spectatorsVoiceChannelId?.let { event.jda.getVoiceChannelById(it) }
            "STAGE", "STAGE_CHANNEL" -> DiscordManager.spectatorsVoiceChannelId?.let { event.jda.getStageChannelById(it) }
            else -> null
        }
        GUILD = VOICE_CHANNEL?.guild
    }

    override fun onGuildVoiceRequestToSpeak(event: GuildVoiceRequestToSpeakEvent) {
        if(VOICE_CHANNEL != null && event.voiceState.channel?.id == VOICE_CHANNEL?.id) { event.approveSpeaker().queue() }
        if(SPECTATORS_VOICE_CHANNEL != null && event.voiceState.channel?.id == SPECTATORS_VOICE_CHANNEL?.id) { event.approveSpeaker().queue() }
    }

    override fun onGuildVoiceUpdate(event: GuildVoiceUpdateEvent) {
        val channel = event.channelJoined
        if(channel != null) {
            if (channel is StageChannel) {
                if (VOICE_CHANNEL != null && event.voiceState.channel?.id == VOICE_CHANNEL?.id) { event.voiceState.inviteSpeaker().queue() }
                if (SPECTATORS_VOICE_CHANNEL != null && event.voiceState.channel?.id == SPECTATORS_VOICE_CHANNEL?.id) { event.voiceState.inviteSpeaker().queue() }
            }
        }
    }
}