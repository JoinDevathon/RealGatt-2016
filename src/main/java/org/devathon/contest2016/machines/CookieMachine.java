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

	public CookieMachine(Location start, Player p, String name) {
		super(start, p, name);
		super.setRelatedClass(this);
	}

	@Override
	public void setupMachine() {
		getHomeLocation().getBlock().setType(Material.CHEST);
		getHomeLocation().add(1, 0, 0).getBlock().setType(Material.COMMAND);
		getHomeLocation().getWorld().playSound(getHomeLocation(), Sound.BLOCK_ANVIL_USE, 1, 1);
		getHomeLocation().getWorld().playEffect(getHomeLocation(), Effect.STEP_SOUND, 23);
		getHomeLocation().getWorld().playEffect(getHomeLocation().add(1, 0, 0), Effect.STEP_SOUND, 23);
		runMachine();
	}

	@Override
	protected void runMachine() {
		setTask(Bukkit.getScheduler().runTaskTimer(DevathonPlugin.getInst(), new Runnable() {
			@Override
			public void run() {
				checkCanRun();
				if (isRunning() && getHomeLocation().getBlock().getType() == Material.CHEST){
					Chest b = (Chest) getHomeLocation().getBlock();
					if (b.getInventory().contains(Material.WHEAT) && b.getInventory().contains(Material.SUGAR)){
						b.getInventory().remove(Material.WHEAT);
						b.getInventory().remove(Material.SUGAR);

						b.getWorld().dropItemNaturally(b.getLocation().add(1, 0.5, 0), new ItemStack(Material.COOKIE));
						b.getWorld().playEffect(b.getLocation(), Effect.BREWING_STAND_BREW, 0);
						b.getWorld().spawnParticle(Particle.SMOKE_NORMAL, b.getLocation().add(1, 0.5, 0), 15);
					}else{
						b.getWorld().playSound(b.getLocation().add(1, 0, 0), Sound.BLOCK_DISPENSER_FAIL, 1, 1);
						b.getWorld().spawnParticle(Particle.SMOKE_NORMAL, b.getLocation().add(1, 0.5, 0), 15);
					}
				}
			}
		}, 20, 20));
	}

	private void checkCanRun(){
		setRunning(getHomeLocation().getBlock().isBlockPowered());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onRedstoneUpdate(BlockRedstoneEvent e){
		if (e.getNewCurrent() > 0 && e.getBlock() == getHomeLocation().getBlock() && !isRunning()){
			setRunning(true);
		}else if (e.getNewCurrent() == 0){
			setRunning(false);
		}
	}

}
