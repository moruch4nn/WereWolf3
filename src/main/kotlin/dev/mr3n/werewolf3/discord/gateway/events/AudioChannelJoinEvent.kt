package dev.mr3n.werewolf3.discord.gateway.events

import dev.mr3n.werewolf3.discord.gateway.Event
import dev.mr3n.werewolf3.discord.gateway.entities.VoiceState

class AudioChannelJoinEvent(val voiceState: VoiceState): Event