package dev.mr3n.werewolf3.discord

import dev.mr3n.werewolf3.*
import dev.mr3n.werewolf3.discord.gateway.DiscordBotManager
import dev.mr3n.werewolf3.discord.gateway.entities.UpdateMemberRequest
import dev.mr3n.werewolf3.utils.conversationalDistance
import dev.mr3n.werewolf3.utils.isAlive
import org.bukkit.entity.Player
import java.util.*

/**
 * ゲーム中に製造者が使用するボイスチャットです。
 */
internal val VOICE_CHANNEL: String? = WereWolf3.CONFIG.getString("voice_chat.discord.voice_channel.id")

/**
 * ゲーム中に観戦者が使用するボイスチャットです。
 */
internal val SPECTATORS_VOICE_CHANNEL: String? = WereWolf3.CONFIG.getString("voice_chat.discord.spectators_voice_chat.id")

internal val GUILD_ID: String? = WereWolf3.CONFIG.getString("voice_chat.discord.guild_id")

/**
 * プレイヤーが所有しているDiscordアカウントのID一覧。
 */
val Player.discordIds: Set<String>
    get() = DiscordManager.discordIdsByPlayer.getOrPut(this.uniqueId) { mutableSetOf() }

fun Player.connectTo(audioChannel: String?) {
    if(audioChannel == null) { return }
    if(!WereWolf3.CONFIG.getBoolean("voice_chat.discord.enable")) { return }
    DiscordManager.manager.voiceStates
        .filterValues { it.channelId == VOICE_CHANNEL }
        .filterKeys { this.discordIds.contains(it) }
        .filterValues { it.channelId != audioChannel }
        .forEach { (id, _) ->
            DiscordManager.manager.patch("https://discord.com/api/v10/guilds/${GUILD_ID}/members/${id}", UpdateMemberRequest(null, audioChannel, null, null))
        }
}

object DiscordManager {
    /**
     * プレイヤーのボイスチャットのミュート状態を管理します。
     * 朝/夜になった場合や会話可能範囲が変更された場合などユーザーのミュート状態を切り替える必要がある場合に呼び出してください
     */
    internal fun updateVoiceChannelState(player: Player) {
        if(!WereWolf3.CONFIG.getBoolean("voice_chat.discord.enable")) { return }
        if(VOICE_CHANNEL == null) { return }
        if(SPECTATORS_VOICE_CHANNEL == null) { return }
        if(!player.isAlive || STATUS != GameStatus.RUNNING || (TIME_OF_DAY == Time.MORNING && player.conversationalDistance < 0)) {
            manager.voiceStates
                .filterValues { it.channelId == VOICE_CHANNEL }
                .filterKeys { player.discordIds.contains(it) }
                .filterValues { it.deaf }
                .forEach { (id, _) ->
                    manager.patch("https://discord.com/api/v10/guilds/${GUILD_ID}/members/${id}", UpdateMemberRequest(false, null, null, null))
                }
        } else {
            manager.voiceStates
                .filterValues { it.channelId == VOICE_CHANNEL }
                .filterKeys { player.discordIds.contains(it) }
                .filterValues { !it.deaf }
                .forEach { (id, _) ->
                    manager.patch("https://discord.com/api/v10/guilds/${GUILD_ID}/members/${id}", UpdateMemberRequest(true, null, null, null))
                }
        }
    }

    val discordIdsByPlayer = mutableMapOf<UUID, MutableSet<String>>()

    lateinit var manager: DiscordBotManager

    /**
     * DiscordBOTを起動し、リスナーを登録する
     */
    private fun initializeBot() {
        if(!WereWolf3.CONFIG.getBoolean("voice_chat.discord.enable")) { return }
        this.manager = DiscordBotManager(WereWolf3.CONFIG.getStringList("voice_chat.discord.tokens"))
    }

    init { this.initializeBot() }
}