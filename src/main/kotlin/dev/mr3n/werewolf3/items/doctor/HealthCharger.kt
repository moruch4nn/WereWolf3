package dev.mr3n.werewolf3.items.doctor

import dev.moru3.minepie.Executor.Companion.runTaskTimer
import dev.moru3.minepie.events.EventRegister.Companion.registerEvent
import dev.mr3n.werewolf3.WereWolf3
import dev.mr3n.werewolf3.items.IShopItem
import dev.mr3n.werewolf3.utils.alivePlayers
import dev.mr3n.werewolf3.utils.asPrefixed
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.util.Vector

@Suppress("unused")
object HealthCharger: IShopItem.ShopItem("health_charger", Material.REDSTONE_ORE) {
    private val HEAL_AMOUNT: Double = itemConstant("heal_amount")

    private val DISTANCE: Double = itemConstant("distance")

    private val CHARGER_TITLE_TEXT = titleText("charger")

    private val healthChargers = mutableListOf<Location>()

    override fun onEnd() {
        healthChargers.forEach { it.block.type = Material.AIR }
        healthChargers.clear()
    }

    init {
        WereWolf3.INSTANCE.runTaskTimer(0L, 35L) {
            healthChargers.forEach { chargerLoc ->
                alivePlayers().filter { it.location.distance(chargerLoc) <= DISTANCE }.forEach { player ->
                    player.sendTitle(CHARGER_TITLE_TEXT, messages("healing"), 0, 1, 20)
                    player.health = minOf(player.healthScale, player.health + HEAL_AMOUNT)
                    player.playSound(player, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 2f)
                }
            }
        }

        WereWolf3.INSTANCE.registerEvent<PlayerInteractEvent> { event ->
            if(event.hand!=EquipmentSlot.HAND) { return@registerEvent }
            if(event.action!=Action.RIGHT_CLICK_BLOCK) { return@registerEvent }
            val player = event.player
            val item = event.item
            if(item == null || !isSimilar(item)) { return@registerEvent }
            val clickedBlock = event.clickedBlock?:return@registerEvent
            val face = event.blockFace
            val placedLocation = clickedBlock.location.clone().add(Vector(face.modX,face.modY,face.modZ))
            event.isCancelled = true
            val canPlaceHere = (-1..1).all { x -> (-1..1).all { z -> (0..2).all { y ->
                placedLocation.clone().add(Vector(x,y,z)).block.isEmpty
            } } }
            if(canPlaceHere) {
                //if: チャージャーを設置できる場合
                item.amount--
                placedLocation.block.type = Material.REDSTONE_ORE
                healthChargers.add(placedLocation)
                player.world.playSound(player,Sound.BLOCK_STONE_PLACE, 1f, 1f)
                player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f)
            } else {
                //if: チャージャーを設置できない場合
                player.sendMessage(messages("cant_placeable").asPrefixed())
                player.playSound(player, Sound.BLOCK_NOTE_BLOCK_BASS, 1f, 1f)
            }
        }
    }
}