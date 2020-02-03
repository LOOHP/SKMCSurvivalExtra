package com.loohp.skmcextra;

import java.io.File;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
//import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.Validate;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
//import org.bukkit.plugin.Plugin;
//import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.Vector;

import com.earth2me.essentials.Essentials;
import com.griefcraft.lwc.LWCPlugin;
import com.loohp.skmcextra.Events.EventsClass;
import com.onarandombox.MultiverseCore.MultiverseCore;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.milkbowl.vault.chat.Chat;

public class Main extends JavaPlugin{
	
	public static Scoreboard scoreboard;
	public static long tpaCooldown = 5;
	public static long PhantomShieldsLifeTime = 2592000;
	public static long PhantomShieldsRange = 150;
	
	public static FileConfiguration config;
	public static File cfile;
	
	public static Plugin skmcsx = null;
	public static Essentials ess3 = null;
	public static MultiverseCore mv = null;
	public static LWCPlugin lwc = null;
	public static Chat chat = null;
	
	public static Map<Player, String> homeWarmUp = new HashMap<Player, String>();
	public static Map<Player, String> spawnWarmUp = new HashMap<Player, String>();
	public static Map<Player, String> backWarmUp = new HashMap<Player, String>();
	
	public static Map<Player, String> tpaWait = new HashMap<Player, String>();
	public static Map<Player, String> tpaCacnelTimer = new HashMap<Player, String>();
	
	public static Map<Player, String> tpaWarmUp = new HashMap<Player, String>();
	public static Map<Player, String> tpahereWarmUp = new HashMap<Player, String>();
	
	public static Map<Player, Long> possiblePS = new HashMap<Player, Long>();
	public static Map<Player, UUID> PSPlayerPair = new HashMap<Player, UUID>();	
	public static Map<Player, Long> PSSpawnTimerLeft = new HashMap<Player, Long>();
	public static Map<Entity, Inventory> PSGUIedit = new HashMap<Entity, Inventory>();
	
	public static Map<Player, Entity> PSEditRange = new HashMap<Player, Entity>();
	public static Map<Player, Long> PSEditRangeTimeOut = new HashMap<Player, Long>();

	@Override
	public void onEnable() {
		skmcsx = getServer().getPluginManager().getPlugin("SKMCSurvivalExtra");
		ess3 = (Essentials)getServer().getPluginManager().getPlugin("Essentials");
		mv = (MultiverseCore)getServer().getPluginManager().getPlugin("Multiverse-Core");
		lwc = (LWCPlugin)getServer().getPluginManager().getPlugin("LWC");
		RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
        chat = rsp.getProvider();
		
		getCommand("randomtp").setExecutor(new Commands());
		getCommand("cooldown").setExecutor(new Commands());
		getCommand("displaychest").setExecutor(new Commands());
		getCommand("phantomshield").setExecutor(new Commands());
		getCommand("skmcsx").setExecutor(new Commands());
		
		homeWarmUp.clear();
		spawnWarmUp.clear();
		backWarmUp.clear();
		
		tpaWait.clear();
		tpaCacnelTimer.clear();
		
		tpaWarmUp.clear();
		tpahereWarmUp.clear();
		
		possiblePS.clear();
		PSPlayerPair.clear();
		PSGUIedit.clear();
		
		scoreboard = this.getServer().getScoreboardManager().getMainScoreboard();
		getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "SKMC Survival Extra has been Enabled!");
		getServer().getPluginManager().registerEvents(new EventsClass(), this);
		
		if (scoreboard.getTeam("ItemExpireYellow") == null) {
			scoreboard.registerNewTeam("ItemExpireYellow");			
		}
		scoreboard.getTeam("ItemExpireYellow").setColor(ChatColor.YELLOW);
		
		if (scoreboard.getTeam("ItemExpireRed") == null) {
			scoreboard.registerNewTeam("ItemExpireRed");
		}
		scoreboard.getTeam("ItemExpireRed").setColor(ChatColor.RED);
		
		if (scoreboard.getTeam("PhantomShield") == null) {
			scoreboard.registerNewTeam("PhantomShield");
		}
		scoreboard.getTeam("PhantomShield").setColor(ChatColor.LIGHT_PURPLE);
		
		skmcsx.getConfig().options().copyDefaults(true);
		skmcsx.saveConfig();
		tpaCooldown = skmcsx.getConfig().getLong("tpaCooldown");
		PhantomShieldsLifeTime = skmcsx.getConfig().getLong("PhantomShieldsLifeTime");
		PhantomShieldsRange = skmcsx.getConfig().getLong("PhantomShieldsRange");

