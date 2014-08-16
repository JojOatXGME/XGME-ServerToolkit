package de.xgme.mc.toolkit;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import de.xgme.mc.toolkit.util.ArgumentAnalyzer;
import de.xgme.mc.toolkit.util.ArgumentAnalyzer.Option;

public class ExpandWorldCommand implements CommandExecutor {

	private List<BukkitTask> tasks = new LinkedList<BukkitTask>();

	@Override
	public boolean onCommand(final CommandSender sender, final Command cmd, final String alias,
			final String[] args) {

		// analyze command arguments
		final ArgumentAnalyzer analyzer = new ArgumentAnalyzer();
		analyzer.addOption("-square", 0);
		analyzer.addAlias("s", "-square");
		analyzer.addOption("-execution-time-per-tick", 1);
		analyzer.addAlias("e", "-execution-time-per-tick");
		analyzer.addOption("-message-interval", 1);
		analyzer.addAlias("m", "-message-interval");
		if (!analyzer.analyze(args))
			return false;
		if (analyzer.getArgumentCount() != 2)
			return false;

		// get center of circle or square
		final Location center;
		if (analyzer.getArgument(1).equalsIgnoreCase("me") && sender instanceof Player) {
			final Player player = (Player) sender;
			center = player.getLocation();
		} else {
			final World world = Bukkit.getWorld(analyzer.getArgument(1));
			if (world == null) {
				sender.sendMessage("World does not exist: " + analyzer.getArgument(1));
				return false;
			}
			center = world.getSpawnLocation();
		}

		// get radius
		final int radius;
		try {
			radius = Integer.parseInt(analyzer.getArgument(0));
		} catch (NumberFormatException e) {
			sender.sendMessage("Radius have to be a valid integer: " + analyzer.getArgument(0));
			return false;
		}

		Option option;
		// get message interval
		final double msgInterval;
		if ((option = analyzer.getOption("m")).isInCommand()) {
			try {
				msgInterval = Double.parseDouble(option.getArgument(0));
			} catch (NumberFormatException e) {
				sender.sendMessage("Message interval have to be a valid floating point number.");
				return false;
			}
		} else {
			msgInterval = ToolkitPlugin.getInstance().getConfig()
					.getDouble("expworld.message_interval");
			if (msgInterval <= 0) {
				ToolkitPlugin.getInstance().getLogger()
						.warning("Message interval have to be positive.");
			}
		}
		if (msgInterval <= 0) {
			sender.sendMessage("Message interval have to be positive.");
		}
		// get execution time in tick
		final int exeTime;
		if ((option = analyzer.getOption("e")).isInCommand()) {
			try {
				exeTime = Integer.parseInt(option.getArgument(0));
			} catch (NumberFormatException e) {
				sender.sendMessage("Execution time have to be a valid integer.");
				return false;
			}
		} else {
			exeTime = ToolkitPlugin.getInstance().getConfig().getInt("expworld.message_interval");
			if (exeTime <= 0) {
				ToolkitPlugin.getInstance().getLogger()
						.warning("Execution time have to be positive.");
			}
		}
		if (exeTime <= 0) {
			sender.sendMessage("Execution time have to be positive.");
		}

		// start expansion
		sender.sendMessage("World expansion started.");
		final WorldExpansion expansion = new WorldExpansion(center, radius, analyzer.getOption("s")
				.isInCommand(), exeTime);
		final BukkitTask[] task = { null, null };
		task[1] = Bukkit.getScheduler().runTaskTimer(ToolkitPlugin.getInstance(), new Runnable() {
			private long lastMessage = System.currentTimeMillis();

			@Override
			public void run() {
				if (expansion.nextSteps() && task[0] != null) {
					Bukkit.getScheduler().cancelTask(task[0].getTaskId());
					tasks.remove(task[0]);
					sender.sendMessage("World expansion successfully finished.");
				} else {
					if (System.currentTimeMillis() - lastMessage > msgInterval * 1000) {
						lastMessage = System.currentTimeMillis();
						int p = (int) ((100 * expansion.chunksLoaded) / expansion.chunksExpected);
						sender.sendMessage(String.format("World expansion completed to %d%%.", p));
					}
				}
			}
		}, 1, 1);
		tasks.add(task[1]);
		task[0] = task[1];

		return true;
	}

	public List<BukkitTask> getTasks() {
		return Collections.unmodifiableList(tasks);
	}

	public void updateTaskList() {
		final List<BukkitTask> newTasks = new LinkedList<BukkitTask>();
		for (BukkitTask task : tasks) {
			if (Bukkit.getScheduler().isQueued(task.getTaskId())
					|| Bukkit.getScheduler().isCurrentlyRunning(task.getTaskId())) {
				newTasks.add(task);
			}
		}
		tasks = newTasks;
	}

	private class WorldExpansion {
		final int r, r2;
		final int mx, mz;
		final World world;
		final boolean square;
		int x, z;

		final int exeTime;
		final long chunksExpected;
		long chunksLoaded = 0;

		WorldExpansion(Location center, int radius, boolean square, int executionTime) {
			this.r = radius;
			this.r2 = r * r;
			this.mx = center.getChunk().getX();
			this.mz = center.getChunk().getZ();
			this.world = center.getWorld();
			this.square = square;
			this.x = mx - r;
			this.z = mz - r;

			this.exeTime = executionTime;

			if (square) {
				chunksExpected = (4 * r * r) + (4 * r) + 1;
			} else {
				chunksExpected = Math.round(Math.PI * r2);
			}
		}

		boolean nextSteps() {
			long endtime = System.currentTimeMillis() + exeTime;
			while (true) {
				// -- load chunk
				int x_mx = x - mx, z_mz = z - mz;
				if (square || x_mx * x_mx + z_mz * z_mz <= r2) {
					world.loadChunk(x, z, true);
					chunksLoaded++;
					ToolkitPlugin.getInstance().getLogger()
							.log(Level.FINEST, "Chunk at (" + x + "," + z + ") loaded.");
					// TODO unload chunk
				}
				// -- chunk loaded
				if (++z > mz + r) {
					z = mz - r;
					if (++x > mx + r) {
						return true;
					}
				}
				if (System.currentTimeMillis() > endtime) {
					return false;
				}
			}
		}
	}
}
