package de.xgme.mc.toolkit;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitTask;

public class StopCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {
		if (args.length == 0) {
			Bukkit.getScheduler().cancelTasks(ToolkitPlugin.getInstance());
			sender.sendMessage("All tasks canceled.");
			return true;
		} else if (args.length == 1) {
			if (args[0].equalsIgnoreCase("expworld")) {
				List<BukkitTask> tasks = ToolkitPlugin.getInstance().expWorldCmd.getTasks();
				for (BukkitTask task : tasks) {
					Bukkit.getScheduler().cancelTask(task.getTaskId());
				}
				ToolkitPlugin.getInstance().expWorldCmd.updateTaskList();
				sender.sendMessage("All tasks of expworld canceled.");
			} else {
				sender.sendMessage("Tool not found: " + args[0]);
				return false;
			}
			return true;
		} else {
			return false;
		}
	}

}