		//Runnable
		velocityRunnable();
		itemNameRunnable();
		checkTpCancel();
		phantomShield();
		phantomShieldSync();
		phantomShieldClearMap();
	}

	@Override
	public void onDisable( ) {
		getServer().getConsoleSender().sendMessage(ChatColor.RED + "SKMC Survival Extra has been Disabled!");
		scoreboard.getTeam("ItemExpireYellow").unregister();
		scoreboard.getTeam("ItemExpireRed").unregister();
	}
	
	public static void loadConfig() {
		skmcsx.reloadConfig();
		tpaCooldown = skmcsx.getConfig().getLong("tpaCooldown");
		PhantomShieldsLifeTime = skmcsx.getConfig().getLong("PhantomShieldsLifeTime");
		PhantomShieldsRange = skmcsx.getConfig().getLong("PhantomShieldsRange");
		
		homeWarmUp.clear();
		spawnWarmUp.clear();
		backWarmUp.clear();
		
		tpaWait.clear();
		tpaCacnelTimer.clear();
		
		tpaWarmUp.clear();
		tpahereWarmUp.clear();
		
		possiblePS.clear();
		PSPlayerPair.clear();
		PSGUIedit.clear();
		
		if (scoreboard.getTeam("ItemExpireYellow") == null) {
			scoreboard.registerNewTeam("ItemExpireYellow");			
		}
		scoreboard.getTeam("ItemExpireYellow").setColor(ChatColor.YELLOW);
		
		if (scoreboard.getTeam("ItemExpireRed") == null) {
			scoreboard.registerNewTeam("ItemExpireRed");
		}
		scoreboard.getTeam("ItemExpireRed").setColor(ChatColor.RED);
		
		if (scoreboard.getTeam("PhantomShield") == null) {
			scoreboard.registerNewTeam("PhantomShield");
		}
		scoreboard.getTeam("PhantomShield").setColor(ChatColor.LIGHT_PURPLE);
	}
	
	public static void setBack(Player player) {
		if (ess3 != null){
			Main.ess3.getUser(player).setLastLocation();
		}
	}
	
	@SuppressWarnings("deprecation")
	public static void exeTeleport(Player player, Location loc) throws Throwable {
		if (ess3 != null){
			Main.ess3.getUser(player).getTeleport().teleport(loc, null);;
		}
	}
	
	public static void openPSGUI(Player player, Entity entity) {
		if (!PSGUIedit.containsKey(entity)) {
			PSGUIedit.put(entity, Bukkit.createInventory(null, 27, "§aEdit §dPhantom §6Shield"));
		}
		long range = Main.skmcsx.getConfig().getLong(("PhantomShields." + entity.getUniqueId().toString() + ".Range"));
		String owner = Main.skmcsx.getConfig().getString(("PhantomShields." + entity.getUniqueId().toString() + ".Owner"));
		if (Main.chat.getPlayerPrefix(Bukkit.getPlayer(owner)) != null && Main.chat.getPlayerPrefix(Bukkit.getPlayer(owner)) != "") {
			owner = Main.chat.getPlayerPrefix(Bukkit.getPlayer(owner)) + owner;
		}
		owner = owner.replace("&", "§");
		String locX = Main.skmcsx.getConfig().getString(("PhantomShields." + entity.getUniqueId().toString() + ".X"));
		String locY = Main.skmcsx.getConfig().getString(("PhantomShields." + entity.getUniqueId().toString() + ".Y"));
		String locZ = Main.skmcsx.getConfig().getString(("PhantomShields." + entity.getUniqueId().toString() + ".Z"));
		String world = Main.skmcsx.getConfig().getString(("PhantomShields." + entity.getUniqueId().toString() + ".World"));
		world = Main.mv.getMVWorldManager().getMVWorld(world).getAlias();
		world = world.replace("&", "§");
		
		String blocksuffix = "blocks";
		if (range == 1) {
			blocksuffix = "block";
		}
		PSGUIedit.get(entity).clear();
		PSGUIedit.get(entity).setItem(0, createItem(Material.WITHER_SKELETON_SKULL, "§6Infomation", "§eOwner: §f" + owner, "§eX: §f" + locX, "§eY: §f" + locY, "§eZ: §f" + locZ, "§eWorld: §f" + world, "§eRange: §f" + String.valueOf(range) + " " + blocksuffix, "§eID: §f" + entity.getUniqueId().toString()));
		PSGUIedit.get(entity).setItem(12, createItem(Material.BOW, "§eModify the effect range", "§bCurrently, it has a radius of §d" + range));
		PSGUIedit.get(entity).setItem(14, createItem(Material.BARRIER, "§cRemove this Phantom Shield"));
		player.openInventory(PSGUIedit.get(entity));
	}
	
	public static ItemStack createItem(Material material, String name, String...lore) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        ArrayList<String> metaLore = new ArrayList<String>();  

        for(String loreComments : lore) {
            metaLore.add(loreComments);
        }

        meta.setLore(metaLore);
        item.setItemMeta(meta);
        return item;
    }
	
    public static BlockFace getClosestFace(float direction) {

        direction = direction % 360;

        if (direction < 0)
            direction += 360;

        direction = Math.round(direction / 45);

        switch ((int) direction) {
        case 0:
            return BlockFace.WEST;
        case 1:
            return BlockFace.NORTH_WEST;
        case 2:
            return BlockFace.NORTH;
        case 3:
            return BlockFace.NORTH_EAST;
        case 4:
            return BlockFace.EAST;
        case 5:
            return BlockFace.SOUTH_EAST;
        case 6:
            return BlockFace.SOUTH;
        case 7:
            return BlockFace.SOUTH_WEST;
        default:
            return BlockFace.WEST;
        }
    }
	
	public static void drawLine(Location point1, Location point2, double space) {
	    World world = point1.getWorld();
	    Validate.isTrue(point2.getWorld().equals(world), "Lines cannot be in different worlds!");
	    double distance = point1.distance(point2);
	    Vector p1 = point1.toVector();
	    Vector p2 = point2.toVector();
	    Vector vector = p2.clone().subtract(p1).normalize().multiply(space);
	    double length = 0;
	    for (; length < distance; p1.add(vector)) {
	    	Particle.DustOptions dustOptions = new Particle.DustOptions(Color.ORANGE, 1);
	        world.spawnParticle(Particle.REDSTONE, p1.getX(), p1.getY(), p1.getZ(), 1, dustOptions);
	        length += space;
	    }
	}
	
	public Entity getClosestPhantom(Entity center, double radius){
	    Entity closestEntity = null;
	    double closestDistance = 0.0;

	    for(Entity entity : center.getNearbyEntities(radius, radius, radius)){
	    	if (entity.getType().equals(EntityType.PHANTOM) && entity.getCustomName() == null) {
	    		double distance = entity.getLocation().distanceSquared(center.getLocation());
	    		if(closestEntity == null || distance < closestDistance){
	    			closestDistance = distance;
	    			closestEntity = entity;
	    		}
	    	}
	    }
	    return closestEntity;
	}
	
	public Entity getClosestPlayer(Entity center, double radius){
	    Entity closestEntity = null;
	    double closestDistance = 0.0;

	    for(Entity entity : center.getNearbyEntities(radius, radius, radius)){
	    	if (entity.getType().equals(EntityType.PLAYER) && entity.getCustomName() == null) {
	    		double distance = entity.getLocation().distanceSquared(center.getLocation());
	    		if(closestEntity == null || distance < closestDistance){
	    			closestDistance = distance;
	    			closestEntity = entity;
	    		}
	    	}
	    }
	    return closestEntity;
	}
	
	public static Location faceLocation(Entity entity, Location to) {
        if (entity.getWorld() != to.getWorld()) {
            return null;
        }
        to = to.subtract(0, 2.15, 0);
        Location fromLocation = entity.getLocation();

        double xDiff = to.getX() - fromLocation.getX();
        double yDiff = to.getY() - fromLocation.getY();
        double zDiff = to.getZ() - fromLocation.getZ();

        double distanceXZ = Math.sqrt(xDiff * xDiff + zDiff * zDiff);
        double distanceY = Math.sqrt(distanceXZ * distanceXZ + yDiff * yDiff);

        double yaw = Math.toDegrees(Math.acos(xDiff / distanceXZ));
        double pitch = Math.toDegrees(Math.acos(yDiff / distanceY)) - 90.0D;
        if (zDiff < 0.0D) {
            yaw += Math.abs(180.0D - yaw) * 2.0D;
        }
        Location loc = entity.getLocation();
        loc.setYaw((float) (yaw - 90.0F));
        loc.setPitch((float) pitch);
        return loc;
    }
	
	public void phantomShieldClearMap() {
		new BukkitRunnable() {

			@Override
			public void run() {	
				for (Entry<Player, Long> entry : Main.possiblePS.entrySet()) {
					long unixTime = System.currentTimeMillis();
					if (unixTime - (entry.getValue()) > 10000) {
						possiblePS.remove(entry.getKey());
						PSSpawnTimerLeft.remove(entry.getKey());
					}
				}
			}				
		}.runTaskTimerAsynchronously(this, 0, 12000);
	}
	
	public void phantomShieldPotion() {
		new BukkitRunnable() {
			
			@Override
			public void run() {
					
				for (World world : getServer().getWorlds()) {
					for (Entity entity : world.getEntities()) {
						if (entity.getType().equals(EntityType.WITHER_SKELETON) && scoreboard.getTeam("PhantomShield").hasEntry(entity.getUniqueId().toString())) {
							if (entity.isSilent() == true) {
								if (skmcsx.getConfig().contains("PhantomShields." + entity.getUniqueId().toString())) {
									LivingEntity shield = (LivingEntity) entity;
									shield.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 999999999, 0));
								}
							}
						}							
					}
				}				
			}			
		}.runTaskTimer(this, 0, 72000);
	}
	
	public void phantomShieldSync() {
		new BukkitRunnable() {

			@SuppressWarnings("deprecation")
			@Override
			public void run() {
					
				for (World world : getServer().getWorlds()) {
					for (Entity entity : world.getEntities()) {
						if (entity.getType().equals(EntityType.WITHER_SKELETON) && scoreboard.getTeam("PhantomShield").hasEntry(entity.getUniqueId().toString())) {
							if (entity.isSilent() == true) {
								if (skmcsx.getConfig().contains("PhantomShields." + entity.getUniqueId().toString())) {
									LivingEntity shield = (LivingEntity) entity;
									if (shield.getHealth() < (shield.getMaxHealth() - 0.1)) {
										shield.setHealth(shield.getHealth() + 0.05);
									} else {
										shield.setHealth(shield.getMaxHealth());
									}
									long range = Main.skmcsx.getConfig().getLong(("PhantomShields." + entity.getUniqueId().toString() + ".Range"));
									if (getClosestPhantom(entity, range) != null) {
										Entity nearbyEntity = getClosestPhantom(entity, range);
										LivingEntity kill = (LivingEntity) nearbyEntity;
										if (kill.getHealth() > 3.5) {
											kill.setHealth(3);
											kill.setFireTicks(1000);
										} else if (kill.getHealth() > 0.5) {
											kill.setHealth((kill.getHealth() - 0.5));
											kill.setFireTicks(1000);
											kill.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 1, 0));
										} else {
											kill.setHealth(0);
										}
										Main.drawLine(shield.getEyeLocation(), kill.getLocation(), 0.2);
										shield.teleport(Main.faceLocation(shield, kill.getLocation()));
									} else {
										for (Entity entity1 : shield.getNearbyEntities(5, 5, 5)) {
											if (entity1.getType().equals(EntityType.PLAYER)) {
												Player player = (Player) getClosestPlayer(shield, 5);
												shield.teleport(Main.faceLocation(shield, player.getEyeLocation()));
												long blazeRod = Math.round(Math.random() * 50000);
												if (blazeRod == 17) {
													Item itemDropped = shield.getWorld().dropItem(shield.getEyeLocation(), new ItemStack(Material.BLAZE_ROD, 1));
													Vector vector = shield.getEyeLocation().getDirection();
													vector = vector.multiply(0.3);
													itemDropped.setVelocity(vector);
													player.sendMessage("§f[§dPhantom §6Shield§f] §e» §bNana loves these!");
												} else if (blazeRod == 23) {
													player.sendMessage("§f[§dPhantom §6Shield§f] §e» §bFun fact! Using Wither Skeletons was Nana\'s choice!");
												}
												long random = Math.round(Math.random() * 10000);
												if (random == 100) {
													player.sendMessage("§f[§dPhantom §6Shield§f] §e» §bGood Day!");
												} else if (random == 200) {
													player.sendMessage("§f[§dPhantom §6Shield§f] §e» §bWwww!");
												} else if (random == 300) {
													player.sendMessage("§f[§dPhantom §6Shield§f] §e» §bWhere are the Phantoms??");
												} else if (random == 400) {
													player.sendMessage("§f[§dPhantom §6Shield§f] §e» §bHi ._./");
												}
												break;
											}
										}										
									}
								}
							}
						}							
					}
				}				
			}			
		}.runTaskTimer(this, 0, 6);
	}
	
	public void phantomShield() {
		new BukkitRunnable() {

			@Override
			public void run() {
					
				for (Entry<Player, Long> entry : Main.PSEditRangeTimeOut.entrySet()) {
					long unixTime = System.currentTimeMillis();
					if (unixTime - (entry.getValue()) > 30000) {
						entry.getKey().sendMessage(ChatColor.RED + "Phantom Shield range input cancelled!");
						PSEditRangeTimeOut.remove(entry.getKey());
						PSEditRange.remove(entry.getKey());
					}
				}
				for (World world : getServer().getWorlds()) {
					for (Entity entity : world.getEntities()) {
						if (entity.getType().equals(EntityType.WITHER_SKELETON) && entity.isSilent() == true) {
							LivingEntity livingentity = (LivingEntity) entity;
							if (livingentity.getEquipment().getItemInMainHand().getType().equals(Material.PHANTOM_SPAWN_EGG) && livingentity.getEquipment().getItemInOffHand().getType().equals(Material.PHANTOM_MEMBRANE)) {
								if (!scoreboard.getTeam("PhantomShield").hasEntry(entity.getUniqueId().toString())) {
									scoreboard.getTeam("PhantomShield").addEntry(entity.getUniqueId().toString());
								}
								if (!skmcsx.getConfig().contains("PhantomShields." + entity.getUniqueId().toString())) {
									Player player = null;
									for (Entry<Player, UUID> entry : Main.PSPlayerPair.entrySet()) {
										if (entry.getValue().equals(entity.getUniqueId())) {
											player = entry.getKey();
											skmcsx.getConfig().set("PhantomShields." + entity.getUniqueId().toString() + ".Owner", player.getName());
											break;
										}
									}
									long unixTime = System.currentTimeMillis();
									if (PSSpawnTimerLeft.containsKey(player)) {
										unixTime = unixTime + PSSpawnTimerLeft.get(player);
										PSSpawnTimerLeft.remove(player);
									} else {
										unixTime = unixTime + (PhantomShieldsLifeTime * 1000);
									}
									Location loc = entity.getLocation();
									skmcsx.getConfig().set("PhantomShields." + entity.getUniqueId().toString() + ".Expire", unixTime);
									skmcsx.getConfig().set("PhantomShields." + entity.getUniqueId().toString() + ".X", loc.getBlockX());
									skmcsx.getConfig().set("PhantomShields." + entity.getUniqueId().toString() + ".Y", loc.getBlockY());
									skmcsx.getConfig().set("PhantomShields." + entity.getUniqueId().toString() + ".Z", loc.getBlockZ());
									skmcsx.getConfig().set("PhantomShields." + entity.getUniqueId().toString() + ".World", loc.getWorld().getName());
									skmcsx.getConfig().set("PhantomShields." + entity.getUniqueId().toString() + ".Range", PhantomShieldsRange);
									skmcsx.saveConfig();
									AttributeInstance attribute = livingentity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
									attribute.setBaseValue(80.0D);
									livingentity.setHealth(80);
								} else {
									Location loc = entity.getLocation();
									
									long expire = skmcsx.getConfig().getLong("PhantomShields." + entity.getUniqueId().toString() + ".Expire");
									long unixTime = System.currentTimeMillis();
									if (unixTime > expire) {
										skmcsx.getConfig().set("PhantomShields." + entity.getUniqueId().toString(), null);
										skmcsx.saveConfig();
										scoreboard.getTeam("PhantomShield").removeEntry(entity.getUniqueId().toString());
										entity.remove();
									} else {
										LocalDateTime now = LocalDateTime.ofInstant(Instant.ofEpochMilli(unixTime), ZoneId.systemDefault());
										LocalDateTime end = LocalDateTime.ofInstant(Instant.ofEpochMilli(expire), ZoneId.systemDefault());
										String timer = "";
										long weeks = ChronoUnit.WEEKS.between(now, end);
										long days = ChronoUnit.DAYS.between(now, end);
										long hrs = ChronoUnit.HOURS.between(now, end);
										long mins = ChronoUnit.MINUTES.between(now, end);
										long secs = ChronoUnit.SECONDS.between(now, end);
										if (days > 365) {
											timer = "More than a Year";
										} else if (days > 31) {
											timer = weeks + " Weeks";
										} else if (days != 0) {
											if (days == 1) {
												timer = days + " Day";
											} else {
												timer = days + " Days";
											}
										} else if (hrs != 0) {
											if (hrs == 1) {
												timer = hrs + " Hour";
											} else {
												timer = hrs + " Hours";
											}
										} else if (mins != 0) {
											if (mins == 1) {
												timer = mins + " Minute";
											} else {
												timer = mins + " Minutes";
											}
										} else {
											if (secs == 1) {
												timer = secs + " Second";
											} else {
												timer = secs + " Seconds";
											}
										}
										String name = ChatColor.LIGHT_PURPLE + "Phantom " + ChatColor.GOLD + "Shield" + ChatColor.YELLOW + " |";
										if (mins < 1) {
											if (secs % 2 == 0) {
												name = name + ChatColor.RED + " Expire: " + timer;
											} else {
												name = name + ChatColor.YELLOW + " Expire: " + timer;
											}
										} else if (mins < 5) {
											name = name + ChatColor.RED + " Expire: " + timer;
										} else if (hrs == 0) {
											name = name + ChatColor.YELLOW + " Expire: " + timer;
										} else {
											name = name + ChatColor.GREEN + " Expire: " + timer;
										}
										entity.setCustomName(name);
										entity.setCustomNameVisible(true);
										entity.setGlowing(true);
										livingentity.setRemoveWhenFarAway(false);
										Location oldLoc = new Location(Bukkit.getWorld(skmcsx.getConfig().getString("PhantomShields." + entity.getUniqueId().toString() + ".World")), skmcsx.getConfig().getInt("PhantomShields." + entity.getUniqueId().toString() + ".X"), skmcsx.getConfig().getInt("PhantomShields." + entity.getUniqueId().toString() + ".Y"), skmcsx.getConfig().getInt("PhantomShields." + entity.getUniqueId().toString() + ".Z"));
										Location newLoc = new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
										if (!oldLoc.equals(newLoc)) {
											skmcsx.getConfig().set("PhantomShields." + entity.getUniqueId().toString() + ".X", loc.getBlockX());
											skmcsx.getConfig().set("PhantomShields." + entity.getUniqueId().toString() + ".Y", loc.getBlockY());
											skmcsx.getConfig().set("PhantomShields." + entity.getUniqueId().toString() + ".Z", loc.getBlockZ());
											skmcsx.getConfig().set("PhantomShields." + entity.getUniqueId().toString() + ".World", loc.getWorld().getName());
											skmcsx.saveConfig();
										}
									}
								}
							}
						}							
					}
				}				
			}			
		}.runTaskTimerAsynchronously(this, 0, 17);
	}
	
	public void checkTpCancel() {
		new BukkitRunnable() {

			@Override
			public void run() {
					
				for (World world : getServer().getWorlds()) {
					for (Player player : world.getPlayers()) {
						if (Main.homeWarmUp.containsKey(player)) {
							Location currLocation = Main.ess3.getUser(player.getName()).getBase().getLocation();
							String data = "";
							for (Entry<Player, String> entry : homeWarmUp.entrySet()) {
								if (entry.getKey().getName().equals(player.getName())) {
									data = entry.getValue();
				                	break;
				                }
				            }
							long timer_initX = Integer.parseInt(data.substring(0, data.indexOf(",")));
							long timer_initY = Integer.parseInt(data.substring(data.indexOf(",") + 1, data.indexOf(".")));
							long timer_initZ = Integer.parseInt(data.substring(data.indexOf(".") + 1, data.indexOf("=")));
							double timer_health = Double.parseDouble(data.substring(data.indexOf("=") + 1, data.length() - 1));
							if ((Math.round(currLocation.getX() * 0.3) != timer_initX || Math.round(currLocation.getY() * 0.3) != timer_initY || Math.round(currLocation.getZ() * 0.3) != timer_initZ || Main.ess3.getUser(player.getName()).getBase().getHealth() < timer_health)) {
								//Main.ess3.getUser(player.getName()).clearCommandCooldown(Pattern.compile("home( .*)?"));
								for (Entry<Pattern, Long> entry : Main.ess3.getUser(player.getName()).getCommandCooldowns().entrySet()) {
									if (entry.getKey().toString().equals(("home( .*)?"))) {
										Main.ess3.getUser(player.getName()).clearCommandCooldown(entry.getKey());
					                	Main.homeWarmUp.remove(player);
					                	break;
					                }
					            }
							}
						}
						
						if (Main.spawnWarmUp.containsKey(player)) {
							Location currLocation = Main.ess3.getUser(player.getName()).getBase().getLocation();
							String data = "";
							for (Entry<Player, String> entry : spawnWarmUp.entrySet()) {
								if (entry.getKey().getName().equals(player.getName())) {
									data = entry.getValue();
				                	break;
				                }
				            }
							long timer_initX = Integer.parseInt(data.substring(0, data.indexOf(",")));
							long timer_initY = Integer.parseInt(data.substring(data.indexOf(",") + 1, data.indexOf(".")));
							long timer_initZ = Integer.parseInt(data.substring(data.indexOf(".") + 1, data.indexOf("=")));
							double timer_health = Double.parseDouble(data.substring(data.indexOf("=") + 1, data.length() - 1));
							if ((Math.round(currLocation.getX() * 0.3) != timer_initX || Math.round(currLocation.getY() * 0.3) != timer_initY || Math.round(currLocation.getZ() * 0.3) != timer_initZ || Main.ess3.getUser(player.getName()).getBase().getHealth() < timer_health)) {
								//Main.ess3.getUser(player.getName()).clearCommandCooldown(Pattern.compile("home( .*)?"));
								for (Entry<Pattern, Long> entry : Main.ess3.getUser(player.getName()).getCommandCooldowns().entrySet()) {
									if (entry.getKey().toString().equals(("spawn( .*)?"))) {
										Main.ess3.getUser(player.getName()).clearCommandCooldown(entry.getKey());
					                	Main.spawnWarmUp.remove(player);
					                	break;
					                }
					            }
							}
						}
						
						if (Main.backWarmUp.containsKey(player)) {
							Location currLocation = Main.ess3.getUser(player.getName()).getBase().getLocation();
							String data = "";
							for (Entry<Player, String> entry : backWarmUp.entrySet()) {
								if (entry.getKey().getName().equals(player.getName())) {
									data = entry.getValue();
				                	break;
				                }
				            }
							long timer_initX = Integer.parseInt(data.substring(0, data.indexOf(",")));
							long timer_initY = Integer.parseInt(data.substring(data.indexOf(",") + 1, data.indexOf(".")));
							long timer_initZ = Integer.parseInt(data.substring(data.indexOf(".") + 1, data.indexOf("=")));
							double timer_health = Double.parseDouble(data.substring(data.indexOf("=") + 1, data.length() - 1));
							if ((Math.round(currLocation.getX() * 0.3) != timer_initX || Math.round(currLocation.getY() * 0.3) != timer_initY || Math.round(currLocation.getZ() * 0.3) != timer_initZ || Main.ess3.getUser(player.getName()).getBase().getHealth() < timer_health)) {
								//Main.ess3.getUser(player.getName()).clearCommandCooldown(Pattern.compile("home( .*)?"));
								for (Entry<Pattern, Long> entry : Main.ess3.getUser(player.getName()).getCommandCooldowns().entrySet()) {
									if (entry.getKey().toString().equals(("back( .*)?"))) {
										Main.ess3.getUser(player.getName()).clearCommandCooldown(entry.getKey());
					                	Main.backWarmUp.remove(player);
					                	break;
					                }
					            }
							}
						}
						
						if (Main.tpaWarmUp.containsKey(player)) {
							Location currLocation = Main.ess3.getUser(player.getName()).getBase().getLocation();
							String data = "";
							for (Entry<Player, String> entry : tpaWarmUp.entrySet()) {
								if (entry.getKey().getName().equals(player.getName())) {
									data = entry.getValue();
				                	break;
				                }
				            }
							long timer_initX = Integer.parseInt(data.substring(0, data.indexOf(",")));
							long timer_initY = Integer.parseInt(data.substring(data.indexOf(",") + 1, data.indexOf(".")));
							long timer_initZ = Integer.parseInt(data.substring(data.indexOf(".") + 1, data.indexOf("=")));
							double timer_health = Double.parseDouble(data.substring(data.indexOf("=") + 1, data.length() - 1));
							if ((Math.round(currLocation.getX() * 0.3) != timer_initX || Math.round(currLocation.getY() * 0.3) != timer_initY || Math.round(currLocation.getZ() * 0.3) != timer_initZ || Main.ess3.getUser(player.getName()).getBase().getHealth() < timer_health)) {
								//Main.ess3.getUser(player.getName()).clearCommandCooldown(Pattern.compile("home( .*)?"));
								for (Entry<Pattern, Long> entry : Main.ess3.getUser(player.getName()).getCommandCooldowns().entrySet()) {
									if (entry.getKey().toString().equals(("tpa( .*)?"))) {
										Main.ess3.getUser(player.getName()).clearCommandCooldown(entry.getKey());
					                	Main.tpaWarmUp.remove(player);
					                	break;
					                }
					            }
							}
						}
						
						if (Main.tpahereWarmUp.containsKey(player)) {
							String data = "";
							for (Entry<Player, String> entry : tpahereWarmUp.entrySet()) {
								if (entry.getKey().getName().equals(player.getName())) {
									data = entry.getValue();
				                	break;
				                }
				            }
							Player acceptPlayer = Bukkit.getPlayer(data.substring(0, data.indexOf(">")));
							Location currLocation = Main.ess3.getUser(acceptPlayer.getName()).getBase().getLocation();
							long timer_initX = Integer.parseInt(data.substring(data.indexOf(">") + 1, data.indexOf(",")));
							long timer_initY = Integer.parseInt(data.substring(data.indexOf(",") + 1, data.indexOf(".")));
							long timer_initZ = Integer.parseInt(data.substring(data.indexOf(".") + 1, data.indexOf("=")));
							double timer_health = Double.parseDouble(data.substring(data.indexOf("=") + 1, data.length() - 1));
							if ((Math.round(currLocation.getX() * 0.3) != timer_initX || Math.round(currLocation.getY() * 0.3) != timer_initY || Math.round(currLocation.getZ() * 0.3) != timer_initZ || Main.ess3.getUser(acceptPlayer.getName()).getBase().getHealth() < timer_health)) {
								//Main.ess3.getUser(player.getName()).clearCommandCooldown(Pattern.compile("home( .*)?"));
								for (Entry<Pattern, Long> entry : Main.ess3.getUser(player.getName()).getCommandCooldowns().entrySet()) {
									if (entry.getKey().toString().equals(("tpahere( .*)?"))) {
										Main.ess3.getUser(player.getName()).clearCommandCooldown(entry.getKey());
					                	Main.tpahereWarmUp.remove(player);
					                	break;
					                }
					            }
							}
						}						
					}
				}
				
			}
			
		}.runTaskTimerAsynchronously(this, 0, 7);
	}
	
	public void velocityRunnable() {
		new BukkitRunnable() {

			@Override
			public void run() {
					
				for (World world : getServer().getWorlds()) {
					for (Player player : world.getPlayers()) {
						
						if (player.getVehicle() instanceof Vehicle) {
							Vehicle vehicleCheck = (Vehicle) player.getVehicle();
						
							if (vehicleCheck!=null) {
							
								Entity vehicle = player.getVehicle(); 
								double velocityVehicle = (Math.sqrt(Math.pow(vehicle.getVelocity().getX(), 2) + Math.pow(vehicle.getVelocity().getY(), 2) + Math.pow(vehicle.getVelocity().getZ(), 2)) * 4.3178 - 0.002141) * 3.6;
//					   	 	double velocityPlayer = Math.sqrt(Math.pow(player.getVelocity().getX(), 2) + Math.pow(player.getVelocity().getY() + 0.0784000015258789, 2) + Math.pow(player.getVelocity().getZ(), 2));
							
								if (player.getVehicle().getType().equals(EntityType.MINECART)) {
							    
									DecimalFormat df2 = new DecimalFormat("0.00");
									String vehicleDisplay = vehicle.getType().toString();
									player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.GOLD + vehicleDisplay.substring(0, 1).toUpperCase() + vehicleDisplay.substring(1, vehicleDisplay.length()).toLowerCase() + ChatColor.DARK_AQUA + " Speed: " + ChatColor.GREEN + df2.format(velocityVehicle) + " km/h"));
							    
//					    } else if (player.getVehicle().getType().equals(EntityType.BOAT) || player.getVehicle().getType().equals(EntityType.HORSE)) {
//						    	
//						    	DecimalFormat df2 = new DecimalFormat("0.00");
//							    String vehicleDisplay = vehicle.getType().toString();
//							    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.GOLD + vehicleDisplay.substring(0, 1).toUpperCase() + vehicleDisplay.substring(1, vehicleDisplay.length()).toLowerCase() + ChatColor.DARK_AQUA + " Speed: " + ChatColor.GREEN + df2.format(velocityPlayer) + " m/s"));
								}
							}
						}
					}
				}
				
			}
			
		}.runTaskTimerAsynchronously(this, 0, 20);
	}
	
	public void itemNameRunnable() {
		new BukkitRunnable() {

			@Override
			public void run() {

                for (World world : getServer().getWorlds()) {
                	for (Entity entity : world.getEntities()) {
                		if (entity instanceof Item) {
                			Item item = (Item) entity;
                			
//                			Player player = getServer().getPlayer("LOOHP");
//                			player.sendMessage(((Item) entity).getPickupDelay() + "");
                			if (((Item) entity).getPickupDelay() < 100) {
                			
                	    		String materialRaw = "";
                    			String materialDisplay = "";
                    			
                    			if (item.getItemStack().getItemMeta().getDisplayName() != "") {
                    				materialRaw = item.getItemStack().getItemMeta().getDisplayName().toString();
                	    			if (item.getItemStack().getItemMeta().hasEnchants() == false) {
                    				    materialDisplay = ChatColor.GREEN.toString() + item.getItemStack().getItemMeta().getDisplayName().toString();
                    				} else {
                    					materialDisplay = ChatColor.AQUA.toString() + item.getItemStack().getItemMeta().getDisplayName().toString();
                    				}
                	    		} else {
                		    		if (materialDisplay != "TNT") {
                			    	    materialRaw = item.getItemStack().getType().toString();
                				        materialDisplay = materialRaw.substring(0, 1).toUpperCase() + materialRaw.substring(1, materialRaw.length()).toLowerCase();
                    				    if (item.getItemStack().getItemMeta().hasEnchants() == true) {
                    				    	materialDisplay = ChatColor.AQUA.toString() + materialDisplay;
                     				    }
                		    		} else {
                		    			materialRaw = item.getItemStack().getType().toString();
                		    			materialDisplay = materialRaw;
                		    			if (item.getItemStack().getItemMeta().hasEnchants() == true) {
                		    				materialDisplay = ChatColor.AQUA.toString() + materialDisplay;
                		    			}
                		    		}
                	    		}
                			
                    			entity.setCustomName(ChatColor.WHITE + materialDisplay + ChatColor.AQUA + " x" + item.getItemStack().getAmount());
                			
                    			entity.setCustomNameVisible(true);
                    			entity.setGlowing(true);
                    			int despawnTimer = 6000 - entity.getTicksLived();
                    			if (despawnTimer < 2400 && despawnTimer > 599) {
                    				scoreboard.getTeam("ItemExpireYellow").addEntry(entity.getUniqueId().toString());   
                    				scoreboard.getTeam("ItemExpireRed").removeEntry(entity.getUniqueId().toString());
                    			} else if (despawnTimer < 600) {
                    				scoreboard.getTeam("ItemExpireRed").addEntry(entity.getUniqueId().toString());    
                    				scoreboard.getTeam("ItemExpireYellow").removeEntry(entity.getUniqueId().toString());
                    			} else {
                    				scoreboard.getTeam("ItemExpireYellow").removeEntry(entity.getUniqueId().toString());
                    				scoreboard.getTeam("ItemExpireRed").removeEntry(entity.getUniqueId().toString());
                    			}
                			
                			}
                		}
                	}
                }				
			}
			
		}.runTaskTimer(this, 0, 11);
	}
}
