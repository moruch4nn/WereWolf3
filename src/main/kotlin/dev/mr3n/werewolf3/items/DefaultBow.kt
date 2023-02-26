package dev.mr3n.werewolf3.items

import dev.moru3.minepie.Executor.Companion.runTaskLater
import dev.moru3.minepie.events.EventRegister.Companion.registerEvent
import dev.moru3.minepie.item.EasyItem
import dev.mr3n.werewolf3.WereWolf3
import dev.mr3n.werewolf3.utils.asPrefixed
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.meta.ItemMeta

@Suppress("unused")
object DefaultBow: IShopItem.ShopItem("default_bow", Material.BOW) {

    val DELAY_IN_ADDING_ARROW: Long = itemConstant("delay_in_adding_arrow")

    override fun onSetItemMeta(itemMeta: ItemMeta) {
        itemMeta.isUnbreakable = true
        itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE)
    }

    init {
        WereWolf3.INSTANCE.registerEvent<EntityShootBowEvent> { event ->
            val player = event.entity
            if(player !is Player) { return@registerEvent }
            val bow = event.bow?:return@registerEvent
            if(isSimilar(bow)) { return@registerEvent }
            player.sendMessage(messages("arrow_will_adding_in", "%sec%" to DELAY_IN_ADDING_ARROW / 20).asPrefixed())
            WereWolf3.INSTANCE.runTaskLater(DELAY_IN_ADDING_ARROW) { player.inventory.addItem(EasyItem(Material.ARROW)) }
        }
    }
}