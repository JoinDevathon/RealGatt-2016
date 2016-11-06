package org.devathon.contest2016.machines;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;

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
		getHomeLocation().getBlock().setType(Material.DISPENSER);
		getHomeLocation().getBlock().setData((byte)1);
		getHomeLocation().getWorld().playSound(getHomeLocation(), Sound.BLOCK_ANVIL_USE, 1, 1);
		getHomeLocation().getWorld().playEffect(getHomeLocation(), Effect.STEP_SOUND, 23);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	private void onRedstoneUpdate(BlockRedstoneEvent e){
		if (e.getNewCurrent() > 0 && e.getBlock() == getHomeLocation().getBlock()){

		}
	}

}
