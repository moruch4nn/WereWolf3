package dev.mr3n.werewolf3

import dev.mr3n.werewolf3.datatypes.BooleanDataType
import dev.mr3n.werewolf3.protocol.DeadBody
import dev.mr3n.werewolf3.protocol.InvisibleEquipmentPacketUtil
import dev.mr3n.werewolf3.protocol.MetadataPacketUtil
import dev.mr3n.werewolf3.roles.Role
import dev.mr3n.werewolf3.sidebar.DeathSidebar
import dev.mr3n.werewolf3.sidebar.ISideBar.Companion.sidebar
import dev.mr3n.werewolf3.sidebar.WaitingSidebar
import dev.mr3n.werewolf3.utils.*
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityPotionEffectEvent
import org.bukkit.event.entity.EntityRegainHealthEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.*
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

object PlayerListener: Listener {

    /**
     * tellコマンドなどその他のメッセージコマンドを無効化
     */
    @Suppress("unused")
    @EventHandler
    fun onCommand(event: PlayerCommandPreprocessEvent) {
        if(event.player.gameMode == GameMode.SPECTATOR && Constants.MESSAGE_COMMANDS.contains(event.message.split(" ").firstOrNull())) {
            event.isCancelled = true
        }
    }

    @Suppress("unused")
    @EventHandler
    fun onEffect(event: EntityPotionEffectEvent) {
        val player = event.entity
        if(player !is Player) { return }
        if(player.isAlive) { return }
        if(event.newEffect!=null) { event.isCancelled = true }
    }

    /**
     * 死んだ際にそのプレイヤーを死体にしてその他死亡時の処理を行う
     */
    @Suppress("unused")
    @EventHandler
    fun onDead(event: PlayerDeathEvent) {
        val player = event.entity
        val killer = player.killer
        if(killer!=null) {
            killer.playSound(killer, Sound.ENTITY_ARROW_HIT_PLAYER, 1f, 1f)
            killer.kills.add(player.playerData)
            player.sendTitle(languages("title.you_are_dead.title"),languages("title.you_are_dead.subtitle_with_killer", "%killer%" to killer.name), 0, 100, 20)
            if(player.role?.team==Role.Team.VILLAGER&&killer.role?.team==Role.Team.VILLAGER) {
                alivePlayers().filter { it.role == Role.WOLF }.forEach { wolf ->
                    wolf.money += Constants.TEAM_KILL_BONUS
                    wolf.sendMessage(languages("messages.team_kill_bonus", "%money%" to "${Constants.TEAM_KILL_BONUS}${Constants.MONEY_UNIT}").asPrefixed())
                    wolf.playSound(wolf, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f)
                }
            }
        } else {
            player.sendTitle(languages("title.you_are_dead.title"),languages("title.you_are_dead.subtitle"), 0, 100, 20)
        }
        // ゲームモードをスペクテイターに設定 注意: 絶対にインベントリを削除する前にスペクテイターに変更してください。
        player.gameMode = GameMode.SPECTATOR
        // 死体を生成 注意: 絶対にインベントリを削除する前に死体を生成してください。
        DeadBody(player)
        // インベントリを削除
        player.inventory.clear()
        // 体力を満タンに設定
        player.health = player.healthScale
        player.sidebar = DeathSidebar(player)
        // すべてのプレイヤーを表示する
        joinedPlayers().forEach {
            InvisibleEquipmentPacketUtil.remove(player, it, 0)
            MetadataPacketUtil.removeFromInvisible(player, it)
        }
        MetadataPacketUtil.removeAllInvisible(player)

        DeadBody.DEAD_BODIES.forEach { it.show(listOf(player)) }

        // 死んだ人にタイトルを表示
        player.addPotionEffect(PotionEffect(PotionEffectType.BLINDNESS,20,1,false,false,false))
        // 死亡メッセージを削除
        event.deathMessage = null
    }

