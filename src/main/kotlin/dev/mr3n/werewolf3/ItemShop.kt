package dev.mr3n.werewolf3

import dev.moru3.minepie.customgui.inventory.CustomContentsSyncGui.Companion.createCustomContentsGui
import dev.moru3.minepie.item.EasyItem
import dev.mr3n.werewolf3.items.IShopItem
import dev.mr3n.werewolf3.utils.addLore
import dev.mr3n.werewolf3.utils.languages
import dev.mr3n.werewolf3.utils.role
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType

object ItemShop {
    private val LIGHT_GRAY_STAINED_GLASS_PANE = EasyItem(Material.LIGHT_GRAY_STAINED_GLASS_PANE, " ")
    private val RED_STAINED_GLASS_PANE = EasyItem(Material.RED_STAINED_GLASS_PANE, " ")

    /**
     * プレイヤーにショップのGUIを表示する
     */
    fun Player.openShopMenu() {
        val player = this
        val role = player.role?:return
        WereWolf3.INSTANCE.createCustomContentsGui(6,"", 1, 1, 7, 4) {
            repeat(9) { x ->
                set(x, 0, RED_STAINED_GLASS_PANE)
                set(x, 5, RED_STAINED_GLASS_PANE)
            }
            repeat(6) { y ->
                set(0, y, RED_STAINED_GLASS_PANE)
                set(8, y, RED_STAINED_GLASS_PANE)
            }
            set(4,0, EasyItem(Material.AMETHYST_SHARD, languages("shop.name")))
            set(4,5, EasyItem(Material.REDSTONE_BLOCK, languages("shop.close"))) {
                action(ClickType.LEFT, ClickType.RIGHT) { player.closeInventory() }
            }
            IShopItem.ShopItem.ITEMS.filter { item -> item.roles.contains(role) }.forEach { item ->
                addContents(item.itemStack.addLore("", languages("shop.price", "%price%" to "${item.price}${Constants.MONEY_UNIT}"))) {
                    action(ClickType.LEFT) { item.buy(player) }
                }
            }
            repeat(9 * 6) { addContents(LIGHT_GRAY_STAINED_GLASS_PANE) }
        }.open(player)
    }
}