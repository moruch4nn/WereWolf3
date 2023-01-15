package dev.mr3n.werewolf3.items

import dev.moru3.minepie.item.EasyItem
import dev.mr3n.werewolf3.Constants
import dev.mr3n.werewolf3.Keys
import dev.mr3n.werewolf3.WereWolf3
import dev.mr3n.werewolf3.items.doctor.DoctorSword
import dev.mr3n.werewolf3.items.doctor.HealthCharger
import dev.mr3n.werewolf3.items.madman.FakeMediumItem
import dev.mr3n.werewolf3.items.madman.FakeSeerItem
import dev.mr3n.werewolf3.items.madman.WolfGuide
import dev.mr3n.werewolf3.items.medium.MediumItem
import dev.mr3n.werewolf3.items.seer.MultipleSeerItem
import dev.mr3n.werewolf3.items.seer.SeerItem
import dev.mr3n.werewolf3.items.wolf.*
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
     * アイテムがこのアイテムかどうかを確認
     */
    fun isSimilar(itemStack: ItemStack): Boolean

    fun onEnd()

    fun onSetItemMeta(itemMeta: ItemMeta)

    fun buy(player: Player): Boolean

    abstract class ShopItem(final override val id: String, val material: Material): IShopItem {
        override val price: Int = itemConstant("price")
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
                    ItemFlag.values().forEach { meta.addItemFlags(it) }
                    this.onSetItemMeta(meta)
                }
            }

        fun messages(key: String, vararg values: Pair<String, Any>): String {
            return WereWolf3.ITEMS_CONFIG.languages("${id}.languages.messages.${key}", *values)
        }

        fun titleText(name: String): String = dev.mr3n.werewolf3.utils.titleText("item.${id}.languages.title.$name")

        protected inline fun <reified T> itemConstant(key: String): T {
            return WereWolf3.ITEMS_CONFIG.constant("${id}.languages.${key}")
        }

        protected inline fun <reified T> itemConstants(key: String): List<T> {
            return WereWolf3.ITEMS_CONFIG.constants("${id}.languages.${key}")
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
            val STAN_BALL = StanBall
            val INVISIBLE_POTION = InvisiblePotion
            val HEAL_POTION = HealPotion
            val GLOW_INK = GlowInk
            val ASSASSIN_SWORD = AssassinSword
            val BOMB_BALL = BombBall
            val LIGHTNING_ROD = LightningRod
            val WOLF_AXE = WolfAxe
            val SEER_ITEM = SeerItem
            val MEDIUM_ITEM = MediumItem
            val WOLF_GUIDE = WolfGuide
            val FAKE_SEER_ITEM = FakeSeerItem
            val FAKE_MEDIUM_ITEM = FakeMediumItem
            val HEALTH_CHARGER = HealthCharger
            val DOCTOR_SWORD = DoctorSword
            val ONE_SHOT_BOW = OneShotCrossbow
            val TOTEM = Totem
            val SPEED_POTION = SpeedPotion
            val STONE_SWORD = StoneSword
            val LAST_RESORT = LastResort
            val DEAD_BODY_REMOVER = DeadBodyRemover
            val MULTIPLE_SEER_ITEM = MultipleSeerItem
        }
    }
}