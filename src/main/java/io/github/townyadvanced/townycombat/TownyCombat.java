package io.github.townyadvanced.townycombat;

import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.util.Version;
import io.github.townyadvanced.townycombat.commands.TownyCombatAdminCommand;
import io.github.townyadvanced.townycombat.integrations.dynmap.DynmapIntegration;
import io.github.townyadvanced.townycombat.listeners.TownyCombatBukkitEventListener;
import io.github.townyadvanced.townycombat.listeners.TownyCombatNationEventListener;
import io.github.townyadvanced.townycombat.listeners.TownyCombatTownEventListener;
import io.github.townyadvanced.townycombat.listeners.TownyCombatTownyEventListener;
import io.github.townyadvanced.townycombat.settings.Settings;
import io.github.townyadvanced.townycombat.settings.TownyCombatSettings;
import io.github.townyadvanced.townycombat.tasks.JumpReductionTask;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class TownyCombat extends JavaPlugin {
	
	private static TownyCombat plugin = null;
	private static DynmapIntegration dynmapIntegration = null;
	private static final Version requiredTownyVersion = Version.fromString("0.98.0.0");
	private static BukkitTask jumpReductionTask = null;

    @Override
    public void onEnable() {
    	plugin = this;
		System.out.println("=================================================================================");
		printSickASCIIArt();
	
    	try {
			if (townyVersionCheck(getTownyVersion())) {
				info("Towny version " + getTownyVersion() + " found.");
			} else {
				throw new RuntimeException("Towny version does not meet required minimum version: " + requiredTownyVersion);
			}
			if(Towny.getPlugin().isError()) {
				throw new RuntimeException("Towny is in error. TownyCombat startup halted.");
			}
			registerListeners();
			Settings.loadConfig();
			Settings.loadLanguages();
			TownyCombatSettings.loadReloadCachedSetting();
			loadIntegrations();
			registerCommands();
			startTasks();
			info("TownyCombat Enabled.");
			System.out.println("=================================================================================");
		} catch (Exception e) {
    		severe(e.getMessage());
    		e.printStackTrace();
			info("TownyCombat did not start correctly.");
			endTasks();
		} 
    }
    
    private boolean townyVersionCheck(String version) {
        return Version.fromString(version).compareTo(requiredTownyVersion) >= 0;
    }

    private String getTownyVersion() {
        return Towny.getPlugin().getDescription().getVersion();
    }

	private void loadIntegrations() {
		if (getServer().getPluginManager().isPluginEnabled("dynmap")) {
			info("TownyCombat found Dynmap plugin. Enabling Dynmap support now.");
			dynmapIntegration = new DynmapIntegration();
		}
	}

	private void registerCommands() {
		getCommand("townycombatadmin").setExecutor(new TownyCombatAdminCommand());
	}

	public String getVersion() {
		return this.getDescription().getVersion();
	}
	
	public static TownyCombat getPlugin() {
		return plugin;
	}
	
	public static DynmapIntegration getDynmapIntegration() {
    	return dynmapIntegration;
	}

	public static void info(String message) {
		plugin.getLogger().info(message);
	}
	
	public static void severe(String message) {
		plugin.getLogger().severe(message);
	}
	
	private void registerListeners() {
		PluginManager pm = getServer().getPluginManager();		
		pm.registerEvents(new TownyCombatBukkitEventListener(this), this);
		pm.registerEvents(new TownyCombatTownEventListener(this), this);		
		pm.registerEvents(new TownyCombatNationEventListener(this), this);
		pm.registerEvents(new TownyCombatTownyEventListener(this), this);
	}
	
	private void startTasks() {
		if(TownyCombatSettings.isCavalryArmourSlowingEnabled()) {
			//Run jump reduction task every 0.5 seconds
        	jumpReductionTask = new JumpReductionTask().runTaskTimerAsynchronously(plugin, 400, 10);
		}
    }

    public void endTasks() {
        jumpReductionTask.cancel();
    }
	
	private void printSickASCIIArt() {
		System.out.println("  *   )                             (                    )           )");  
		System.out.println("` )  /(     (  (           (        )\\           )    ( /(     )  ( /( "); 
		System.out.println(" ( )(_))(   )\\))(    (     )\\ )   (((_)   (     (     )\\()) ( /(  )\\()) ");
		System.out.println("(_(_()) )\\ ((_)()\\   )\\ ) (()/(   )\\___   )\\    )\\  '((_)\\  )(_))(_))/  ");
		System.out.println("|_   _|((_)_(()((_) _(_/(  )(_)) ((/ __| ((_) _((_)) | |(_)((_)_ | |_   ");
		System.out.println("  | | / _ \\\\ V  V /| ' \\))| || |  | (__ / _ \\| '  \\()| '_ \\/ _` ||  _|  ");
		System.out.println("  |_| \\___/ \\_/\\_/ |_||_|  \\_, |   \\___|\\___/|_|_|_| |_.__/\\__,_| \\__|  ");
		System.out.println("                           |__/ ");                                        
		System.out.println();	
	}
}
