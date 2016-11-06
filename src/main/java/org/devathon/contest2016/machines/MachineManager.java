package org.devathon.contest2016.machines;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.CommandBlock;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.devathon.contest2016.DevathonPlugin;

import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by zacha on 11/6/2016.
 */
public class MachineManager implements Listener{

	private HashMap<UUID, Machine> machineStorage = new HashMap<>();

	public void registerMachine(Machine machine){
		machineStorage.put(machine.getMachineUUID(), machine);
		Bukkit.getPluginManager().registerEvents(machine, DevathonPlugin.getInst());
	}

	public Machine getMachineAtLocation(Location l){ // Horrible way of doing this, I'll change it later.
		for (UUID u : machineStorage.keySet()){
			if (machineStorage.get(u).getCurrentLocation().getBlock().getLocation() == l.getBlock().getLocation() || machineStorage.get(u).getHomeLocation().getBlock().getLocation() == l.getBlock().getLocation()){
				return machineStorage.get(u);
			}
		}
		return null;
	}

	public Machine getMachineFromKey(String key){ // Horrible way of doing this, I'll change it later.
		UUID u = UUID.fromString(key);
		return machineStorage.get(u);
	}

	public Collection<Machine> getAllMachines(){
		return machineStorage.values();
	}


	@EventHandler
	private void onMachinePlace(BlockPlaceEvent e){
		if (e.getBlockPlaced().getType() == Material.COMMAND){
			CommandBlock cb = (CommandBlock) e.getBlockPlaced().getState();
			if (cb.getName().contains("Cookie Machine")){
				String n = ChatColor.stripColor(cb.getName()).replace("Cookie Machine [", "").replaceAll("]", "").trim();
				e.getPlayer().sendMessage(ChatColor.GREEN + "You've setup a Cookie Machine! " + ChatColor.GRAY + "(" + ChatColor.YELLOW + n + ChatColor.GRAY + ")");
				registerMachine(new CookieMachine(cb.getLocation(), e.getPlayer(), n));
			}else if (cb.getName().contains("Mining Machine")){
				String n = ChatColor.stripColor(cb.getName()).replace("Mining Machine [", "").replaceAll("]", "").trim();
				e.getPlayer().sendMessage(ChatColor.GREEN + "You've setup a Mining Machine! " + ChatColor.GRAY + "(" + ChatColor.YELLOW + n + ChatColor.GRAY + ")");
				registerMachine(new MiningMachine(cb.getLocation(), e.getPlayer(), n));
			}
		}
	}

	@EventHandler
	private void onAnvilRename(InventoryClickEvent e){
		if (e.getClickedInventory() != null) {
			if (e.getClickedInventory().getType() == InventoryType.ANVIL) {
				AnvilInventory ai = (AnvilInventory) e.getClickedInventory();
				/*try {
					Bukkit.broadcastMessage("1: " + ai.getItem(0).toString());
				} catch (NullPointerException npe) {

				}
				try {
					Bukkit.broadcastMessage("2: " + ai.getItem(1).toString());

				} catch (NullPointerException npe) {

				}
				try {
					Bukkit.broadcastMessage("3: " + ai.getItem(2).toString());
				} catch (NullPointerException npe) {

				}
				try {
					Bukkit.broadcastMessage("c: " + e.getCurrentItem().toString());
				} catch (NullPointerException npe) {

				}*/
				if (e.getCurrentItem().toString().equalsIgnoreCase(ai.getItem(2).toString())) {
					if (ai.getItem(2).getType() == Material.COMMAND || e.getCurrentItem().getType() == Material.COMMAND) {
						if (ai.getItem(2).containsEnchantment(Enchantment.DURABILITY) || e.getCurrentItem().containsEnchantment(Enchantment.DURABILITY)) {
							ItemStack newItem = ai.getItem(2);
							ItemMeta im = newItem.getItemMeta();
							String name = ChatColor.stripColor(im.getDisplayName());
							im.setDisplayName(DevathonPlugin.getInst().color("&dCookie Machine &7[&e" + name + "&7]"));
							newItem.setItemMeta(im);
						} else if (e.getCurrentItem().containsEnchantment(Enchantment.DAMAGE_ALL)) {
							ItemStack newItem = ai.getItem(2);
							ItemMeta im = newItem.getItemMeta();
							String name = ChatColor.stripColor(im.getDisplayName());
							im.setDisplayName(DevathonPlugin.getInst().color("&cMining Machine &7[&e" + name + "&7]"));
							newItem.setItemMeta(im);
						}
					}
				}
			}
		}
	}

	@EventHandler
	private void onMachineInteract(PlayerInteractEvent e){
		if (e.getAction().name().toLowerCase().contains("right")){
			if (e.getClickedBlock() != null && e.getClickedBlock().getType() == Material.COMMAND){
				e.setCancelled(true);
				CommandBlock cb = (CommandBlock)e.getClickedBlock().getState();
				if (!cb.getCommand().equalsIgnoreCase("")) {
					try{
						UUID.fromString(cb.getCommand());
					}catch (IllegalArgumentException iae){
						return;
					}
					Machine machine = getMachineFromKey(cb.getCommand());
					if (machine == null) {
						System.out.println("Not a machine");
						return;
					}
					if (machine.getOwnerUUID() == e.getPlayer().getUniqueId()) {
						e.getPlayer().sendMessage(ChatColor.GRAY + "This is your machine!");
						if (machine instanceof CookieMachine) {
							CookieMachine cm = (CookieMachine) machine;
							e.getPlayer().sendMessage(cm.getMachineStatus());
						}
					} else {
						e.getPlayer().sendMessage(ChatColor.RED + "This isn't your Machine!");
					}
				}
			}
		}
	}


}
