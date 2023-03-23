package dev.mr3n.werewolf3.discord

import dev.mr3n.werewolf3.*
import dev.mr3n.werewolf3.utils.conversationalDistance
import dev.mr3n.werewolf3.utils.isAlive
import org.bukkit.entity.Player
import java.util.*
import kotlin.concurrent.thread

internal var GUILD: String? = null

/**
 * ゲーム中に製造者が使用するボイスチャットです。
 */
internal var VOICE_CHANNEL: String? = null

/**
 * ゲーム中に観戦者が使用するボイスチャットです。
 */
internal var SPECTATORS_VOICE_CHANNEL: String? = null

/**
 * プレイヤーが所有しているDiscordアカウントのID一覧。
 */
val Player.discordIds: Set<String>
    get() = DiscordManager.discordIdsByPlayer.getOrPut(this.uniqueId) { mutableSetOf() }

fun Player.connectTo(audioChannel: String?) {
    if(audioChannel == null) { return }
    if(!WereWolf3.CONFIG.getBoolean("voice_chat.discord.enable")) { return }
}

object DiscordManager {
    val voiceChannelId = WereWolf3.CONFIG.getString("voice_chat.discord.voice_channel.id")
    val spectatorsVoiceChannelId = WereWolf3.CONFIG.getString("voice_chat.discord.spectators_voice_chat.id")

    /**
     * プレイヤーのボイスチャットのミュート状態を管理します。
     * 朝/夜になった場合や会話可能範囲が変更された場合などユーザーのミュート状態を切り替える必要がある場合に呼び出してください
     */
    internal fun updateVoiceChannelState(player: Player) {
        if(!player.isAlive) {
//            player.members.forEach { it.deafen(false).queue() }
        } else if(STATUS != GameStatus.RUNNING) {
//            player.members.forEach { it.deafen(false).queue() }
        } else if(TIME_OF_DAY == Time.MORNING && player.conversationalDistance < 0) {
//            player.members.forEach { it.deafen(false).queue() }
        } else {
//            player.members.forEach { it.deafen(true).queue() }
        }
    }

    val discordIdsByPlayer = mutableMapOf<UUID, MutableSet<String>>()

    /**
     * DiscordBOTを起動し、リスナーを登録する
     */
    private fun initializeBot() {
        if(!WereWolf3.CONFIG.getBoolean("voice_chat.discord.enable")) { return }
        val token = WereWolf3.CONFIG.getString("voice_chat.discord.bot_token")
    }

    init { this.initializeBot() }
}