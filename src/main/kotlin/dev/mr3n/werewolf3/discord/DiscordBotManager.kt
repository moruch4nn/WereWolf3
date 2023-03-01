package dev.mr3n.werewolf3.discord

import dev.mr3n.werewolf3.WereWolf3
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel
import net.dv8tion.jda.api.requests.GatewayIntent

/**
 * ゲーム中に製造者が使用するボイスチャットです。
 */
internal var VOICE_CHANNEL: AudioChannel? = null

/**
 * ゲーム中に観戦者が使用するボイスチャットです。
 */
internal var SPECTATORS_VOICE_CHANNEL: AudioChannel? = null

object DiscordBotManager {
    private var jda: JDA? = null

    init { this.initializeBot() }

    private val voiceChannelId = WereWolf3.CONFIG.getString("voice_chat.discord.voice_channel.id")
    private val deathVoiceChannelId = WereWolf3.CONFIG.getString("voice_chat.discord.spectators_voice_chat.id")

    /**
     * DiscordBOTを起動し、リスナーを登録する
     */
    private fun initializeBot() {
        if(!WereWolf3.CONFIG.getBoolean("voice_chat.discord.enable")) { return }
        val token = WereWolf3.CONFIG.getString("voice_chat.discord.bot_token")
        this.jda = JDABuilder.createDefault(token)
            .enableIntents(GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_MEMBERS)
            .addEventListeners(JDAEventListener)
            .build()
        VOICE_CHANNEL = when(WereWolf3.CONFIG.getString("voice_chat.discord.voice_channel.type")) {
            "VOICE", "VOICE_CHANNEL" -> voiceChannelId?.let { jda?.getVoiceChannelById(it) }
            "STAGE", "STAGE_CHANNEL" -> voiceChannelId?.let { jda?.getStageChannelById(it) }
            else -> null
        }
        SPECTATORS_VOICE_CHANNEL = when(WereWolf3.CONFIG.getString("voice_chat.discord.spectators_voice_chat.type")) {
            "VOICE", "VOICE_CHANNEL" -> deathVoiceChannelId?.let { jda?.getVoiceChannelById(it) }
            "STAGE", "STAGE_CHANNEL" -> deathVoiceChannelId?.let { jda?.getStageChannelById(it) }
            else -> null
        }
    }
}