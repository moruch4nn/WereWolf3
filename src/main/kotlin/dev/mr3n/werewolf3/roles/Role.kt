package dev.mr3n.werewolf3.roles

import dev.moru3.minepie.Executor.Companion.runTaskTimerAsync
import dev.mr3n.werewolf3.Keys
import dev.mr3n.werewolf3.PLAYERS
import dev.mr3n.werewolf3.PlayerData
import dev.mr3n.werewolf3.WereWolf3
import dev.mr3n.werewolf3.datatypes.BooleanDataType
import dev.mr3n.werewolf3.datatypes.RoleDataType
import dev.mr3n.werewolf3.items.IShopItem
import dev.mr3n.werewolf3.utils.*
import org.bukkit.ChatColor
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.LeatherArmorMeta
import kotlin.math.ceil

enum class Role {
    VILLAGER,
    DOCTOR,
    WOLF,
    MADMAN,
    DIVINER,
    MEDIUM
    ;

    private val config = WereWolf3.CONFIG

    // 役職名を小文字で
    fun lowercase() = this.toString().lowercase()

    // 役職の色
    val color = ChatColor.valueOf(config.getString("roles.${this.lowercase()}.color")!!)
    // 役職の陣営 (白陣営/黒陣営)
    val team = Team.valueOf(config.getString("roles.${this.lowercase()}.team")!!)
    // 表示名
    val displayName = config.getString("roles.${this.lowercase()}.name")!!
    // 役職の説明
    val description = config.getString("roles.${this.lowercase()}.description")!!


    // 役職の優先度 高->0,低->∞
    val priority = config.getInt("roles.${this.lowercase()}.priority")
    // 役職の最少人数。minPが優先されます。
    private val min = config.getInt("roles.${this.lowercase()}.min")
    // 役職の人数
    val num = config.getInt("roles.${this.lowercase()}.num")
    // 人数の単位 人/パーセント
    private val unit = Unit.valueOf(config.getString("roles.${this.lowercase()}.unit")!!)
    // 役職を有効にする際の最小参加人数
    private val minP = config.getInt("roles.${this.lowercase()}.min_players")
    val items: List<IShopItem>
        get() = constants<String>("roles.${this.lowercase()}.items").mapNotNull { IShopItem.ShopItem.ITEMS_BY_ID[it] }


    // 役職の人数を参加者数から計算
    fun calc(players: Int): Int {
        // 最小参加人数に達していない場合は0
        if(players <= minP) { return 0 }
        // autoかどうか
        return if(num==-1) {
            //if:autoだった場合
            // autoじゃない役職一覧を取得
            val filters = values().filterNot { it.num == -1 }
            // autoの役職の数
            val count = values().size - filters.size
            // autoの合計役職人数を足す
            val sum = filters.sumOf { it.calc(players) }
            // 足した値をautoの役職の数で割って最小役職人数を適用
            (players - sum)/count
        } else {
            // パーセント/人で人数を決定。
            val base = when(unit) {
                Unit.PERCENT -> { (players / 100.0) * num }
                Unit.PLAYER -> { num.toDouble() }
            }
            // 最小役職人数を適用。
            maxOf(min, ceil(base).toInt())
        }
    }

    val players: List<PlayerData>
        get() = PLAYERS.filter { it.role == this }

    // toStringをoverrideするとvalueOfを使えないため。
    fun asString() = "${color}${displayName}"

    // 役職のヘルメットを取得。
    val helmet: ItemStack
        get() = ItemStack(Material.LEATHER_HELMET).also { item ->
            item.itemMeta = (item.itemMeta as LeatherArmorMeta?)?.also { meta ->
                val color = this.color.asBungee().color
                meta.container.set(HELMET_ROLE_TAG_KEY,RoleDataType,this)
                meta.container.set(Keys.ITEM_DROPPABLE,BooleanDataType,false)
                meta.setColor(Color.fromRGB(color.red,color.green,color.blue))
                meta.isUnbreakable = true
                meta.setDisplayName(languages("items.co_helmet.name", "%color%" to this.color, "%role%" to this.displayName))
                ItemFlag.values().forEach { meta.addItemFlags(it) }
            }
        }

    companion object {
        val HELMET_ROLE_TAG_KEY = NamespacedKey(WereWolf3.INSTANCE,"role")

        init {
            // >>> カミングアウト帽子に関する処理 >>>
            WereWolf3.INSTANCE.runTaskTimerAsync(1L,1L) {
                alivePlayers().forEach { player ->
                    // プレイヤーのヘルメットを取得
                    val helmet = player.inventory.helmet
                    // ヘルメットのCoの役職を取得。nullだった場合はreturn
                    val coRole = helmet?.getContainerValue(Role.HELMET_ROLE_TAG_KEY, RoleDataType)
                    // まだCoしていない役職だった場合
                    if (player.co != coRole) {
                        if (coRole == null) {
                            player.setDisplayName(player.name)
                            player.setPlayerListName(player.name)
                            // 何をcoしたかをほぞん
                            player.co = null
                        } else {
                            // すべてのプレイヤーにCoした旨を伝える。
                            joinedPlayers().forEach { it.sendMessage(languages("messages.coming_out", "%color%" to coRole.color, "%player%" to player.name, "%role%" to coRole.displayName)) }
                            // プレイヤーのprefixにCoした役職を表示
                            player.setDisplayName("${coRole.color}[${coRole.displayName}Co]${player.name}")
                            player.setPlayerListName("${coRole.color}[${coRole.displayName}Co]${player.name}")
                            // 何をcoしたかをほぞん
                            player.co = coRole
                        }
                    }
                }
            }
            // <<< カミングアウト帽子に関する処理 <<<
        }
    }

    // プレイヤー数の単位。
    enum class Unit {
        PLAYER,
        PERCENT
    }

    // 陣営一覧。白陣営、黒陣営。
    enum class Team(val displayName: String, val color: ChatColor) {
        WOLF("人狼", ChatColor.DARK_RED),
        VILLAGER("村人", ChatColor.GOLD)
    }
}