package dev.mr3n.werewolf3.runners

import dev.mr3n.werewolf3.GameStatus
import dev.mr3n.werewolf3.WereWolf3
import dev.mr3n.werewolf3.utils.LoopProcess
import dev.mr3n.werewolf3.utils.joinedPlayers
import dev.mr3n.werewolf3.utils.languages


object GameTipsRunner: LoopProcess(WereWolf3.TIPS_CONFIG.getLong("delay"),WereWolf3.TIPS_CONFIG.getLong("delay"), false, GameStatus.RUNNING) {
    private val tips = WereWolf3.TIPS_CONFIG.getStringList("tips")
    override fun run() {
        val tip = tips.randomOrNull()?:return
        joinedPlayers().forEach { it.sendMessage(WereWolf3.TIPS_CONFIG.languages("format", "%tips%" to tip)) }
    }
}