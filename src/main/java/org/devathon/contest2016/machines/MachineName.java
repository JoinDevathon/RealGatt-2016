package org.devathon.contest2016.machines;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.ArmorStand;
import org.bukkit.scheduler.BukkitTask;
import org.devathon.contest2016.DevathonPlugin;

/**
 * Created by zacha on 11/6/2016.
 */
public class MachineName {

	private String display;
	private Machine machine;
	private long updateTime;

	private BukkitTask task;

	private ArmorStand stand;

	private boolean targetHome;

	public MachineName(Machine machine, String name, long updatetime){
		this.display = name;
		this.machine = machine;
		this.updateTime = updatetime;
		stand = machine.getCurrentLocation().getWorld().spawn(machine.getCurrentLocation().add(0.5, 1.5, 0.5), ArmorStand.class);
		stand.setMarker(true);
		stand.setVisible(false);
		stand.setSmall(true);
		stand.setGravity(false);
		stand.setCustomNameVisible(true);
		stand.setCustomName(cc(name));
		this.targetHome = false;
		startTask();
	}

	public void destroy(){
		this.stand.remove();
		this.task.cancel();
	}

	private void startTask(){
		this.task = Bukkit.getScheduler().runTaskTimer(DevathonPlugin.getInst(), new Runnable() {
			@Override
			public void run() {
				if (display.equalsIgnoreCase("")){
					stand.setCustomNameVisible(false);
				}else{
					stand.setCustomNameVisible(true);
				}
				if (targetHome) {
					stand.teleport(machine.getHomeLocation().add(0.5, 1.5, 0.5));
				}else{
					stand.teleport(machine.getCurrentLocation().add(0.5, 1.5, 0.5));
				}
				stand.setCustomName(cc(display));
			}
		}, updateTime, updateTime);
	}

	public String getDisplay() {
		return display;
	}

	public void setDisplay(String display) {
		this.display = display;
	}

	public Machine getMachine() {
		return machine;
	}

	public long getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
	}

	public BukkitTask getTask() {
		return task;
	}

	public void setTask(BukkitTask task) {
		this.task = task;
	}

	public boolean doesTargetHome() {
		return targetHome;
	}

	public void setTargetHome(boolean targetHome) {
		this.targetHome = targetHome;
	}

	private String cc(String s){
		return ChatColor.translateAlternateColorCodes('&', s);
	}

}
