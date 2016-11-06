package org.devathon.contest2016.machines;

import org.bukkit.*;
import org.bukkit.block.Chest;
import org.bukkit.block.CommandBlock;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.inventory.ItemStack;
import org.devathon.contest2016.DevathonPlugin;

/**
 * Created by zacha on 11/6/2016.
 */
public class CookieMachine extends Machine implements Listener{

	private long cookiesMade = 0;

	public CookieMachine(Location start, Player p, String name) {
		super(start, p, name);
		super.setRelatedClass(this);
		setupMachine();
	}

	@Override
	public void setupMachine() {
		getHomeLocation().clone().add(1, 0, 0).getBlock().setType(Material.CHEST);
		getHomeLocation().getWorld().playSound(getHomeLocation(), Sound.BLOCK_ANVIL_USE, 1, 1);
		getHomeLocation().getWorld().playEffect(getHomeLocation(), Effect.STEP_SOUND, 137);
		getHomeLocation().getWorld().playEffect(getHomeLocation().clone().add(1, 0, 0), Effect.STEP_SOUND, 137);
		setCurrentLocation(getHomeLocation().clone().add(1, 0, 0));
		CommandBlock cb = (CommandBlock)getHomeLocation().getBlock().getState();
		cb.setCommand(getMachineUUID().toString());
		runMachine();
		getMachineNameCurrent().setDisplay("&ePut Wheat and Sugar in here");
		getMachineNameHome().setTargetHome(true);
		getMachineNameHome().setUpdateTime(20);
		getMachineNameHome().setDisplay("&7" + getOwnerName() + "'s Cookie Machine");
	}

	@Override
	protected void runMachine() {
		setTask(Bukkit.getScheduler().runTaskTimer(DevathonPlugin.getInst(), new Runnable() {
			@Override
			public void run() {
				checkCanRun();
				if (getHomeLocation().getBlock().getType() == Material.COMMAND && getCurrentLocation().getBlock().getType() == Material.CHEST){
					CommandBlock cb = (CommandBlock)getHomeLocation().getBlock().getState();
					cb.setCommand(getMachineUUID().toString());
					cb.update();
					Chest b = (Chest) getHomeLocation().clone().add(1, 0, 0).getBlock().getState();
					if (isRunning()) {
						if (b.getInventory().contains(Material.WHEAT) && b.getInventory().contains(Material.SUGAR)) {
							getMachineNameHome().setDisplay("&a" + getOwnerName() + "'s Cookie Machine  ✔");
							ItemStack sugar = b.getInventory().getItem(b.getInventory().first(Material.SUGAR));
							if (sugar.getAmount() - 1 > 0) {
								b.getInventory().getItem(b.getInventory().first(Material.SUGAR)).setAmount(sugar.getAmount() - 1);
							}else{
								b.getInventory().remove(b.getInventory().getItem(b.getInventory().first(Material.SUGAR)));
							}

							ItemStack wheat = b.getInventory().getItem(b.getInventory().first(Material.WHEAT));

							if (wheat.getAmount() - 1 > 0){
								b.getInventory().getItem(b.getInventory().first(Material.WHEAT)).setAmount(wheat.getAmount() - 1);
							}else{
								b.getInventory().remove(b.getInventory().getItem(b.getInventory().first(Material.WHEAT)));
							}


							b.getWorld().playEffect(b.getLocation(), Effect.BREWING_STAND_BREW, 0);
							b.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, getHomeLocation().clone().add(0.5, 0.5, 0.5), 15, 0.5, 0.5, 0.5, 0);
							b.getWorld().dropItemNaturally(getHomeLocation().add(0.5, 1.5, 0.5), new ItemStack(Material.COOKIE));
							cookiesMade++;
							getMachineNameCurrent().setDisplay("");
						} else {
							getMachineNameCurrent().setDisplay("&ePut Wheat and Sugar in here");
							getMachineNameHome().setDisplay("&7" + getOwnerName() + "'s Cookie Machine  ?");
							b.getWorld().playSound(b.getLocation().add(0, 0, 0), Sound.BLOCK_DISPENSER_FAIL, 1, 1);
							b.getWorld().spawnParticle(Particle.SMOKE_NORMAL, getHomeLocation().clone().add(0.5, 0.5, 0.5), 15, 0.5, 0.5, 0.5, 0);
						}
					}else{
						getMachineNameCurrent().setDisplay("&ePut Wheat and Sugar in here");
						getMachineNameHome().setDisplay("&c" + getOwnerName() + "'s Cookie Machine  X");
						b.getWorld().spawnParticle(Particle.BARRIER, getHomeLocation().clone().add(0.5, 1.5, 0.5), 5, 0, 0, 0);
					}
				}else{
					destroy("home or current location is not machine. expected COMMAND and CHEST, but got: \n" + getHomeLocation().getBlock().getType() + " and " + getCurrentLocation().getBlock().getType());
					getTask().cancel();
				}
			}
		}, 20, 20));
	}

	@Override
	public void destroy(String reason){
		getHomeLocation().getBlock().setType(Material.AIR);
		getMachineNameHome().destroy();
		getMachineNameCurrent().destroy();
		System.out.println("destroyed machine " + getName() + ". reason: " + reason);
	}

	private void checkCanRun(){
		setRunning(getHomeLocation().getBlock().isBlockPowered());
		/*  || getHomeLocation().add(1, 0, 0).getBlock().isBlockPowered() ||
			getHomeLocation().add(0, 1, 0).getBlock().isBlockPowered() || getHomeLocation().add(0, 0, 1).getBlock().isBlockPowered() ||
			getHomeLocation().add(-1, 0, 0).getBlock().isBlockPowered() || getHomeLocation().add(0, -1, 0).getBlock().isBlockPowered() ||
			getHomeLocation().add(0, 0, -1).getBlock().isBlockPowered()*/
	}

	@Override
	protected String getExtraInformation() {
		return "Cookies Made: " + cookiesMade;
	}
}
