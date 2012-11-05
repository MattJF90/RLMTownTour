package com.rlminecraft.RLMTownTour;

import java.util.Hashtable;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import com.palmergames.bukkit.towny.Towny;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;

public class RLMTownTour extends JavaPlugin {
	
	public Logger console;
	private Towny towny;
	
	@Override
	public void onEnable() {
		console = this.getLogger();
		// As RLMTownTour depends on Towny, it can safely be linked automatically.
		towny = (Towny) this.getServer().getPluginManager().getPlugin("Towny");
	}
	
	public void onDisable() {
		
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
			
			Location warp = null;
			Hashtable<String,Town> towns = towny.getTownyUniverse().getTownsMap();
			Hashtable<String,Resident> residents = towny.getTownyUniverse().getResidentMap();
			boolean allowedToTour = true;
			// Check resident data
			if (residents.containsKey(sender.getName().toLowerCase())) {
				Resident resident = residents.get(sender.getName().toLowerCase());
				if (resident.hasTown()) {
					allowedToTour = false;
					sender.sendMessage(ChatColor.RED + "You are already part of a town and are unable to tour for free.");
					sender.sendMessage(ChatColor.RED + "Use " + ChatColor.YELLOW + "/town spawn " + args[0] + ChatColor.RED + " instead.");
				}
			}
			// Check town data
			String townEntry = args[0].toLowerCase();
			if (!towns.containsKey(townEntry)) {
				allowedToTour = false;
				sender.sendMessage(ChatColor.RED + args[0] + " is not a valid town.");
			} else {
				Town town = towns.get(townEntry);
				if (!town.hasSpawn()) {
					allowedToTour = false;
					sender.sendMessage(ChatColor.RED + args[0] + " does not have a spawn location.");
				}
				if (!town.isPublic()) {
					allowedToTour = false;
					sender.sendMessage(ChatColor.RED + args[0] + " is not a public town and may not be toured.");
				}
				try {
					warp = town.getSpawn();
				} catch (TownyException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (allowedToTour) {
				sender.sendMessage(ChatColor.GREEN + "You are now touring " + args[0] + "!");
				((Player) sender).teleport(warp);
			}
		}
		return true;
	}
}
