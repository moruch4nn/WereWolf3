package dev.mr3n.werewolf3

import org.bukkit.NamespacedKey

object Keys {
    /**
     * プレイヤーの本来の役職
     */
    val ROLE = NamespacedKey(WereWolf3.INSTANCE,"role")

    /**
     * プレイヤーが何をCOしているか
     */
    val CO = NamespacedKey(WereWolf3.INSTANCE,"co")

    val MONEY = NamespacedKey(WereWolf3.INSTANCE,"money")

    /**
     * アイテムID
     */
    val ITEM_ID = NamespacedKey(WereWolf3.INSTANCE, "item_id")

    /**
     * ヒールの残り残量
     */
    val HEAL_AMOUNT = NamespacedKey(WereWolf3.INSTANCE, "health_amount")

    /**
     * エンティティの種類(爆発玉、一撃の矢など)
     */
    val ENTITY_TYPE = NamespacedKey(WereWolf3.INSTANCE, "entity_type")

    /**
     * プレイヤーのキル数
     */
    val KILLS = NamespacedKey(WereWolf3.INSTANCE, "kills")

    /**
     * プレイヤーの遺言
     */
    val PLAYER_WILL = NamespacedKey(WereWolf3.INSTANCE, "will")

    /**
     * Longで最後の使用時間をミリ秒で格納します。
     */
    val LAST_USED_TIME = NamespacedKey(WereWolf3.INSTANCE, "last_used_time")
}