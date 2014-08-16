package de.xgme.mc.toolkit;

import org.bukkit.plugin.java.JavaPlugin;

public class ToolkitPlugin extends JavaPlugin {

	final ExpandWorldCommand expWorldCmd = new ExpandWorldCommand();
	final StopCommand stopCmd = new StopCommand();

	@Override
	public void onEnable() {
		instance = this;

		// write configuration (for the first start)
		saveDefaultConfig();
		
		// register command executors
		getCommand("xgme-toolkit-expworld").setExecutor(expWorldCmd);
		getCommand("xgme-toolkit-stop").setExecutor(stopCmd);
	}

	@Override
	public void onDisable() {
		instance = null;
	}

	// --- Static Member
	// ============================================================

	private static ToolkitPlugin instance = null;

	/**
	 * Gets the instance of plug-in.
	 * 
	 * @return Instance of plug-in.
	 */
	public static ToolkitPlugin getInstance() {
		return instance;
	}

}
