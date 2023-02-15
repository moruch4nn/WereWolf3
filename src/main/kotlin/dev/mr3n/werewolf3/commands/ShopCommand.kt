package dev.mr3n.werewolf3.commands

import dev.mr3n.werewolf3.GameStatus
import dev.mr3n.werewolf3.ItemShop.openShopMenu
import dev.mr3n.werewolf3.*
import org.bukkit.Sound
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

object ShopCommand: CommandExecutor, TabCompleter {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if(sender !is Player) { return false }
        if(!PLAYERS.contains(sender)) { return true }
        if(STATUS!=GameStatus.RUNNING) { return true }
        sender.playSound(sender, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f)
        sender.openShopMenu()
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): MutableList<String>? {
        return null
    }

}