package dev.mr3n.werewolf3.utils

import dev.mr3n.werewolf3.Constants
import dev.mr3n.werewolf3.Keys
import dev.mr3n.werewolf3.datatypes.RoleDataType
import dev.mr3n.werewolf3.events.WereWolf3DamageEvent
import dev.mr3n.werewolf3.items.Currency
import dev.mr3n.werewolf3.roles.Role
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType

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

val Player.isBE: Boolean
    get() = this.name.startsWith(Constants.BE_PREFIX)

var Player.kills: IntArray?
    get() = this.persistentDataContainer.get(Keys.KILLS, PersistentDataType.INTEGER_ARRAY)
    set(value) {
        if(value==null) { this.persistentDataContainer.remove(Keys.KILLS) } else { this.persistentDataContainer.set(Keys.KILLS, PersistentDataType.INTEGER_ARRAY, value) }
    }

fun Player.addKill(target: Player) {
    val kills = this.kills?.toMutableSet()?:mutableSetOf()
    kills.add(target.entityId)
    this.kills = kills.toIntArray()
}

var Player.role: Role?
    get() = this.persistentDataContainer.get(Keys.ROLE, RoleDataType)
    set(value) {
        if(value==null) {
            val role = this.role
            if(role!=null) {
                val list = Role.ROLES[role]?.toMutableList()?: mutableListOf()
                list.remove(this.uniqueId)
                Role.ROLES[role] = list
            }
            this.persistentDataContainer.remove(Keys.ROLE)
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
            this.persistentDataContainer.set(Keys.ROLE, RoleDataType, value)
        }
    }

var Player.co: Role?
    get() = this.persistentDataContainer.get(Keys.CO, RoleDataType)
    set(value) {
        if(value==null) { this.persistentDataContainer.remove(Keys.CO) } else { this.persistentDataContainer.set(Keys.CO, RoleDataType, value) }
    }

var Player.will: String?
    get() = this.persistentDataContainer.get(Keys.PLAYER_WILL, PersistentDataType.STRING)
    set(value) {
        if(value==null) { this.persistentDataContainer.remove(Keys.PLAYER_WILL) } else { this.persistentDataContainer.set(Keys.PLAYER_WILL, PersistentDataType.STRING, value) }
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
    get() = this.gameMode != GameMode.SPECTATOR

/**
 * 観戦中のプレイヤー一覧
 */
fun spectatePlayers(): Collection<Player> = joinedPlayers().filter { it.gameMode == GameMode.SPECTATOR }

/**
 * 生きているプレイヤー一覧
 */
fun alivePlayers(): Collection<Player> = joinedPlayers().filter { it.gameMode != GameMode.SPECTATOR }

/**
 * ゲームに参加しているプレイヤー一覧
 */
fun joinedPlayers(): Collection<Player> = Bukkit.getOnlinePlayers()