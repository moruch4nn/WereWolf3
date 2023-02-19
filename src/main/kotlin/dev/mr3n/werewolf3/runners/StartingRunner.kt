package dev.mr3n.werewolf3.runners

import dev.mr3n.werewolf3.GameInitializer
import dev.mr3n.werewolf3.GameStatus
import dev.mr3n.werewolf3.STATUS
import dev.mr3n.werewolf3.TIME_LEFT
import dev.mr3n.werewolf3.sidebar.ISideBar.Companion.sidebar
import dev.mr3n.werewolf3.sidebar.StartingSidebar
import dev.mr3n.werewolf3.utils.LoopProcess
import dev.mr3n.werewolf3.utils.joinedPlayers

object StartingRunner: LoopProcess(1L,1L) {
    override fun run() {
        if(STATUS != GameStatus.STARTING) { return }
        // 残り時間を減らす
        TIME_LEFT--
        joinedPlayers().forEach { player ->
            val sidebar = player.sidebar
            // プレイヤーのサイドバーがStartingSidebarではない場合はreturn
            if(sidebar !is StartingSidebar) { return@forEach }
            // サイドバーの推定プレイヤー数を更新
            sidebar.players(joinedPlayers().size)
            // サイドバーの残り時間を更新
            sidebar.time(TIME_LEFT /20)
        }
        // 準備時間が終わったらゲーム開始
        if(TIME_LEFT <=0) { GameInitializer.run() }
    }
}