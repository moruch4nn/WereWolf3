package dev.mr3n.werewolf3.items.seer

import dev.moru3.minepie.events.EventRegister.Companion.registerEvent
import dev.mr3n.werewolf3.WereWolf3
import dev.mr3n.werewolf3.items.IShopItem
import dev.mr3n.werewolf3.roles.Role
import dev.mr3n.werewolf3.utils.alivePlayers
import dev.mr3n.werewolf3.utils.asPrefixed
import dev.mr3n.werewolf3.utils.role
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.event.player.PlayerInteractEvent

@Suppress("unused")
object MultipleDivinationItem: IShopItem.ShopItem("multiple_divination", Material.ENDER_EYE) {
    private val DIVINATION_TITLE_TEXT = titleText("divination")

    private val TIME: Long = itemConstant("time")

    private val DISTANCE: Double = itemConstant("distance")

    // なんか明るめの紫色みたいなやつ。ChatColor.LIGHT_PURPLE。名前統一してくれ
    private val PARTICLE_COLOR = Particle.DustOptions(Color.FUCHSIA, 1f)

    init {
        WereWolf3.INSTANCE.registerEvent<PlayerInteractEvent> { event ->
            val player = event.player
            val item = player.inventory.itemInMainHand
            // 占いアイテムを手に持っていない場合はreturn
            if(!isSimilar(item)) { return@registerEvent }
            val base = player.location.clone()
            event.isCancelled = true
            item.amount--
            val wolfInRange = alivePlayers().filter { base.distance(it.location) < DISTANCE }.any { it.role == Role.WOLF }
            player.playSound(base, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f)
            if(wolfInRange) {
                // if:人狼がいた場合
                val result = messages("result.wolf")
                player.sendTitle(DIVINATION_TITLE_TEXT, result, 0, 100, 20)
                player.sendMessage(result.asPrefixed())
            } else {
                // if:人狼がいなかった場合
                val result = messages("result.villager")
                player.sendTitle(DIVINATION_TITLE_TEXT, result, 0, 100, 20)
                player.sendMessage(result.asPrefixed())
            }
        }
    }
}