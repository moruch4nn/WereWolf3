package dev.mr3n.werewolf3

import dev.mr3n.werewolf3.discord.SPECTATORS_VOICE_CHANNEL
import dev.mr3n.werewolf3.discord.connectTo
import dev.mr3n.werewolf3.items.IShopItem
import dev.mr3n.werewolf3.protocol.DeadBody
import dev.mr3n.werewolf3.protocol.MetadataPacketUtil
import dev.mr3n.werewolf3.protocol.TeamPacketUtil
import dev.mr3n.werewolf3.roles.Role
import dev.mr3n.werewolf3.sidebar.ISideBar.Companion.sidebar
import dev.mr3n.werewolf3.sidebar.WaitingSidebar
import dev.mr3n.werewolf3.utils.*
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.hover.content.Text
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Sound

object GameTerminator {

    /**
     * 結果発表とともにゲームを終了する際に使用します。
     */
    fun end(win: Role.Team, reason: String) {
        if(STATUS==GameStatus.ENDING||STATUS==GameStatus.WAITING) { return }
        STATUS = GameStatus.ENDING
        // 役職 to プレイヤー一覧 のマップ
        val players = Role.values().map { it to it.players }
        joinedPlayers().forEach { player ->
            // どちらサイドが勝利したかをタイトルで表示
            player.sendTitle(languages("title.win.title", "%role%" to win.displayName, "%color%" to win.color), reason, 20, 100, 20)
            // ﾋﾟﾛﾘｰﾝ
            player.playSound(player, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f)
            // 結果発表のヘッダー
            player.sendMessage("${languages("messages.result.prefix")}${languages("messages.result.header")}")
            players.forEach s@{  (role, players) ->
                // 役職にプレイヤーが一人も属していない場合は内訳の表示をスキップ
                if(players.isEmpty()) { return@s }
                val textComponents = TextComponent("${role.color}${role.displayName}:")
                players.forEach { player ->
                        // 身内を殺した回数
                        val killTeams = player.kills.count { it.role?.team == role.team }
                        // プレイヤー名にホバーした際に身内キル/敵キルのなどの内訳を表示
                        val hover = HoverEvent(HoverEvent.Action.SHOW_TEXT, Text(languages("messages.result.kills_info", "%kills%" to player.kills.size-killTeams, "%kill_teams%" to killTeams)))
                        // テキスト化
                        val component = TextComponent("${role.color}${player.name}(${if(player.kills.isEmpty()) "0" else "${ChatColor.UNDERLINE}${player.kills.size}${role.color}"})")
                        // ホバーした歳のイベントを設定
                        component.hoverEvent = hover
                        // プレイヤー名の間に空白を挿入
                        textComponents.addExtra(" ")
                        // それをメインのテキストに追加
                        textComponents.addExtra(component)
                    }
                player.spigot().sendMessage(textComponents)
            }
            player.sendMessage("${languages("messages.result.prefix")}${languages("messages.result.header")}")
            player.sendMessage(languages("messages.result.winner", "%team%" to win.displayName, "%color%" to win.color).asPrefixed())
        }
        this.run()
    }

    /**
     * 結果発表を無視して強制的にゲームを終了する際に使用します。
     */
    fun run() {
        try {
            STATUS = GameStatus.WAITING
            GAME_ID = null
        } catch(_: Exception) { }

        // 死体を全削除
        try { DeadBody.DEAD_BODIES.forEach { it.destroy() } } catch(_: Exception) {}

        try { IShopItem.ShopItem.ITEMS.forEach { it.onEnd() } } catch(_: Exception) { }

        try {
            joinedPlayers().forEach { player -> player.connectTo(SPECTATORS_VOICE_CHANNEL) }
        } catch(_: Exception) { }

        try {
            joinedPlayers().forEach { player ->
                // 人狼ゲーム関係のデータを削除
                player.kills.clear()
                player.role = null
                player.co = null
                player.will = null
                // インベントリを削除
                player.inventory.clear()

            }
        } catch(e: Exception) { e.printStackTrace() }
        try {
            joinedPlayers().forEach { player -> TeamPacketUtil.colours.forEach { color -> TeamPacketUtil.removeTeam(player, color) } }
        } catch(_: Exception) {}
        try {
            joinedPlayers().forEach { player ->
                // プレイヤーの会話可能範囲の制限をなくす
                player.clearConversationalDistance()
                // サイドバーを待機中のものに変更
                try { player.sidebar = WaitingSidebar() } catch(e: Exception) { e.printStackTrace() }
                player.gameMode = GameMode.ADVENTURE
                // co帽子などで表示名が変わっている場合は戻す
                player.setDisplayName(player.name)
                player.setPlayerListName(player.name)
                player.world.time = 8000
                player.flySpeed = 0.2f
                player.walkSpeed = 0.2f
                // チームの色を削除
                try { TeamPacketUtil.removeAll(player, ChatColor.DARK_RED,) } catch(e: Exception) { e.printStackTrace() }
                // パケットで発光、透明化を送信していた場合は削除
                try { MetadataPacketUtil.removeAllInvisible(player) } catch(e: Exception) { e.printStackTrace() }
                try { MetadataPacketUtil.removeAllGlowing(player) } catch(e: Exception) { e.printStackTrace() }
                player.activePotionEffects.forEach { player.removePotionEffect(it.type) }
                // >>> バグって動かないようにちょっとずらしてスポーン地点にてレポート >>>
                val tc = (0..100)
                player.teleport(player.world.spawnLocation.clone().add(tc.random()/100.0,0.0,tc.random()/100.0))
                // <<< バグって動かないようにちょっとずらしてスポーン地点にてレポート <<<
            }
        } catch(e: Exception) { e.printStackTrace() }

        try {
            PLAYERS.clear()
            FOUNDED_PLAYERS.clear()
        } catch(_: Exception) {}

        // プラグインをreload
        // if(!shutdown) { if(isPlugmanLoaded) { PluginUtil.reload(WereWolf3.INSTANCE) } else { Bukkit.getServer().reload() } }
    }

    fun init() {

    }
}