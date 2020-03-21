package com.loohp.skmcextra.Events;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.regex.Pattern;

import org.apache.commons.lang.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Container;
import org.bukkit.block.ShulkerBox;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import com.loohp.skmcextra.Main;

import net.craftersland.data.bridge.PD;
import net.craftersland.data.bridge.api.events.SyncCompleteEvent;
import net.ess3.api.MaxMoneyException;
import net.ess3.api.events.TPARequestEvent;
import net.md_5.bungee.api.ChatColor;

public class EventsClass implements Listener {
	
	@EventHandler
	public void onHealthSync(SyncCompleteEvent event) {
		Player player = event.getPlayer();
		UUID uuid = player.getUniqueId();
		
		new BukkitRunnable() {
        
			@Override
			public void run() {
		
				Main.mysqlSetup(false);
				
				double health = 20.0;
				boolean scaleToggle = false;
				
				if (playerExists(uuid) == false) {
					createPlayer(uuid, player);
				} else {
					try {
						PreparedStatement statement = Main.getConnection().prepareStatement("SELECT * FROM " + Main.table + " WHERE UUID=?");
						statement.setString(1, uuid.toString());
						ResultSet results = statement.executeQuery();
						results.next();
						
						health = results.getDouble("HEALTH");
						scaleToggle = results.getBoolean("SCALE_TOGGLE");
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
				
				try {
					Main.getConnection().close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
				
				double max_health = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
				player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health);
				player.setHealth(health);
				player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(max_health);
				
				if (PD.instance.getConfig().getBoolean("Debug.HealthSync") == true) {
					Bukkit.getConsoleSender().sendMessage("[MysqlPlayerDataBridge - SKMCHealthPatch] Debug Health - Set data - " + player.getName() + " - Health: " + health);
				}
				
				player.setHealthScaled(scaleToggle);
			}
		}.runTaskLater(Main.skmcsx, 5);
	}
	
	@EventHandler
	public void onHealthSave(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		UUID uuid = player.getUniqueId();
		
		Main.mysqlSetup(false);
		
		if (playerExists(uuid) == false) {
			createPlayer(uuid, player);
		} else {
			try {
				PreparedStatement statement = Main.getConnection().prepareStatement("UPDATE " + Main.table + " SET HEALTH=? WHERE UUID=?");
				statement.setDouble(1, player.getHealth());
				statement.setString(2, uuid.toString());
				statement.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		if (PD.instance.getConfig().getBoolean("Debug.HealthSync") == true) {
			Bukkit.getConsoleSender().sendMessage("[MysqlPlayerDataBridge - SKMCHealthPatch] Debug Health - Data save - " + player.getName() + " - Health: " + player.getHealth());
		}
		
		try {
			Main.getConnection().close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public boolean playerExists(UUID uuid) {
		try {
			PreparedStatement statement = Main.getConnection().prepareStatement("SELECT * FROM " + Main.table + " WHERE UUID=?");
			statement.setString(1, uuid.toString());

			ResultSet results = statement.executeQuery();
			if (results.next()) {
				return true;
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public void createPlayer(UUID uuid, Player player) {
		try {
			PreparedStatement insert = Main.getConnection().prepareStatement("INSERT INTO " + Main.table + " (UUID,NAME,HEALTH,SCALE_TOGGLE) VALUES (?,?,?,?)");
			insert.setString(1, uuid.toString());
			insert.setString(2, player.getName());
			insert.setDouble(3, player.getHealth());
			insert.setBoolean(4, false);
			insert.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	@EventHandler
	public void onRightClickShulkerBoxItem(InventoryClickEvent event) {
		Player player = (Player) event.getWhoClicked();
		if (Main.shulkerView.containsKey(player)) {
			if (event.getView().getTopInventory().equals(Main.shulkerView.get(player))) {
				event.setCancelled(true);
				if (event.getRawSlot() != 44) {
					return;
				} else {
			        player.closeInventory();
			        return;
				}	
			}		
		}
		if (!event.getClick().equals(ClickType.RIGHT)) {
			return;
		}
		if (event.getCurrentItem() == null) {
			return;
		}
		if (!event.getCurrentItem().getType().toString().toUpperCase().contains("SHULKER_BOX")) {
			return;
		}
		if (!player.getOpenInventory().getTopInventory().getType().equals(InventoryType.CRAFTING)) {
			if (!Main.isMPDB.containsKey(player)) {
				Main.shulkerLastInv.put(player, player.getOpenInventory().getTopInventory());
			}
		}
		ItemStack item = event.getCurrentItem();
	    if(item.getItemMeta() instanceof BlockStateMeta){
	        BlockStateMeta im = (BlockStateMeta)item.getItemMeta();
	        if(im.getBlockState() instanceof ShulkerBox){
	            ShulkerBox shulker = (ShulkerBox) im.getBlockState();
	            Inventory inv = Bukkit.createInventory(null, 45, WordUtils.capitalize(event.getCurrentItem().getType().toString().toLowerCase().replace("_", " ")) + " Content");
	            inv.setContents(shulker.getInventory().getContents());
	            ItemStack pane = new ItemStack(Material.MAGENTA_STAINED_GLASS_PANE, 1);
	            ItemMeta panemeta = pane.getItemMeta();
	            panemeta.setDisplayName(ChatColor.LIGHT_PURPLE + "");
	            pane.setItemMeta(panemeta);
	            inv.setItem(27, pane);
	            inv.setItem(28, pane);
	            inv.setItem(29, pane);
	            inv.setItem(30, pane);
	            inv.setItem(31, pane);
	            inv.setItem(32, pane);
	            inv.setItem(33, pane);
	            inv.setItem(34, pane);
	            inv.setItem(35, pane);
	            ItemStack barrier = new ItemStack(Material.BARRIER, 1);
	            ItemMeta barriermeta = barrier.getItemMeta();
	            barriermeta.setDisplayName(ChatColor.RED + "Exit Preview");
	            barrier.setItemMeta(barriermeta);
	            inv.setItem(44, barrier);
	            Main.shulkerView.put(player, inv);
	            Bukkit.getScheduler().runTask(Main.skmcsx, () -> player.openInventory(inv));
	        }
	    }
	    event.setCancelled(true);
	}
	
	@EventHandler
	public void onCloseShulkerBoxView(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        if (Main.shulkerLastInv.containsKey(player)) {
        	if (!Main.shulkerView.containsKey(player)) {
        		if (Main.isMPDB.containsKey(player)) {
            		Main.isMPDB.remove(player);
            	}
        	}
        }
        if (!Main.shulkerView.containsKey(player)) {
        	return;
        }
        if (event.getInventory().equals(Main.shulkerView.get(player))) {
        	if (Main.shulkerLastInv.containsKey(player) && !Main.isMPDB.containsKey(player)) {
        		Bukkit.getScheduler().runTaskLater(Main.skmcsx, () -> player.openInventory(Main.shulkerLastInv.get(player)), 1);
        		Bukkit.getScheduler().runTaskLater(Main.skmcsx, () -> Main.shulkerLastInv.remove(player), 2);
        	} else {
        		String string = Main.isMPDB.get(player);
        		Bukkit.getScheduler().runTaskLater(Main.skmcsx, () -> player.chat(string), 1);
        	}
        	Main.shulkerView.remove(player);
        }
    }
	
	@EventHandler
	public void onMPDBbutMoves(PlayerMoveEvent event) {
		Player player = (Player) event.getPlayer();
		if (Main.isMPDB.containsKey(player)) {
			Main.isMPDB.remove(player);
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlaceHat(InventoryClickEvent event) {
		if (event.getCursor() == null || event.getCursor().getType().equals(Material.AIR)) {
			return;
		}
		if (event.getClickedInventory() == null || !(event.getClickedInventory().getType().equals(InventoryType.PLAYER) || event.getClickedInventory().getType().equals(InventoryType.CREATIVE))) {
			return;
		}
		Player player = (Player) event.getWhoClicked();
		
		if (event.getRawSlot() == 5) {
			if (player.hasPermission("essentials.hat")) {
				if (!player.hasPermission("essentials.hat.prevent-type." + event.getCursor().getType().toString())) {
					if (!event.getCursor().getType().toString().toLowerCase().contains("helmet")) {
						if (event.getClick().isLeftClick()) {
							Bukkit.getPluginManager().callEvent(new InventoryClickEvent(event.getView(), event.getSlotType(), event.getSlot(), ClickType.RIGHT, InventoryAction.SWAP_WITH_CURSOR));
							event.setCancelled(true);
							return;
						}
						ItemStack oriHelmet = player.getInventory().getHelmet();
						ItemStack newHelmet = event.getCursor();
						player.getInventory().setHelmet(newHelmet);
						event.setCursor(oriHelmet);
					}
				}
			}
		}		
	}
	
	@EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
		if (event.getPlayer() == null) {
			return;
		}
		Player player = event.getPlayer();
		if (!Main.PSEditRange.containsKey(player)) {
			return;
		}
		event.setCancelled(true);
		String message = event.getMessage();
		long unixTime = System.currentTimeMillis();
		Main.PSEditRangeTimeOut.put(player, unixTime);
		if (message.toLowerCase().equals("cancel")) {
			player.sendMessage(ChatColor.RED + "Phantom Shield range input cancelled!");
			Main.PSEditRangeTimeOut.remove(player);
			Main.PSEditRange.remove(player);
			return;
		}
		long range = 150;
		try {
	        range = Long.parseLong(message);
	    } catch (NumberFormatException nfe) {
	        player.sendMessage(ChatColor.RED + "Invalid Range! Please input a number between 0 and 150 (Type \"Cancel\" to cancel)");
	        return;
        }
		if (range < 0 || range > 150) {
			player.sendMessage(ChatColor.RED + "Invalid Range! Please input a number between 0 and 150 (Type \"Cancel\" to cancel)");
	        return;
		}
		player.sendMessage(ChatColor.AQUA + "New Range: " + String.valueOf(range));
		Entity entity = Main.PSEditRange.get(player);
		Main.skmcsx.getConfig().set("PhantomShields." + entity.getUniqueId().toString() + ".Range", range);
		Main.skmcsx.saveConfig();
		player.sendMessage(ChatColor.GREEN + "Range sucessfully updated!");
		player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.0F, 8.0F);
		Main.PSEditRangeTimeOut.remove(player);
		Main.PSEditRange.remove(player);
		Bukkit.getScheduler().runTask(Main.skmcsx, () -> Main.openPSGUI(player, entity));
	}
	
	@EventHandler 
	public void onInteractShieldGUI(InventoryClickEvent event) {
		boolean isShieldGUI = false;
		Entity entity = null;
		for (Entry<Entity, Inventory> entry : Main.PSGUIedit.entrySet()) {
			if (entry.getValue().equals(event.getInventory())) {
				isShieldGUI = true;
				entity = entry.getKey();
				break;
			}
		}
		if (isShieldGUI == false) {
			return;
		}
		if (event.getClick().equals(ClickType.NUMBER_KEY)){
			event.setCancelled(true);
        }
		event.setCancelled(true);
		Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
        	return;
        }
        
        if (event.getAction().equals(InventoryAction.NOTHING) || event.getAction().equals(InventoryAction.UNKNOWN) || event.getAction().equals(InventoryAction.DROP_ALL_CURSOR) || event.getAction().equals(InventoryAction.DROP_ALL_SLOT) || event.getAction().equals(InventoryAction.DROP_ONE_CURSOR) || event.getAction().equals(InventoryAction.DROP_ONE_SLOT  )) {
        	return;
        }

        if (event.getRawSlot() == 12) {
        	player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 8.0F);
        	player.sendMessage(ChatColor.GREEN + "Please type the new radius in the chat! (Accepts 0 - 150)");
        	Main.PSEditRange.put(player, entity);
        	long unixTime = System.currentTimeMillis();
        	Main.PSEditRangeTimeOut.put(player, unixTime);
        	player.closeInventory();
        }
        
        if (event.getRawSlot() == 14) {
        	player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.0F, 8.0F);
        	player.sendMessage(ChatColor.YELLOW + "Removed Phantom Shield! A Phantom Shield with the remaining duration has been given to you!");
        	ItemStack item = Main.skmcsx.getConfig().getItemStack("PhantomShieldsItem");
			item.setAmount(1);
			ItemMeta meta = item.getItemMeta();
			ArrayList<String> lore = (ArrayList<String>) meta.getLore();
			if (lore.size() < 4) {
				lore.add("");
			}
			if (lore.size() < 5) {
				lore.add("");
			}
			if (lore.size() < 6) {
				lore.add("");
			}
			long expire = Main.skmcsx.getConfig().getLong("PhantomShields." + entity.getUniqueId().toString() + ".Expire");
			long unixTime = System.currentTimeMillis();
			
			LocalDateTime now = LocalDateTime.ofInstant(Instant.ofEpochMilli(unixTime), ZoneId.systemDefault());
			LocalDateTime end = LocalDateTime.ofInstant(Instant.ofEpochMilli(expire), ZoneId.systemDefault());
			String timer = "";
			long weeks = ChronoUnit.WEEKS.between(now, end);
			long days = ChronoUnit.DAYS.between(now, end);
			long hrs = ChronoUnit.HOURS.between(now, end);
			long mins = ChronoUnit.MINUTES.between(now, end);
			long secs = ChronoUnit.SECONDS.between(now, end);
			long milli = ChronoUnit.MILLIS.between(now, end);
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
				if (secs < 2) {
					timer = secs + " Second";
				} else {
					timer = secs + " Seconds";
				}
			}
			String timeLeft = ChatColor.LIGHT_PURPLE + "Time Left: ";
			if (mins < 5) {
				timeLeft = timeLeft + ChatColor.RED + timer;
			} else if (hrs == 0) {
				timeLeft = timeLeft + ChatColor.YELLOW + timer;
			} else {
				timeLeft = timeLeft + ChatColor.GREEN + timer;
			}
			
			lore.set(4, timeLeft);
			lore.set(5, ChatColor.DARK_GRAY + String.valueOf(milli));
			
			meta.setLore(lore);
			meta.setDisplayName(meta.getDisplayName() + ChatColor.GRAY + " (" + timer + " Left)");
			ItemStack newItem = new ItemStack(Material.WITHER_SKELETON_SPAWN_EGG, 1);
			newItem.setItemMeta(meta);
			
			LivingEntity livingentity = (LivingEntity) entity;
			livingentity.setHealth(0);
			
			player.getWorld().dropItem(player.getEyeLocation(), newItem);

			player.closeInventory();
        }
	}
	
	@EventHandler
	public void onInteractShield(PlayerInteractEntityEvent event) {
		Entity entity = event.getRightClicked();
		Player player = event.getPlayer();
		if (event.getHand().equals(EquipmentSlot.OFF_HAND)) {
			return;
		}
		if (entity.getType().equals(EntityType.WITHER_SKELETON) && entity.isSilent() == true) {
			LivingEntity livingentity = (LivingEntity) entity;
			if (livingentity.getEquipment().getItemInMainHand().getType().equals(Material.PHANTOM_SPAWN_EGG) && livingentity.getEquipment().getItemInOffHand().getType().equals(Material.PHANTOM_MEMBRANE)) {
				for (Entry<UUID, Player> entry : Main.PSPlayerPair.entrySet()) {
					if (entry.getValue().equals(event.getPlayer())) {
						entry.getValue().sendMessage(ChatColor.RED + "Please wait..");
						event.setCancelled(true);
						return;
					}
				}
				for (Entry<Player, Long> entry : Main.possiblePS.entrySet()) {
					if (entry.getKey().equals(event.getPlayer())) {
						entry.getKey().sendMessage(ChatColor.RED + "Please wait..");
						event.setCancelled(true);
						return;
					}
				}
				String owner = Main.skmcsx.getConfig().getString("PhantomShields." + entity.getUniqueId().toString() + ".Owner");
				if (!owner.equals(player.getName())) {
					long range = Main.skmcsx.getConfig().getLong("PhantomShields." + entity.getUniqueId().toString() + ".Range");
					player.sendMessage(ChatColor.AQUA + "This " + ChatColor.LIGHT_PURPLE + "Phantom " + ChatColor.GOLD + "Shield " + ChatColor.AQUA + "has a range of " + ChatColor.LIGHT_PURPLE + range + ChatColor.AQUA + " and it belongs to " + ChatColor.YELLOW + owner + ChatColor.AQUA + "!");
				}
				if (player.hasPermission("skmcextra.admin.phantomshield") && !owner.equals(player.getName())) {
					player.sendMessage(ChatColor.YELLOW + "You are bypassing protection on this Phantom Shield that belongs to " + ChatColor.GREEN + owner + ChatColor.YELLOW + "!");
					Main.openPSGUI(player, entity);
					player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0F, 8.0F);
				} else if (owner.equals(player.getName())) {
					Main.openPSGUI(player, entity);
					player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, 8.0F);
				}
			}
		}
	}
	
	@EventHandler
	public void onEntityTarget(EntityTargetEvent event) {
		Entity entity = event.getTarget();
		if (entity != null) {
			if (entity.getType().equals(EntityType.WITHER_SKELETON) && entity.isSilent() == true) {
				LivingEntity livingentity = (LivingEntity) entity;
				if (livingentity.getEquipment().getItemInMainHand().getType().equals(Material.PHANTOM_SPAWN_EGG) && livingentity.getEquipment().getItemInOffHand().getType().equals(Material.PHANTOM_MEMBRANE)) {
					event.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler
	public void hitPhantomShield(EntityDamageByEntityEvent event) {
		if(event.getDamager().getType().equals(EntityType.PLAYER)) {
			Player player = (Player) event.getDamager();
			Entity entity = event.getEntity();
			if (entity.getType().equals(EntityType.WITHER_SKELETON) && entity.isSilent() == true) {
				LivingEntity livingentity = (LivingEntity) entity;
				if (livingentity.getEquipment().getItemInMainHand().getType().equals(Material.PHANTOM_SPAWN_EGG) && livingentity.getEquipment().getItemInOffHand().getType().equals(Material.PHANTOM_MEMBRANE)) {
					String owner = Main.skmcsx.getConfig().getString("PhantomShields." + entity.getUniqueId().toString() + ".Owner");
					if (player.hasPermission("skmcextra.admin.phantomshield")) {
						if (!owner.equals(player.getName())) {
							player.sendMessage(ChatColor.YELLOW + "You are bypassing protection on this Phantom Shield that belongs to " + ChatColor.GREEN + owner + ChatColor.YELLOW + "!");
						} else {
							player.sendMessage(ChatColor.YELLOW + "You are bypassing protection from damaging phantom shields! Note that items will not be refunded this way.");
						}
					} else if (!owner.equals(player.getName())) {
						event.setCancelled(true);
						player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0F, 8.0F);
						player.sendMessage(ChatColor.RED + "You cannot remove this Phantom Shield! It belongs to " + ChatColor.YELLOW + owner + ChatColor.RED + "!");
					} else {
						event.setCancelled(true);
						player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0F, 8.0F);
						player.sendMessage(ChatColor.RED + "You cannot damage Phantom Shields! Right Click to remove it!");
					}
				}
			}
		} else {
			Entity entity = event.getEntity();
			if (entity.getType().equals(EntityType.WITHER_SKELETON) && entity.isSilent() == true) {
				LivingEntity livingentity = (LivingEntity) entity;
				if (livingentity.getEquipment().getItemInMainHand().getType().equals(Material.PHANTOM_SPAWN_EGG) && livingentity.getEquipment().getItemInOffHand().getType().equals(Material.PHANTOM_MEMBRANE)) {
					event.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler
	public void PSCreation(CreatureSpawnEvent event) {
		Entity entity = event.getEntity();
		if (entity.getType().equals(EntityType.WITHER_SKELETON) && entity.isSilent() == true) {
			LivingEntity livingentity = (LivingEntity) entity;
			if (livingentity.getEquipment().getItemInMainHand().getType().equals(Material.PHANTOM_SPAWN_EGG) && livingentity.getEquipment().getItemInOffHand().getType().equals(Material.PHANTOM_MEMBRANE)) {
				livingentity.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 999999999, 0));
				long unixTime = System.currentTimeMillis();
				boolean paired = false;
				for (Entry<Player, Long> entry : Main.possiblePS.entrySet()) {
					if (unixTime - (entry.getValue()) < 700 && entity.getNearbyEntities(10, 10, 10).contains(Bukkit.getEntity(entry.getKey().getUniqueId()))) {
						Main.PSPlayerPair.put(entity.getUniqueId(), entry.getKey());
						entry.getKey().playSound(entry.getKey().getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 8.0F);
						Main.possiblePS.remove(entry.getKey());
						paired = true;
						break;
					}
				}
				if (paired == false) {
					Player playerPair = (Player) Main.getClosestPlayer(entity, 7);
					Main.PSPlayerPair.put(entity.getUniqueId(), playerPair);
					playerPair.playSound(playerPair.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0F, 8.0F);
					Main.possiblePS.remove(playerPair);
				}
			}
		}
	}
	
	@EventHandler
    public void placeShield(PlayerInteractEvent event){
		if (event.getItem() != null) {
			ItemMeta meta1 = event.getItem().getItemMeta();
			if (meta1.hasLore() == false) {
				return;
			}
			ArrayList<String> lore1 = (ArrayList<String>) meta1.getLore();
			if (lore1.size() != 6) {
				return;
			}
			if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && event.getItem().getType().equals(Material.WITHER_SKELETON_SPAWN_EGG)){
				for (Entry<UUID, Player> entry : Main.PSPlayerPair.entrySet()) {
					if (entry.getValue().equals(event.getPlayer())) {
						entry.getValue().sendMessage(ChatColor.RED + "Please wait a few seconds between placing Phantom Shields!");
						event.setCancelled(true);
						return;
					}
				}
				for (Entry<Player, Long> entry : Main.possiblePS.entrySet()) {
					if (entry.getKey().equals(event.getPlayer())) {
						entry.getKey().sendMessage(ChatColor.RED + "Please wait a few seconds between placing Phantom Shields!");
						event.setCancelled(true);
						return;
					}
				}
				long unixTime = System.currentTimeMillis();
				Main.possiblePS.put(event.getPlayer(), unixTime);
				ItemStack item = event.getItem();
				ItemMeta meta = item.getItemMeta();
				ArrayList<String> lore = (ArrayList<String>) meta.getLore();
				if (lore.size() == 6) {
					Main.PSSpawnTimerLeft.put(event.getPlayer(), Long.parseLong(lore.get(5).substring(2)));
				}
			}
		}
	}
	
	@EventHandler
	public void phantomShieldKilled(EntityDeathEvent event) throws MaxMoneyException {
		Entity entity = event.getEntity();
		if (entity.getType().equals(EntityType.WITHER_SKELETON) && entity.isSilent() == true) {
			LivingEntity livingentity = (LivingEntity) entity;
			if (livingentity.getEquipment().getItemInMainHand().getType().equals(Material.PHANTOM_SPAWN_EGG) && livingentity.getEquipment().getItemInOffHand().getType().equals(Material.PHANTOM_MEMBRANE)) {
				event.getDrops().clear();
				event.setDroppedExp(0);
				for (Player player : Bukkit.getOnlinePlayers()) {
					Inventory viewing = player.getOpenInventory().getTopInventory();
					Inventory entityView = Main.PSGUIedit.get(entity);
					if (viewing.equals(entityView)) {
						player.closeInventory();
					}
				}
				Main.skmcsx.getConfig().set("PhantomShields." + entity.getUniqueId().toString(), null);
				Main.skmcsx.saveConfig();
			}
		}
	}
	
//=========================================================================	
	@EventHandler
	public void tpSuccess(PlayerTeleportEvent event) {
		Player player = event.getPlayer();
		if (Main.homeWarmUp.containsKey(player)) {
			for (Entry<Pattern, Long> entry : Main.ess3.getUser(player.getName()).getCommandCooldowns().entrySet()) {
                if (entry.getKey().toString().equals(("home( .*)?"))) {
                	Main.homeWarmUp.remove(player);
                	break;
                }
            }
		}
		
		if (Main.spawnWarmUp.containsKey(player)) {
			for (Entry<Pattern, Long> entry : Main.ess3.getUser(player.getName()).getCommandCooldowns().entrySet()) {
                if (entry.getKey().toString().equals(("spawn( .*)?"))) {
                	Main.spawnWarmUp.remove(player);
                	break;
                }
            }
		}
		
		if (Main.backWarmUp.containsKey(player)) {
			for (Entry<Pattern, Long> entry : Main.ess3.getUser(player.getName()).getCommandCooldowns().entrySet()) {
                if (entry.getKey().toString().equals(("back( .*)?"))) {
                	Main.backWarmUp.remove(player);
                	break;
                }
            }
		}
		
		if (Main.tpaWarmUp.containsKey(player)) {
			for (Entry<Pattern, Long> entry : Main.ess3.getUser(player.getName()).getCommandCooldowns().entrySet()) {
                if (entry.getKey().toString().equals(("tpa( .*)?"))) {
                	Main.tpaWarmUp.remove(player);
                	break;
                }
            }
		}
		
		for (Entry<Player, String> entry : Main.tpahereWarmUp.entrySet()) {
			String data = entry.getValue();
			Player acceptPlayer = Bukkit.getPlayer(data.substring(0, data.indexOf(">")));
			if (acceptPlayer.getName().equals(player.getName())) {
				Player target = entry.getKey();
				for (Entry<Pattern, Long> entry1 : Main.ess3.getUser(target.getName()).getCommandCooldowns().entrySet()) {
					if (entry1.getKey().toString().equals(("tpahere( .*)?"))) {
						Main.tpahereWarmUp.remove(target);
						break;
					}
				}
			}
		}
		
	}
	
	@EventHandler
	public void tpaRequest(TPARequestEvent event) {
		Player requester = Bukkit.getPlayer(event.getRequester().getPlayer().getName());
		Player target = Bukkit.getPlayer(event.getTarget().getName());
		String reason = "";
		if (event.isTeleportHere() == false) {
			reason = "tpa";
		} else {
			reason = "tpahere";
		}
		String value = requester.getName() + "," + reason;
		Main.tpaWait.put(target, value);
		requester.sendMessage("§aPlease avoid moving or taking damage while waiting for player response.");
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void tpCommand(PlayerCommandPreprocessEvent event) {
		if (event.getMessage().toLowerCase().startsWith("/tpyes") || event.getMessage().toLowerCase().startsWith("/tpaccept")) {
			Player acceptPlayer = event.getPlayer();
			for (Entry<Player, String> entry : Main.tpaWait.entrySet()) {
				if (entry.getKey().getName().equals(acceptPlayer.getName())) {
					String data = entry.getValue();
					Player player = Bukkit.getPlayer(data.substring(0, data.indexOf(",")));
					String reason = data.substring(data.indexOf(",") + 1, data.length());
					Main.tpaWait.remove(entry.getKey());
					if (reason.equals("tpa")) {
						boolean alreadyOnCooldown = false;
						long locX = Math.round(Main.ess3.getUser(player.getName()).getBase().getLocation().getX() * 0.3);
						long locY = Math.round(Main.ess3.getUser(player.getName()).getBase().getLocation().getY() * 0.3);
						long locZ = Math.round(Main.ess3.getUser(player.getName()).getBase().getLocation().getZ() * 0.3);
						double health = Main.ess3.getUser(player.getName()).getBase().getHealth();
						String loc = String.valueOf(locX) + "," + String.valueOf(locY) + "." + String.valueOf(locZ) + "=" + String.valueOf(health);
						for (Entry<Pattern, Long> entry1 : Main.ess3.getUser(player.getName()).getCommandCooldowns().entrySet()) {
			                if (entry1.getKey().toString().equals(("tpa( .*)?"))) {
			                	long unixTime = System.currentTimeMillis();
			                	if (entry1.getValue() > unixTime) {
			                		alreadyOnCooldown = true;
			                	}
			                }
			            }

						if (alreadyOnCooldown == false) {
							Main.tpaWarmUp.put(player, loc);
							long expiry = System.currentTimeMillis() + (Main.tpaCooldown * 60 * 1000);
							Date date = new Date(expiry);
							Main.ess3.getUser(player.getName()).addCommandCooldown(Pattern.compile("tpa( .*)?"), date, true);
						}
						
					} else if (reason.equals("tpahere")) {
						boolean alreadyOnCooldown = false;
						long locX = Math.round(Main.ess3.getUser(acceptPlayer.getName()).getBase().getLocation().getX() * 0.3);
						long locY = Math.round(Main.ess3.getUser(acceptPlayer.getName()).getBase().getLocation().getY() * 0.3);
						long locZ = Math.round(Main.ess3.getUser(acceptPlayer.getName()).getBase().getLocation().getZ() * 0.3);
						double health = Main.ess3.getUser(acceptPlayer.getName()).getBase().getHealth();
						String loc = acceptPlayer.getName() + ">" + String.valueOf(locX) + "," + String.valueOf(locY) + "." + String.valueOf(locZ) + "=" + String.valueOf(health);
						for (Entry<Pattern, Long> entry1 : Main.ess3.getUser(acceptPlayer.getName()).getCommandCooldowns().entrySet()) {
			                if (entry1.getKey().toString().equals(("tpahere( .*)?"))) {
			                	long unixTime = System.currentTimeMillis();
			                	if (entry1.getValue() > unixTime) {
			                		alreadyOnCooldown = true;
			                	}
			                }
			            }

						if (alreadyOnCooldown == false) {
							Main.tpahereWarmUp.put(player, loc);
							long expiry = System.currentTimeMillis() + (Main.tpaCooldown * 60 * 1000);
							Date date = new Date(expiry);
							Main.ess3.getUser(player.getName()).addCommandCooldown(Pattern.compile("tpahere( .*)?"), date, true);
						}
					}
				}
			}
		}
		
		if (event.getMessage().toLowerCase().equals("/tpa")) {
			event.setCancelled(true);
			event.getPlayer().sendMessage("§aRequest to teleport to a player. §b/tpa <player>");
		}
		
		if (event.getMessage().toLowerCase().equals("/tpahere")) {
			event.setCancelled(true);
			event.getPlayer().sendMessage("§eRequest a player to teleport to you. §b/tpahere <player>");
		}
		
		if (event.getMessage().toLowerCase().startsWith("/home ")) {
			boolean homeExist = false;
			boolean alreadyOnCooldown = false;
			Player player = event.getPlayer();
			String selected = event.getMessage().substring(event.getMessage().indexOf(" ") + 1, event.getMessage().length());
			for (String home : Main.ess3.getUser(player.getName()).getHomes()) {
				if (home.toLowerCase().equals(selected.toLowerCase())) {
					
					long locX = Math.round(Main.ess3.getUser(player.getName()).getBase().getLocation().getX() * 0.3);
					long locY = Math.round(Main.ess3.getUser(player.getName()).getBase().getLocation().getY() * 0.3);
					long locZ = Math.round(Main.ess3.getUser(player.getName()).getBase().getLocation().getZ() * 0.3);
					double health = Main.ess3.getUser(player.getName()).getBase().getHealth();
					String loc = String.valueOf(locX) + "," + String.valueOf(locY) + "." + String.valueOf(locZ) + "=" + String.valueOf(health);
					for (Entry<Pattern, Long> entry : Main.ess3.getUser(player.getName()).getCommandCooldowns().entrySet()) {
		                if (entry.getKey().toString().equals(("home( .*)?"))) {
		                	long unixTime = System.currentTimeMillis();
		                	if (entry.getValue() > unixTime) {
		                		alreadyOnCooldown = true;
		                	}
		                }
		            }

					if (alreadyOnCooldown == false) {
					Main.homeWarmUp.put(player, loc);
					}
					
					homeExist = true;
					break;
				}
			}
			
			if (homeExist == false) {
				event.setCancelled(true);
				player.chat("/home");
			}
		}
		
		if (event.getMessage().toLowerCase().startsWith("/spawn")) {
			boolean alreadyOnCooldown = false;
			Player player = event.getPlayer();
			long locX = Math.round(Main.ess3.getUser(player.getName()).getBase().getLocation().getX() * 0.3);
			long locY = Math.round(Main.ess3.getUser(player.getName()).getBase().getLocation().getY() * 0.3);
			long locZ = Math.round(Main.ess3.getUser(player.getName()).getBase().getLocation().getZ() * 0.3);
			double health = Main.ess3.getUser(player.getName()).getBase().getHealth();
			String loc = String.valueOf(locX) + "," + String.valueOf(locY) + "." + String.valueOf(locZ) + "=" + String.valueOf(health);
			for (Entry<Pattern, Long> entry : Main.ess3.getUser(player.getName()).getCommandCooldowns().entrySet()) {
                if (entry.getKey().toString().equals(("spawn( .*)?"))) {
                	long unixTime = System.currentTimeMillis();
                	if (entry.getValue() > unixTime) {
                		alreadyOnCooldown = true;
                	}
                }
            }

			if (alreadyOnCooldown == false) {
			Main.spawnWarmUp.put(player, loc);
			}
		}
		
		if (event.getMessage().toLowerCase().startsWith("/back")) {
			boolean alreadyOnCooldown = false;
			Player player = event.getPlayer();
			long locX = Math.round(Main.ess3.getUser(player.getName()).getBase().getLocation().getX() * 0.3);
			long locY = Math.round(Main.ess3.getUser(player.getName()).getBase().getLocation().getY() * 0.3);
			long locZ = Math.round(Main.ess3.getUser(player.getName()).getBase().getLocation().getZ() * 0.3);
			double health = Main.ess3.getUser(player.getName()).getBase().getHealth();
			String loc = String.valueOf(locX) + "," + String.valueOf(locY) + "." + String.valueOf(locZ) + "=" + String.valueOf(health);
			for (Entry<Pattern, Long> entry : Main.ess3.getUser(player.getName()).getCommandCooldowns().entrySet()) {
                if (entry.getKey().toString().equals(("back( .*)?"))) {
                	long unixTime = System.currentTimeMillis();
                	if (entry.getValue() > unixTime) {
                		alreadyOnCooldown = true;
                	}
                }
            }

			if (alreadyOnCooldown == false) {
			Main.backWarmUp.put(player, loc);
			}
		}
	}
	
	
//=====================================================================================
	
	@EventHandler
	public void onDeath(PlayerDeathEvent event) {
		Player player = event.getEntity();
		if (player.getName().equals("NARLIAR")) {
			ItemStack item = new ItemStack(Material.BONE, 1);
			player.getWorld().dropItemNaturally(player.getLocation(), item);
		} else if (player.getName().equals("LOOHP")) {
			ItemStack item = new ItemStack(Material.APPLE, 1);
			player.getWorld().dropItemNaturally(player.getLocation(), item);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void displayChestBreak(BlockBreakEvent event) {
		Block block = event.getBlock();
		if (block.getState() instanceof Container) {							
			String locString = block.getLocation().getBlockX() + "," + block.getLocation().getBlockY() + "," + block.getLocation().getBlockZ() + "," + block.getLocation().getWorld().getName();
			if (Main.skmcsx.getConfig().contains("DisplayContainers." + locString)) {
				if (event.getPlayer().hasPermission("skmcextra.admin.displaycontainers")) {
					event.getPlayer().sendMessage(ChatColor.YELLOW + "You have removed a display container!");
					Main.skmcsx.getConfig().set("DisplayContainers." + locString, null);
					Main.skmcsx.saveConfig();
					if (Main.lwc.getLWC().findProtection(block) != null && Main.lwc.getLWC().isProtectable(block)) {
						Bukkit.getServer().dispatchCommand(event.getPlayer(), "unlock");
						float dir = (float) Math.toDegrees(Math.atan2(event.getPlayer().getLocation().getBlockX() - block.getX(), block.getZ() - event.getPlayer().getLocation().getBlockZ()));
						BlockFace face = Main.getClosestFace(dir);
						Bukkit.getPluginManager().callEvent(new PlayerInteractEvent(event.getPlayer(), Action.RIGHT_CLICK_BLOCK, new ItemStack(Material.AIR), block, face));
					}
				} else {
					if (Main.lwc.getLWC().findProtection(block) == null) {
						event.setCancelled(true);
					}
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void displayChest(InventoryClickEvent event) {
		if (event.getView().getTopInventory().getLocation() != null) {
			if (event.getView().getTopInventory().getLocation().getBlock().getState() instanceof Container) {
				Block block = event.getView().getTopInventory().getLocation().getBlock();
				String locString = block.getLocation().getBlockX() + "," + block.getLocation().getBlockY() + "," + block.getLocation().getBlockZ() + "," + block.getLocation().getWorld().getName();
				if (Main.skmcsx.getConfig().contains("DisplayContainers." + locString)) {
					if (!event.getWhoClicked().hasPermission("skmcextra.admin.displaycontainers")) {
						event.getWhoClicked().sendMessage(ChatColor.RED + "You cannot move items while looking at a display container!");
						event.setCancelled(true);
					} else {
						event.getWhoClicked().sendMessage(ChatColor.YELLOW + "You are modifying a display container!");
					}
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onInteract(InventoryClickEvent event) {
		if (event.getClickedInventory() == null) {
			return;
		}
		if (event.isCancelled() == true) {
			return;
		}
		if (!event.getAction().equals(InventoryAction.NOTHING) && !event.getAction().equals(InventoryAction.UNKNOWN) && !event.getAction().equals(InventoryAction.DROP_ALL_CURSOR) && !event.getAction().equals(InventoryAction.DROP_ALL_SLOT) && !event.getAction().equals(InventoryAction.DROP_ONE_CURSOR) && !event.getAction().equals(InventoryAction.DROP_ONE_SLOT  )) {
			if (event.getCurrentItem() == null) {
				return;
			}
			ItemStack item = event.getCurrentItem();
			if (item.hasItemMeta() == false) {
				return;
			}
			ItemMeta meta = item.getItemMeta();
			if (item.getType().equals(Material.DIAMOND_SWORD) && meta.hasLore() == true) {
				if (meta.getLore().contains("§eThe Legendary Sword from §6the sky~~")) {
					int attackDamge = 8;
					String swordName = "§d§lSunKnights §6§lSword";
					meta.setDisplayName(swordName);
					if (meta.hasEnchant(Enchantment.DAMAGE_ALL)) {
						if (meta.getEnchantLevel(Enchantment.DAMAGE_ALL) == 1) {
							AttributeModifier modifier = new AttributeModifier(UUID.randomUUID(), "generic.attackDamage", 7.5, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND);
							attackDamge = 10;
							meta.removeAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE);
							meta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, modifier);
						} else if (meta.getEnchantLevel(Enchantment.DAMAGE_ALL) == 2) {
							AttributeModifier modifier = new AttributeModifier(UUID.randomUUID(), "generic.attackDamage", 9.5, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND);
							attackDamge = 12;
							meta.removeAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE);
							meta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, modifier);
						} else if (meta.getEnchantLevel(Enchantment.DAMAGE_ALL) == 3) {
							AttributeModifier modifier = new AttributeModifier(UUID.randomUUID(), "generic.attackDamage", 11.5, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND);
							attackDamge = 14;
							meta.removeAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE);
							meta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, modifier);
						} else if (meta.getEnchantLevel(Enchantment.DAMAGE_ALL) == 4) {
							AttributeModifier modifier = new AttributeModifier(UUID.randomUUID(), "generic.attackDamage", 14.5, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND);
							attackDamge = 17;
							meta.removeAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE);
							meta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, modifier);
						} else if (meta.getEnchantLevel(Enchantment.DAMAGE_ALL) == 5) {
							AttributeModifier modifier = new AttributeModifier(UUID.randomUUID(), "generic.attackDamage", 17.5, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND);
							attackDamge = 20;
							meta.removeAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE);
							meta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, modifier);
						} else {
							AttributeModifier modifier = new AttributeModifier(UUID.randomUUID(), "generic.attackDamage", 5.5, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND);
							attackDamge = 8;
							meta.removeAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE);
							meta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, modifier);
						} 
					}
					ArrayList<String> lore = (ArrayList<String>) meta.getLore();
					if (lore.size() < 11) {
						if (lore.size() > 3) {
							lore.subList(3, lore.size() - 1).clear();
						}
						lore.add("");
						lore.add("");
						lore.add("");
						lore.add("");
						lore.add("");
						lore.add("");
						lore.add("");
						
						lore.set(3, "");
						lore.set(4, ChatColor.YELLOW + "+" + String.valueOf(attackDamge) + " Attack Damage (Sharpness)");
						lore.set(5, ChatColor.RED + "+40% Max Health");
						lore.set(6, ChatColor.AQUA + "+10% Speed");
						lore.set(7, ChatColor.GREEN + "+5% Knockback Resistance");
						lore.set(8, "");
						lore.set(9, "§aTotal Strikes: 0");
					}
					lore.set(4, ChatColor.YELLOW + "+" + String.valueOf(attackDamge) + " Attack Damage (Sharpness)");
					if (!meta.getItemFlags().contains(ItemFlag.HIDE_ATTRIBUTES)) {
						meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
					}
					meta.setLore(lore);
					item.setItemMeta(meta);
				}
			}
		}
	}
	
	@EventHandler
	public void onHit(EntityDamageByEntityEvent event) {
		if (event.getDamager().getType().equals(EntityType.PLAYER)) {
			Player player = (Player) event.getDamager();
			ItemStack mainHand = player.getInventory().getItemInMainHand();
			ItemMeta meta = mainHand.getItemMeta();
			if (mainHand.getType().equals(Material.DIAMOND_SWORD) && meta.hasLore() == true) {
				if (meta.getLore().contains("§eThe Legendary Sword from §6the sky~~")) {
					int attackDamge = 7;
					String swordName = "§d§lSunKnights §6§lSword";
					meta.setDisplayName(swordName);
					if (meta.hasEnchant(Enchantment.DAMAGE_ALL)) {
						if (meta.getEnchantLevel(Enchantment.DAMAGE_ALL) == 1) {
							AttributeModifier modifier = new AttributeModifier(UUID.randomUUID(), "generic.attackDamage", 7.5, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND);
							attackDamge = 10;
							meta.removeAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE);
							meta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, modifier);
						} else if (meta.getEnchantLevel(Enchantment.DAMAGE_ALL) == 2) {
							AttributeModifier modifier = new AttributeModifier(UUID.randomUUID(), "generic.attackDamage", 9.5, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND);
							attackDamge = 12;
							meta.removeAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE);
							meta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, modifier);
						} else if (meta.getEnchantLevel(Enchantment.DAMAGE_ALL) == 3) {
							AttributeModifier modifier = new AttributeModifier(UUID.randomUUID(), "generic.attackDamage", 11.5, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND);
							attackDamge = 14;
							meta.removeAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE);
							meta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, modifier);
						} else if (meta.getEnchantLevel(Enchantment.DAMAGE_ALL) == 4) {
							AttributeModifier modifier = new AttributeModifier(UUID.randomUUID(), "generic.attackDamage", 14.5, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND);
							attackDamge = 17;
							meta.removeAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE);
							meta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, modifier);
						} else if (meta.getEnchantLevel(Enchantment.DAMAGE_ALL) == 5) {
							AttributeModifier modifier = new AttributeModifier(UUID.randomUUID(), "generic.attackDamage", 17.5, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND);
							attackDamge = 20;
							meta.removeAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE);
							meta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, modifier);
						} else {
							AttributeModifier modifier = new AttributeModifier(UUID.randomUUID(), "generic.attackDamage", 5.5, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND);
							attackDamge = 8;
							meta.removeAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE);
							meta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, modifier);
						} 
					}
					ArrayList<String> lore = (ArrayList<String>) meta.getLore();
					if (lore.size() < 11) {
						if (lore.size() > 3) {
						lore.subList(3, lore.size() - 1).clear();
						}
						lore.add("");
						lore.add("");
						lore.add("");
						lore.add("");
						lore.add("");
						lore.add("");
						lore.add("");
						
						lore.set(3, "");
						lore.set(4, ChatColor.YELLOW + "+" + String.valueOf(attackDamge) + " Attack Damage (Sharpness)");
						lore.set(5, ChatColor.RED + "+40% Max Health");
						lore.set(6, ChatColor.AQUA + "+10% Speed");
						lore.set(7, ChatColor.GREEN + "+5% Knockback Resistance");
						lore.set(8, "");
						lore.set(9, "§aTotal Strikes: 0");
					}
					lore.set(4, ChatColor.YELLOW + "+" + String.valueOf(attackDamge) + " Attack Damage (Sharpness)");
					String hitCountLine = lore.get(9);
					String hitCountChar = hitCountLine.substring(hitCountLine.indexOf("Total Strikes:") + 15, hitCountLine.length());
					int hitCount = Integer.parseInt(hitCountChar);
					hitCount = hitCount + 1;
					lore.set(9, "§aTotal Strikes: " + String.valueOf(hitCount));
					meta.setLore(lore);
					if (!meta.getItemFlags().contains(ItemFlag.HIDE_ATTRIBUTES)) {
						meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
					}
					mainHand.setItemMeta(meta);
				}
			}
		}
	}

	@EventHandler
	public void onMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		Block block = player.getLocation().add(0, 1.5, 0).getBlock();
		ItemStack item = player.getInventory().getHelmet();

		if (block.getType().equals(Material.WATER)) {
			if (item.getType().equals(Material.GLASS) 
			|| item.getType().equals(Material.WHITE_STAINED_GLASS)
			|| item.getType().equals(Material.ORANGE_STAINED_GLASS)
			|| item.getType().equals(Material.MAGENTA_STAINED_GLASS)
			|| item.getType().equals(Material.LIGHT_BLUE_STAINED_GLASS)
			|| item.getType().equals(Material.YELLOW_STAINED_GLASS)
			|| item.getType().equals(Material.LIME_STAINED_GLASS)
			|| item.getType().equals(Material.PINK_STAINED_GLASS)
			|| item.getType().equals(Material.GRAY_STAINED_GLASS)
			|| item.getType().equals(Material.LIGHT_GRAY_STAINED_GLASS)
			|| item.getType().equals(Material.CYAN_STAINED_GLASS)
			|| item.getType().equals(Material.PURPLE_STAINED_GLASS)
			|| item.getType().equals(Material.BLUE_STAINED_GLASS)
			|| item.getType().equals(Material.BROWN_STAINED_GLASS)
			|| item.getType().equals(Material.GREEN_STAINED_GLASS)
			|| item.getType().equals(Material.RED_STAINED_GLASS)
			|| item.getType().equals(Material.BLACK_STAINED_GLASS)) {
				player.addPotionEffect(new PotionEffect(PotionEffectType.WATER_BREATHING, 999999999, 0));
			}
		} else {
			player.removePotionEffect(PotionEffectType.WATER_BREATHING);
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onCommand(PlayerCommandPreprocessEvent event) {
		if (event.getMessage().toLowerCase().startsWith("/openinv") || event.getMessage().toLowerCase().startsWith("/inv") || event.getMessage().toLowerCase().startsWith("/invsee")) {
			String[] raw = event.getMessage().split(" ");
			if (raw.length > 0) {
				String target = raw[1];
				if (Bukkit.getServer().getPlayer(target) == null) {
					event.setCancelled(true);
					event.getPlayer().chat("/mpdb inv " + target);
					if (Bukkit.getOfflinePlayer(target) != null) {
						if (!Main.isMPDB.containsKey(event.getPlayer())) {
							Main.isMPDB.put(event.getPlayer(), "/mpdb inv " + target);
						}
					}
				}
			}
		}
		
		if (event.getMessage().toLowerCase().startsWith("/mbdb inv")) {
			String[] raw = event.getMessage().split(" ");
			if (raw.length > 0) {
				String target = raw[1];
				if (Bukkit.getOfflinePlayer(target) != null) {
					if (!Main.isMPDB.containsKey(event.getPlayer())) {
						Main.isMPDB.put(event.getPlayer(), "/mpdb inv " + target);
					}
				}
			}
		}
		
		if (event.getMessage().toLowerCase().startsWith("/openender") || event.getMessage().toLowerCase().startsWith("/enderchest") || event.getMessage().toLowerCase().startsWith("/endersee")) {
			String[] raw = event.getMessage().split(" ");
			if (raw.length > 0) {
				String target = raw[1];
				if (Bukkit.getServer().getPlayer(target) == null) {
					event.setCancelled(true);
					event.getPlayer().chat("/mpdb end " + target);	
					if (Bukkit.getOfflinePlayer(target) != null) {
						if (!Main.isMPDB.containsKey(event.getPlayer())) {
							Main.isMPDB.put(event.getPlayer(), "/mpdb end " + target);
						}
					}
				}
			}
		}
		
		if (event.getMessage().toLowerCase().startsWith("/mbdb end")) {
			String[] raw = event.getMessage().split(" ");
			if (raw.length > 0) {
				String target = raw[1];
				if (Bukkit.getOfflinePlayer(target) != null) {
					if (!Main.isMPDB.containsKey(event.getPlayer())) {
						Main.isMPDB.put(event.getPlayer(), "/mpdb end " + target);
					}
				}
			}
		}
	}
}
