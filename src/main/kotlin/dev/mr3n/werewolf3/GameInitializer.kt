package dev.mr3n.werewolf3

import dev.moru3.minepie.item.EasyItem
import dev.mr3n.werewolf3.discord.VOICE_CHANNEL
import dev.mr3n.werewolf3.discord.connectTo
import dev.mr3n.werewolf3.items.DefaultBow
import dev.mr3n.werewolf3.items.DefaultSword
import dev.mr3n.werewolf3.items.quickchat.TrustYou
import dev.mr3n.werewolf3.items.quickchat.WithYou
import dev.mr3n.werewolf3.items.quickchat.YouAreWolf
import dev.mr3n.werewolf3.items.shop.OpenShop
import dev.mr3n.werewolf3.protocol.DeadBody
import dev.mr3n.werewolf3.protocol.MetadataPacketUtil
import dev.mr3n.werewolf3.protocol.TeamPacketUtil
import dev.mr3n.werewolf3.roles.Role
import dev.mr3n.werewolf3.sidebar.ISideBar.Companion.sidebar
import dev.mr3n.werewolf3.sidebar.RunningSidebar
import dev.mr3n.werewolf3.sidebar.StartingSidebar
import dev.mr3n.werewolf3.utils.*
import org.bukkit.*
import org.bukkit.entity.Player
import java.util.*

object GameInitializer {

    private val RANDOM = Random(System.currentTimeMillis())

    init {
        // Javaの乱数はnextDoubleをしたあとだとある程度偏りが軽減されるためnextDoubleを10回実行
        repeat(10) { RANDOM.nextDouble() }
    }

    /**
     * ゲームを初期化し、試合を開始します。
     */
    fun start(location: Location, vararg ignores: Player) {
        if(WereWolf3.isRunning) { return }
        // 死体を全削除
        DeadBody.DEAD_BODIES.forEach { it.destroy() }
        // ゲームIDを設定。
        GAME_ID = UUID.randomUUID().toString()
        // ゲームのステータスを設定、
        STATUS = GameStatus.STARTING
        location.world?.apply {
            setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
            setGameRule(GameRule.DO_WEATHER_CYCLE, false)
            setGameRule(GameRule.KEEP_INVENTORY, true)
            setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false)
            setGameRule(GameRule.DO_MOB_SPAWNING, false)
            setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true)
        }
        // 時間を朝に
        location.world!!.time = 0L
        // 日にちを0に設定
        DAYS = 0
        // 参加プレイヤー一覧。
        val players = Bukkit.getOnlinePlayers().toMutableList()
        players.removeAll(ignores.toSet())
        // プレイヤー人数から役職数を推定してリストに格納。 roleList.length == players.length
        val roleList = Role.values().map { role -> MutableList(role.calc(players.size)) { role } }.flatten().shuffled(RANDOM).shuffled().shuffled().shuffled()
        // 推定プレイヤー数を参加人数に設定(死亡確認時に減らしていく)
        // 役職リストとプレイヤーのリストを合体してfor
        players.zip(roleList).toMap().forEach { (player, role) ->
            BOSSBAR.addPlayer(player)
            // プレイヤーの役職を設定。
            player.role = role
            player.co = null
            // 開始場所にテレポート。
            val tc = (0..100)
            player.teleport(location.clone().add(tc.random()/100.0,0.0,tc.random()/100.0))
            // タイトルに自分の役職を表示。
            player.sendTitle(languages("title.start.title", "%time%" to (Constants.STARTING_TIME / 20), "%role%" to "${role.color}${ChatColor.BOLD}${role.displayName}"),languages("title.start.subtitle", "%time%" to Constants.STARTING_TIME / 20, "%role%" to "${role.color}${ChatColor.BOLD}${role.displayName}"), 0, 100, 20)
            // 怖い音を鳴らす。
            repeat(10) { player.playSound(player,Sound.AMBIENT_NETHER_WASTES_MOOD,1F,1F) }
            player.sidebar = StartingSidebar(player)
            player.flySpeed = 0.2f
            player.walkSpeed = 0.2f
            player.health = player.healthScale
            TeamPacketUtil.add(player,ChatColor.WHITE,players)
            player.setDisplayName(player.name)
            player.setPlayerListName(player.name)
            player.gameMode = GameMode.ADVENTURE
            player.kills.clear()
            player.inventory.contents.filterNotNull().forEach { it.amount = 0 }
            player.conversationalDistance(100,-1.0)

            // Discordのボイスチャットに接続する
            player.connectTo(VOICE_CHANNEL)

            TeamPacketUtil.colours.forEach { color ->
                TeamPacketUtil.removeTeam(player, color)
                PROTOCOL_MANAGER.sendServerPacket(player, TeamPacketUtil.createTeamColorPacket(color))
            }
        }
        PLAYERS.addAll(players.map { it.playerData })
        // 時間を設定
        TIME_LEFT = Constants.STARTING_TIME

        val wolfs = players.filter { it.role==Role.WOLF }

        wolfs.forEach { player ->
            wolfs.forEach { wolf ->
                // 人狼チームからは身内が発光しているように
                MetadataPacketUtil.addToGlowing(player,wolf)
            }
            // 人狼チームからは身内が赤く見えるように
            TeamPacketUtil.add(player,ChatColor.DARK_RED,wolfs)
            player.sendMessage(languages("messages.wolfs", "%wolfs%" to wolfs.joinToString(" ") { it.name }).asPrefixed())
            player.sendMessage(languages("messages.how_to_speak_in_wolf_group"))
        }
    }

    /**
     * 役職発表など準備完了後に行う処理
     */
    fun run() {
        val wolfs = alivePlayers().filter { it.role?.team == Role.Team.WOLF }
        alivePlayers().forEach {  player ->
            // アイテム配布時に人狼アイテムとかを持たないようにスロットを0に設定
            player.inventory.heldItemSlot = 0
            // ショップを開くアイテム
            player.inventory.setItem(8, OpenShop.itemStack)
            // クイックチャットのアイテムを渡す。
            player.inventory.setItem(5, WithYou.itemStack)
            player.inventory.setItem(6, TrustYou.itemStack)
            player.inventory.setItem(7, YouAreWolf.itemStack)
            // 木の剣を渡す。
            player.inventory.addItem(DefaultSword.itemStack)

            // 弓を渡す。
            player.inventory.addItem(DefaultBow.itemStack) // 矢を渡す。
            player.inventory.addItem(EasyItem(Material.ARROW))
            Role.values().forEachIndexed { index, role -> player.inventory.setItem(9+index, role.helmet) }
            // 人狼チームと村人チームの数を公表
            player.sendMessage(languages("title.start.messages.info", "%wolf_teams%" to wolfs.size, "%villager_teams%" to alivePlayers().size - wolfs.size))
            player.sidebar = RunningSidebar(player)
            player.role?.items?.map { it.itemStack }?.forEach { player.inventory.addItem(it) }
            player.money = Constants.INITIAL_FUNDS
        }
        STATUS = GameStatus.RUNNING
        TIME_OF_DAY = Time.MORNING
    }
}