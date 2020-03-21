package com.loohp.skmcextra;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.Validate;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.text.WordUtils;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
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

import net.craftersland.data.bridge.PD;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.milkbowl.vault.chat.Chat;

public class Main extends JavaPlugin {
	
	public static java.sql.Connection connection;
    public static String host, database, username, password, table;
    public static int port;
	
	public static Scoreboard scoreboard;
	public static long tpaCooldown = 5;
	public static long PhantomShieldsLifeTime = 2592000;
	public static long PhantomShieldsRange = 150;
	
	public static int saveInterval = 3600;
	
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
	public static Map<UUID, Player> PSPlayerPair = new HashMap<UUID, Player>();	
	public static Map<Player, Long> PSSpawnTimerLeft = new HashMap<Player, Long>();
	public static Map<Entity, Inventory> PSGUIedit = new HashMap<Entity, Inventory>();
	
	public static Map<Player, Entity> PSEditRange = new HashMap<Player, Entity>();
	public static Map<Player, Long> PSEditRangeTimeOut = new HashMap<Player, Long>();
	
	public static Map<Player, Location> velocitylastLoc = new HashMap<Player, Location>();
	public static Map<Player, Long> velocityDisplay = new HashMap<Player, Long>();
	
	public static Map<Material, Long> lightLevel = new HashMap<Material, Long>();
	public static Map<Location, UUID> dynamicLight = new HashMap<Location, UUID>();
	
	public static Map<Player, Inventory> shulkerView = new HashMap<Player, Inventory>();
	public static Map<Player, Inventory> shulkerLastInv = new HashMap<Player, Inventory>();
	
	public static Map<Player, String> isMPDB = new HashMap<Player, String>();
	
	public static Map<String, String> specialName = new HashMap<String, String>();
	
	public static Map<Player, Boolean> enderpearl = new HashMap<Player, Boolean>();
	
	public static long blazeRodMultiplier = 40000;
	public static long blazeRodNanaRod = 17;
	public static long blazeRodNanaChoice = 23;
	public static long randomMultiplier = 10000;
	public static long randomGoodDay = 100;
	public static long randomWwww = 200;
	public static long randomPhantomsWhere = 300;
	public static long randomHi = 400;

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
		getCommand("togglehpscale").setExecutor(new Commands());
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
		
		try {
			loadChances();
		} catch (IOException e) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Failed to get chance multiplier from github");
			e.printStackTrace();
		}
		
		Main.lightLevel.put(Material.BEACON, (long) 15);
		Main.lightLevel.put(Material.LAVA_BUCKET, (long) 15);
		Main.lightLevel.put(Material.GLOWSTONE, (long) 15);
		Main.lightLevel.put(Material.JACK_O_LANTERN, (long) 15);
		Main.lightLevel.put(Material.SEA_LANTERN, (long) 15);
		Main.lightLevel.put(Material.CONDUIT, (long) 15);
		Main.lightLevel.put(Material.LANTERN, (long) 15);
		Main.lightLevel.put(Material.CAMPFIRE, (long) 15);
		Main.lightLevel.put(Material.BLAZE_ROD, (long) 15);		
		Main.lightLevel.put(Material.END_ROD, (long) 14);
		Main.lightLevel.put(Material.TORCH, (long) 14);
		Main.lightLevel.put(Material.ENDER_CHEST, (long) 7);
		Main.lightLevel.put(Material.REDSTONE_TORCH, (long) 7);
		Main.lightLevel.put(Material.MAGMA_BLOCK, (long) 3);
		Main.lightLevel.put(Material.BREWING_STAND, (long) 1);
		Main.lightLevel.put(Material.BROWN_MUSHROOM, (long) 1);
		Main.lightLevel.put(Material.DRAGON_EGG, (long) 1);
		Main.lightLevel.put(Material.END_PORTAL_FRAME, (long) 1);		
		
		Main.specialName.put("TNT", "TNT");
		Main.specialName.put("CHEST_MINECART", "Minecart with Chest");
		Main.specialName.put("FURNACE_MINECART", "Minecart with Furnace");
		Main.specialName.put("HOPPER_MINECART", "Minecart with Hopper");
		Main.specialName.put("TNT_MINECART", "Minecart with TNT");
		
		Main.specialName.put("POTION.WATER", "Water Bottle");
		Main.specialName.put("POTION.MUNDANE", "Mundane Potion");
		Main.specialName.put("POTION.THICK", "Thick Potion");
		Main.specialName.put("POTION.AWKWARD", "Awkward Potion");
		Main.specialName.put("POTION.NIGHT_VISION", "Potion of Night Vision");
		Main.specialName.put("POTION.INVISIBILITY", "Potion of Invisiblity");
		Main.specialName.put("POTION.JUMP", "Potion of Leaping");
		Main.specialName.put("POTION.FIRE_RESISTANCE", "Potion of Fire Resistance");
		Main.specialName.put("POTION.SPEED", "Potion of Swiftness");
		Main.specialName.put("POTION.SLOWNESS", "Potion of Slowness");
		Main.specialName.put("POTION.TURTLE_MASTER", "Potion of the Turtle Master");
		Main.specialName.put("POTION.WATER_BREATHING", "Potion of Water Breathing");
		Main.specialName.put("POTION.INSTANT_HEAL", "Potion of Healing");
		Main.specialName.put("POTION.INSTANT_HARM", "Potion of Harming");
		Main.specialName.put("POTION.POISON", "Potion of Poison");
		Main.specialName.put("POTION.REGEN", "Potion of Regeneration");
		Main.specialName.put("POTION.STRENGTH", "Potion of Strength");
		Main.specialName.put("POTION.WEAKNESS", "Potion of Weakness");
		Main.specialName.put("POTION.LUCK", "Potion of Luck");
		Main.specialName.put("POTION.SLOW_FALLING", "Potion of Slow Falling");
		Main.specialName.put("POTION.UNCRAFTABLE", "Uncraftable Potion");
		Main.specialName.put("POTION.UNCRAFTABLE", "Uncraftable Potion");
		Main.specialName.put("POTION.MULTIPLE", "Magical Potion");
		
		//Runnable
		velocityRunnable();
		itemNameRunnable();
		checkTpCancel();
		phantomShield();
		phantomShieldSync();
		phantomShieldClearMap();
		
		Main.saveInterval = PD.instance.getConfig().getInt("General.saveDataTask.interval") * 60 * 20;
		saveHealth();
		
