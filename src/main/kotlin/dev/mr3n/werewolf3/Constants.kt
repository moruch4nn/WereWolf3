package dev.mr3n.werewolf3

import dev.mr3n.werewolf3.utils.constant
import dev.mr3n.werewolf3.utils.constants
import dev.mr3n.werewolf3.utils.languages

/**
 * 定数一覧。
 * 今後スプレッドシート上から変更できるようにするため一元化しています。
 */
object Constants {
    val POINT_FLUSH_SPEED: Int = constant("general.point_flush_speed")
    val STARTING_TIME: Int = constant("game.starting_time")
    val DAY_TIME: Int = constant("game.day_time")
    val NIGHT_TIME: Int = constant("game.night_time")
    val MONEY_INTERVAL: Int = constant("game.money.money_interval")
    val MONEY_AMOUNT: Int = constant("game.money.money_amount")
    val INITIAL_FUNDS: Int = constant("game.money.initial_funds")
    val MAX_DAYS: Int = constant("game.max_days")
    val END_TIME: Time = try { Time.valueOf(constant("end_time")) } catch(_: Exception) { Time.MORNING }
    val CONVERSATION_DISTANCE: Double = constant("game.conversation_distance")
    val BE_PREFIX: String = constant("bedrock_edition.prefix")
    val MESSAGE_COMMANDS: List<String> = constants("message_commands")
    val MONEY_UNIT: String = languages("money_unit")
    val TEAM_KILL_BONUS: Int = constant("game.team_kill_bonus")
}