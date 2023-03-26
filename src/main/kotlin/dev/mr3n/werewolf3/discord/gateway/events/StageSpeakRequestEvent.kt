package dev.mr3n.werewolf3.discord.gateway.events

import dev.mr3n.werewolf3.discord.gateway.Event
import dev.mr3n.werewolf3.discord.gateway.entities.VoiceState

class StageSpeakRequestEvent(val voiceState: VoiceState): Event