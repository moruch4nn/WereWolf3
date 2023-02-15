package dev.mr3n.werewolf3.items.madman

import dev.moru3.minepie.Executor.Companion.runTaskLater
import dev.moru3.minepie.events.EventRegister.Companion.registerEvent
import dev.mr3n.werewolf3.PLAYERS
import dev.mr3n.werewolf3.WereWolf3
import dev.mr3n.werewolf3.items.IShopItem
import dev.mr3n.werewolf3.protocol.MetadataPacketUtil
import dev.mr3n.werewolf3.protocol.TeamPacketUtil
import dev.mr3n.werewolf3.roles.Role
import dev.mr3n.werewolf3.utils.role
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.meta.ItemMeta

@Suppress("unused")
object WolfGuide: IShopItem.ShopItem("wolf_guide", Material.BOOK) {
    override fun onSetItemMeta(itemMeta: ItemMeta) {
        itemMeta.addEnchant(Enchantment.LUCK, 1, true)
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
    }

    private val GUIDE_TITLE_TEXT = titleText("guide")

    private val SEARCH_TIME = itemConstant<Long>("search_time")

    init {
        WereWolf3.INSTANCE.registerEvent<PlayerInteractEvent> { event ->
            val player = event.player
            if(!PLAYERS.contains(player)) { return@registerEvent }
            // main handじゃない場合はreturn
            if(event.hand!=EquipmentSlot.HAND) { return@registerEvent }
            // 右クリックしていない場合はreturn
            if(event.action!=Action.RIGHT_CLICK_AIR&&event.action!=Action.RIGHT_CLICK_BLOCK) { return@registerEvent }
            val item = player.inventory.itemInMainHand
            // 人狼ガイドを持っていない場合はreturn
            if(!isSimilar(item)) { return@registerEvent }
            player.sendTitle(GUIDE_TITLE_TEXT, messages("searching"), 10, SEARCH_TIME.toInt() + 10, 0)
            player.playSound(player, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1f, 1f)
            item.amount--
            WereWolf3.INSTANCE.runTaskLater(SEARCH_TIME) {
                val wolf = PLAYERS.filter { it.role == Role.WOLF }.randomOrNull()
                if(wolf==null) {
                    player.sendTitle(GUIDE_TITLE_TEXT, messages("wolf_not_found"), 0, 100, 0)
                    player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f)
                } else {
                    player.sendTitle(GUIDE_TITLE_TEXT, messages("searched", "%player%" to wolf.name), 0, 100, 0)
                    player.playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f)
                    TeamPacketUtil.add(player, ChatColor.DARK_RED, listOf(wolf))
                    MetadataPacketUtil.addToGlowing(player, wolf)
                }
            }
        }
    }
}