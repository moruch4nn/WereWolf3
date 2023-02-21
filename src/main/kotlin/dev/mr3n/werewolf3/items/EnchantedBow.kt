package dev.mr3n.werewolf3.items

import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.meta.ItemMeta

@Suppress("unused")
object EnchantedBow: IShopItem.ShopItem("enchanted_bow", Material.BOW) {
    override fun onSetItemMeta(itemMeta: ItemMeta) {
        itemMeta.addEnchant(Enchantment.ARROW_INFINITE,1,false)
        itemMeta.addEnchant(Enchantment.ARROW_DAMAGE,1,false)
        itemMeta.isUnbreakable = true
    }
}