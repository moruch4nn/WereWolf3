package dev.mr3n.werewolf3

import dev.mr3n.werewolf3.roles.Role
import org.bukkit.GameMode
import org.bukkit.OfflinePlayer

class PlayerData(player: OfflinePlayer) {
    val uniqueId = player.uniqueId
    val name = player.name!!
    var role: Role? = null
    var co: Role? = null
    var will: String? = null
    val offlinePlayer: OfflinePlayer = player
    val kills = mutableSetOf<PlayerData>()
    val isAlive: Boolean
        get() = (offlinePlayer.player?.gameMode?:GameMode.SPECTATOR) != GameMode.SPECTATOR

    override fun equals(other: Any?): Boolean {
        return other is PlayerData && other.uniqueId == this.uniqueId
    }

    override fun hashCode(): Int { return uniqueId.hashCode() }
}