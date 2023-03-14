package dev.mr3n.werewolf3.items.madman

import dev.moru3.minepie.Executor.Companion.runTaskLater
import dev.moru3.minepie.events.EventRegister.Companion.registerEvent
import dev.mr3n.werewolf3.PLAYERS
import dev.mr3n.werewolf3.WereWolf3
import dev.mr3n.werewolf3.items.IShopItem
import dev.mr3n.werewolf3.protocol.MetadataPacketUtil
import dev.mr3n.werewolf3.protocol.TeamPacketUtil
import dev.mr3n.werewolf3.roles.Role
import dev.mr3n.werewolf3.utils.asPrefixed
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.meta.ItemMeta

@Suppress("unused")
object WolfGuide: IShopItem.ShopItem("wolf_guide", Material.BOOK) {
    override fun onSetItemMeta(itemMeta: ItemMeta) {
        itemMeta.addEnchant(Enchantment.LUCK, 1, true)
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
    }

    private val GUIDE_TITLE_TEXT = titleText("guide")

    init {
        WereWolf3.INSTANCE.registerEvent<PlayerInteractEvent> { event ->
            val player = event.player
            // 右クリックしていない場合はreturn
            if(event.action!=Action.RIGHT_CLICK_AIR&&event.action!=Action.RIGHT_CLICK_BLOCK) { return@registerEvent }
            val item = event.item
            // 人狼ガイドを持っていない場合はreturn
            if(item == null || !isSimilar(item)) { return@registerEvent }
            player.sendTitle(GUIDE_TITLE_TEXT, messages("searching"), 10, 35 + 10, 0)
            player.playSound(event.player, Sound.BLOCK_PORTAL_TRIGGER, 0.3F, 2F)
            item.amount--
            WereWolf3.INSTANCE.runTaskLater(35) {
                val wolf = PLAYERS.filter { it.role == Role.WOLF }.randomOrNull()
                if(wolf==null) {
                    val message = messages("wolf_not_found")
                    player.sendTitle(GUIDE_TITLE_TEXT, message, 20, 100, 20)
                    player.sendMessage(message.asPrefixed())
                } else {
                    val message = messages("searched", "%player%" to wolf.name)
                    player.sendTitle(GUIDE_TITLE_TEXT, message, 20, 100, 0)
                    player.sendMessage(message.asPrefixed())
                    wolf.player?.let {
                        TeamPacketUtil.add(player, ChatColor.DARK_RED, listOf(it))
                        MetadataPacketUtil.addToGlowing(player, it)
                    }
                }
            }
        }
    }
}