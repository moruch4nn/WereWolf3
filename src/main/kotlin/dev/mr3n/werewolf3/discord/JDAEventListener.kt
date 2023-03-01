package dev.mr3n.werewolf3.discord

import net.dv8tion.jda.api.entities.channel.concrete.StageChannel
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceRequestToSpeakEvent
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

object JDAEventListener: ListenerAdapter() {

    override fun onReady(event: ReadyEvent) {
        VOICE_CHANNEL = event.jda.getStageChannelById("1076499067271647352")
        SPECTATORS_VOICE_CHANNEL = event.jda.getVoiceChannelById("924636289461022780")
    }

    override fun onGuildVoiceRequestToSpeak(event: GuildVoiceRequestToSpeakEvent) {
        if(VOICE_CHANNEL != null && event.voiceState.channel?.id == VOICE_CHANNEL?.id) { event.approveSpeaker().queue() }
        if(SPECTATORS_VOICE_CHANNEL != null && event.voiceState.channel?.id == SPECTATORS_VOICE_CHANNEL?.id) { event.approveSpeaker().queue() }
    }

    override fun onGuildVoiceUpdate(event: GuildVoiceUpdateEvent) {
        val channel = event.channelJoined?:return
        if(channel is StageChannel) {
            if(VOICE_CHANNEL != null && event.voiceState.channel?.id == VOICE_CHANNEL?.id) { event.voiceState.inviteSpeaker().queue() }
            if(SPECTATORS_VOICE_CHANNEL != null && event.voiceState.channel?.id == SPECTATORS_VOICE_CHANNEL?.id) { event.voiceState.inviteSpeaker().queue() }
        }
    }
}