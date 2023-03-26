package dev.mr3n.werewolf3.discord.gateway

enum class Intent(val key: Int) {
    GUILDS(1 shl 0),
    GUILD_VOICE_STATES(1 shl 7);

    companion object {
        fun createIntents(vararg intents: Intent): Int {
            var intent = 0
            intents.forEach { intent += it.key }
            return intent
        }
    }
}