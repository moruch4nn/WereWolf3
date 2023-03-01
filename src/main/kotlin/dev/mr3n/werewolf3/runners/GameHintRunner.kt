package dev.mr3n.werewolf3.runners

import dev.mr3n.werewolf3.GameStatus
import dev.mr3n.werewolf3.WereWolf3
import dev.mr3n.werewolf3.utils.LoopProcess
import dev.mr3n.werewolf3.utils.joinedPlayers
import dev.mr3n.werewolf3.utils.languages


object GameHintRunner: LoopProcess(WereWolf3.HINTS_CONFIG.getLong("delay"),WereWolf3.HINTS_CONFIG.getLong("delay"), false, GameStatus.RUNNING) {
    private val hints = WereWolf3.HINTS_CONFIG.getStringList("hints")
    override fun run() {
        val hint = hints.randomOrNull()?:return
        joinedPlayers().forEach { it.sendMessage(WereWolf3.HINTS_CONFIG.languages("format", "%hint%" to hint)) }
    }
}