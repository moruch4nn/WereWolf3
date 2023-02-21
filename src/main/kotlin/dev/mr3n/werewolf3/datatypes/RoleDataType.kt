package dev.mr3n.werewolf3.datatypes

import dev.mr3n.werewolf3.roles.Role
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType

// NBTタグの単位。
object RoleDataType: PersistentDataType<String, Role> {
    override fun getPrimitiveType(): Class<String> = String::class.java
    override fun getComplexType(): Class<Role> = Role::class.java
    override fun toPrimitive(complex: Role, context: PersistentDataAdapterContext): String = complex.toString()
    override fun fromPrimitive(primitive: String, context: PersistentDataAdapterContext): Role = Role.valueOf(primitive)
}