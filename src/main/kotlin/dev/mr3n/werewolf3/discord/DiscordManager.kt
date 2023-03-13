package dev.mr3n.werewolf3.discord

import dev.mr3n.werewolf3.*
import dev.mr3n.werewolf3.utils.conversationalDistance
import dev.mr3n.werewolf3.utils.isAlive
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel
import net.dv8tion.jda.api.requests.GatewayIntent
import org.bukkit.entity.Player
import java.util.*

internal var GUILD: Guild? = null

/**
 * ゲーム中に製造者が使用するボイスチャットです。
 */
internal var VOICE_CHANNEL: AudioChannel? = null

/**
 * ゲーム中に観戦者が使用するボイスチャットです。
 */
internal var SPECTATORS_VOICE_CHANNEL: AudioChannel? = null

/**
 * プレイヤーが所有しているDiscordアカウントのID一覧。
 */
val Player.discordIds: Set<String>
    get() = DiscordManager.discordIdsByPlayer.getOrPut(this.uniqueId) { mutableSetOf() }

val Player.members: List<Member>
    get() = this.discordIds.mapNotNull { GUILD?.getMemberById(it) }

fun Player.connectTo(audioChannel: AudioChannel?) {
    if(audioChannel == null) { return }
    if(!WereWolf3.CONFIG.getBoolean("voice_chat.discord.enable")) { return }
    this.members.forEach { member -> audioChannel.guild.moveVoiceMember(member, audioChannel) }
    DiscordManager.updateVoiceChannelState(this)
}

object DiscordManager {
    private var jda: JDA? = null

    init { this.initializeBot() }

    val voiceChannelId = WereWolf3.CONFIG.getString("voice_chat.discord.voice_channel.id")
    val spectatorsVoiceChannelId = WereWolf3.CONFIG.getString("voice_chat.discord.spectators_voice_chat.id")

    /**
     * プレイヤーのボイスチャットのミュート状態を管理します。
     * 朝/夜になった場合や会話可能範囲が変更された場合などユーザーのミュート状態を切り替える必要がある場合に呼び出してください
     */
    internal fun updateVoiceChannelState(player: Player) {
        if(player.isAlive) {
            player.members.forEach { it.deafen(false) }
        } else {
            if (STATUS != GameStatus.RUNNING && (TIME_OF_DAY == Time.NIGHT || player.conversationalDistance < 0)) {
                player.members.forEach { it.deafen(false) }
            } else {
                player.members.forEach { it.deafen(true) }
            }
        }
    }

    val discordIdsByPlayer = mutableMapOf<UUID, MutableSet<String>>()

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
    }
}