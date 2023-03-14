package dev.mr3n.werewolf3.items

import dev.moru3.minepie.events.EventRegister.Companion.registerEvent
import dev.mr3n.werewolf3.Keys
import dev.mr3n.werewolf3.WereWolf3
import dev.mr3n.werewolf3.utils.damageTo
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Arrow
import org.bukkit.entity.Player
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType

@Suppress("unused")
object OneShotBow: IShopItem.ShopItem("one_shot_bow", Material.BOW) {
    override fun onSetItemMeta(itemMeta: ItemMeta) {
        if(itemMeta !is Damageable) { return }
        itemMeta.isUnbreakable = true
        itemMeta.damage = material.maxDurability.toInt()
        itemMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE)
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS)
        itemMeta.addEnchant(Enchantment.ARROW_INFINITE,1,true)
    }

    private const val ENTITY_TYPE = "ONE_SHOT_ARROW"

    private val BOW_TITLE_TEXT = titleText("bow")

    init {
        WereWolf3.INSTANCE.registerEvent<PlayerInteractEvent> { event ->
            val player = event.player
            val item = event.item
            if(item == null || !isSimilar(item)) { return@registerEvent }
            val projectile = player.launchProjectile(Arrow::class.java)
            item.amount--
            projectile.persistentDataContainer.set(Keys.ENTITY_TYPE, PersistentDataType.STRING, ENTITY_TYPE)
            player.sendTitle(BOW_TITLE_TEXT, messages("used"), 0, 60, 20)
        }

        WereWolf3.INSTANCE.registerEvent<ProjectileHitEvent> { event ->
            val projectile = event.entity
            // 当たったやつが一撃の矢じゃなかったらreturn
            if(projectile.persistentDataContainer.get(Keys.ENTITY_TYPE, PersistentDataType.STRING) != ENTITY_TYPE) { return@registerEvent }

            val shooter = projectile.shooter?:return@registerEvent
            if(shooter !is Player) { return@registerEvent }

            val hitEntity = event.hitEntity?:return@registerEvent
            if(hitEntity !is Player) { return@registerEvent }

            event.isCancelled = true

            projectile.remove()

            shooter.world.playSound(shooter, Sound.ENTITY_ITEM_BREAK, 1f, 1f)

            shooter.damageTo(hitEntity, -1.0)
        }
    }
}