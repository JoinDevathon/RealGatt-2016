package org.devathon.contest2016.machines;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.devathon.contest2016.DevathonPlugin;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Created by zacha on 11/6/2016.
 */
public class MachineManager {

	private HashMap<UUID, Machine> machineStorage = new HashMap<>();

	public void registerMachine(Machine machine){
		machineStorage.put(machine.getMachineUUID(), machine);
		Bukkit.getPluginManager().registerEvents(machine, DevathonPlugin.getInst());
	}

	public Machine getMachineAtLocation(Location l){ // Horrible way of doing this, I'll change it later.
		for (UUID u : machineStorage.keySet()){
			if (machineStorage.get(u).getCurrentLocation() == l){
				return machineStorage.get(u);
			}
		}
		return null;
	}

	public Collection<Machine> getAllMachines(){
		return machineStorage.values();
	}

}
