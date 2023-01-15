package dev.mr3n.werewolf3.utils

import dev.mr3n.werewolf3.WereWolf3
import net.md_5.bungee.api.ChatColor
import org.bukkit.configuration.file.FileConfiguration

fun FileConfiguration.languages(key: String, vararg args: Pair<String, Any>): String {
    var message = this.getString(key)?:"&cMessage not found"
    args.forEach { message = message.replace(it.first,it.second.toString()) }
    return ChatColor.translateAlternateColorCodes('&',message)
}

/**
 * languages.ymlから翻訳されたメッセージを取得。
 */
fun languages(key: String, vararg args: Pair<String, Any>): String = WereWolf3.LANGUAGES_CONFIG.languages(key, *args)

fun String.asPrefixed() = "${languages("prefix")} $this"