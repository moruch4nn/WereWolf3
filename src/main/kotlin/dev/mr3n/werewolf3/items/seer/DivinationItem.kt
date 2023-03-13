package dev.mr3n.werewolf3.items.seer

import dev.moru3.minepie.Executor.Companion.runTaskTimer
import dev.moru3.minepie.events.EventRegister.Companion.registerEvent
import dev.mr3n.werewolf3.WereWolf3
import dev.mr3n.werewolf3.items.IShopItem
import dev.mr3n.werewolf3.roles.Role
import dev.mr3n.werewolf3.utils.asPrefixed
import dev.mr3n.werewolf3.utils.role
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.scheduler.BukkitTask
import java.util.*

@Suppress("unused")
object DivinationItem: IShopItem.ShopItem("divination", Material.MUSIC_DISC_CHIRP) {
    private val DIVINATION_TITLE_TEXT = titleText("divination")

    // map<占い師,triple<最後クリックした際のタイムスタンプ,対象者,クリックしたミリ秒>>
    private val divinationPlayers = mutableMapOf<UUID, DivinationInfo>()

    private val TIME: Int = itemConstant("time")

    init {
        WereWolf3.INSTANCE.registerEvent<PlayerInteractAtEntityEvent> { event ->
            val player = event.player
            val item = player.inventory.itemInMainHand
            // 占いアイテムを手に持っていない場合はreturn
            if (!isSimilar(item)) {
                return@registerEvent
            }
            val target = event.rightClicked
            if (target !is Player) { return@registerEvent }
            // すでに占いちゅの場合はreturn
            if(this.divinationPlayers.containsKey(player.uniqueId)) { return@registerEvent }
            // 占い中のタイトルを表示
            player.sendTitle(DIVINATION_TITLE_TEXT, messages("init", "%player%" to target.name), 0, Int.MAX_VALUE, 0)
            player.playSound(player, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1f)
            var task: BukkitTask? = null
            task = WereWolf3.INSTANCE.runTaskTimer(2, 2) {
                val info = this.divinationPlayers[player.uniqueId]
                if(info == null) {
                    task?.cancel()
                } else {
                    info.length += 2
                    val traceResult = player.world.rayTraceEntities(player.eyeLocation, player.eyeLocation.direction, 4.0) { it == target && it is Player }
                    val lookingTarget = traceResult?.hitEntity
                    if (lookingTarget is Player && lookingTarget.uniqueId == target.uniqueId) {
                        if(info.length >= TIME) {
                            // if:3秒以上押し続けている場合
                            this.divinationPlayers.remove(player.uniqueId)
                            item.amount--
                            task?.cancel()
                            val result = if (target.role == Role.WOLF) messages(
                                "result.wolf",
                                "%player%" to target.name
                            ) else messages("result.villager", "%player%" to target.name)
                            player.sendTitle(DIVINATION_TITLE_TEXT, result, 0, 100, 20)
                            player.sendMessage(result.asPrefixed())
                            player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f)
                        }
                    } else {
                        this.divinationPlayers.remove(player.uniqueId)
                        task?.cancel()
                        // 占いがキャンセルされた旨を通知
                        player.sendTitle(DIVINATION_TITLE_TEXT, messages("canceled"), 0, TIME, 20)
                        // キラリーンの音を鳴らす
                        player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f)
                    }
                }
            }
            this.divinationPlayers[player.uniqueId] = DivinationInfo(target,0,task)
        }
    }

    data class DivinationInfo(val target: Player, var length: Int, val bukkitTask: BukkitTask)
}