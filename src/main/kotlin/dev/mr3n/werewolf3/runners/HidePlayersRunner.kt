package dev.mr3n.werewolf3.runners

import com.comphenix.protocol.wrappers.EnumWrappers
import dev.mr3n.werewolf3.GameStatus
import dev.mr3n.werewolf3.protocol.InvisibleEquipmentPacketUtil
import dev.mr3n.werewolf3.protocol.MetadataPacketUtil
import dev.mr3n.werewolf3.roles.Role
import dev.mr3n.werewolf3.utils.LoopProcess
import dev.mr3n.werewolf3.utils.alivePlayers
import dev.mr3n.werewolf3.utils.hasObstacleInSightPath
import dev.mr3n.werewolf3.utils.role
import org.bukkit.GameMode

object HidePlayersRunner: LoopProcess(1L,1L, true, GameStatus.STARTING, GameStatus.RUNNING) {
    override fun run() {
        // for:ゲームに参加しているすべてのプレイヤー
        alivePlayers().forEach { player ->
            // プレイヤー同士が見えている場合
            alivePlayers().forEach s@{ player2 ->
                if(player2 == player) { return@s }
                if(player.gameMode == GameMode.SPECTATOR) { return@s }
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
}