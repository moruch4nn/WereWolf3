package dev.mr3n.werewolf3.utils

fun ticks() = TickingUtil.tick

object TickingUtil: LoopProcess(1L,1L) {

    var tick = 0L

    override fun run() { ++tick }
}