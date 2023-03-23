package dev.mr3n.werewolf3.discord.gateway

enum class Intent(val index: Int) {
    GUILDS(0),
    GUILD_MEMBERS(1),
    GUILD_MODERATION(2),
    GUILD_EMOJI_AND_STICKERS(3),
    GUILD_INTERACTIONS(4),
    GUILD_WEBHOOKS(5),
    GUILD_INVITES(6),
    GUILD_VOICE_STATES(7),
    GUILD_PRESENCES(8),
    GUILD_MESSAGES(9),
    GUILD_MESSAGE_REACTIONS(10),
    GUILD_MESSAGE_TYPING(11),
    DIRECT_MESSAGES(12),
    DIRECT_MESSAGE_REACTIONS(13),
    DIRECT_MESSAGE_TYPING(14),
    MESSAGE_CONTENT(15),
    GUILD_SCHEDULES_EVENTS(16),
    AUTO_MODERATION_CONFIGURATION(20),
    AUTO_MODERATION_EXECUTION(21);

    companion object {
        fun createIntents(vararg intents: Intent): Int {
            var intent = 0
            intents.forEach { intent = intent shl it.index }
            return intent
        }
    }
}