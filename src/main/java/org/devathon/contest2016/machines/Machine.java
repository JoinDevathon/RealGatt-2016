package org.devathon.contest2016.machines;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;

import java.util.UUID;

/**
 * Created by zacha on 11/6/2016.
 */
public class Machine implements Listener{

	private enum MACHINETYPE{
		MINING, COOKIE;
	}

	private Location homeLocation;
	private String name;
	private UUID ownerUUID, machineUUID;

	private Location currentLocation;

	private Machine related;

	private BukkitTask task;

	public Machine(Location start, Player p, String name){
		this.homeLocation = start;
		this.ownerUUID = p.getUniqueId();
		this.machineUUID = UUID.randomUUID();
		this.name = name;
		this.currentLocation = start;
	}

	protected void setupMachine(){

	}

	protected void runMachine(){

	}

	protected BukkitTask getTask() {
		return task;
	}

	protected void setTask(BukkitTask task) {
		this.task = task;
	}

	public void destroy(){

	}


	protected void setRelatedClass(Machine related) {
		this.related = related;
	}


	public Machine getRelatedClass() {
		return related;
	}

	public Location getHomeLocation() {
		return homeLocation;
	}

	public String getName() {
		return name;
	}

	public UUID getOwnerUUID() {
		return ownerUUID;
	}

	public UUID getMachineUUID() {
		return machineUUID;
	}

	public Location getCurrentLocation() {
		return currentLocation;
	}

	protected void setCurrentLocation(Location currentLocation) {
		this.currentLocation = currentLocation;
	}
}
