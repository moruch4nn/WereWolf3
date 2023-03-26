package dev.mr3n.werewolf3.discord.gateway.events

import dev.mr3n.werewolf3.discord.gateway.Event
import dev.mr3n.werewolf3.discord.gateway.entities.Guild

class GuildCreateEvent(val guild: Guild, override val sequence: Int): Event.DispatchEvent