//		handLight();
		
		enderpearlLand();
	}

	@Override
	public void onDisable( ) {
		getServer().getConsoleSender().sendMessage(ChatColor.RED + "SKMC Survival Extra has been Disabled!");
		scoreboard.getTeam("ItemExpireYellow").unregister();
		scoreboard.getTeam("ItemExpireRed").unregister();
	}
	
	//Default
	//blazeRodMultiplier = 40000;
	//blazeRodNanaRod = 17;
	//blazeRodNanaChoice = 23;
	//randomMultiplier = 10000;
	//randomGoodDay = 100;
	//randomWwww = 200;
	//randomPhantomsWhere = 300;
	//randomHi = 400;
	
	public static void loadChances() throws MalformedURLException, IOException {
		String out = readStringFromURL("https://raw.githubusercontent.com/LOOHP/SunKnightsMinecraft/master/ChanceMultiplier.txt");
		List<String> list = new ArrayList<String>();
		list = Arrays.asList(out.split("\\R", -1));
		for (String string : list) {
			if (string.contains("blazeRodMultiplier")) {
				blazeRodMultiplier = Long.parseLong(string.substring(string.indexOf("blazeRodMultiplier=") + 19));
			} else if (string.contains("blazeRodNanaRod")) {
				blazeRodNanaRod = Long.parseLong(string.substring(string.indexOf("blazeRodNanaRod=") + 16));
			} else if (string.contains("blazeRodNanaChoice")) {
				blazeRodNanaChoice = Long.parseLong(string.substring(string.indexOf("blazeRodNanaChoice=") + 19));
			} else if (string.contains("randomMultiplier")) {
				randomMultiplier = Long.parseLong(string.substring(string.indexOf("randomMultiplier=") + 17));
			} else if (string.contains("randomGoodDay")) {
				randomGoodDay = Long.parseLong(string.substring(string.indexOf("randomGoodDay=") + 14));
			} else if (string.contains("randomWwww")) {
				randomWwww = Long.parseLong(string.substring(string.indexOf("randomWwww=") + 11));
			} else if (string.contains("randomPhantomsWhere")) {
				randomPhantomsWhere = Long.parseLong(string.substring(string.indexOf("randomPhantomsWhere=") + 20));
			} else if (string.contains("randomHi")) {
				randomHi = Long.parseLong(string.substring(string.indexOf("randomHi=") + 9));
			}
		}
	}
	
	public static String readStringFromURL(String requestURL) throws IOException
	{
	    try (Scanner scanner = new Scanner(new URL(requestURL).openStream(),
	            StandardCharsets.UTF_8.toString()))
	    {
	        scanner.useDelimiter("\\A");
	        return scanner.hasNext() ? scanner.next() : "";
	    }
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
		
		try {
			loadChances();
		} catch (IOException e) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Failed to get chance multiplier from github");
			e.printStackTrace();
		}
		
		Main.mysqlSetup(true);
		try {
			Main.getConnection().close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public static void mysqlSetup(boolean echo) {
        host = PD.instance.getConfig().getString("database.mysql.host");
        port =  PD.instance.getConfig().getInt("database.mysql.port");
        database = PD.instance.getConfig().getString("database.mysql.databaseName");
        username = PD.instance.getConfig().getString("database.mysql.user");
        password = PD.instance.getConfig().getString("database.mysql.password");
		table = "skmc_health_custompatch";

		try {
			synchronized (Main.class) {
				if (getConnection() != null && !getConnection().isClosed()) {
					Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "MYSQL Failed to connect! [getConnection() != null && !getConnection().isClosed()]");
					return;
				}
				Class.forName("com.mysql.jdbc.Driver");
				setConnection(DriverManager.getConnection("jdbc:mysql://" + Main.host + ":" + Main.port + "/" + Main.database, Main.username, Main.password));
				
				if (echo == true) {
					Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "MYSQL CONNECTED");
				}
			}
		} catch (SQLException e) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "MYSQL Failed to connect! (SQLException)");
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "MYSQL Failed to connect! (ClassNotFoundException)");
			e.printStackTrace();
		}
	}

	public static Connection getConnection() {
		return Main.connection;
	}

	public static void setConnection(Connection connection) {
		Main.connection = connection;
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
	
	@SuppressWarnings("deprecation")
	public static void openPSGUI(Player player, Entity entity) {
		if (!PSGUIedit.containsKey(entity)) {
			PSGUIedit.put(entity, Bukkit.createInventory(null, 27, "§aEdit §dPhantom §6Shield"));
		}
		long range = Main.skmcsx.getConfig().getLong(("PhantomShields." + entity.getUniqueId().toString() + ".Range"));
		String owner = Main.skmcsx.getConfig().getString(("PhantomShields." + entity.getUniqueId().toString() + ".Owner"));
		if (Main.chat.getPlayerPrefix(player.getWorld(), owner) != null) {
			owner = Main.chat.getPlayerPrefix(player.getWorld(), owner) + owner;
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
	
	public static Entity getClosestPlayer(Entity center, double radius){
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
	
//	public void handLight() {
//		new BukkitRunnable() {
//			
//			@Override
//			public void run() {
//				for (World world : Bukkit.getWorlds()) {
//					for (Entity ent : world.getEntities()) {
//						long light = 0;
//						if (ent instanceof LivingEntity) {
//							LivingEntity entity = (LivingEntity) ent;
//							List<Material> item = new ArrayList<Material>();
//							if (entity.getEquipment().getItemInMainHand() != null) {
//								item.add(entity.getEquipment().getItemInMainHand().getType());
//							}
//							if (entity.getEquipment().getItemInOffHand() != null) {
//								item.add(entity.getEquipment().getItemInOffHand().getType());
//							}
//							if (entity.getEquipment().getHelmet() != null) {
//								item.add(entity.getEquipment().getHelmet().getType());
//							}
//							if (entity.getEquipment().getChestplate() != null) {
//								item.add(entity.getEquipment().getChestplate().getType());
//							}
//							if (entity.getEquipment().getLeggings() != null) {
//								item.add(entity.getEquipment().getLeggings().getType());
//							}
//							if (entity.getEquipment().getBoots() != null) {
//								item.add(entity.getEquipment().getBoots().getType());
//							}
//							for (Material material : item) {
//								if (Main.lightLevel.containsKey(material)) {
//									if (light < Main.lightLevel.get(material)) {
//										light = Main.lightLevel.get(material);
//									}
//								}
//							}
//						} else if (ent instanceof Item){ 							
//							Item item = (Item) ent;
//							if (Main.lightLevel.containsKey(item.getItemStack().getType())) {
//								light = Main.lightLevel.get(item.getItemStack().getType());
//							}
//						} else if (ent instanceof ItemFrame) {
//							ItemFrame itemframe = (ItemFrame) ent;
//							if (Main.lightLevel.containsKey(itemframe.getItem().getType())) {
//								light = Main.lightLevel.get(itemframe.getItem().getType());
//							}
//						}
//						if (light > 0) {
//							if (ent.getLocation().getBlock().getLightFromBlocks() < light) {
//								LightAPI.createLight(ent.getLocation().getBlock().getLocation(), LightType.BLOCK, (int) light, false);
//								Main.dynamicLight.put(ent.getLocation().getBlock().getLocation(), ent.getUniqueId());
//							}
//						} else if (Main.dynamicLight.containsValue(ent.getUniqueId())) {
//							for (Entry<Location, UUID> entry : Main.dynamicLight.entrySet()) {
//								if (entry.getValue().equals(ent.getUniqueId())) {
//									LightAPI.deleteLight(entry.getKey(), LightType.BLOCK, false);
//									Main.dynamicLight.remove(entry.getKey());
//									Bukkit.getConsoleSender().sendMessage("000");
//								}
//								break;
//							}
//						}
//						for (Entry<Location, UUID> entry : Main.dynamicLight.entrySet()) {
//							if (entry.getValue().equals(ent.getUniqueId())) {
//								Entity entity = Bukkit.getEntity(entry.getValue());
//								if (!entity.getLocation().getWorld().equals(entry.getKey().getWorld())) {
//									LightAPI.deleteLight(entry.getKey(), LightType.BLOCK, false);
//									Main.dynamicLight.remove(entry.getKey());
//									Bukkit.getConsoleSender().sendMessage("111");
//									break;
//								} else if (!entity.getLocation().getBlock().getLocation().equals(entry.getKey().getBlock().getLocation())) {
//									LightAPI.deleteLight(entry.getKey(), LightType.BLOCK, false);
//									Main.dynamicLight.remove(entry.getKey());
//									Bukkit.getConsoleSender().sendMessage("222");
//									break;
//								}
//							}
//						}
//						for(ChunkInfo info: LightAPI.collectChunks(ent.getLocation().getBlock().getLocation() , LightType.BLOCK, (int) light)) {
//							LightAPI.updateChunk(info, LightType.BLOCK);
//						}
//						Material block = ent.getLocation().getBlock().getType();
//						world.getBlockAt(ent.getLocation().getBlock().getLocation()).setType(Material.BLACK_WOOL);
//					}
//				}
//			}			
//		}.runTaskTimer(this, 0, 1);
//	}
	
	public void saveHealth() {
		new BukkitRunnable() {

			@Override
			public void run() {
				Main.mysqlSetup(false);
				
				for (Player player : Bukkit.getOnlinePlayers()) {
					try {
						PreparedStatement statement = Main.getConnection().prepareStatement("UPDATE " + Main.table + " SET HEALTH=? WHERE UUID=?");
						statement.setDouble(1, player.getHealth());
						statement.setString(2, player.getUniqueId().toString());
						statement.executeUpdate();
					} catch (SQLException e) {
						e.printStackTrace();
					}
					if (PD.instance.getConfig().getBoolean("Debug.HealthSync") == true) {
						Bukkit.getConsoleSender().sendMessage("[MysqlPlayerDataBridge - SKMCHealthPatch] Debug Health - Data save - " + player.getName() + " - Health: " + player.getHealth());
					}
				}
				
				try {
					Main.getConnection().close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}.runTaskTimerAsynchronously(this, 0, Main.saveInterval);
	}
	
	public void enderpearlLand() {
		new BukkitRunnable() {

			@Override
			public void run() {
				if (Main.enderpearl.isEmpty()) {
					for (Player player : Bukkit.getOnlinePlayers()) {
						if (player.getInventory().getItemInMainHand() != null) {
							if (player.getInventory().getItemInMainHand().getType().equals(Material.ENDER_PEARL)) {
								Main.enderpearl.put(player, false);
							}
						}
						if (player.getInventory().getItemInOffHand() != null) {
							if (player.getInventory().getItemInOffHand().getType().equals(Material.ENDER_PEARL)) {
								Main.enderpearl.put(player, false);
							}
						}
					}
				}
				
				int i = 0;
				List<Player> toTrue = new ArrayList<Player>();
				for (Entry<Player, Boolean> entry : Main.enderpearl.entrySet()) {
					if (entry.getValue() == false) {
						i = i + 1;
						Player player = entry.getKey();
						
						Location base = player.getEyeLocation();
						Vector velocity = player.getEyeLocation().getDirection().normalize().multiply(1.5);
						Vector drag = player.getEyeLocation().getDirection().normalize().multiply(0.01).multiply(-1);
						Vector downwardAccel = new Vector(0, -0.03, 0);
						
						Location land = base;
						
						double tick = 0;
						
						while (base.getY() > -10) {
							
						    if (base.getBlock().getType().equals(Material.AIR)) {
						    	land = base.clone();
						    	Vector half = velocity.clone().add(drag.clone().multiply(0.5)).add(downwardAccel.clone().multiply(0.5));
						    	
						    	if (tick % 1 == 0) {
						    		velocity.add(drag).add(downwardAccel);
							    }
						    	
							    base.add(half.multiply(0.5));
						    } else {
						    	break;
						    }
						    
						    tick = tick + 0.5;
						}
						
						double time = ((double) ((double) tick) / 20);
						
						double timeRounded = ((double) ((double) Math.round(time * 10)) / 10);

						Location u = land.clone();
						boolean safe = false;
						while (u.getBlockY() > -1 && (land.getBlockY() - u.getBlockY() < 15)) {
							if (u.getBlock().getType().isSolid() == true) {
								safe = true;
								break;
							}
							u = u.add(0, -1, 0);
						}
						String message = "";
						if (safe == true) {
							message = ChatColor.GREEN + "Ender Pearl " + ChatColor.AQUA + "Predicted Landing: " + ChatColor.YELLOW + "X:" + Math.ceil(land.getX()) + " Y:" + Math.ceil(land.getY()) + " Z:" + Math.ceil(land.getZ()) + " (" + Math.round(land.distance(player.getLocation().getBlock().getLocation())) + " Blocks) Flight Time: " + timeRounded + "s";
						} else {
							message = ChatColor.GREEN + "Ender Pearl " + ChatColor.AQUA + "Predicted Landing: " + ChatColor.RED + "X:" + Math.ceil(land.getX()) + " Y:" + Math.ceil(land.getY()) + " Z:" + Math.ceil(land.getZ()) + " (" + Math.round(land.distance(player.getLocation().getBlock().getLocation())) + " Blocks) [COULD BE UNSAFE]";
						}
						player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
						
						toTrue.add(player);
					}
					if (i > 15) {
						break;
					}
				}
				for (Player player : toTrue) {
					Main.enderpearl.put(player, true);
				}
				
				if (!Main.enderpearl.containsValue(false)) {
					Main.enderpearl.clear();
				}
			}
		}.runTaskTimer(this, 0, 10);
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
												long blazeRod = Math.round(Math.random() * blazeRodMultiplier);
												if (blazeRod == blazeRodNanaRod) {
													Item itemDropped = shield.getWorld().dropItem(shield.getEyeLocation(), new ItemStack(Material.BLAZE_ROD, 1));
													Vector vector = shield.getEyeLocation().getDirection();
													vector = vector.multiply(0.3);
													itemDropped.setVelocity(vector);
													player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 100.0F, 8.0F);
													player.sendMessage("§f[§dPhantom §6Shield§f] §e» §bNana loves these!");
												} else if (blazeRod == blazeRodNanaChoice) {
													Item itemDropped = shield.getWorld().dropItem(shield.getEyeLocation(), new ItemStack(Material.BONE, 1));
													Vector vector = shield.getEyeLocation().getDirection();
													vector = vector.multiply(0.3);
													itemDropped.setVelocity(vector);
													player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 100.0F, 8.0F);
													player.sendMessage("§f[§dPhantom §6Shield§f] §e» §bFun fact! Using Wither Skeletons was Nana\'s choice!");
												}
												long random = Math.round(Math.random() * randomMultiplier);
												if (random == randomGoodDay) {
													player.sendMessage("§f[§dPhantom §6Shield§f] §e» §bGood Day!");
													player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SKELETON_AMBIENT, 1.0F, 8.0F);
												} else if (random == randomWwww) {
													player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SKELETON_AMBIENT, 1.0F, 8.0F);
													player.sendMessage("§f[§dPhantom §6Shield§f] §e» §bWwww!");
												} else if (random == randomPhantomsWhere) {
													player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SKELETON_AMBIENT, 1.0F, 8.0F);
													player.sendMessage("§f[§dPhantom §6Shield§f] §e» §bWhere are the Phantoms??");
												} else if (random == randomHi) {
													player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SKELETON_AMBIENT, 1.0F, 8.0F);
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
									for (Entry<UUID, Player> entry : Main.PSPlayerPair.entrySet()) {
										if (entry.getKey().equals(entity.getUniqueId())) {
											player = entry.getValue();
											skmcsx.getConfig().set("PhantomShields." + entity.getUniqueId().toString() + ".Owner", player.getName());
											Main.PSPlayerPair.remove(entity.getUniqueId());
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
					
//				for (World world : getServer().getWorlds()) {
//					for (Player player : world.getPlayers()) {
//						
//						if (player.getVehicle() instanceof Vehicle) {
//							Vehicle vehicleCheck = (Vehicle) player.getVehicle();
//						
//							if (vehicleCheck!=null) {
//							
//								Entity vehicle = player.getVehicle(); 
//								double velocityVehicle = (Math.sqrt(Math.pow(vehicle.getVelocity().getX(), 2) + Math.pow(vehicle.getVelocity().getY(), 2) + Math.pow(vehicle.getVelocity().getZ(), 2)) * 4.3178 - 0.002141) * 3.6;
//					   	 	double velocityPlayer = Math.sqrt(Math.pow(player.getVelocity().getX(), 2) + Math.pow(player.getVelocity().getY() + 0.0784000015258789, 2) + Math.pow(player.getVelocity().getZ(), 2));
//							
//								if (player.getVehicle().getType().equals(EntityType.MINECART)) {
//							    
//									DecimalFormat df2 = new DecimalFormat("0.00");
//									String vehicleDisplay = vehicle.getType().toString();
//									player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.GOLD + vehicleDisplay.substring(0, 1).toUpperCase() + vehicleDisplay.substring(1, vehicleDisplay.length()).toLowerCase() + ChatColor.DARK_AQUA + " Speed: " + ChatColor.GREEN + df2.format(velocityVehicle) + " km/h"));
//							    
//					    } else if (player.getVehicle().getType().equals(EntityType.BOAT) || player.getVehicle().getType().equals(EntityType.HORSE)) {
//						    	
//						    	DecimalFormat df2 = new DecimalFormat("0.00");
//							    String vehicleDisplay = vehicle.getType().toString();
//							    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.GOLD + vehicleDisplay.substring(0, 1).toUpperCase() + vehicleDisplay.substring(1, vehicleDisplay.length()).toLowerCase() + ChatColor.DARK_AQUA + " Speed: " + ChatColor.GREEN + df2.format(velocityPlayer) + " m/s"));
//								}
//							}
//						}
//					}
//				}
				
				for (Player player : Bukkit.getOnlinePlayers()) {
					boolean show = true;
					if (player.getInventory().getItemInMainHand() != null) {
						if (player.getInventory().getItemInMainHand().getType().equals(Material.ENDER_PEARL)) {
							show = false;
						}
					}
					if (player.getInventory().getItemInOffHand() != null) {
						if (player.getInventory().getItemInOffHand().getType().equals(Material.ENDER_PEARL)) {
							show = false;
						}
					}
						
					if (show == true) {
						if (player.getVehicle() instanceof Vehicle || player.isGliding() || velocityDisplay.containsKey(player)) {
							if (!Main.velocitylastLoc.containsKey(player)) {
								Main.velocitylastLoc.put(player, player.getLocation());
								player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.DARK_AQUA + "Loading.."));
							} else {
								Location posNow = player.getLocation();
								Location posLast = Main.velocitylastLoc.get(player);
								Main.velocitylastLoc.put(player, player.getLocation());
								
								if (posNow.getWorld().equals(posLast.getWorld())) {
									double velocity = posNow.distance(posLast) * 3.6;
									
									if (player.isGliding() == false && !(player.getVehicle() instanceof Vehicle)) {
										if (!velocityDisplay.containsKey(player)) {
											velocityDisplay.put(player, (long) 0);
										} else {
											if (velocityDisplay.get(player) < 4) {
												velocityDisplay.put(player, velocityDisplay.get(player) + 1);
											} else {
												velocityDisplay.remove(player);
											}
										}
									} else if (player.isGliding() == true){
										velocityDisplay.put(player, (long) 0);
									} else {
										velocityDisplay.remove(player);
									}
								
									DecimalFormat df2 = new DecimalFormat("0.00");
									if (player.getVehicle() instanceof Vehicle) {
										Vehicle vehicle = (Vehicle) player.getVehicle();
										player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.GOLD + WordUtils.capitalize(vehicle.getType().toString().replace("_", " ").toLowerCase()) + ChatColor.DARK_AQUA + " Speed: " + ChatColor.GREEN + df2.format(velocity) + " km/h"));
									} else if (player.getInventory().getChestplate() != null){
										if (player.getInventory().getChestplate().getType().equals(Material.ELYTRA)){
											String message = ChatColor.GOLD + "Gliding" + ChatColor.DARK_AQUA + " Speed: " + ChatColor.GREEN + df2.format(velocity) + " km/h";									
											ItemStack item = player.getInventory().getChestplate();
											if (player.getGameMode().equals(GameMode.CREATIVE) || item.getItemMeta().isUnbreakable() == true) {
												message = message + ChatColor.DARK_GREEN + " |" + ChatColor.LIGHT_PURPLE + " Estimated Elytra Lifetime: " + ChatColor.AQUA + "FOREVER";
											} else {
												Damageable damageable = (Damageable) player.getInventory().getChestplate().getItemMeta();
												long timeLeft = item.getType().getMaxDurability() - damageable.getDamage() - 1;
												if (item.containsEnchantment(Enchantment.DURABILITY)) {
													timeLeft = timeLeft * (item.getEnchantmentLevel(Enchantment.DURABILITY) + 1);
												}
												long minutes = TimeUnit.SECONDS.toMinutes(timeLeft);
												
												if (minutes > 1) {
													message = message + ChatColor.DARK_GREEN + " |" + ChatColor.LIGHT_PURPLE + " Estimated Elytra Lifetime: " + ChatColor.AQUA + minutes + " Minutes";
												} else if (minutes == 1) {
													message = message + ChatColor.DARK_GREEN + " |" + ChatColor.LIGHT_PURPLE + " Estimated Elytra Lifetime: " + ChatColor.AQUA + minutes + " Minute";
												} else if (timeLeft != 1){
													message = message + ChatColor.DARK_GREEN + " |" + ChatColor.LIGHT_PURPLE + " Estimated Elytra Lifetime: " + ChatColor.RED + timeLeft + " Seconds";
												} else {
													message = message + ChatColor.DARK_GREEN + " |" + ChatColor.LIGHT_PURPLE + " Estimated Elytra Lifetime: " + ChatColor.RED + timeLeft + " Second";
												}
											}
											player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
										}
									}
								} else {
									player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.DARK_AQUA + "Loading.."));
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
                			
                			if (((Item) entity).getPickupDelay() < 100) {
                				
                				boolean addTimer = false;
                				String preTimerName = "";
                				ChatColor timerColor = ChatColor.GREEN;
                				
                	    		String materialRaw = "";
                    			String materialDisplay = "";
                				
                				entity.setGlowing(true);
                    			int despawnTimer = 6000 - entity.getTicksLived();
                    			if (despawnTimer < 2400 && despawnTimer > 599) {
                    				scoreboard.getTeam("ItemExpireYellow").addEntry(entity.getUniqueId().toString());   
                    				scoreboard.getTeam("ItemExpireRed").removeEntry(entity.getUniqueId().toString());
                    				timerColor = ChatColor.YELLOW;
                    			} else if (despawnTimer < 600) {
                    				scoreboard.getTeam("ItemExpireRed").addEntry(entity.getUniqueId().toString());    
                    				scoreboard.getTeam("ItemExpireYellow").removeEntry(entity.getUniqueId().toString());
                    				timerColor = ChatColor.RED;
                    			} else {
                    				scoreboard.getTeam("ItemExpireYellow").removeEntry(entity.getUniqueId().toString());
                    				scoreboard.getTeam("ItemExpireRed").removeEntry(entity.getUniqueId().toString());
                    				timerColor = ChatColor.GREEN;
                    			}
                				
                				if (entity.getNearbyEntities(1, 1, 1).size() < 6) {
	                    			
	                    			if (item.getItemStack().getItemMeta().getDisplayName() != "") {
	                    				materialRaw = item.getItemStack().getItemMeta().getDisplayName().toString();
	                	    			if (item.getItemStack().getItemMeta().hasEnchants() == false) {
	                    				    materialDisplay = ChatColor.GREEN.toString() + item.getItemStack().getItemMeta().getDisplayName().toString();
	                    				} else {
	                    					materialDisplay = ChatColor.AQUA.toString() + item.getItemStack().getItemMeta().getDisplayName().toString();
	                    				}
	                	    		} else {
	                	    			materialRaw = item.getItemStack().getType().toString();
	                		    		if (Main.specialName.containsKey(materialRaw.toUpperCase())) {
	                		    			materialDisplay = Main.specialName.get(materialRaw.toUpperCase());
	                		    			if (item.getItemStack().getItemMeta().hasEnchants() == true) {
	                		    				materialDisplay = ChatColor.AQUA.toString() + materialDisplay;
	                		    			}
	                		    		} else if (item.getItemStack().getType().equals(Material.POTION)) {
	                		    			PotionMeta meta = (PotionMeta) item.getItemStack().getItemMeta();
	                		    			String potionTypeName = "";
	                		    			if (meta.getCustomEffects().size() < 1) {
	                		    				potionTypeName = "POTION." + meta.getBasePotionData().getType().toString().toUpperCase();
	                		    			} else {
	                		    				potionTypeName = "POTION.MULTIPLE";
	                		    			}
	                		    			if (Main.specialName.containsKey(potionTypeName)) {
	                		    				materialDisplay = Main.specialName.get(potionTypeName.toUpperCase());
	                		    			} else {
	                		    				materialDisplay = WordUtils.capitalizeFully(meta.getBasePotionData().getType().toString().toLowerCase());
	                		    			}
	                		    			if (item.getItemStack().getItemMeta().hasEnchants() == true) {
	                		    				materialDisplay = ChatColor.AQUA.toString() + materialDisplay;
	                		    			}
	                		    		} else {
	                				        materialDisplay = WordUtils.capitalizeFully(materialRaw.replace("_", " "));
	                    				    if (item.getItemStack().getItemMeta().hasEnchants() == true) {
	                    				    	materialDisplay = ChatColor.AQUA.toString() + materialDisplay;
	                     				    }
	                		    		}
	                	    		}
	                    			
	                    			if (item.getItemStack().getType().getMaxStackSize() == 1 || item.getItemStack().getAmount() == 1) {
	                    				if (item.getItemStack().getItemMeta() instanceof Damageable) {
	                    					Damageable tool = (Damageable) item.getItemStack().getItemMeta();
	                    					if (tool.getDamage() != 0) {
		                    					int durability = item.getItemStack().getType().getMaxDurability() - tool.getDamage();
		                    					int maxDur = item.getItemStack().getType().getMaxDurability();
		                    					double percentage = ((double) durability / (double) maxDur) * 100;
		                    					ChatColor color = ChatColor.GREEN;
		                    					if (percentage > 66.666) {
		                    						color = ChatColor.GREEN;
		                    					} else if (percentage > 33.333) {
		                    						color = ChatColor.YELLOW;
		                    					} else {
		                    						color = ChatColor.RED;
		                    					}
		                    					String durDisplay = color + String.valueOf(durability) + "/" + String.valueOf(maxDur);
		                    					preTimerName = ChatColor.WHITE + materialDisplay + ChatColor.GOLD + " [" + durDisplay + ChatColor.GOLD + "]";
	                    					} else {
	                    						preTimerName = ChatColor.WHITE + materialDisplay + ChatColor.GOLD + " [%Timer%" + ChatColor.GOLD + "]";
		                    					addTimer = true;
	                    					}
	                    				} else {
	                    					preTimerName = ChatColor.WHITE + materialDisplay + ChatColor.GOLD + " [%Timer%" + ChatColor.GOLD + "]";
	                    					addTimer = true;
	                    				}
	                    			} else {
	                    				preTimerName = ChatColor.WHITE + materialDisplay + ChatColor.AQUA + " x" + item.getItemStack().getAmount();
	                    			}
	                			
	                    			entity.setCustomNameVisible(true);	                    			
                				} else {
                					entity.setCustomNameVisible(false);	   
                				}
                				
                				if (despawnTimer < 600) {
                    				addTimer = true;
                    			}
                    			
                    			if (addTimer == true) {
                    				int despawnSec = (int) Math.floor((double) despawnTimer / (double) 20);
                    				int minutes = (int) Math.floor((double) despawnSec / (double) 60);
                    				int seconds = despawnSec - (minutes * 60);
                    				if (minutes < 0) {
                    					minutes = 0;
                    				}
                    				if (seconds < 0) {
                    					seconds = 0;
                    				}
                    				String timerDisplay = "0" + String.valueOf(minutes) + ":";
                    				if (seconds < 10) {
                    					timerDisplay = timerDisplay + "0" + String.valueOf(seconds);
                    				} else {
                    					timerDisplay = timerDisplay + String.valueOf(seconds);
                    				}
                    				
                    				if (!timerDisplay.contains("%Timer%")) {
                    					preTimerName = ChatColor.WHITE + materialDisplay + ChatColor.GOLD + " [%Timer%" + ChatColor.GOLD + "]";
                    				}
                    				entity.setCustomName(preTimerName.replace("%Timer%", timerColor + timerDisplay)); 
                    			} else {
                    				entity.setCustomName(preTimerName);
                    			}
                			
                			}
                		}
                	}
                }				
			}
			
		}.runTaskTimer(this, 0, 10);
	}
}
