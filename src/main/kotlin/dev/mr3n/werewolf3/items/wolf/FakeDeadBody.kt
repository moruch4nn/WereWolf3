package dev.mr3n.werewolf3.items.wolf

import dev.moru3.minepie.events.EventRegister.Companion.registerEvent
import dev.mr3n.werewolf3.WereWolf3
import dev.mr3n.werewolf3.items.IShopItem
import dev.mr3n.werewolf3.protocol.DeadBody
import dev.mr3n.werewolf3.utils.*
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.event.player.PlayerInteractEvent

@Suppress("unused")
object FakeDeadBody: IShopItem.ShopItem("fake_dead_body", Material.LEAD) {
    private val SUCCESS_TITLE_TEXT = titleText("fake_dead_body")

    init {
        WereWolf3.INSTANCE.registerEvent<PlayerInteractEvent> { event ->
            val item = event.player.inventory.itemInMainHand
            if(!isSimilar(item)) { return@registerEvent }
            DeadBody(event.player)
            event.player.sendTitle(SUCCESS_TITLE_TEXT,messages("success"),0,100,20)
            event.player.playSound(event.player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f)
        }
    }
}