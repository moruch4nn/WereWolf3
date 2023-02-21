package dev.mr3n.werewolf3.items.wolf

import dev.moru3.minepie.events.EventRegister.Companion.registerEvent
import dev.mr3n.werewolf3.Keys
import dev.mr3n.werewolf3.WereWolf3
import dev.mr3n.werewolf3.items.IShopItem
import dev.mr3n.werewolf3.utils.damageTo
import dev.mr3n.werewolf3.utils.getContainerValue
import dev.mr3n.werewolf3.utils.setContainerValue
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.persistence.PersistentDataType
import kotlin.math.abs

@Suppress("unused")
object AssassinSword: IShopItem.ShopItem("assassin_sword", Material.IRON_SWORD) {
    private val SUCCESS_TITLE_TEXT = titleText("assassin_success")

    private val FAILED_TITLE_TEXT = titleText("assassin_failed")

    private val ATTACK_ANGLE: Double = itemConstant("attack_angle")

    private val COOLDOWN_TIME: Long = itemConstant("cooldown_time")

    init {
        WereWolf3.INSTANCE.registerEvent<EntityDamageByEntityEvent> { event ->
            val player = event.damager
            val target = event.entity
            if(player !is Player) { return@registerEvent }
            if(target !is Player) { return@registerEvent }
            val item = player.inventory.itemInMainHand
            if(!isSimilar(item)) { return@registerEvent }
            event.isCancelled = true
            val lastUsedTime = item.getContainerValue(Keys.LAST_USED_TIME, PersistentDataType.LONG)?:0L
            val coolDown = COOLDOWN_TIME - ((System.currentTimeMillis()-lastUsedTime) / 50)
            if(coolDown > 0) {
                // if:クールダウン中の場合
                player.sendTitle(FAILED_TITLE_TEXT, messages("cooldown_now", "%sec%" to coolDown / 20), 0, 100, 20)
                return@registerEvent
            }

            // ターゲット"の"方角(playerから見た)
            val direction = target.location.toVector().subtract(player.location.toVector())
            // ターゲットの方向を向いた際のyawの値
            var yaw = player.location.clone().setDirection(direction).yaw
            if(yaw > 180) { yaw -= 360 }
            // ターゲットか向いている方角
            val targetYaw = target.location.yaw
            // しっかりと背後から殴ったかどうかを判定
            val success = if(abs(targetYaw) > 180 - ATTACK_ANGLE) {
                val pos: ClosedFloatingPointRange<Double>
                val neg: ClosedFloatingPointRange<Double>
                if(targetYaw > 0) {
                    // if:yawが正の値の場合
                    pos = targetYaw-ATTACK_ANGLE..180.0
                    neg = -180.0..targetYaw-(360-ATTACK_ANGLE)
                } else {
                    // if:yawが負の値の場合
                    neg = -180.0..targetYaw+ATTACK_ANGLE
                    pos = targetYaw+(360-ATTACK_ANGLE)..180.0
                }
                yaw in pos || yaw in neg
            } else {
                yaw in (targetYaw-ATTACK_ANGLE)..(targetYaw+ATTACK_ANGLE)
            }
            if(success) {
                // if:背後から殴っていた場合
                item.amount--
                player.damageTo(target, -1.0)
                player.sendTitle(SUCCESS_TITLE_TEXT,messages("success"),0,60,20)
            } else {
                // if:背後から殴っていなかった場合
                player.sendTitle(FAILED_TITLE_TEXT,messages("need_from_back"),0,60,20)
                player.world.playSound(player,Sound.ENTITY_ITEM_BREAK,1f,1f)
                item.setContainerValue(Keys.LAST_USED_TIME, PersistentDataType.LONG, System.currentTimeMillis())
            }
        }
    }
}