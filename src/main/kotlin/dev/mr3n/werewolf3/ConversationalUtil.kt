package dev.mr3n.werewolf3

import dev.moru3.minepie.events.EventRegister.Companion.registerEvent
import dev.mr3n.werewolf3.utils.joinedPlayers
import dev.mr3n.werewolf3.utils.languages
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.GameMode
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.player.AsyncPlayerChatEvent
import java.util.*

/**
 * プレイヤーの会話可能範囲を設定する
 */
fun Player.conversationalDistance(priority: Int, distance: Double) {
    val map = ConversationalUtil.players[this]?:TreeMap()
    map[priority] = distance
    ConversationalUtil.players[this] = map
}

/**
 * プレイヤーの会話可能範囲の制限をなくす
 */
fun Player.clearConversationalDistance() {
    ConversationalUtil.players[this]?.clear()
}

/**
 * プレイヤーの特定のpriorityの会話可能範囲の制限を消す
 */
fun Player.remConversationalDistance(priority: Int) {
    ConversationalUtil.players[this]?.remove(priority)
}

/**
 * Playerとtargetが会話できるかどうかをtrue/falseで返す。
 */
fun Player.canConversationWith(target: Player): Boolean {
    val player = this
    // 二人のプレイヤーの距離を取得
    val distance = player.location.distance(target.location)
    // プレイヤーの会話可能範囲を超えている場合は return false
    if(distance > player.conversationalDistance) { return false }
    // ターゲットの会話可能範囲を超えている場合は return false
    if(distance > target.conversationalDistance) { return false }
    return true
}

/**
 * プレイヤーの会話可能範囲
 */
val Player.conversationalDistance: Double
    get() {
        val map = ConversationalUtil.players[this]?:TreeMap()
        return if(map.isEmpty()) { -1.0 } else { map[map.firstKey()] ?: -1.0 }
    }

object ConversationalUtil {
    val players = mutableMapOf<Player, SortedMap<Int, Double>>()

    init {
        // プレイヤーがチャットした際に実行
        WereWolf3.INSTANCE.registerEvent<AsyncPlayerChatEvent>(p = EventPriority.HIGHEST, ic = true) { event ->
            // プレイヤーの会話可能範囲 / メートル
            val conversationalDistance = event.player.conversationalDistance
            // 0だった場合は会話できないのでイベントキャンセル
            if(conversationalDistance == 0.0) {
                event.isCancelled = true
            } else if(conversationalDistance > 0) {
                event.isCancelled = true
                // スペクテイター、もしくは会話可能範囲内のプレイヤーにチャットを送信
                joinedPlayers()
                    .forEach { player2 ->
                        // 会話を聞き取れる範囲にいるかどうか
                        val listenable = event.player.canConversationWith(player2)
                        if(player2.gameMode == GameMode.SPECTATOR) {
                            if(!listenable) {
                                // 聞き取れない場合でもスペクテイターの場合は薄く表示する
                                event.format = ChatColor.stripColor(event.format)
                                player2.sendMessage(String.format(event.format, "${ChatColor.GRAY}${ChatColor.stripColor(event.player.displayName)}", "${ChatColor.GRAY}${event.message}"))
                            } else {
                                player2.sendMessage(String.format(event.format, event.player.displayName, event.message))
                            }
                        } else {
                            // 聞き取れるプレイヤーにのみメッセージを表示
                            if(listenable) { player2.sendMessage(String.format(event.format, event.player.displayName, event.message)) }
                        }
                    }
                // 制限がある場合はその旨をアクションバーに表示
                event.player.playSound(event.player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f)
                event.player.spigot().sendMessage(ChatMessageType.ACTION_BAR, *TextComponent.fromLegacyText(languages("send_message_at_night", "%distance%" to conversationalDistance)))
            }
        }
    }
}