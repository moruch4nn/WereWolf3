package dev.mr3n.werewolf3.items.wolf

import dev.moru3.minepie.Executor.Companion.runTaskLater
import dev.moru3.minepie.Executor.Companion.runTaskTimer
import dev.moru3.minepie.events.EventRegister.Companion.registerEvent
import dev.mr3n.werewolf3.Keys
import dev.mr3n.werewolf3.WereWolf3
import dev.mr3n.werewolf3.items.IShopItem
import dev.mr3n.werewolf3.utils.alivePlayers
import dev.mr3n.werewolf3.utils.damageTo
import dev.mr3n.werewolf3.utils.joinedPlayers
import org.bukkit.*
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.entity.Snowball
import org.bukkit.event.block.Action
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

@Suppress("unused")
object BombBall: IShopItem.ShopItem("bomb_ball", Material.SNOWBALL) {
    private val WARNING_TITLE_TEXT = titleText("warning")

    private const val ENTITY_TYPE = "BOMB_BALL"

    private val DAMAGE_DISTANCE: Int = itemConstant("damage_distance")
    private val DEATH_DISTANCE: Int = itemConstant("death_distance")
    private val FUSE_TIME: Long = itemConstant("fuse_time")
    private val MAX_DAMAGE: Int = itemConstant("max_damage")
    private val WARNING_COUNT: Int = itemConstant("warning_count")

    private val balls = mutableSetOf<Projectile>()

    /**
     * 距離から爆発玉の爆発ダメージを計算します。
     */
    private fun calcDamage(distance: Double): Double {
        return if(distance > DAMAGE_DISTANCE) {
            // if:ダメージ範囲の外にいる場合は0
            0.0
        } else if(distance< DEATH_DISTANCE) {
            // if:死亡範囲内にいる場合は-1.0ダメージ(即死)を与える
            -1.0
        } else {
            // 死亡範囲を除いたダメージ範囲を計算。
            val damageDistance = DAMAGE_DISTANCE - DEATH_DISTANCE
            // 最大ダメージが範囲内でmaxDamageにになるように係数を計算
            val coefficient = MAX_DAMAGE / damageDistance
            // 距離を反転させて係数をかける。
            (damageDistance - ((distance / 2) - DEATH_DISTANCE)) * coefficient
        }
    }

    init {
        WereWolf3.INSTANCE.registerEvent<PlayerInteractEvent> { event ->
            val player = event.player
            val item = event.item
            if(item == null || !isSimilar(item)) { return@registerEvent }
            if(event.action != Action.RIGHT_CLICK_BLOCK && event.action != Action.RIGHT_CLICK_AIR) { return@registerEvent }
            val projectile = player.launchProjectile(Snowball::class.java)
            this.balls.add(projectile)
            projectile.persistentDataContainer.set(Keys.ENTITY_TYPE, PersistentDataType.STRING, ENTITY_TYPE)
        }

        WereWolf3.INSTANCE.runTaskTimer(3L,3L) {
            balls.forEach {
                it.world.spawnParticle(Particle.REDSTONE,it.location,1,.0,.0,.0,Particle.DustOptions(Color.RED,1f))
            }
        }

        WereWolf3.INSTANCE.registerEvent<ProjectileHitEvent> { event ->
            val projectile = event.entity
            // あたった発射物が爆発玉じゃない場合はreturn
            if(projectile.persistentDataContainer.get(Keys.ENTITY_TYPE, PersistentDataType.STRING) != ENTITY_TYPE) { return@registerEvent }
            val shooter = projectile.shooter?:return@registerEvent
            this.balls.remove(projectile)
            if(shooter !is Player) { return@registerEvent }
            // 着弾点
            val location = projectile.location.clone()
            // worldがnullableなためnullではないことを保証する
            val world = location.world?:return@registerEvent
            // まれに発射物が二回当たった判定になる場合があるので発射物を削除
            projectile.remove()
            event.isCancelled = true
            // TNTのアイテムをワールドにスポーン
            val tnt = world.spawn(location, Item::class.java)
            // アイテムの内容をTNTに変更
            tnt.itemStack = ItemStack(Material.TNT)
            // アイテムを拾えないようにする
            tnt.pickupDelay = Int.MAX_VALUE
            // アイテムが消えないようにする
            tnt.isUnlimitedLifetime = true
            // TNTの着火音で警告
            world.playSound(location, Sound.ENTITY_TNT_PRIMED, 1f, 1f)
            repeat(WARNING_COUNT) { count ->
                WereWolf3.INSTANCE.runTaskLater((FUSE_TIME / (WARNING_COUNT)) * count) {
                    joinedPlayers().filter { it.location.distance(location) < DAMAGE_DISTANCE }.forEach { player -> player.sendTitle(
                        WARNING_TITLE_TEXT, "", 0, 0, (FUSE_TIME / 4).toInt()) }
                    world.playSound(location, Sound.BLOCK_NOTE_BLOCK_BIT, 1f, 1.8f)
                }
            }
            WereWolf3.INSTANCE.runTaskLater(FUSE_TIME) {
                tnt.remove()
                world.spawnParticle(Particle.EXPLOSION_HUGE, location, 30, .0, .0, .0)
                joinedPlayers().forEach { it.stopSound(SoundCategory.RECORDS) }
                world.playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 1000f, 1f)
                alivePlayers().forEach { player ->
                    // 着弾点とプレイヤーの距離
                    val distance = location.distance(player.location)
                    // shooterがプレイヤーにダメージを与えている判定にする
                    shooter.damageTo(player, calcDamage(distance))
                }
            }
        }
    }
}