package dev.mr3n.werewolf3

import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import com.comphenix.protocol.wrappers.EnumWrappers
import dev.moru3.minepie.Executor.Companion.runTaskTimerAsync
import dev.moru3.minepie.config.Config
import dev.mr3n.werewolf3.GameStatus.*
import dev.mr3n.werewolf3.commands.EndCommand
import dev.mr3n.werewolf3.commands.ShopCommand
import dev.mr3n.werewolf3.commands.StartCommand
import dev.mr3n.werewolf3.items.IShopItem
import dev.mr3n.werewolf3.protocol.*
import dev.mr3n.werewolf3.roles.Role
import dev.mr3n.werewolf3.sidebar.ISideBar.Companion.sidebar
import dev.mr3n.werewolf3.sidebar.RunningSidebar
import dev.mr3n.werewolf3.sidebar.StartingSidebar
import dev.mr3n.werewolf3.sidebar.WaitingSidebar
import dev.mr3n.werewolf3.utils.hasObstacleInSightPath
import dev.mr3n.werewolf3.utils.languages
import dev.mr3n.werewolf3.utils.role
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.GameRule
import org.bukkit.Location
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.java.JavaPlugin

class WereWolf3: JavaPlugin() {
    override fun onEnable() {
        CONFIG.getKeys(true)
        LANGUAGES_CONFIG.getKeys(true)
        ITEMS_CONFIG.getKeys(true)
        // ゲームルールを設定
        Bukkit.getWorlds()[0].apply {
            setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false)
            setGameRule(GameRule.DO_WEATHER_CYCLE, false)
            setGameRule(GameRule.KEEP_INVENTORY, true)
            setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false)
            setGameRule(GameRule.DO_MOB_SPAWNING, false)
        }
        // いつもの
        Bukkit.getPluginManager().registerEvents(PlayerListener,this)
        // すでにサーバーにいるプレイヤーのjoin eventを発生させる(初期化用)
        Bukkit.getOnlinePlayers().forEach { PlayerListener.onJoin(PlayerJoinEvent(it,null)) }
        // /start コマンドの登録
        this.getCommand("start")?.also {
            it.setExecutor(StartCommand)
            it.tabCompleter = StartCommand
        }
        // /end コマンドの登録
        this.getCommand("end")?.also {
            it.setExecutor(EndCommand)
            it.tabCompleter = EndCommand
        }
        // /shop コマンドの登録
        this.getCommand("shop")?.also {
            it.setExecutor(ShopCommand)
            it.tabCompleter = ShopCommand
        }
        // >>> クラスの初期化 >>>
        IShopItem.ShopItem.Companion
        SpectatorPacketUtil.init()
        GameTerminator.init()
        Role.ROLES
        DeadBody.DEAD_BODIES
        TeamPacketUtil
        Time.MORNING.title
        Time.NIGHT.title
        // <<< クラスの初期化 <<<

        // >>> プレイヤー間に障害物がある場合プレイヤーを透明にする処理:非同期 >>>
        this.runTaskTimerAsync(3, 3) {
            when(STATUS) {
                // if:ゲームが実行中の場合のみ実行
                RUNNING, STARTING -> {
                    // for:ゲームに参加しているすべてのプレイヤー
                    PLAYERS.forEach { player ->
                        // プレイヤー同士が見えている場合
                        PLAYERS.forEach s@{ player2 ->
                                if(player2 == player) { return@s }
                                if(player2.gameMode == GameMode.SPECTATOR) { return@s }
                                // 人狼同士は離れてもお互いが見えるように
                                if (player.role == Role.WOLF && player2.role == Role.WOLF) { return@s }
                                // プレイヤー間に障害物があるかどうか。ある場合はtrueなのでfilterNotでfalseのみ残す
                                if(player.hasObstacleInSightPath(player2)) {
                                    // if:プレイヤーとの間に障害物がある場合
                                    InvisibleEquipmentPacketUtil.add(player, player2, 0, *EnumWrappers.ItemSlot.values())
                                    MetadataPacketUtil.addToInvisible(player, player2)
                                } else {
                                    // if:プレイヤーとの間に障害物がない場合
                                    InvisibleEquipmentPacketUtil.remove(player, player2, 0)
                                    MetadataPacketUtil.removeFromInvisible(player, player2)
                                }
                            }
                    }
                }
                else -> {}
            }
        }
        // <<< プレイヤー間に障害物がある場合プレイヤーを透明にする処理:非同期 <<<

        // 毎tickループ
        TickTask.task { loopCount ->
            when(STATUS) {
                // 待機中にループする処理
                WAITING -> {
                    // 点滅速度
                    if(loopCount % Constants.POINT_FLUSH_SPEED!=0) {
                        // ...の.の数を計算
                        val loadingDots = ".".repeat((loopCount%(Constants.POINT_FLUSH_SPEED*4))/ Constants.POINT_FLUSH_SPEED)
                        // bossbarに...のアニメーションを追加
                        BOSSBAR.setTitle(languages("messages.please_wait_for_start") +loadingDots)
                        PLAYERS.forEach { player ->
                            val sidebar = player.sidebar
                            // プレイヤーのサイドバーがWaitingSidebarの場合
                            if(sidebar is WaitingSidebar) {
                                // 待機中l...の..にアニメーションを付与
                                sidebar.status(languages("sidebar.global.status.waiting") +loadingDots)
                            }
                        }
                    }
                }
                STARTING -> {
                    // 残り時間を減らす
                    TIME_LEFT--
                    PLAYERS.forEach { player ->
                        val sidebar = player.sidebar
                        // プレイヤーのサイドバーがStartingSidebarではない場合はreturn
                        if(sidebar !is StartingSidebar) { return@forEach }
                        // サイドバーの推定プレイヤー数を更新
                        sidebar.players(PLAYERS.size)
                        // サイドバーの残り時間を更新
                        sidebar.time(TIME_LEFT/20)
                    }
                    // 準備時間が終わったらゲーム開始
                    if(TIME_LEFT<=0) { GameInitializer.run() }
                }
                RUNNING -> {
                    GameRunner.running(loopCount = loopCount)
                }
                ENDING -> {}
            }
        }
    }

    override fun onDisable() {
        // ゲームが起動中の場合停止
        if(running) {
            GameTerminator.run(true)
        }
        // ボスバーを削除
        BOSSBAR.removeAll()
        // すべての死体を削除
        DeadBody.DEAD_BODIES.forEach { it.destroy() }
    }

    // インスタンスを公開変数に保存する
    init { INSTANCE = this }

    companion object {
        // 上に常時表示しているボスバー
        val BOSSBAR: BossBar by lazy { Bukkit.createBossBar(languages("messages.please_wait_for_start"), BarColor.RED, BarStyle.SOLID) }
        // 現在実行中のゲームID
        var GAME_ID: String? = null
        // 現在のゲームステータス
        var STATUS: GameStatus = WAITING
        // 現在のターン(昼/夜)の残り時間。/ゲーム全体残り時間ではありません。
        var TIME_LEFT = 0
        // 残り時間の長さ(カウントが減らされる前の長さ)
        var PLAYERS_EST = 0
            set(value) {
                PLAYERS.forEach { player ->
                    val sidebar = player.sidebar
                    if(sidebar is RunningSidebar) { sidebar.playersEst(value) }
                }
                field = value
            }
        // このターンのゲームの長さ(時間を示しています。 TIME_LEFT とは違い時間経過ごとに減っていきません
        var GAME_TIME = 0
        // 現在の時刻が何日目かを表します。
        var DAYS: Int = 0
        // ゲームに参加中のプレイヤー
        val PLAYERS = mutableListOf<Player>()
        // EntityID to Playerのマップ
        val PLAYER_BY_ENTITY_ID: MutableMap<Int, Player> = mutableMapOf()
        // サーバーにPlugmanXが導入されているかどうか
        val isPlugmanLoaded: Boolean by lazy { Bukkit.getPluginManager().isPluginEnabled("PlugManX") }

        // 現在の時刻(朝/夜)
        var TIME_OF_DAY: Time = Time.MORNING
            set(time) {
                field = time
                // 朝/夜の変更の処理を実行
                time()
            }

        // PROTOCOL_LIBのマネージャー
        val PROTOCOL_MANAGER: ProtocolManager by lazy { ProtocolLibrary.getProtocolManager() }

        // ゲームが実行中かどうかを true/falseで
        val running: Boolean
            get() = STATUS != WAITING
        // WereWolf3のインスタンス
        lateinit var INSTANCE: WereWolf3
            private set
        // languages.ymlファイル
        val LANGUAGES_CONFIG: FileConfiguration by lazy { Config(INSTANCE,"languages.yml").apply{saveDefaultConfig()}.config()!! }
        // config.ymlファイル
        val CONFIG: FileConfiguration by lazy { Config(INSTANCE,"config.yml").apply{saveDefaultConfig()}.config()!! }
        val ITEMS_CONFIG: FileConfiguration by lazy { Config(INSTANCE, "items.yml").apply{saveDefaultConfig()}.config()!! }

        /**
         * 人狼を開始する関数です。
         * locationはスポーン地点。
         */
        fun start(location: Location) {
            GameInitializer.start(location)
        }
    }
}