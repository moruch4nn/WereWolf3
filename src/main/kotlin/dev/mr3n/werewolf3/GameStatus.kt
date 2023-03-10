package dev.mr3n.werewolf3

/**
 * ゲームのステータス
 */
enum class GameStatus(val id: Int) {
    /**
     * ゲームが開始していない時、つまり待機中の時のステータスです。
     */
    WAITING(3),

    /**
     * ゲーム開始時の逃げ時間のときのステータスです。
     */
    STARTING(0),

    /**
     * ゲームが実行中の時のステータスです。
     */
    RUNNING(1),

    /**
     * ゲーム終了時の時のステータスです。結果発表などがココで行われます。
     */
    ENDING(2),
}