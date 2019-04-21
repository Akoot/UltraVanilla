package com.akoot.plugins.ultravanilla.commands;

import com.akoot.plugins.ultravanilla.Ultravanilla;
import com.akoot.plugins.ultravanilla.serializable.Position;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SpawnCommand extends UltraCommand implements CommandExecutor {

    public static final ChatColor COLOR = ChatColor.GREEN;

    public SpawnCommand(Ultravanilla instance) {
        super(instance);
        this.color = COLOR;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length == 0) {
                Position spawn = (Position) plugin.getConfig().get("spawn");
                if (spawn != null) {
                    sender.sendMessage(spawn.serialize().toString());
                    player.teleport(spawn.getLocation());
                } else {
                    sender.sendMessage(wrong("Spawn is not set"));
                }
            } else if (args.length == 1 && player.hasPermission("ultravanilla.command.spawn.set")) {
                sender.sendMessage(format("Spawn was set to your location"));
                plugin.getConfig().set("spawn", new Position(player.getLocation()));
                plugin.saveConfig();
            } else {
                return false;
            }
        } else {
            sender.sendMessage(playerOnly());
        }
        return true;
    }
}