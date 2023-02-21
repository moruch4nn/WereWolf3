package dev.mr3n.werewolf3.utils

import dev.mr3n.werewolf3.WereWolf3
import org.bukkit.configuration.file.FileConfiguration

inline fun <reified T> constant(key: String): T = WereWolf3.CONFIG.constant(key)

inline fun <reified T> FileConfiguration.constant(key: String): T {
    val config = this
    return when(T::class.java) {
        Int::class.java, Int::class.javaObjectType -> {
            config.getInt(key) as T
        }
        Long::class.java, Long::class.javaObjectType -> {
            config.getLong(key) as T
        }
        String::class.java, String::class.javaObjectType -> {
            config.getString(key) as T
        }
        Boolean::class.java, Boolean::class.javaObjectType -> {
            config.getBoolean(key) as T
        }
        else -> {
            config.get(key) as T
        }
    }
}

inline fun <reified T> constants(key: String): List<T> = WereWolf3.CONFIG.constants(key)

inline fun <reified T> FileConfiguration.constants(key: String): List<T> {
    val config = this
    return when(T::class.java) {
        Int::class.java, Int::class.javaObjectType -> {
            config.getIntegerList(key).filterIsInstance(T::class.java)
        }
        Long::class.java, Long::class.javaObjectType -> {
            config.getLongList(key).filterIsInstance(T::class.java)
        }
        String::class.java, String::class.javaObjectType -> {
            config.getStringList(key).filterIsInstance(T::class.java)
        }
        else -> {
            config.getList(key)!!.filterIsInstance(T::class.java)
        }
    }
}

fun main() {
    println(Int::class.javaObjectType)
}