    /**
     * 夜は近くの人としか喋れない、また死亡者同士の死亡者チャットなどのの処理
     */
    @Suppress("unused")
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    fun onChat(event: AsyncPlayerChatEvent) {
        if(!event.player.isAlive) {
            event.isCancelled = true
            val format = languages("death_chat_format", "%name%" to "%1\$s", "%message%" to "%2\$s")
            spectatePlayers().forEach { player ->
                player.sendMessage(String.format(format, event.player.displayName, event.message))
            }
        } else {
            if(event.message.startsWith("!") || event.message.startsWith("！")) {
                event.isCancelled = true
                // if:人狼チャットを使用しようとしている場合
                if(event.player.role==Role.WOLF) {
                    val format = languages("wolf_chat_format", "%name%" to event.player.displayName, "%message%" to event.message)
                    alivePlayers().filter { it.role == Role.WOLF }.forEach { player ->
                        // 人狼グルチャを全人狼に送信
                        player.sendMessage(format)
                        // 通知音を鳴らす
                        player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f)
                    }
                    // スペクテイターにも送信
                    spectatePlayers().forEach { player -> player.sendMessage(format) }
                } else {
                    // if:人狼じゃない場合は!から始まるチャットを使用できない旨を知らせる。
                    event.player.sendMessage(languages("messages.cant_start_with_ex").asPrefixed())
                }
            } else {
                // 遺言を設定
                event.player.will = event.message
                // チャットのフォーマットを設定
                event.format = languages("chat_format", "%name%" to event.player.displayName, "%message%" to event.message)
            }
        }
    }

    /**
     * 体力の自然回復を無効化。
     */
    @Suppress("unused")
    @EventHandler
    fun onRegainHealth(event: EntityRegainHealthEvent) {
        val player = event.entity
        if(player !is Player) { return }
        when(event.regainReason) {
            EntityRegainHealthEvent.RegainReason.MAGIC, EntityRegainHealthEvent.RegainReason.MAGIC_REGEN -> {}
            else -> {
                event.isCancelled = true
            }
        }
    }

    /**
     * アイテムをドロップできないようにする
     * TODO 特定のアイテムのみドロップできないようにする
     */
    @Suppress("unused")
    @EventHandler
    fun onDrop(event: PlayerDropItemEvent) {
        val droppable = event.itemDrop.itemStack.getContainerValue(Keys.ITEM_DROPPABLE,BooleanDataType)?:true
        if(!droppable) {
            event.isCancelled = true
        }
    }

    /**
     * プレイヤーが途中抜けした際にそのプレイヤーを死体にする
     */
    @Suppress("unused")
    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        if(event.player.isAlive) {
            //if: プレイヤーが生きている場合
            // 途中抜けしたプレイヤーの下を生成し、その上発見させる。
            DeadBody(event.player).found(event.player)
            // ゲームモードをスペクテイターに
            event.player.gameMode = GameMode.SPECTATOR
        }
        PLAYER_BY_ENTITY_ID.remove(event.player.entityId)
        DeadBody.CARRYING.remove(event.player)
    }

    /**
     * プレイヤーが参加した際に実行中だった場合スペクテイターにし、大気中だった場合はボスバーやサイドバーを表示する
     */
    @Suppress("unused")
    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val player = event.player
        PLAYER_BY_ENTITY_ID[player.entityId] = player
        // 参加メッセージを"人狼に参加しました"に変更
        event.joinMessage = languages("messages.player_joined", "%player%" to player.name).asPrefixed()
        BOSSBAR.addPlayer(player)
        // ゲームが実行中かどうか
        if(WereWolf3.isRunning) {
            // if:実行中だった場合
            // ｷﾗﾘｰﾝを鳴らす
            player.playSound(player,Sound.ENTITY_EXPERIENCE_ORB_PICKUP,1f,1f)
            // 実行中であるため最後まで提起する必要であるという旨を表示
            player.sendTitle(languages("name"), languages("messages.please_wait_for_end"), 0, 100, 20)
            // スペクテイターに
            player.gameMode = GameMode.SPECTATOR
            // なめに取り消し線
            player.setDisplayName("${ChatColor.STRIKETHROUGH}${player.name}")
            // プレイヤーにサイドバーを表示
            player.sidebar = DeathSidebar(player)
        } else {
            event.player.gameMode = GameMode.ADVENTURE
            // if:実行中ではない場合
            // プレイヤーにサイドバーを表示
            player.sidebar = WaitingSidebar()
            joinedPlayers().forEach { p ->
                val sidebar = p.sidebar
                if(sidebar !is WaitingSidebar) { return@forEach }
                sidebar.players(joinedPlayers().size)
            }
        }
    }

    @Suppress("unused")
    @EventHandler
    fun onDamage(event: EntityDamageEvent) {
        val player = event.entity
        if(player !is Player) { return }
        player.removePotionEffect(PotionEffectType.INVISIBILITY)
        if(STATUS == GameStatus.RUNNING) { return }
        event.isCancelled = true
    }
}