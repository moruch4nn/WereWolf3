package dev.mr3n.werewolf3

import dev.mr3n.werewolf3.discord.*
import dev.mr3n.werewolf3.roles.Role
import dev.mr3n.werewolf3.sidebar.ISideBar.Companion.sidebar
import dev.mr3n.werewolf3.sidebar.RunningSidebar
import dev.mr3n.werewolf3.utils.asPrefixed
import dev.mr3n.werewolf3.utils.conversationalDistance
import dev.mr3n.werewolf3.utils.joinedPlayers
import dev.mr3n.werewolf3.utils.languages
import net.md_5.bungee.api.ChatColor
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.boss.BarColor

enum class Time(val barColor: BarColor) {
    MORNING(BarColor.YELLOW),
    NIGHT(BarColor.PURPLE);

    fun lowercase() = this.toString().lowercase()

    /**
     * 時間の表示名
     */
    val displayName: String
        get() = languages("time.${lowercase()}.name")

    /**
     * 時間に合う絵文字~EMOJI~
     */
    val emoji: String
        get() = languages("time.${lowercase()}.emoji")

    /**
     * 時間帯の説明
     */
    val description: String
        get() = languages("time.${lowercase()}.description", "%day%" to Constants.MAX_DAYS - DAYS)


    /**
     * 朝/夜の変更の処理
     */
    operator fun invoke() {
        when(this) {
            MORNING -> { morning() }
            NIGHT -> { night() }
        }
        // プレイヤーのミュート状態を更新
        joinedPlayers().forEach(DiscordManager::updateVoiceChannelState)
    }

    val title: String
        get() = languages("title.time.title", "%emoji%" to emoji, "%time%" to displayName, "%day%" to DAYS)

    /**
     * 次の時間帯を変えします。
     */
    fun next(): Time {
        return when(this) {
            MORNING -> { NIGHT }
            NIGHT -> { MORNING }
        }
    }

    companion object {
        /**
         * 朝になったときの処理
         */
        fun morning() {
            // ゲームが実行中ではない場合return
            if(!WereWolf3.isRunning) { return }
            if(Constants.END_TIME==NIGHT&&DAYS>=Constants.MAX_DAYS) {
                GameTerminator.end(Role.Team.VILLAGER, languages("title.win.reason.time_up"))
                return
            }
            // 残り時間を朝の時間に設定(20はtick)
            TIME_LEFT = Constants.DAY_TIME
            GAME_LENGTH = TIME_LEFT
            // 日付を1追加する
            DAYS++

            joinedPlayers().forEach { player ->
                // プレイヤーに朝になった旨を伝える。
                player.sendMessage("${TIME_OF_DAY.title}:${ChatColor.WHITE} ${TIME_OF_DAY.description}".asPrefixed())
                // プレイヤーに朝になった旨を伝える。
                player.sendTitle(TIME_OF_DAY.title, TIME_OF_DAY.description, 0, 100, 20)
                player.conversationalDistance(100, -1.0)
                // ぷーぷっぷぷーの効果音
                player.world.playSound(player, Sound.ITEM_GOAT_HORN_SOUND_0, 1f, 1f)
                // ボスバーの色を朝の色に変更
                BOSSBAR.color = TIME_OF_DAY.barColor
                // ワールドの時間帯を朝に変更
                player.world.time = 8000
                val sidebar = player.sidebar
                if(sidebar is RunningSidebar) {
                    sidebar.day(Constants.MAX_DAYS - DAYS)
                }
            }
        }

        /**
         * 夜になったときの処理
         */
        fun night() {
            // ゲームが実行中ではない場合return
            if(!WereWolf3.isRunning) { return }
            if(Constants.END_TIME==MORNING&&DAYS>=Constants.MAX_DAYS) {
                GameTerminator.end(Role.Team.VILLAGER, languages("title.win.reason.time_up"))
                return
            }
            // 残り時間を夜の時間に設定(20はtick)
            TIME_LEFT = Constants.NIGHT_TIME
            GAME_LENGTH = TIME_LEFT

            joinedPlayers().forEach { player ->
                player.sendMessage("${TIME_OF_DAY.title}:${ChatColor.WHITE} ${TIME_OF_DAY.description}".asPrefixed())
                player.conversationalDistance(100, Constants.CONVERSATION_DISTANCE)

                // プレイヤーに夜になった旨を伝える。
                player.sendTitle(TIME_OF_DAY.title, TIME_OF_DAY.description, 0, 100, 20)
                // ボスバーを夜の色に変更
                BOSSBAR.color = TIME_OF_DAY.barColor
                // すべての効果音を停止(BGM停止用)
                player.stopAllSounds()
                // 狼の遠吠えを再生
                player.world.playSound(player, Sound.ENTITY_WOLF_HOWL, SoundCategory.MASTER, 1f, 1f)
                // ワールドの時間帯を夜に変更
                player.world.time = 16000
            }
        }
    }
}