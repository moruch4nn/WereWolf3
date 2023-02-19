package dev.mr3n.werewolf3.runners

import dev.mr3n.werewolf3.BOSSBAR
import dev.mr3n.werewolf3.Constants
import dev.mr3n.werewolf3.GameStatus
import dev.mr3n.werewolf3.sidebar.ISideBar.Companion.sidebar
import dev.mr3n.werewolf3.sidebar.WaitingSidebar
import dev.mr3n.werewolf3.utils.LoopProcess
import dev.mr3n.werewolf3.utils.joinedPlayers
import dev.mr3n.werewolf3.utils.languages
import dev.mr3n.werewolf3.utils.ticks

object WaitingRunner: LoopProcess(1L,1L, false, GameStatus.WAITING) {
    override fun run() {
        // 点滅速度
        if(ticks() % Constants.POINT_FLUSH_SPEED!=0L) {
            // ...の.の数を計算
            val loadingDots = ".".repeat(((ticks()%(Constants.POINT_FLUSH_SPEED*4))/ Constants.POINT_FLUSH_SPEED).toInt())
            // bossbarに...のアニメーションを追加
            BOSSBAR.setTitle(languages("messages.please_wait_for_start") +loadingDots)
            joinedPlayers().forEach { player ->
                val sidebar = player.sidebar
                // プレイヤーのサイドバーがWaitingSidebarの場合
                if(sidebar is WaitingSidebar) {
                    // 待機中l...の..にアニメーションを付与
                    sidebar.status(languages("sidebar.global.status.waiting") +loadingDots)
                }
            }
        }
    }
}