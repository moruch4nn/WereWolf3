package dev.mr3n.werewolf3

import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import dev.moru3.minepie.config.Config
import dev.mr3n.werewolf3.GameStatus.WAITING
import dev.mr3n.werewolf3.commands.EndCommand
import dev.mr3n.werewolf3.commands.ShopCommand
import dev.mr3n.werewolf3.commands.StartCommand
import dev.mr3n.werewolf3.items.IShopItem
import dev.mr3n.werewolf3.protocol.DeadBody
import dev.mr3n.werewolf3.protocol.SpectatorPacketUtil
import dev.mr3n.werewolf3.protocol.TeamPacketUtil
import dev.mr3n.werewolf3.roles.Role
import dev.mr3n.werewolf3.runners.GameRunner
import dev.mr3n.werewolf3.runners.HidePlayersRunner
import dev.mr3n.werewolf3.runners.StartingRunner
import dev.mr3n.werewolf3.runners.WaitingRunner
import dev.mr3n.werewolf3.utils.joinedPlayers
import dev.mr3n.werewolf3.utils.languages
import org.bukkit.Bukkit
import org.bukkit.GameRule
import org.bukkit.Location
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.BossBar
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.java.JavaPlugin
import java.util.UUID

// 上に常時表示しているボスバー
val BOSSBAR: BossBar by lazy { Bukkit.createBossBar(languages("messages.please_wait_for_start"), BarColor.RED, BarStyle.SOLID) }
// 現在実行中のゲームID
var GAME_ID: String? = null
// 現在のゲームステータス
var STATUS: GameStatus = WAITING
// 現在のターン(昼/夜)の残り時間。/ゲーム全体残り時間ではありません。
var TIME_LEFT = 0
// 残り時間の長さ(カウントが減らされる前の長さ)
// このゲーム全体の長さ/何日 (固定)
var GAME_LENGTH = 0
// 現在の時刻が何日目かを表します。 (一日経つたびに減っていきます)
var DAYS: Int = 0

val PLAYERS = mutableSetOf<PlayerData>()

// 死体が見つかったプレイヤー一覧
var FOUNDED_PLAYERS = mutableSetOf<UUID>()

// EntityID to Playerのマップ
val PLAYER_BY_ENTITY_ID: MutableMap<Int, Player> = mutableMapOf()
// サーバーにPlugmanXが導入されているかどうか
val isPlugmanLoaded: Boolean by lazy { Bukkit.getPluginManager().isPluginEnabled("PlugManX") }

// 現在の時刻(朝/夜)
var TIME_OF_DAY: Time = Time.MORNING
    set(time) {
        field = time
        // 朝/夜の変更の処理を実行
        time.invoke()
    }

// PROTOCOL_LIBのマネージャー
val PROTOCOL_MANAGER: ProtocolManager by lazy { ProtocolLibrary.getProtocolManager() }

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
        // すでにサーバーにいるプレイヤーのjoin eventを発生させる(初期化用)
        joinedPlayers().forEach { PlayerListener.onJoin(PlayerJoinEvent(it,null)) }
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
        GameRunner
        HidePlayersRunner
        StartingRunner
        WaitingRunner
        // <<< クラスの初期化 <<<
    }

    override fun onDisable() {
        // ゲームが起動中の場合停止
        if(isRunning) {
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
        // ゲームが実行中かどうかを true/falseで
        val isRunning: Boolean
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
        fun start(location: Location, vararg ignores: Player) {
            GameInitializer.start(location, *ignores)
        }
    }
}