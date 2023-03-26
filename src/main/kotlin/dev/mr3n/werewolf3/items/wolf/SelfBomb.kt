package dev.mr3n.werewolf3.items.wolf

import dev.moru3.minepie.events.EventRegister.Companion.registerEvent
import dev.mr3n.werewolf3.WereWolf3
import dev.mr3n.werewolf3.items.IShopItem
import dev.mr3n.werewolf3.utils.alivePlayers
import dev.mr3n.werewolf3.utils.damageTo
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.event.player.PlayerInteractEvent

@Suppress("unused")
object SelfBomb: IShopItem.ShopItem("self_bomb", Material.TNT) {
    private val DISTANCE: Int = itemConstant("distance")

    init {
        WereWolf3.INSTANCE.registerEvent<PlayerInteractEvent> { event ->
            val player = event.player
            val item = event.item
            if(item == null || !isSimilar(item)) { return@registerEvent }
            player.world.playSound(player.location, Sound.ENTITY_GENERIC_EXPLODE, 2f, 1f)
            player.world.spawnParticle(Particle.EXPLOSION_HUGE,player.location, 100)
            alivePlayers().filter { it.location.distance(player.location) < DISTANCE }
                .sortedBy { it.location.distance(player.location) }
                .forEach { player1 -> player.damageTo(player1, -1.0) }
        }
    }
}