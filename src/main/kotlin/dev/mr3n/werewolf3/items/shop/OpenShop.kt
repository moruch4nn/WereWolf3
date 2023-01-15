package dev.mr3n.werewolf3.items.shop

import dev.moru3.minepie.events.EventRegister.Companion.registerEvent
import dev.mr3n.werewolf3.ItemShop.openShopMenu
import dev.mr3n.werewolf3.WereWolf3
import dev.mr3n.werewolf3.items.IShopItem
import dev.mr3n.werewolf3.roles.Role
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerInteractEvent

object OpenShop: IShopItem.ShopItem("open_shop", Material.AMETHYST_SHARD) {
    override val roles: List<Role> = listOf()
    init {
        // インベントリをクリックした場合
        WereWolf3.INSTANCE.registerEvent<InventoryClickEvent>() { event ->
            val item = event.currentItem?:return@registerEvent
            val player = event.whoClicked
            if(player !is Player) { return@registerEvent }
            // アイテムがOpenShopではない場合return
            if(!this.isSimilar(item)) { return@registerEvent }
            event.isCancelled = true
            // プレイヤーにショップメニューを表示
            player.openShopMenu()
        }
        // アイテムをクリックした場合
        WereWolf3.INSTANCE.registerEvent<PlayerInteractEvent>() { event ->
            val item = event.item?:return@registerEvent
            val player = event.player
            // アイテムがOpenShopではない場合return
            if(!this.isSimilar(item)) { return@registerEvent }
            event.isCancelled = true
            // プレイヤーにショップメニューを表示
            player.openShopMenu()
        }
    }
}