package org.devathon.contest2016.machines;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;
import org.devathon.contest2016.DevathonPlugin;

import java.util.UUID;

/**
 * Created by zacha on 11/6/2016.
 */
public class Machine implements Listener{

	private enum MACHINETYPE{
		MINING, COOKIE;
	}

	private Location homeLocation;
	private String name, ownerName;
	private UUID ownerUUID, machineUUID;

	private Location currentLocation;

	private Machine related;

	private BukkitTask task;

	private boolean running = false;

	private MachineName machineNameHome, machineNameCurrent;

	public Machine(Location start, Player p, String name){
		this.ownerName = p.getDisplayName();
		this.homeLocation = start;
		this.ownerUUID = p.getUniqueId();
		this.machineUUID = UUID.randomUUID();
		this.name = name;
		this.currentLocation = start;
		this.machineNameHome = new MachineName(this, this.name + " home base", 20);
		this.machineNameHome.setTargetHome(true);
		this.machineNameCurrent = new MachineName(this, this.name, 20);
	}

	public String getOwnerName() {
		return ownerName;
	}

	public MachineName getMachineNameHome() {
		return machineNameHome;
	}

	public MachineName getMachineNameCurrent() {
		return machineNameCurrent;
	}

	protected boolean isRunning() {
		return running;
	}

	protected void setRunning(boolean running) {
		this.running = running;
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

	public void destroy(String reason){
		getHomeLocation().getBlock().setType(Material.AIR);
		getCurrentLocation().getBlock().setType(Material.AIR);
		getMachineNameHome().destroy();
		getMachineNameCurrent().destroy();
		System.out.println("destroyed machine " + getName() + ". reason: " + reason);
	}


	protected void setRelatedClass(Machine related) {
		this.related = related;
	}


	public Machine getRelatedClass() {
		return related;
	}

	public Location getHomeLocation() {
		return homeLocation.clone();
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
		return currentLocation.clone();
	}

	protected void setCurrentLocation(Location currentLocation) {
		this.currentLocation = currentLocation.clone();
	}


	public String getMachineStatus(){
		if (isRunning()){
			return DevathonPlugin.getInst().color("&a Current Status: Running ✔    " + getExtraInformation());
		}else{
			return DevathonPlugin.getInst().color("&c Current Status: Not Running ❌    " + getExtraInformation());
		}
	}

	protected String getExtraInformation(){
		return "";
	}

}
