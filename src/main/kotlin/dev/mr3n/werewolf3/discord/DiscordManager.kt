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
    if(!WereWolf3.CONFIG.getString("voice_chat.discord.mode").equals("channel", true)) { return }
    DiscordManager.manager.voiceStates
        .filterKeys { this.discordIds.contains(it) }
        .filterValues { it.channelId != audioChannel }
        .keys.parallelStream()
        .forEach { id ->
            DiscordManager.manager.patch("https://discord.com/api/v10/guilds/${GUILD_ID}/members/${id}", UpdateMemberRequest(null, audioChannel, null, null, null))
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
        if(STATUS != GameStatus.RUNNING) {
            this.deaf(player, false)
            this.mute(player, false)
        } else {
            if(!player.isAlive) {
                this.deaf(player, false)
                this.mute(player, true)
            } else if(TIME_OF_DAY == Time.MORNING && player.conversationalDistance < 0) {
                this.deaf(player, false)
                this.mute(player, false)
            } else {
                this.deaf(player, true)
                this.mute(player, true)
            }
        }
    }

    private fun deaf(player: Player, deaf: Boolean) {
        manager.voiceStates
            .filterKeys { player.discordIds.contains(it) }
            .filterValues { it.deaf != deaf}
            .forEach { (id, _) ->
                manager.patch("https://discord.com/api/v10/guilds/${GUILD_ID}/members/${id}", UpdateMemberRequest(deaf, null, null, null, null))
            }
    }

    private fun mute(player: Player, mute: Boolean) {
        manager.voiceStates
            .filterKeys { player.discordIds.contains(it) }
            .filterValues { it.mute != mute}
            .forEach { (id, _) ->
                manager.patch("https://discord.com/api/v10/guilds/${GUILD_ID}/members/${id}", UpdateMemberRequest(null, null, null, null, mute))
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