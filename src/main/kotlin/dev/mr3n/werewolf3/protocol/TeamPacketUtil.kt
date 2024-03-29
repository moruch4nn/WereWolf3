package dev.mr3n.werewolf3.protocol

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.utility.MinecraftReflection
import com.comphenix.protocol.wrappers.WrappedChatComponent
import dev.mr3n.werewolf3.Constants
import dev.mr3n.werewolf3.PROTOCOL_MANAGER
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import java.util.*

object TeamPacketUtil {
    private val TEAMS = mutableMapOf<Player, MutableMap<ChatColor, MutableList<String>>>()

    /**
     * 色用のチームを作成するパケットです。
     */
    fun createTeamColorPacket(color: ChatColor): PacketContainer {
        // チーム作成のためのパケットを生成
        val packet = PROTOCOL_MANAGER.createPacket(PacketType.Play.Server.SCOREBOARD_TEAM)
        // チーム名を設定。この場合は色の名前にしています。
        packet.strings.write(0,color.name)
        // 操作内容は0、つまりチームの新規作成
        packet.integers.write(0,0)
        // チームの情報格納用のos
        val internalStructure = packet.optionalStructures.read(0).get()
        // チームの表示名を設定。個々の場合は色の名前
        internalStructure.chatComponents.write(0, WrappedChatComponent.fromText(color.name))
        // 当たり判定やネームタグの表示/非表示を設定。
        internalStructure.integers.write(0,0x01)
        internalStructure.strings.write(0,"always")
        internalStructure.strings.write(1,"never")
        // チームの色を設定
        internalStructure.getEnumModifier(ChatColor::class.java, MinecraftReflection.getMinecraftClass("EnumChatFormat")).write(0,color)
        // 作成した情報をパケットに収納
        packet.optionalStructures.write(0, Optional.of(internalStructure))
        // チームのプレイヤー一覧を格納
        packet.getSpecificModifier(Collection::class.java).write(0, listOf<String>())
        return packet
    }

    fun sendTeamJLPacket(player: Player, color: ChatColor, entities: Collection<String>, operation: Int) {
        if(player.name.startsWith(Constants.BE_PREFIX, true)) { return }
        // パケットを作成
        val packet = PROTOCOL_MANAGER.createPacket(PacketType.Play.Server.SCOREBOARD_TEAM)
        packet.modifier.writeDefaults()
        // チーム名を指定
        packet.strings.write(0,color.name)
        // 操作を4，つまりエンティティの削除に設定
        packet.integers.write(0,operation)
        // 追加するプレイヤーを格納
        packet.getSpecificModifier(Collection::class.java).write(0,entities)
        // パケットを送信
        PROTOCOL_MANAGER.sendServerPacket(player, packet)
    }

    /**
     * チームのメンバーを設定できる関数です。
     */
    fun add(player: Player,color: ChatColor,players: Collection<Player>) {
        val members = TEAMS[player]?: mutableMapOf()
        TEAMS[player]?.forEach { (color, members1) ->
            val filteredMembers = players.filter { members1.contains(it.name) }
            if(filteredMembers.isNotEmpty()) { remove(player, color, filteredMembers.map { it.name }) }
        }
        val playerList = members[color]?: mutableListOf()
        playerList.addAll(players.map { it.name })
        members[color] = playerList
        TEAMS[player] = members
        sendTeamJLPacket(player, color, players.map { it.name }, 3)
    }

    /**
     * チームからプレイヤーを削除数パケットです。
     */
    fun remove(player: Player, color: ChatColor, entities: Collection<String>) {
        val teams = TEAMS[player]?.get(color)?: return
        teams.removeAll(entities)
        TEAMS[player]?.put(color, teams)
        sendTeamJLPacket(player, color, entities, 4)
    }

    val colours = listOf(
        ChatColor.DARK_RED,
        ChatColor.RED,
        ChatColor.GOLD,
        ChatColor.YELLOW,
        ChatColor.DARK_GREEN,
        ChatColor.GREEN,
        ChatColor.AQUA,
        ChatColor.DARK_AQUA,
        ChatColor.DARK_BLUE,
        ChatColor.BLUE,
        ChatColor.LIGHT_PURPLE,
        ChatColor.DARK_PURPLE,
        ChatColor.WHITE,
        ChatColor.GRAY,
        ChatColor.DARK_GRAY,
        ChatColor.BLACK
    )

    /**
     * チームからプレイヤーを削除数パケットです。
     */
    fun removeAll(player: Player, color: ChatColor) {
        remove(player, color, TEAMS[player]?.get(color)?: listOf())
    }

    /**
     * チームを削除するパケットです。
     */
    fun removeTeam(player: Player, color: ChatColor) {
        // パケットを作成
        val packet = PROTOCOL_MANAGER.createPacket(PacketType.Play.Server.SCOREBOARD_TEAM)
        // 削除するチーム名を指定
        packet.strings.write(0,color.name)
        // 操作を１，つまりチームの削除に設定
        packet.integers.write(0,1)
        // パケットを送信
        PROTOCOL_MANAGER.sendServerPacket(player, packet)
    }
}
