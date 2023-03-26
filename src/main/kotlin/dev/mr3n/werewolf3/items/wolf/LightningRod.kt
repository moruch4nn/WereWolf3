package dev.mr3n.werewolf3.items.wolf

import dev.moru3.minepie.Executor.Companion.runTaskTimer
import dev.moru3.minepie.events.EventRegister.Companion.registerEvent
import dev.mr3n.werewolf3.WereWolf3
import dev.mr3n.werewolf3.utils.conversationalDistance
import dev.mr3n.werewolf3.items.IShopItem
import dev.mr3n.werewolf3.utils.remConversationalDistance
import dev.mr3n.werewolf3.roles.Role
import dev.mr3n.werewolf3.utils.joinedPlayers
import dev.mr3n.werewolf3.utils.role
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

@Suppress("unused")
object LightningRod: IShopItem.ShopItem("lightning_rod", Material.LIGHTNING_ROD) {
    private val BLINDNESS_TIME: Long = itemConstant("blindness_time")

    private val BLINDNESS_TITLE_TEXT = titleText("blindness")

    private const val CONVERSATIONAL_PRIORITY = 10

    private var blindness = 0L

    init {
        WereWolf3.INSTANCE.registerEvent<PlayerInteractEvent> { event ->
            val player = event.player
            // main handじゃない場合はreturn
            if(event.hand!=EquipmentSlot.HAND) { return@registerEvent }
            // 右クリックしていない場合はreturn
            if(event.action!=Action.RIGHT_CLICK_AIR&&event.action!=Action.RIGHT_CLICK_BLOCK) { return@registerEvent }
            val item = event.item
            // ピカピカインクを持っていない場合はreturn
            if(item == null || !isSimilar(item)) { return@registerEvent }
            item.amount--
            player.playSound(player, Sound.ENTITY_GLOW_SQUID_SQUIRT, 2f, 1f)
            // 盲目時間を設定
            blindness = BLINDNESS_TIME
            player.world.strikeLightningEffect(player.location)
            joinedPlayers().forEach { player1 ->
                // 雷が落ちた音
                player1.playSound(player1, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1f, 1f)
                // 停電中は会話をできなくする
                player1.conversationalDistance(CONVERSATIONAL_PRIORITY, 0.0)
                if(player1.role==Role.WOLF) {
                    // 人狼には残りの停電の時間を表示する
                    player1.sendTitle(BLINDNESS_TITLE_TEXT, messages("for_wolf", "%sec%" to blindness / 20), 0, 40, 10)
                } else {
                    player1.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 50, 10, false, false, false))
                    player1.sendTitle(BLINDNESS_TITLE_TEXT, messages("blindness", "%sec%" to blindness / 20), 0, 40, 10)
                }
            }
        }
        WereWolf3.INSTANCE.runTaskTimer(0L,20L) {
            if(blindness>0) {
                blindness-=20
                joinedPlayers().forEach { player1 ->
                    if(player1.role==Role.WOLF) {
                        player1.sendTitle(BLINDNESS_TITLE_TEXT, messages("for_wolf", "%sec%" to blindness / 20), 0, 30, 10)
                    } else {
                        player1.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS, 50, 10, false, false, false))
                        player1.sendTitle(BLINDNESS_TITLE_TEXT, messages("blindness", "%sec%" to blindness / 20), 0, 30, 10)
                    }
                    if(blindness <= 0) {
                        player1.remConversationalDistance(CONVERSATIONAL_PRIORITY)
                    }
                }
            }
        }
    }
}