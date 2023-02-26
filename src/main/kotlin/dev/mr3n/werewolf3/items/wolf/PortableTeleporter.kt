package dev.mr3n.werewolf3.items.wolf

import dev.moru3.minepie.Executor.Companion.runTaskTimer
import dev.moru3.minepie.events.EventRegister.Companion.registerEvent
import dev.mr3n.werewolf3.Keys
import dev.mr3n.werewolf3.WereWolf3
import dev.mr3n.werewolf3.datatypes.LocationDataType
import dev.mr3n.werewolf3.items.IShopItem
import dev.mr3n.werewolf3.utils.*
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.scheduler.BukkitTask

@Suppress("unused")
object PortableTeleporter: IShopItem.ShopItem("portable_teleporter", Material.ECHO_SHARD) {
    private val PORTABLE_TELEPORTER_TEXT = titleText("teleporter")
    private val DELAY_PER_BLOCK: Double = itemConstant("delay_per_block")

    init {
        WereWolf3.INSTANCE.registerEvent<PlayerInteractEvent> { event ->
            val player = event.player
            // main handじゃない場合はreturn
            if(event.hand!= EquipmentSlot.HAND) { return@registerEvent }
            // 右クリックしていない場合はreturn
            if(event.action!= Action.RIGHT_CLICK_AIR&&event.action!= Action.RIGHT_CLICK_BLOCK) { return@registerEvent }
            val item = player.inventory.itemInMainHand
            // ピカピカインクを持っていない場合はreturn
            if(!isSimilar(item)) { return@registerEvent }

            val targetLocation = item.getContainerValue(Keys.TELEPORTER_TARGET, LocationDataType)
            if(targetLocation==null) {
                player.sendTitle(PORTABLE_TELEPORTER_TEXT, messages("set"), 0, 100, 20)
                player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f)
                item.setContainerValue(Keys.TELEPORTER_TARGET, LocationDataType, player.location)
            } else {
                var delay = (targetLocation.distance(player.location) * DELAY_PER_BLOCK).toInt() * 20
                var task: BukkitTask? = null
                task = WereWolf3.INSTANCE.runTaskTimer(1,1) {
                    delay--
                    if(delay % 20 == 0 && delay != 0) { player.playSound(player, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1f) }
                    player.sendTitle(PORTABLE_TELEPORTER_TEXT, messages("teleporting", "%sec%" to (delay/20L)), 0, 5, 0)
                    player.spawnParticle(Particle.SPELL_WITCH, player.location, 10, 0.5, 1.0, 0.5)
                    if(!isSimilar(player.inventory.itemInMainHand)) {
                        task?.cancel()
                        player.sendTitle(PORTABLE_TELEPORTER_TEXT, messages("cancelled"), 0, 100, 20)
                        player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f)
                    } else {
                        if (delay <= 0) {
                            item.amount--
                            task?.cancel()
                            player.teleport(targetLocation)
                            player.sendTitle(PORTABLE_TELEPORTER_TEXT, messages("teleported"), 0, 100, 20)
                            player.playSound(player, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f)
                            player.spawnParticle(Particle.SPELL_WITCH, player.location, 10, 1.0, 1.0, 1.0)
                        }
                    }
                }
            }
        }
    }
}