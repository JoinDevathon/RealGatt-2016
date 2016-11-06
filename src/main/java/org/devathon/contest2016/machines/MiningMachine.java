package org.devathon.contest2016.machines;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.CommandBlock;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.devathon.contest2016.BlockLooping;
import org.devathon.contest2016.DevathonPlugin;
import org.devathon.contest2016.utils.Reflection;

import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * Created by zacha on 11/6/2016.
 */
public class MiningMachine extends Machine implements Listener{

	private enum State{
		MINING, RETURNINGHOME, NEEDFUEL, NEEDPOWER, SCANNING, WALKING
	}

	private enum Direction{
		NORTH, SOUTH, EAST, WEST
	}

	private int movesSinceHome = 0;

	private State currentState = State.NEEDPOWER;

	private Zombie miner;
	private MachineName minerName;

	private ArrayList<String> log = new ArrayList<>();

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

		miner = getHomeLocation().getWorld().spawn(getHomeLocation().clone().add(0, -20, 0), Zombie.class);
		miner.setMaxHealth(2000);
		miner.setHealth(miner.getMaxHealth());
		miner.setBaby(true);
		miner.setAI(false);
		/*miner.setMarker(false);
		miner.setSmall(true);
		miner.setArms(true);
		miner.setGlowing(false);
		miner.setBasePlate(false);
		miner.setGravity(true);
		miner.setVisible(false);
		miner.setCollidable(false);*/
		miner.getEquipment().setHelmet(new ItemStack(Material.IRON_HELMET));
		miner.setSilent(true);
		ItemMeta helmmeta = miner.getEquipment().getHelmet().getItemMeta();
		helmmeta.spigot().setUnbreakable(true);
		miner.getEquipment().getHelmet().setItemMeta(helmmeta);
		miner.getEquipment().setItemInMainHand(new ItemStack(Material.IRON_PICKAXE));
		miner.getEquipment().setItemInOffHand(new ItemStack(Material.TORCH));
		miner.teleport(getHomeLocation().add(0.5, 1.5, 0.5));
		miner.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 0, true, false));
		minerName.setOverrideTargetLocation(getMiner().getLocation());
		miner.setRemoveWhenFarAway(false);

		runMachine();
	}

	private void logItem(String s){
		log.add("&8(&c" + new SimpleDateFormat("HH:mm:ss.SSS").format(new Date(System.currentTimeMillis())) + "&8)&e " + s);
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
							if (getFuel() < 95) {
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
							miner.setAI(true);
						} else {
							currentState = State.NEEDFUEL;
							getMinerName().setDisplay("&c&lOUT OF FUEL");
							getMachineNameHome().setDisplay("&c&lMINER OUT OF FUEL");
							miner.setAI(false);
							getMachineNameCurrent().setDisplay("");
							b.getWorld().playSound(b.getLocation().add(0, 0, 0), Sound.BLOCK_DISPENSER_FAIL, 1, 1);
							b.getWorld().spawnParticle(Particle.SMOKE_NORMAL, getHomeLocation().clone().add(0.5, 0.5, 0.5), 15, 0.5, 0.5, 0.5, 0);
						}
					}else{
						miner.setAI(false);
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
		}, 5, 5));
	}

	private long oresFound = 0;
	private ArrayList<Block> ores = new ArrayList<>();
	private int scannedDistance = 0;
	private Block closestOre;

	private int miningPoints = 0, restartPoints = 0;

	private Location prevLoc;

	private Random rnd = new Random();

	private List<Block> buildingBlocks = new ArrayList<>();

	private Block attemptBlock;

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
			if (scannedDistance <= 50) {
				scannedDistance += 15;
				logItem("Beginning scan with radius " + scannedDistance);
				for (Block b : BlockLooping.loopSphere(miner.getLocation(), scannedDistance, true)) {
					if (isOre(b)) {
						if (!ores.contains(b) && surroundedByAir(b.getLocation())) {
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
				if (p.isOnline() && closestOre != null) {
					p.getPlayer().spawnParticle(Particle.SMOKE_NORMAL, closestOre.getLocation().add(0.5, 0.5, 0.5), 15, 0.5, 0.5, 0.5, 0);
					p.getPlayer().spawnParticle(Particle.FIREWORKS_SPARK, miner.getLocation(), 200, scannedDistance / 2, scannedDistance / 2, scannedDistance / 2, 0);
				}
			}else{
				minerName.setDisplay("&aPew! Let's go!");
				currentState = State.MINING;
			}
		}else{
			miner.getWorld().playSound(miner.getLocation(), Sound.ENTITY_IRONGOLEM_STEP, 2, 2);

			if (getHomeLocation().distance(miner.getLocation()) < 1 && currentState == State.RETURNINGHOME){
				currentState = State.SCANNING;
				scannedDistance = 0;
				if (!log.get(0).contains("Nothing else to mine. Heading home.")) {
					logItem("Nothing else to mine. Heading home.");
				}
				return;
			}
			if (getHomeLocation().distance(miner.getLocation()) > 100 || closestOre == null){
				currentState = State.RETURNINGHOME;
			}
			else{
				currentState = State.WALKING;
				if (closestOre.getLocation().distance(miner.getLocation()) < 2){
					if (rnd.nextInt() * 100 < 20){
						fuel--;
					}
					miner.setAI(false);
					currentState = State.MINING;
					miningPoints++;
					closestOre.getWorld().playEffect(closestOre.getLocation(), Effect.STEP_SOUND, closestOre.getType().getId());
					if (miningPoints == 3){
						logItem("Boyah! Mined a " + closestOre.getType().name());
						closestOre.breakNaturally(new ItemStack(Material.IRON_PICKAXE));
						miningPoints = 0;
						ores.remove(closestOre);
						closestOre = getNearestBlock(closestOre.getLocation());
						currentState = State.WALKING;
						miner.setAI(true);

					}
				}else{
					if (prevLoc == null){
						prevLoc = miner.getLocation();
					}else{
						if (prevLoc.distance(miner.getLocation()) < 0.5){
							restartPoints++;
							if (restartPoints == 15) {
								if (attemptBlock != closestOre){
									if (buildingBlocks.contains(miner.getLocation().subtract(0, 1, 0).getBlock())){
										buildingBlocks.remove(miner.getLocation().subtract(0, 1, 0).getBlock());
										miner.getLocation().subtract(0, 1, 0).getBlock().setType(Material.AIR);
										miner.getWorld().playEffect(miner.getLocation().subtract(0, 1, 0), Effect.STEP_SOUND, 3);
									}
								}
								if (closestOre.getLocation().getY() < miner.getLocation().getY() - 2){
									ores.remove(closestOre);
									closestOre = getNearestBlock(miner.getLocation());
									logItem("Gave up on getting to an " + closestOre.getType().name()+ ". I'm sorry!!!");
								}else {
									boolean goHome = true;
									if (closestOre.getLocation().getY() > miner.getLocation().getY()){
										attemptBlock = closestOre;
										for (double y = miner.getLocation().getY(); y < 256; y++){
											Location tempL = miner.getLocation().clone();
											tempL.setY(y);
											if (closestOre.getLocation().distance(tempL) < 3){
												miner.setAI(false);
												goHome = false;
												miner.teleport(miner.getLocation().getBlock().getLocation().add(0.5, 1.5, 0.5));
												miner.getLocation().add(0, -1, 0).getBlock().setType(Material.DIRT);
												buildingBlocks.add(miner.getLocation().add(0, -1, 0).getBlock());
												miner.getWorld().playEffect(miner.getLocation().subtract(0, 1, 0), Effect.STEP_SOUND, 3);
												logItem("Built up towards a block!");
												break;
											}
										}
									}
									if (goHome) {
										currentState = State.RETURNINGHOME;
									}
								}
								restartPoints = 0;
							}
						}else{
							restartPoints--;
						}
						prevLoc = miner.getLocation();
					}
				}
			}
			if (p.isOnline()) {
				p.getPlayer().spawnParticle(Particle.SMOKE_NORMAL, closestOre.getLocation().add(0.5, 0.5, 0.5), 15, 0.5, 0.5, 0.5, 0);
			}
			if (currentState == State.WALKING || currentState == State.MINING) {
				if (rnd.nextInt() * 100 < 40){
					fuel--;
				}
				moveTo(closestOre.getLocation(), 0.6);
				minerName.setDisplay("&aMining! &7(&e" + Math.round(closestOre.getLocation().distance(miner.getLocation())) + " blocks away from target&7)");
			}else{
				if (rnd.nextInt() * 100 < 20){
					fuel--;
				}
				moveTo(getHomeLocation(), 0.8);
				minerName.setDisplay("&cHeading home!");
			}
			movesSinceHome++;
		}

	}

	private boolean isOre(Block b){
		return b.getType().name().toLowerCase().replace("_", " ").contains(" ore");
	}

	private Block getNearestBlock(Location l){

		if (isOre(l.clone().add(1, 0, 0).getBlock())){
			return l.clone().add(1, 0, 0).getBlock();
		}

		if (isOre(l.clone().add(-1, 0, 0).getBlock())){
			return l.clone().add(-1, 0, 0).getBlock();
		}

		if (isOre(l.clone().add(0, 1, 0).getBlock())){
			return l.clone().add(0, 1, 0).getBlock();
		}

		if (isOre(l.clone().add(0, -1, 0).getBlock())){
			return l.clone().add(0, -1, 0).getBlock();
		}

		if (isOre(l.clone().add(0, 0, 1).getBlock())){
			return l.clone().add(0, 0, 1).getBlock();
		}

		if (isOre(l.clone().add(0, 0, -1).getBlock())){
			return l.clone().add(0, 0, -1).getBlock();
		}

		if (isOre(l.clone().add(1, 0, 1).getBlock())){
			return l.clone().add(1, 0, 1).getBlock();
		}

		if (isOre(l.clone().add(-1, 0, -1).getBlock())){
			return l.clone().add(-1, 0, -1).getBlock();
		}

		if (isOre(l.clone().add(1, 0, -1).getBlock())){
			return l.clone().add(1, 0, -1).getBlock();
		}

		if (isOre(l.clone().add(-1, 0, 1).getBlock())){
			return l.clone().add(-1, 0, 1).getBlock();
		}

		if (isOre(l.clone().add(1, 1, 1).getBlock())){
			return l.clone().add(1, 1, 1).getBlock();
		}

		if (isOre(l.clone().add(-1, 1, -1).getBlock())){
			return l.clone().add(-1, 1, -1).getBlock();
		}

		if (isOre(l.clone().add(1, 1, -1).getBlock())){
			return l.clone().add(1, 1, -1).getBlock();
		}

		if (isOre(l.clone().add(-1, 1, 1).getBlock())){
			return l.clone().add(-1, 1, 1).getBlock();
		}

		if (isOre(l.clone().add(1, -1, 1).getBlock())){
			return l.clone().add(1, -1, 1).getBlock();
		}

		if (isOre(l.clone().add(-1, -1, -1).getBlock())){
			return l.clone().add(-1, -1, -1).getBlock();
		}

		if (isOre(l.clone().add(1, -1, -1).getBlock())){
			return l.clone().add(1, -1, -1).getBlock();
		}

		if (isOre(l.clone().add(-1, -1, 1).getBlock())){
			return l.clone().add(-1, -1, 1).getBlock();
		}

		Block closestOre = null;
		for (Block b : ores) {
			if (closestOre == null){
				closestOre = b;
			}else{
				if (closestOre.getLocation().distance(l) > b.getLocation().distance(l)){
					closestOre = b;
				}
			}
		}
		return closestOre;
	}

	private boolean surroundedByAir(Location l){
		return l.clone().add(1, 0, 0).getBlock().getType().isTransparent()|| l.clone().add(-1, 0, 0).getBlock().getType().isTransparent()||
				l.clone().add(0, -1, 0).getBlock().getType().isTransparent() || l.clone().add(0, 1, 0).getBlock().getType().isTransparent() ||
				l.clone().add(0, 0, 1).getBlock().getType().isTransparent() || l.clone().add(0, 0, -1).getBlock().getType().isTransparent();
	}

	private boolean move(Direction direction){
		miner.getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(500);
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

	private boolean moveTo(Location l, double speed){
		miner.setAI(true);
		LivingEntity livStand = (LivingEntity)miner;
		try {
			Object entityInsentient = Reflection.obcClass("entity.CraftLivingEntity").getMethod("getHandle").invoke(livStand);
			Object navigation = Reflection.nmsClass("EntityInsentient").getMethod("getNavigation").invoke(entityInsentient);
			navigation.getClass().getMethod("a", double.class, double.class, double.class, double.class).invoke(navigation, l.getX(), l.getY(), l.getZ(), speed);
		} catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e1) {
			e1.printStackTrace();
			return false;
		}
		return true;
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
	private void onMinerDamage(EntityDamageEvent e){
		if (e.getEntity() == miner){
			e.setCancelled(true);
			minerName.setDisplay("&eBoop!");
		}
	}

	@EventHandler
	private void onMinerTarget(EntityTargetEvent e){
		if (e.getTarget() != null) {
			if (e.getEntity() == miner && e.getTarget().getType() == EntityType.PLAYER) {
				e.setCancelled(true);
			}
		}
	}

	private void checkCanRun(){
		setRunning(getHomeLocation().getBlock().isBlockPowered() && getFuel() > 0);
	}

	public Zombie getMiner() {
		return miner;
	}

	public MachineName getMinerName() {
		return minerName;
	}

	public long getFuel() {
		return fuel;
	}

	@Override
	protected String getExtraInformation() {
		String r = ChatColor.RED + " Log of latest things done:\n";
		int count = 10;
		int done = 0;
		if (log.size() < 15){
			count = log.size();
		}
		List<String> list = (log.size() > 15 ? log.subList(log.size() - 15, log.size()) : log);
		for (String s : list){
			if (done < count) {
				r += DevathonPlugin.getInst().color("&7[&e#" + done + "&7] &e" + s + "\n");
				done++;
			}else{
				return r;
			}
		}
		return r;
	}
}
