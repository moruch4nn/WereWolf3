package dev.mr3n.werewolf3.datatypes

import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType

// NBTタグの単位。
object BooleanDataType: PersistentDataType<Boolean, Boolean> {
    override fun getPrimitiveType(): Class<Boolean> = Boolean::class.java
    override fun getComplexType(): Class<Boolean> = Boolean::class.java
    override fun toPrimitive(complex: Boolean, context: PersistentDataAdapterContext): Boolean = complex
    override fun fromPrimitive(primitive: Boolean, context: PersistentDataAdapterContext): Boolean = primitive
}