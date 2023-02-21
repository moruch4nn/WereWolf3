package dev.mr3n.werewolf3.datatypes

import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType

// NBTタグの単位。
object BooleanDataType: PersistentDataType<String, Boolean> {
    override fun getPrimitiveType(): Class<String> = String::class.java
    override fun getComplexType(): Class<Boolean> = Boolean::class.java
    override fun toPrimitive(complex: Boolean, context: PersistentDataAdapterContext): String = complex.toString()
    override fun fromPrimitive(primitive: String, context: PersistentDataAdapterContext): Boolean = primitive.toBoolean()
}