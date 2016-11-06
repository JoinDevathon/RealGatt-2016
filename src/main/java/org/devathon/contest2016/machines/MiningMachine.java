package org.devathon.contest2016.machines;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.CommandBlock;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.devathon.contest2016.BlockLooping;
import org.devathon.contest2016.DevathonPlugin;

import java.util.ArrayList;

/**
 * Created by zacha on 11/6/2016.
 */
public class MiningMachine extends Machine implements Listener{

	private enum State{
		MINING, RETURNINGHOME, NEEDFUEL, NEEDPOWER, SCANNING
	}

	private enum Direction{
		NORTH, SOUTH, EAST, WEST
	}

	private int movesSinceHome = 0;

	private State currentState = State.NEEDPOWER;

	private ArmorStand miner;
	private MachineName minerName;

	private long fuel = 0;


	public MiningMachine(Location start, Player p, String name) {
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
		getMachineNameCurrent().setDisplay("&7Fuel goes here");
		getMachineNameHome().setDisplay("&7" + getOwnerName() + "'s Control Panel");

		minerName = new MachineName(this, "Your Miner", 10);
		minerName.setDisplay("");
		minerName.setOverrideTarget(true);

		miner = getHomeLocation().getWorld().spawn(getHomeLocation().clone().add(0, -20, 0), ArmorStand.class);
		miner.setMarker(false);
		miner.setSmall(true);
		miner.setArms(true);
		miner.setGlowing(false);
		miner.setBasePlate(false);
		miner.setGravity(true);
		miner.setVisible(false);
		miner.setCollidable(false);
		miner.getEquipment().setHelmet(new ItemStack(Material.COMMAND));
		miner.getEquipment().setItemInMainHand(new ItemStack(Material.IRON_PICKAXE));
		miner.getEquipment().setItemInOffHand(new ItemStack(Material.TORCH));
		miner.teleport(getHomeLocation().add(0.5, 1.5, 0.5));
		minerName.setOverrideTargetLocation(getMiner().getLocation());

		runMachine();

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
						if (getFuel() > 0){
							runMover();
							b.getWorld().spawnParticle(Particle.REDSTONE, getHomeLocation().clone().add(0.5, 0.5, 0.5), 15, 0.5, 0.5, 0.5, 0);
							if (getFuel() < 100) {
								if (b.getInventory().contains(Material.COAL)) {
									ItemStack coal = b.getInventory().getItem(b.getInventory().first(Material.COAL));
									if (coal.getAmount() - 1 > 0) {
										b.getInventory().getItem(b.getInventory().first(Material.COAL)).setAmount(coal.getAmount() - 1);
									} else {
										b.getInventory().remove(b.getInventory().getItem(b.getInventory().first(Material.COAL)));
									}
									fuel += 5;
									b.getWorld().spawnParticle(Particle.SMOKE_NORMAL, getHomeLocation().clone().add(0.5, 0.5, 0.5), 15, 0.5, 0.5, 0.5, 0);
								}
								if (b.getInventory().contains(Material.COAL_BLOCK)) {
									ItemStack coalB = b.getInventory().getItem(b.getInventory().first(Material.COAL_BLOCK));

									if (coalB.getAmount() - 1 > 0) {
										b.getInventory().getItem(b.getInventory().first(Material.COAL_BLOCK)).setAmount(coalB.getAmount() - 1);
									} else {
										b.getInventory().remove(b.getInventory().getItem(b.getInventory().first(Material.COAL_BLOCK)));
									}
									fuel += 30;
									b.getWorld().spawnParticle(Particle.SMOKE_NORMAL, getHomeLocation().clone().add(0.5, 0.5, 0.5), 15, 0.5, 0.5, 0.5, 0);
								}
								if (getFuel() > 100){
									fuel = 100;
								}
							}
							getMinerName().setOverrideTargetLocation(miner.getLocation().add(0, 1, 0));
							getMachineNameCurrent().setDisplay("&aMiner is running.");
							getMachineNameHome().setDisplay("&aFuel Level: " +  getFuel() +"%");
						} else {
							currentState = State.NEEDFUEL;
							getMinerName().setDisplay("&c&lOUT OF FUEL");
							getMachineNameHome().setDisplay("&c&lMINER OUT OF FUEL");
							getMachineNameCurrent().setDisplay("");
							b.getWorld().playSound(b.getLocation().add(0, 0, 0), Sound.BLOCK_DISPENSER_FAIL, 1, 1);
							b.getWorld().spawnParticle(Particle.SMOKE_NORMAL, getHomeLocation().clone().add(0.5, 0.5, 0.5), 15, 0.5, 0.5, 0.5, 0);
						}
					}else{
						if (getFuel() <= 100) {
							if (b.getInventory().contains(Material.COAL)) {
								ItemStack coal = b.getInventory().getItem(b.getInventory().first(Material.COAL));
								if (coal.getAmount() - 1 > 0) {
									b.getInventory().getItem(b.getInventory().first(Material.COAL)).setAmount(coal.getAmount() - 1);
								} else {
									b.getInventory().remove(b.getInventory().getItem(b.getInventory().first(Material.COAL)));
								}
								fuel += 5;
								b.getWorld().spawnParticle(Particle.SMOKE_NORMAL, getHomeLocation().clone().add(0.5, 0.5, 0.5), 15, 0.5, 0.5, 0.5, 0);
							}
							if (b.getInventory().contains(Material.COAL_BLOCK)) {
								ItemStack coalB = b.getInventory().getItem(b.getInventory().first(Material.COAL_BLOCK));

								if (coalB.getAmount() - 1 > 0) {
									b.getInventory().getItem(b.getInventory().first(Material.COAL_BLOCK)).setAmount(coalB.getAmount() - 1);
								} else {
									b.getInventory().remove(b.getInventory().getItem(b.getInventory().first(Material.COAL_BLOCK)));
								}
								fuel += 30;
								b.getWorld().spawnParticle(Particle.SMOKE_NORMAL, getHomeLocation().clone().add(0.5, 0.5, 0.5), 15, 0.5, 0.5, 0.5, 0);
							}
						}
						currentState = State.NEEDPOWER;
						getMinerName().setDisplay("&cCan't locate the home console! Paused!");
						getMachineNameCurrent().setDisplay("&cNo power going to Control Panel");
						getMachineNameHome().setDisplay("&cFuel Level: &l" +  getFuel() +"%");
						b.getWorld().spawnParticle(Particle.BARRIER, getHomeLocation().clone().add(0.5, 1.5, 0.5), 5, 0, 0, 0);
					}
				}else{
					destroy("home or current location is not machine. expected COMMAND and CHEST, but got: \n" + getHomeLocation().getBlock().getType() + " and " + getCurrentLocation().getBlock().getType());
					getTask().cancel();
				}
			}
		}, 20, 20));
	}

	private long oresFound = 0;
	private ArrayList<Block> ores = new ArrayList<>();
	private int scannedDistance = 0;
	private Block closestOre;

	private void runMover(){

		OfflinePlayer p = Bukkit.getPlayer(getOwnerUUID());
		if (movesSinceHome == 0){
			if (!move(Direction.NORTH)){
				if (!move(Direction.SOUTH)){
					if (!move(Direction.EAST)){
						if (!move(Direction.WEST)){
							minerName.setDisplay("&c&lHelp! I'm trapped!");
							return;
						}
					}
				}
			}
			minerName.setDisplay("&e&oScanning for ores...");
			currentState = State.SCANNING;
			movesSinceHome++;
		}

		if (currentState == State.SCANNING){

			if (scannedDistance <= 100) {
				scannedDistance += 15;
				for (Block b : BlockLooping.loopSphere(miner.getLocation(), scannedDistance, true)) {
					if (b.getType().name().toLowerCase().replace("_", " ").contains(" ore")) {
						if (!ores.contains(b)) {
							if (closestOre == null){
								closestOre = b;
							}else{
								if (closestOre.getLocation().distance(miner.getLocation()) > b.getLocation().distance(miner.getLocation())){
									closestOre = b;
								}
							}
							p.getPlayer().spawnParticle(Particle.END_ROD, b.getLocation().add(0.5, 0.5, 0.5), 10, 0.5, 0.5, 0.5);
							oresFound++;
							ores.add(b);
						}
					}
				}
				minerName.setDisplay("&e&oScanning for ores... &7(&eFound " + oresFound + " within a " + scannedDistance + " block range&7)");
				if (p.isOnline()) {
					p.getPlayer().spawnParticle(Particle.SMOKE_NORMAL, closestOre.getLocation().add(0.5, 0.5, 0.5), 15, 0.5, 0.5, 0.5, 0);
					p.getPlayer().spawnParticle(Particle.FIREWORKS_SPARK, miner.getLocation(), 200, scannedDistance / 2, scannedDistance / 2, scannedDistance / 2, 0);
				}
			}else{
				minerName.setDisplay("&aPew! Let's go!");
				currentState = State.MINING;
			}
		}else{
			minerName.setDisplay("&aMining~ &7(&e" + Math.round(closestOre.getLocation().distance(miner.getLocation())) + " blocks away from target&7)");
			if (getHomeLocation().distance(miner.getLocation()) > 100){
				currentState = State.RETURNINGHOME;
			}
			if (p.isOnline()) {
				p.getPlayer().spawnParticle(Particle.SMOKE_NORMAL, closestOre.getLocation().add(0.5, 0.5, 0.5), 15, 0.5, 0.5, 0.5, 0);
			}
			movesSinceHome++;
		}

	}

	private boolean move(Direction direction){
		Location targLoc = miner.getLocation();
		float yaw = 0;
		if (direction == Direction.NORTH){
			targLoc.add(0, 0, -1);
			yaw = 180;
		}
		if (direction == Direction.SOUTH){
			targLoc.add(0, 0, 1);
			yaw = 0;
		}
		if (direction == Direction.WEST){
			targLoc.add(-1, 0, 0);
			yaw = 90;
		}
		if (direction == Direction.EAST){
			targLoc.add(1, 0, 0);
			yaw = -90;
		}
		targLoc.setYaw(yaw);
		if (targLoc.getBlock().getType() == Material.AIR){
			miner.teleport(targLoc);
			return true;
		}else{
			if (targLoc.add(0, 1, 0).getBlock().getType() == Material.AIR){
				miner.teleport(targLoc);
				return true;
			}
		}
		return false;
	}

	private boolean moveTo(Location l){
		try {
			Object entityInsentient = Reflection.obcClass("entity.CraftLivingEntity").getMethod("getHandle").invoke(e);
			Object navigation = Reflection.nmsClass("EntityInsentient").getMethod("getNavigation").invoke(entityInsentient);
			navigation.getClass().getMethod("a", double.class, double.class, double.class, double.class).invoke(navigation, l.getX(), l.getY(), l.getZ(), s.doubleValue());
		} catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e1) {
			e1.printStackTrace();
		}
	}

	@Override
	public void destroy(String reason){
		getHomeLocation().getBlock().setType(Material.AIR);
		getMachineNameHome().destroy();
		getMachineNameCurrent().destroy();
		getMinerName().destroy();
		miner.remove();
		System.out.println("destroyed machine " + getName() + ". reason: " + reason);
	}

	@EventHandler
	private void onStandClick(PlayerInteractAtEntityEvent e){
		if (e.getRightClicked() == miner){
			e.setCancelled(true);
			minerName.setDisplay("&eBoop!");
		}
	}

	private void checkCanRun(){
		setRunning(getHomeLocation().getBlock().isBlockPowered() && getFuel() > 0);
	}

	public ArmorStand getMiner() {
		return miner;
	}

	public MachineName getMinerName() {
		return minerName;
	}

	public long getFuel() {
		return fuel;
	}
}
