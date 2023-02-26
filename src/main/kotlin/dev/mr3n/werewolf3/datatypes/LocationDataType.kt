package dev.mr3n.werewolf3.datatypes

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType

// NBTタグの単位。
object LocationDataType: PersistentDataType<String, Location> {
    override fun getPrimitiveType(): Class<String> = String::class.java
    override fun getComplexType(): Class<Location> = Location::class.java
    override fun fromPrimitive(primitive: String, context: PersistentDataAdapterContext): Location {
        val args = primitive.split(",")
        return Location(Bukkit.getWorld(args[0]),args[1].toDouble(),args[2].toDouble(),args[3].toDouble(),args[4].toFloat(),args[5].toFloat())
    }
    override fun toPrimitive(complex: Location, context: PersistentDataAdapterContext): String {
        return "${complex.world?.name},${complex.x},${complex.y},${complex.z},${complex.yaw},${complex.pitch}"
    }
}