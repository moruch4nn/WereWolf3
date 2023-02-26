package dev.mr3n.werewolf3

import org.bukkit.NamespacedKey

object Keys {
    /**
     * アイテムID
     */
    val ITEM_ID = NamespacedKey(WereWolf3.INSTANCE, "item_id")

    /**
     * アイテムがドロップ可能かどうか
     */
    val ITEM_DROPPABLE = NamespacedKey(WereWolf3.INSTANCE, "droppable")

    /**
     *
     */
    val ITEM_ROLE_LOCK = NamespacedKey(WereWolf3.INSTANCE, "role_lock")

    /**
     * ヒールの残り残量
     */
    val HEAL_AMOUNT = NamespacedKey(WereWolf3.INSTANCE, "health_amount")

    /**
     * エンティティの種類(爆発玉、一撃の矢など)
     */
    val ENTITY_TYPE = NamespacedKey(WereWolf3.INSTANCE, "entity_type")

    /**
     * Longで最後の使用時間をミリ秒で格納します。
     */
    val LAST_USED_TIME = NamespacedKey(WereWolf3.INSTANCE, "last_used_time")

    val TELEPORTER_TARGET = NamespacedKey(WereWolf3.INSTANCE, "teleporter_target")
}