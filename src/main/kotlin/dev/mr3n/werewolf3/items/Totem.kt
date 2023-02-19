package dev.mr3n.werewolf3.items

import dev.moru3.minepie.events.EventRegister.Companion.registerEvent
import dev.mr3n.werewolf3.TIME_OF_DAY
import dev.mr3n.werewolf3.Time
import dev.mr3n.werewolf3.WereWolf3
import dev.mr3n.werewolf3.events.WereWolf3DamageEvent
import org.bukkit.EntityEffect
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityResurrectEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

@Suppress("unused")
object Totem: IShopItem.ShopItem("totem_of_undying", Material.TOTEM_OF_UNDYING) {
    private val SPEED_LEVEL: Int = itemConstant("speed_level")

    private val SPEED_TIME: Int = itemConstant("speed_time")

    init {
        // バニラのトーテムを無効化
        WereWolf3.INSTANCE.registerEvent<EntityResurrectEvent> { event ->
            val player = event.entity
            if(player !is Player) { return@registerEvent }
            event.isCancelled = true
        }
        WereWolf3.INSTANCE.registerEvent<WereWolf3DamageEvent> { event ->
            if(event.damage > 0) { return@registerEvent }
            // 夜じゃない場合はreturn
            if(TIME_OF_DAY!=Time.NIGHT) { return@registerEvent }
            val player = event.player
            val totem = listOf(player.inventory.itemInMainHand,player.inventory.itemInOffHand).find { isSimilar(it) }
            if(totem!=null) {
                // if:トーテムを手に持っていた場合
                totem.amount--
                event.isCancelled = true
                player.health = player.healthScale
                // パーティクルとか音とかですごい感じを出す
                player.world.playSound(player, Sound.ITEM_TOTEM_USE, 2f, 1f)
                player.world.spawnParticle(Particle.TOTEM, player.location, 200, 1.0, 1.5, 1.0)
                player.playEffect(EntityEffect.TOTEM_RESURRECT)
                player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, SPEED_TIME, SPEED_LEVEL, false, false, false))
            }
        }
    }
}