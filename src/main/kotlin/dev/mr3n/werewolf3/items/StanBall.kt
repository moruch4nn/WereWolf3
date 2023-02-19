package dev.mr3n.werewolf3.items

import dev.moru3.minepie.Executor.Companion.runTaskLater
import dev.moru3.minepie.events.EventRegister.Companion.registerEvent
import dev.mr3n.werewolf3.Keys
import dev.mr3n.werewolf3.WereWolf3
import dev.mr3n.werewolf3.utils.alivePlayers
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector

@Suppress("unused")
object StanBall: IShopItem.ShopItem("stan_ball", Material.SNOWBALL) {
    private const val ENTITY_TYPE = "STAN_BALL"

    private val STAN_RADIUS: Int = itemConstant("radius")
    private val STAN_TIME: Int = itemConstant("stan_time")

    private val STAN_TITLE_TEXT = titleText("stan")

    private val stunnedPlayers = mutableListOf<Player>()

    init {
        WereWolf3.INSTANCE.registerEvent<ProjectileLaunchEvent> { event ->
            val projectile = event.entity
            val shooter = projectile.shooter?:return@registerEvent
            if(shooter !is Player) { return@registerEvent }
            if(!isSimilar(shooter.inventory.itemInMainHand)) { return@registerEvent }
            // 爆発玉識別用のタグを付与
            projectile.persistentDataContainer.set(Keys.ENTITY_TYPE, PersistentDataType.STRING, ENTITY_TYPE)
        }
        WereWolf3.INSTANCE.registerEvent<PlayerMoveEvent> { event ->
            if(event.player.gameMode == GameMode.SPECTATOR) { return@registerEvent }
            if(stunnedPlayers.contains(event.player)) {
                val to = (event.to?:event.player.location)
                if(to.pitch != -90f) {
                    event.player.teleport(to.setDirection(Vector(0,1,0)))
                }
            }
        }
        WereWolf3.INSTANCE.registerEvent<ProjectileHitEvent> { event ->
            val projectile = event.entity
            // あたった発射物が爆発玉じゃない場合はreturn
            if(projectile.persistentDataContainer.get(Keys.ENTITY_TYPE, PersistentDataType.STRING) != ENTITY_TYPE) { return@registerEvent }
            val shooter = projectile.shooter?:return@registerEvent
            if(shooter !is Player) { return@registerEvent }
            // 着弾点
            val location = projectile.location.clone()
            // worldがnullableなためnullではないことを保証する
            val world = location.world?:return@registerEvent
            // まれに発射物が二回当たった判定になる場合があるので発射物を削除
            projectile.remove()
            event.isCancelled = true

            world.spawnParticle(Particle.EXPLOSION_HUGE, location, 30, .0, .0, .0)
            world.playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 1f, 1f)

            alivePlayers().forEach { player ->
                when(player.location.distance(location).toInt()) {
                    // プレイヤーが爆発範囲に入っている場合
                    in 0..STAN_RADIUS -> {
                        player.flySpeed = -1f
                        player.walkSpeed = -1f
                        player.addPotionEffect(PotionEffect(PotionEffectType.JUMP, Int.MAX_VALUE, 250, false, false, false))
                        player.playSound(player, Sound.ITEM_TOTEM_USE, 1F,0.7F)
                        player.sendTitle(STAN_TITLE_TEXT, messages("stan"), 0, STAN_TIME, 10)
                        this.stunnedPlayers.add(player)
                        player.teleport(player.location.also { it.pitch=-90f;it.yaw=0f })
                        WereWolf3.INSTANCE.runTaskLater(STAN_TIME.toLong()) {
                            player.flySpeed = 0.2f
                            player.walkSpeed = 0.2f
                            player.removePotionEffect(PotionEffectType.JUMP)
                            this.stunnedPlayers.remove(player)
                        }
                    }
                }
            }
        }
    }
}