package fr.ipazu.advancedrealm.commands;

import fr.ipazu.advancedrealm.utils.ConfigFiles;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnCommand implements CommandExecutor {
    public boolean onCommand(CommandSender sender, Command cmd, String arg, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("The command is only executable by a player !");
            return false;
        }
        Player player = (Player) sender;
        if (!player.hasPermission("realm.spawn")) {
            player.sendMessage("§cYou don't have the permission to do this.");
            return false;
        }
        player.teleport(ConfigFiles.getSpawn());
        player.sendMessage("§aTeleported to spawn.");
        return true;
    }
}
