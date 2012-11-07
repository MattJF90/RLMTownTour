package com.rlminecraft.RLMTownTour;

import java.util.Hashtable;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;
import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;

/**
 * @author Matt Fielding
 *
 */
public class RLMTownTour extends JavaPlugin implements Listener {
	
	public Logger console;
	private Towny towny;
	
	
	@Override
	public void onEnable() {
		console = this.getLogger();
		getServer().getPluginManager().registerEvents(this, this);
		// As RLMTownTour depends on Towny, it can safely be linked automatically.
		towny = (Towny) this.getServer().getPluginManager().getPlugin("Towny");
	}
	
	
	public void onDisable() {
		
	}
	
	
	@EventHandler(priority = EventPriority.HIGH)
	public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
		String[] message = event.getMessage().split(" ");
		if (message.length != 3) return;
		if (!message[0].equalsIgnoreCase("/town") && !message[0].equalsIgnoreCase("/t")) return;
		if (!message[1].equalsIgnoreCase("spawn")) return;
		TourStatus status = tourIfAllowed(event.getPlayer(),message[2]);
		if (status == TourStatus.VALID) event.setCancelled(true);
	}
	
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("This command can only be run as a player!");
			return true;
		}
		// Anything run from this point on is being run as a player.
		if(cmd.getName().equalsIgnoreCase("tour")) {
			if (args.length != 1) {
				return false;
			}
			TourStatus status = tourIfAllowed((Player) sender,args[0]);
			switch (status) {
			case VALID:
				// No further action required
				break;
			case RESIDENT:
				sender.sendMessage(ChatColor.RED + "You are already part of a town and are unable to tour for free.");
				sender.sendMessage(ChatColor.RED + "Use " + ChatColor.YELLOW + "/town spawn " + args[0] + ChatColor.RED + " instead.");
				break;
			case INVALIDTOWN:
				sender.sendMessage(ChatColor.RED + args[0] + " is not a valid town.");
				break;
			case NOSPAWN:
				sender.sendMessage(ChatColor.RED + args[0] + " does not have a spawn location.");
				break;
			case NOTPUBLIC:
				sender.sendMessage(ChatColor.RED + args[0] + " is not a public town and may not be toured.");
				break;
			case ERROR:
			default:
				break;
			}
		}
		return true;
	}
	
	
	public TourStatus tourIfAllowed(Player player, String townName) {
		Location warp = null;
		Hashtable<String,Town> towns = towny.getTownyUniverse().getTownsMap();
		Hashtable<String,Resident> residents = towny.getTownyUniverse().getResidentMap();
		boolean allowedToTour = true;
		// Check resident data
		if (residents.containsKey(player.getName().toLowerCase())) {
			Resident resident = residents.get(player.getName().toLowerCase());
			if (resident.hasTown()) {
				allowedToTour = false;
				return TourStatus.RESIDENT;
			}
		}
		// Check town data
		String townEntry = townName.toLowerCase();
		if (!towns.containsKey(townEntry)) {
			allowedToTour = false;
			return TourStatus.INVALIDTOWN;
		} else {
			Town town = towns.get(townEntry);
			if (!town.hasSpawn()) {
				allowedToTour = false;
				return TourStatus.NOSPAWN;
			}
			else if (!town.isPublic()) {
				allowedToTour = false;
				return TourStatus.NOTPUBLIC;
			}
			try {
				warp = town.getSpawn();
			} catch (TownyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		// Teleport player to town spawn
		if (allowedToTour) {
			player.sendMessage(ChatColor.GREEN + "You are now touring " + townName + "!");
			((Player) player).teleport(warp);
			// Log town as toured possibly?
		}
		return TourStatus.VALID;
	}
}
