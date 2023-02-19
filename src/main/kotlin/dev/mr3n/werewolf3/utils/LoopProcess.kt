package dev.mr3n.werewolf3.utils

import dev.moru3.minepie.Executor.Companion.runTaskTimer
import dev.moru3.minepie.Executor.Companion.runTaskTimerAsync
import dev.mr3n.werewolf3.GameStatus
import dev.mr3n.werewolf3.STATUS
import dev.mr3n.werewolf3.WereWolf3

abstract class LoopProcess(delay: Long, period: Long, async: Boolean = false, vararg statuses: GameStatus = GameStatus.values()): Runnable {
    init {
        if(async) {
            WereWolf3.INSTANCE.runTaskTimerAsync(delay,period) { if(STATUS in statuses) { this.run() } }
        } else {
            WereWolf3.INSTANCE.runTaskTimer(delay,period) { if(STATUS in statuses) { this.run() } }
        }
    }
}