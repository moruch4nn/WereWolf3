package dev.mr3n.werewolf3.items

import dev.moru3.minepie.events.EventRegister.Companion.registerEvent
import dev.mr3n.werewolf3.PLAYERS
import dev.mr3n.werewolf3.WereWolf3
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

@Suppress("unused")
object SpeedPotion: IShopItem.ShopItem("speed_potion", Material.POTION) {
    private val TIME: Int = itemConstant("time")

    private val LEVEL: Int = itemConstant("level")

    override val description: List<String> = super.description.map { it.replace("%time%", "${TIME/20}") }

    override fun onSetItemMeta(itemMeta: ItemMeta) {
        if(itemMeta !is PotionMeta) { return }
        itemMeta.color = Color.BLUE
        itemMeta.itemFlags.add(ItemFlag.HIDE_POTION_EFFECTS)
    }

    init {
        WereWolf3.INSTANCE.registerEvent<PlayerItemConsumeEvent> { event ->
            val player = event.player
            val item = event.item
            if(!isSimilar(item)) { return@registerEvent }
            if(!PLAYERS.contains(player)) { return@registerEvent }
            player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, TIME, LEVEL, false, false, true))
            player.inventory.itemInMainHand.amount--
        }
    }
}