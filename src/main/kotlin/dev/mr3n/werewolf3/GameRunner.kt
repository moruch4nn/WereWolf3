package dev.mr3n.werewolf3

import dev.mr3n.werewolf3.roles.Role
import dev.mr3n.werewolf3.sidebar.DeathSidebar
import dev.mr3n.werewolf3.sidebar.ISideBar.Companion.sidebar
import dev.mr3n.werewolf3.sidebar.RunningSidebar
import dev.mr3n.werewolf3.utils.*
import org.bukkit.GameMode


object GameRunner {
    /**
     * ゲーム実行中(STATUS == RUNNING)に毎チック実行される関数。
     */
    fun running(loopCount: Int) {
        PLAYERS.forEach { player ->
            // サイドバーの情報を更新する
            val sidebar = player.sidebar
            if(sidebar is RunningSidebar) {
                sidebar.playersEst(PLAYERS_EST)
                sidebar.money(player.money)
            }
            if(sidebar is DeathSidebar) {
                sidebar.players(PLAYERS.count { it.gameMode != GameMode.SPECTATOR })
            }
            if(player.gameMode != GameMode.SPECTATOR) {
                // 30秒おきにお金を追加する
                if (loopCount % (20 * 30) == 0) { player.money += Constants.ADD_MONEY }
            }

            // 残り時間を減らす
            TIME_LEFT--
            // 時間が来たら朝/夜反転
            if(TIME_LEFT <=0) { TIME_OF_DAY = TIME_OF_DAY.next() }
            // ボスバーの進行度を現在の残り時間に合わせる
            BOSSBAR.progress = TIME_LEFT * (1.0 / GAME_LENGTH)
            // ボスバーのタイトルにタイマーを表示
            BOSSBAR.setTitle(languages("bossbar.title","%time%" to TIME_OF_DAY.displayName, "%emoji%" to TIME_OF_DAY.emoji, "%time_left%" to (TIME_LEFT / 20).parseTime()))

            // 生きているプレイヤー一覧(スペクテイターじゃないプレイヤー)
            val alivePlayers = PLAYERS.filter { p->p.gameMode!=GameMode.SPECTATOR }
            if(alivePlayers.count { p->p.role?.team==Role.Team.WOLF }<=0) {
                // 人狼陣営の数が0になった場合ゲームを終了
                GameTerminator.end(Role.Team.VILLAGER, languages("title.win.reason.anni", "%role%" to Role.Team.WOLF.displayName))
            } else if(alivePlayers.count { p->p.role?.team==Role.Team.VILLAGER && p.role != Role.MADMAN }<=0) {
                // 村人陣営(狂人は除く)の数が0になった場合ゲームを終了
                GameTerminator.end(Role.Team.WOLF, languages("title.win.reason.anni", "%role%" to Role.Team.VILLAGER.displayName))
            }
        }
    }
}