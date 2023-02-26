package dev.mr3n.werewolf3.items

import org.bukkit.Material
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.meta.ItemMeta

@Suppress("unused")
object DefaultSword: IShopItem.ShopItem("default_sword", Material.WOODEN_SWORD) {
    override fun onSetItemMeta(itemMeta: ItemMeta) {
        itemMeta.isUnbreakable = true
        itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE)
    }
}