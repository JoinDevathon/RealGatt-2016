package org.devathon.contest2016;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.devathon.contest2016.machines.Machine;
import org.devathon.contest2016.machines.MachineManager;

import java.util.ArrayList;
import java.util.List;

public class DevathonPlugin extends JavaPlugin {

	private static DevathonPlugin inst;

	private MachineManager machineManager;

    @Override
    public void onEnable() {
	    this.inst = this;
	    registerRecipes();
	    this.machineManager = new MachineManager();
	    Bukkit.getPluginManager().registerEvents(getMachineManager(), this);
	    System.out.println("Evoltr is bae. Evoltr x Gatt 2016. Everyone ships it. (Do I get extra points now, bae? <3)");
    }

    @Override
    public void onDisable() {
	    for (Machine m : getMachineManager().getAllMachines()){
		    m.destroy("server shutdown");
	    }
    }


    private void registerRecipes(){
	    ItemStack miningMachine = new ItemStack(Material.COMMAND);
	    ItemMeta im = miningMachine.getItemMeta();
	    im.setDisplayName(color("&cMining Machine &7[&eNo Name&7]"));
	    List<String> loreList = new ArrayList<>();
	    loreList.add(color("&eRename me in an anvil to set my name!"));
	    im.setLore(loreList);
	    im.addItemFlags(ItemFlag.values());
	    im.spigot().setUnbreakable(true);
	    im.addEnchant(Enchantment.DAMAGE_ALL, 1, true);
	    miningMachine.setItemMeta(im);
	    ShapelessRecipe machineRecipe = new ShapelessRecipe(miningMachine);
	    machineRecipe.addIngredient(2, Material.REDSTONE)
			    .addIngredient(1, Material.IRON_PICKAXE)
			    .addIngredient(1, Material.CHEST)
			    .addIngredient(1, Material.STONE_BUTTON);

	    ItemStack cookieMachine = new ItemStack(Material.COMMAND);
	    im = cookieMachine.getItemMeta();
	    im.setDisplayName(color("&dCookie Machine &7[&eNo Name&7]"));
	    im.setLore(loreList);
	    im.addItemFlags(ItemFlag.values());
	    im.spigot().setUnbreakable(true);
	    im.addEnchant(Enchantment.DURABILITY, 1, true);
	    cookieMachine.setItemMeta(im);
	    ShapelessRecipe cookieRecipe = new ShapelessRecipe(cookieMachine);
	    cookieRecipe.addIngredient(2, Material.REDSTONE)
			    .addIngredient(1, Material.COOKIE)
			    .addIngredient(1, Material.STONE_BUTTON).addIngredient(1, Material.CHEST);

	    Bukkit.addRecipe(machineRecipe);
	    Bukkit.addRecipe(cookieRecipe);
    }

	public static DevathonPlugin getInst() {
		return inst;
	}

	public MachineManager getMachineManager() {
		return machineManager;
	}

	public String color(String s){
		return ChatColor.translateAlternateColorCodes('&', s);
	}

}

