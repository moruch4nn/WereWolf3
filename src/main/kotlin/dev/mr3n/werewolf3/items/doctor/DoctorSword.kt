package dev.mr3n.werewolf3.items.doctor

import dev.moru3.minepie.events.EventRegister.Companion.registerEvent
import dev.mr3n.werewolf3.Keys
import dev.mr3n.werewolf3.WereWolf3
import dev.mr3n.werewolf3.items.IShopItem
import dev.mr3n.werewolf3.utils.getContainerValue
import dev.mr3n.werewolf3.utils.setContainerValue
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.persistence.PersistentDataType

object DoctorSword: IShopItem.ShopItem("doctor_sword", Material.IRON_SWORD) {
    private val MAX_HEAL_AMOUNT: Double = itemConstant("health_amount")

    private val HEAL_TITLE_TEXT: String = titleText("heal")

    private val SWORD_TITLE_TEXT = titleText("sword")

    override val displayName: String = super.displayName.replace("%amount%", "$MAX_HEAL_AMOUNT")

    private var ItemStack.healthAmount: Double
        get() = this.getContainerValue(Keys.HEALTH_AMOUNT, PersistentDataType.DOUBLE)?: MAX_HEAL_AMOUNT
        set(value) { this.setContainerValue(Keys.HEALTH_AMOUNT, PersistentDataType.DOUBLE, value) }

    fun Player.healTo(target: Player,event: Cancellable? = null) {
        val player = this
        val item = player.inventory.itemInMainHand
        // アイテムがヒールの剣じゃない場合はreturn
        if(!isSimilar(item)) { return }
        if(!WereWolf3.PLAYERS.contains(player)) { return }
        if(!WereWolf3.PLAYERS.contains(target)) { return }
        event?.isCancelled = true
        // ヒール量を推定
        val healAmount = minOf(target.healthScale-target.health, minOf(8.0, item.healthAmount))
        if(healAmount<=0) {
            // ヒール量が0以下の場合はヒールできない旨を通知
            player.sendTitle(SWORD_TITLE_TEXT, messages("not_working"), 0, 1, 20)
        } else {
            // 1以上の場合はプレイヤーをヒール
            target.health += healAmount
            // ヒール残量をヒール量分減らす
            item.healthAmount -= healAmount
            // 剣にヒール量を記載
            item.itemMeta = (item.itemMeta as Damageable?)?.also { meta ->
                // アイテムの耐久値をヒールの残り残量の応じて変更。
                meta.damage = ((MAX_HEAL_AMOUNT - item.healthAmount) * (item.type.maxDurability/ MAX_HEAL_AMOUNT)).toInt()
                // アイテム名にヒールの残り残量を表示。
                meta.setDisplayName(super.displayName.replace("%amount%", "${item.healthAmount}"))
            }
            // ヒールした旨を通知
            player.sendTitle(SWORD_TITLE_TEXT, messages("healing", "%player%" to target.name), 0, 1, 20)
            // ヒールされた旨を通知
            target.sendTitle(HEAL_TITLE_TEXT, messages("healed"), 0, 1, 20)

            // ヒール残量が0を下回った場合のみアイテムを壊す
            if(item.healthAmount <= 0) {
                // アイテムが壊れる音を再生
                player.playSound(player,Sound.ENTITY_ITEM_BREAK, 1f,1f)
                item.amount--
            }
        }
    }

    init {
        WereWolf3.INSTANCE.registerEvent<EntityDamageByEntityEvent> { event ->
            val player = event.damager
            val target = event.entity
            // playerおよびtargetがプレイヤーではない場合return
            if(player !is Player || target !is Player) { return@registerEvent }
            player.healTo(target, event)
        }
    }
}