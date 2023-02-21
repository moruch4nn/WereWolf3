package dev.mr3n.werewolf3.items

import org.bukkit.Material
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta

@Suppress("unused")
object Shield: IShopItem.ShopItem("shield", Material.SHIELD) {
    private val DURABILITY: Int = itemConstant("durability")

    override fun onSetItemMeta(itemMeta: ItemMeta) {
        if(itemMeta is Damageable) {
            itemMeta.damage = Material.SHIELD.maxDurability-DURABILITY
        }
    }
}