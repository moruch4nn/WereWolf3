package dev.mr3n.werewolf3.items.wolf

import dev.moru3.minepie.events.EventRegister.Companion.registerEvent
import dev.mr3n.werewolf3.*
import dev.mr3n.werewolf3.datatypes.RoleDataType
import dev.mr3n.werewolf3.items.IShopItem
import dev.mr3n.werewolf3.protocol.TeamPacketUtil
import dev.mr3n.werewolf3.roles.Role
import dev.mr3n.werewolf3.sidebar.ISideBar.Companion.sidebar
import dev.mr3n.werewolf3.sidebar.RunningSidebar
import dev.mr3n.werewolf3.utils.asPrefixed
import dev.mr3n.werewolf3.utils.getContainerValue
import dev.mr3n.werewolf3.utils.joinedPlayers
import dev.mr3n.werewolf3.utils.role
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.event.player.PlayerInteractEvent

@Suppress("unused")
object LastResort: IShopItem.ShopItem("last_resort",Material.END_CRYSTAL) {
    private val LAST_RESORT_TITLE_TEXT = titleText("last_resort")

    private var isUsed = false

    override fun onEnd() {
        isUsed = false
    }

    init {
        WereWolf3.INSTANCE.registerEvent<PlayerInteractEvent> { event ->
            val player = event.player
            val item = event.item
            if(item == null || !isSimilar(item)) { return@registerEvent }
            if(isUsed) {
                player.sendMessage(messages("already_used").asPrefixed())
                return@registerEvent
            }
            DAYS--
            item.amount--
            val wolfs = joinedPlayers().filter { p -> p.role == Role.WOLF }
            wolfs.forEach { player2 ->
                player2.inventory.contents.filterNotNull().filter { item -> item.getContainerValue(Role.HELMET_ROLE_TAG_KEY, RoleDataType) != null }.forEach { item -> item.amount = 0 }
            }
            joinedPlayers().forEach { player2 ->
                player2.playSound(player2, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1f, 1f)
                player2.playSound(player2, Sound.ENTITY_WOLF_HOWL,2f, 0.6f)
                player2.sendTitle(LAST_RESORT_TITLE_TEXT, messages("subtitle"), 0, 100, 20)
                player2.sendMessage(messages("description").split("\n").joinToString("\n") { it.asPrefixed() })
                player2.sendMessage(messages("wolfs", "%wolfs%" to wolfs.joinToString(" ") { it.name }))
                val sidebar = player2.sidebar
                if(sidebar is RunningSidebar) {
                    sidebar.day(Constants.MAX_DAYS - DAYS)
                }
                TeamPacketUtil.add(player2, ChatColor.DARK_RED, wolfs)
            }
            this.isUsed = true
        }
    }
}