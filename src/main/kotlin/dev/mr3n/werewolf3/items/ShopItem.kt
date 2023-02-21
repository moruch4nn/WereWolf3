package dev.mr3n.werewolf3.items

import dev.moru3.minepie.item.EasyItem
import dev.mr3n.werewolf3.Constants
import dev.mr3n.werewolf3.Keys
import dev.mr3n.werewolf3.WereWolf3
import dev.mr3n.werewolf3.datatypes.BooleanDataType
import dev.mr3n.werewolf3.roles.Role
import dev.mr3n.werewolf3.utils.*
import net.md_5.bungee.api.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType

/**
 * 人狼で使用するアイテムの情報
 */
interface IShopItem {
    /**
     * アイテムを識別するためのユニークなID
     */
    val id: String

    /**
     * ひとこと
     */
    val comment: String

    /**
     * アイテムの表示名
     */
    val displayName: String

    /**
     * アイテムの説明
     */
    val description: List<String>

    /**
     * アイテム
     */
    val itemStack: ItemStack

    /**
     * このアイテムを所持できる役職
     */
    val roles: List<Role>

    /**
     * アイテムの価格
     */
    val price: Int

    /**
     * アイテムがドロップ可能かどうか
     */
    val droppable: Boolean

    /**
     * 役職によって使用可否が決まっているかどうかをtrue/falseで表します。
     * falseの場合rolesに記載されている以外のプレイヤーはアイテムを使用できません。
     */
    val roleLock: Boolean

    /**
     * アイテムがこのアイテムかどうかを確認
     */
    fun isSimilar(itemStack: ItemStack): Boolean

    fun onEnd()

    fun onSetItemMeta(itemMeta: ItemMeta)

    fun buy(player: Player): Boolean

    abstract class ShopItem(final override val id: String, val material: Material): IShopItem {
        override val price: Int = itemConstant("price")
        override val droppable: Boolean = itemConstant("droppable")
        override val roleLock: Boolean = itemConstant("role_lock")
        override val roles: List<Role> = itemConstants<String>("roles").map{Role.valueOf(it)}
        override val displayName: String = WereWolf3.ITEMS_CONFIG.languages("${id}.languages.name")
        override val comment: String = WereWolf3.ITEMS_CONFIG.languages("${id}.languages.comment")
        override val description = WereWolf3.ITEMS_CONFIG.languages("${id}.languages.description").split("\n")

        private val lore: List<String>
            get() {
                return if(roles.isEmpty()) {
                    description
                } else {
                    val roles = if(roles.size >= Role.values().size) languages("everyone") else roles.joinToString("${ChatColor.WHITE},") { it.asString() }
                    description.toMutableList().apply { add("");add(languages("items.general.languages.roles", "%roles%" to roles)) }
                }
            }
        override val itemStack: ItemStack
            get() = EasyItem(material, displayName, lore).also { item ->
                item.itemMeta = item.itemMeta?.also { meta ->
                    meta.container.set(Keys.ITEM_ID, PersistentDataType.STRING, id)
                    meta.container.set(Keys.ITEM_DROPPABLE, BooleanDataType, droppable)
                    meta.container.set(Keys.ITEM_ROLE_LOCK, BooleanDataType, roleLock)
                    ItemFlag.values().forEach { meta.addItemFlags(it) }
                    this.onSetItemMeta(meta)
                }
            }

        fun messages(key: String, vararg values: Pair<String, Any>): String {
            return WereWolf3.ITEMS_CONFIG.languages("${id}.languages.messages.${key}", *values)
        }

        fun titleText(name: String): String = WereWolf3.ITEMS_CONFIG.languages("${id}.languages.title.${name}.title").toTitleText(WereWolf3.ITEMS_CONFIG.languages("${id}.languages.title.${name}.color"))

        protected inline fun <reified T> itemConstant(key: String): T {
            return WereWolf3.ITEMS_CONFIG.constant("${id}.${key}")
        }

        protected inline fun <reified T> itemConstants(key: String): List<T> {
            return WereWolf3.ITEMS_CONFIG.constants("${id}.${key}")
        }

        override fun isSimilar(itemStack: ItemStack): Boolean {
            return itemStack.getContainerValue(Keys.ITEM_ID, PersistentDataType.STRING) == id
        }

        override fun onEnd() {}

        override fun buy(player: Player): Boolean {
            return if(player.money >= price) {
                player.money -= price
                player.inventory.addItem(itemStack)
                player.sendMessage(languages("shop.messages.bought", "%item%" to displayName, "%price%" to "${price}${Constants.MONEY_UNIT}").asPrefixed())
                true
            } else {
                player.sendMessage(languages("shop.messages.not_enough_money", "%item%" to displayName, "%price%" to "${price}${Constants.MONEY_UNIT}").asPrefixed())
                false
            }
        }

        override fun onSetItemMeta(itemMeta: ItemMeta) {}

        init {
            ITEMS.add(this)
            ITEMS_BY_ID[id] = this
        }

        companion object {
            val ITEMS = mutableListOf<IShopItem>()
            val ITEMS_BY_ID = mutableMapOf<String, IShopItem>()

            init {
                // config.ymlから全てのアイテムを取得
                val itemIds = WereWolf3.ITEMS_CONFIG.getKeys(false).map { WereWolf3.ITEMS_CONFIG.getString("${it}.class") }
                // すべてのクラスを初期化する
                itemIds.forEach { try { Class.forName(it) } catch(e: Exception) { WereWolf3.INSTANCE.logger.warning("$it class not found.") } }
            }
        }
    }
}