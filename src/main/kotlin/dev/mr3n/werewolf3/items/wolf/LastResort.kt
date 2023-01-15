package dev.mr3n.werewolf3.items.wolf

import dev.moru3.minepie.events.EventRegister.Companion.registerEvent
import dev.mr3n.werewolf3.Constants
import dev.mr3n.werewolf3.WereWolf3
import dev.mr3n.werewolf3.items.IShopItem
import dev.mr3n.werewolf3.protocol.TeamPacketUtil
import dev.mr3n.werewolf3.roles.Role
import dev.mr3n.werewolf3.sidebar.ISideBar.Companion.sidebar
import dev.mr3n.werewolf3.sidebar.RunningSidebar
import dev.mr3n.werewolf3.utils.asPrefixed
import dev.mr3n.werewolf3.utils.getContainerValue
import dev.mr3n.werewolf3.utils.role
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent

object LastResort: IShopItem.ShopItem("last_resort",Material.END_CRYSTAL) {
    private val LAST_RESORT_TITLE_TEXT = titleText("item.$id.title.last_resort")

    private var hasBought = false

    override fun buy(player: Player): Boolean {
        return if(hasBought) {
            player.sendMessage(messages("already_bought").asPrefixed())
            false
        } else {
            hasBought = true
            // 購入が成功した場合すべての人狼に購入した旨を通知する
            if(super.buy(player)) {
                WereWolf3.PLAYERS.filter { it.role == Role.WOLF }.forEach { wolf ->
                    wolf.playSound(wolf, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 0f)
                    wolf.sendTitle(LAST_RESORT_TITLE_TEXT, messages("bought", "%player%" to player.name), 0, 100, 20)
                    wolf.sendMessage(messages("what").asPrefixed())
                }
                true
            } else {
                false
            }
        }
    }

    override fun onEnd() {
        hasBought = false
    }

    init {
        WereWolf3.INSTANCE.registerEvent<PlayerInteractEvent> {
            val item = it.player.inventory.itemInMainHand
            if(!isSimilar(item)) { return@registerEvent }
            WereWolf3.DAYS--
            item.amount--
            val wolfs = WereWolf3.PLAYERS.filter { p -> p.role == Role.WOLF }
            wolfs.forEach { player ->
                player.inventory.contents.filterNotNull().filter { item -> item.getContainerValue(Role.HELMET_ROLE_TAG_KEY, Role.RoleTagType) != null }.forEach { item -> item.amount = 0 }
            }
            WereWolf3.PLAYERS.forEach { player ->
                player.playSound(player, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1f, 1f)
                player.playSound(player, Sound.ENTITY_WOLF_HOWL,2f, 0.6f)
                player.sendTitle(LAST_RESORT_TITLE_TEXT, messages("subtitle"), 0, 100, 20)
                player.sendMessage(messages("description").split("\n").joinToString("\n") { it.asPrefixed() })
                player.sendMessage(messages("wolfs", "%wolfs%" to WereWolf3.PLAYERS.filter { p -> p.role == Role.WOLF }.joinToString(" ") { it.name }))
                val sidebar = player.sidebar
                if(sidebar is RunningSidebar) {
                    sidebar.day(Constants.MAX_DAYS - WereWolf3.DAYS)
                }
                TeamPacketUtil.add(player, ChatColor.DARK_RED, wolfs)
            }
        }
    }
}