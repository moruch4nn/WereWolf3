package dev.mr3n.werewolf3.items.wolf

import dev.moru3.minepie.events.EventRegister.Companion.registerEvent
import dev.mr3n.werewolf3.WereWolf3
import dev.mr3n.werewolf3.items.IShopItem
import dev.mr3n.werewolf3.protocol.DeadBody
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.event.player.PlayerInteractEvent

@Suppress("unused")
object FakeDeadBody: IShopItem.ShopItem("fake_dead_body", Material.LEAD) {
    private val SUCCESS_TITLE_TEXT = titleText("fake_dead_body")

    init {
        WereWolf3.INSTANCE.registerEvent<PlayerInteractEvent> { event ->
            val player = event.player
            val item = event.item
            if(item == null || !isSimilar(item)) { return@registerEvent }
            val deadBody = DeadBody(player)
            deadBody.balance = (1..3).random()
            player.sendTitle(SUCCESS_TITLE_TEXT,messages("success"),0,100,20)
            player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f)
            item.amount--
            event.isCancelled = true
        }
    }
}