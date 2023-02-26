package dev.mr3n.werewolf3.utils

import dev.mr3n.werewolf3.Constants
import dev.mr3n.werewolf3.PLAYERS
import dev.mr3n.werewolf3.PlayerData
import dev.mr3n.werewolf3.events.WereWolf3DamageEvent
import dev.mr3n.werewolf3.items.Currency
import dev.mr3n.werewolf3.roles.Role
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.util.*

private val playerDataList = mutableMapOf<UUID,PlayerData>()

fun Player.damageTo(target: Player, damage: Double) {
    // ダメージが0だったらreturn
    if(damage == 0.0) { return }
    // WereWolf3ダメージイベントを発火
    val event = WereWolf3DamageEvent(target, damage)
    Bukkit.getPluginManager().callEvent(event)
    // イベントがキャンセルされている場合はreturn
    if(event.isCancelled) { return }
    // プレイヤーの体力
    val health = target.health
    // プレイヤーの体力に+1する
    target.health = minOf(target.healthScale, health + 1)
    // playerからtargetに1ダメージを与えて上の+1をなかったことにする
    target.damage(1.0,this)
    // ダメージが0未満だった場合体力を0に(殺す)
    if(event.damage < 0) {
        target.health = 0.0
    } else {
        target.health = maxOf(.0, health - event.damage)
    }
}

val OfflinePlayer.isBE: Boolean
    get() = this.playerData.name.startsWith(Constants.BE_PREFIX)

val OfflinePlayer.kills: MutableSet<PlayerData>
    get() = this.playerData.kills

val OfflinePlayer.playerData: PlayerData
    get() = playerDataList.getOrPut(this.uniqueId) { PlayerData(this) }

var OfflinePlayer.role: Role?
    get() = this.playerData.role
    set(value) {
        if(value==null) {
            val role = this.role
            if(role!=null) {
                val list = Role.ROLES[role]?.toMutableList()?: mutableListOf()
                list.remove(this.uniqueId)
                Role.ROLES[role] = list
            }
            this.playerData.role = null
        } else {
            val role = this.role
            if(role!=null) {
                val old = Role.ROLES[role]?.toMutableList()?: mutableListOf()
                old.remove(this.uniqueId)
                Role.ROLES[role] = old
            }
            val new = Role.ROLES[value]?.toMutableList()?: mutableListOf()
            new.add(this.uniqueId)
            Role.ROLES[value] = new
            this.playerData.role = value
        }
    }

var OfflinePlayer.co: Role?
    get() = this.playerData.co
    set(value) {
        this.playerData.co = value
    }

var OfflinePlayer.will: String?
    get() = this.playerData.will
    set(value) {
        this.playerData.will = value
    }

var Player.money: Int
    get() = this.inventory.filterNotNull().filter { Currency.isSimilar(it) }.sumOf { it.amount }
    set(value) {
        val field = this.money
        val diff = value - field
        val currencies = Currency.itemStack
        if(diff > 0) {
            currencies.amount = diff
            this.inventory.addItem(currencies)
        } else {
            currencies.amount = -diff
            this.inventory.removeItem(currencies)
        }
    }

val Player.isAlive: Boolean
    get() = this.playerData.isAlive

/**
 * 観戦中のプレイヤー一覧
 */
fun spectatePlayers(): Collection<Player> = joinedPlayers().filterNot { it.isAlive }

/**
 * 生きているプレイヤー一覧
 */
fun alivePlayers(): Collection<Player> = joinedPlayers().filter { it.isAlive }

/**
 * ゲームに参加しているプレイヤー一覧
 */
fun joinedPlayers(): Collection<Player> = PLAYERS.mapNotNull { it.player }