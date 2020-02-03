package com.loohp.skmcextra;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Container;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.earth2me.essentials.User;
//import com.onarandombox.MultiverseCore.api.MultiverseWorld;
//import com.onarandombox.MultiverseCore.utils.WorldManager;
import com.wimbli.WorldBorder.BorderData;
import com.wimbli.WorldBorder.Config;

import net.md_5.bungee.api.ChatColor;

//public class Commands extends CommandExecute implements Listener, CommandExecutor {
    public class Commands implements Listener, CommandExecutor, TabCompleter {
    	
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
    	if (cmd.getName().equalsIgnoreCase("cooldown")) {
    		ArrayList<String> list = new ArrayList<String>();
    		if (args.length < 2) {
    			list.add("clear");
    		} else if (Bukkit.getServer().getOnlinePlayers().contains(Bukkit.getPlayer(args[1])) && args.length < 4) {
    			list.add("spawn");
    			list.add("home");
				list.add("back");
				list.add("tpa");
				list.add("tpahere");
				list.add("all");
    		}
    		return list;
    	}
		return null;   	
    }
    
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		if (label.equalsIgnoreCase("skmcsx")) {
			if (sender.hasPermission("skmcextra.admin.reload")) {
				Main.loadConfig();
				sender.sendMessage(ChatColor.GREEN + "SKMC Survival Extra has been reloaded!");
				return true;
			} else {
				sender.sendMessage(ChatColor.RED + "You do not have permission to use that command!");
			}
		}
		
		if (sender instanceof Player) {		
			if (label.equalsIgnoreCase("phantomshield") || label.equalsIgnoreCase("psd")) {
				if (sender.hasPermission("skmcextra.admin.phantomshield")) {
					if (args.length < 1) {
						sender.sendMessage(ChatColor.RED + "Usage: /phantomshield <set/give> [player]");
					} else {
						if (args[0].equals("set")) {
							Player player = (Player) sender;
							ItemStack item = player.getInventory().getItemInMainHand();
							item.setAmount(1);							
							ItemMeta meta = item.getItemMeta();
							if (meta.hasItemFlag(ItemFlag.HIDE_ENCHANTS) == false) {
								meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
								item.setItemMeta(meta);
							}
							if (item.getItemMeta().getDisplayName() != null) {
								if (item.getItemMeta().getDisplayName().contains("Phantom") && item.getItemMeta().getDisplayName().contains("Shield")) {
									Main.skmcsx.getConfig().set("PhantomShieldsItem", item);
									Main.skmcsx.saveConfig();
									sender.sendMessage(ChatColor.GREEN + "Save the item in your mainhand to config. Remember to update the shop manuelly.");
								} else {
									sender.sendMessage(ChatColor.RED + "The item in your mainhand is most likely not a Phantom Shield.");
								}
							} else {
								sender.sendMessage(ChatColor.RED + "The item in your mainhand is most likely not a Phantom Shield.");
							}
						} else if (args[0].equals("give")) {
							Player player = (Player) sender;
							if (args.length < 2) {
								ItemStack item = Main.skmcsx.getConfig().getItemStack("PhantomShieldsItem");
								item.setAmount(1);
								player.getInventory().addItem(item);
								sender.sendMessage(ChatColor.GREEN + "You have been given a " + ChatColor.LIGHT_PURPLE + "Phantom " + ChatColor.GOLD + "Shield " + ChatColor.GREEN + "item");
							} else {
								ItemStack item = Main.skmcsx.getConfig().getItemStack("PhantomShieldsItem");
								item.setAmount(1);
								if (Bukkit.getPlayer(args[1]) != null) {
									Player givePlayer = Bukkit.getPlayer(args[1]);
									givePlayer.getInventory().addItem(item);
									sender.sendMessage(ChatColor.GREEN + "Given " + ChatColor.LIGHT_PURPLE + "Phantom " + ChatColor.GOLD + "Shield " + ChatColor.GREEN + "item to player " + ChatColor.YELLOW + givePlayer.getName());
									givePlayer.sendMessage(ChatColor.GREEN + "You have been given a " + ChatColor.LIGHT_PURPLE + "Phantom " + ChatColor.GOLD + "Shield " + ChatColor.GREEN + "item");
								} else {
									sender.sendMessage(ChatColor.RED + "The player " + ChatColor.YELLOW + args[1] + ChatColor.RED + " isn\'t a valid name");
								}
							}						
						} else {
							sender.sendMessage(ChatColor.RED + "Usage: /phantomshield <set/give> [player]");
						}
					}
				} else {
					sender.sendMessage(ChatColor.RED + "You do not have permission to use that command!");
				}
				return true;
			}
		
			if (label.equalsIgnoreCase("displaychest") || label.equalsIgnoreCase("displaycontainer")) {
				if (sender.hasPermission("skmcextra.admin.displaycontainers")) {
					if (args.length < 1) {
						sender.sendMessage(ChatColor.RED + "Usage: /displaychest <set/clear>");
					} else {
						if (args[0].equals("set") || args[0].equals("clear")) {
							if (((Player) sender).getTargetBlockExact(10, FluidCollisionMode.NEVER) != null) {								
								if (((Player) sender).getTargetBlockExact(10, FluidCollisionMode.NEVER).getState() instanceof Container) {
									Block block = ((Player) sender).getTargetBlockExact(10, FluidCollisionMode.NEVER);
									if (block.getType().equals(Material.CHEST)) {
										Chest chest = (Chest) block.getState();
										InventoryHolder holder = chest.getInventory().getHolder();
										if (holder instanceof DoubleChest) {
											DoubleChest doublechest = (DoubleChest) holder;
											BlockData blockData = chest.getBlockData();
											BlockFace facing = ((Directional) blockData).getFacing();
											if (facing.equals(BlockFace.EAST)) {
												block = doublechest.getLeftSide().getInventory().getLocation().getBlock();
											} else if (facing.equals(BlockFace.SOUTH)) {
												block = doublechest.getRightSide().getInventory().getLocation().getBlock();
											} else if (facing.equals(BlockFace.WEST)) {
												block = doublechest.getRightSide().getInventory().getLocation().getBlock();
											} else if (facing.equals(BlockFace.NORTH)) {
												block = doublechest.getLeftSide().getInventory().getLocation().getBlock();
											}
										}
									}									
									String locString = block.getLocation().getBlockX() + "," + block.getLocation().getBlockY() + "," + block.getLocation().getBlockZ() + "," + block.getLocation().getWorld().getName();
									if (args[0].equals("set")) {
										if (Main.skmcsx.getConfig().contains("DisplayContainers." + locString)) {
											sender.sendMessage(ChatColor.RED + "This container is already a display container!");
										} else {
											Main.skmcsx.getConfig().set("DisplayContainers." + locString, sender.getName());
											sender.sendMessage(ChatColor.GREEN + "Set the block you are looking at as display container!");
											block = ((Player) sender).getTargetBlockExact(10, FluidCollisionMode.NEVER);
											if (Main.lwc.getLWC().findProtection(block) == null && Main.lwc.getLWC().isProtectable(block)) {
												Bukkit.getServer().dispatchCommand(sender, "cpublic");
												float dir = (float) Math.toDegrees(Math.atan2(((Player) sender).getLocation().getBlockX() - block.getX(), block.getZ() - ((Player) sender).getLocation().getBlockZ()));
												BlockFace face = Main.getClosestFace(dir);
												Bukkit.getPluginManager().callEvent(new PlayerInteractEvent(((Player) sender), Action.RIGHT_CLICK_BLOCK, new ItemStack(Material.AIR), block, face));
											}
										}
										Main.skmcsx.saveConfig();
									}
									if (args[0].equals("clear")) {
										if (!Main.skmcsx.getConfig().contains("DisplayContainers." + locString)) {
											sender.sendMessage(ChatColor.RED + "This container is not a display container!");
										} else {
											Main.skmcsx.getConfig().set("DisplayContainers." + locString, null);
											sender.sendMessage(ChatColor.YELLOW + "Unset the block you are looking at from being a display chest!");
											if (Main.lwc.getLWC().findProtection(block) != null && Main.lwc.getLWC().isProtectable(block)) {
												Bukkit.getServer().dispatchCommand(((Player) sender), "unlock");
												float dir = (float) Math.toDegrees(Math.atan2(((Player) sender).getLocation().getBlockX() - block.getX(), block.getZ() - ((Player) sender).getLocation().getBlockZ()));
												BlockFace face = Main.getClosestFace(dir);
												Bukkit.getPluginManager().callEvent(new PlayerInteractEvent(((Player) sender).getPlayer(), Action.RIGHT_CLICK_BLOCK, new ItemStack(Material.AIR), block, face));
											}
										}
										Main.skmcsx.saveConfig();
									}
								} else {
									sender.sendMessage(ChatColor.RED + "You must be looking at a container block!");
								}
							} else {
								sender.sendMessage(ChatColor.RED + "You must be looking at a container block!");
							}
						} else {
							sender.sendMessage(ChatColor.RED + "Usage: /displaychest <set/clear>");
						}
					}
					return true;
				} else {
					sender.sendMessage(ChatColor.RED + "You do not have permission to use that command!");
				}
			}
			
			if (label.equalsIgnoreCase("cooldown") || label.equalsIgnoreCase("cd")) {
				if (sender.hasPermission("skmcextra.admin.cooldownclear")) {
					if (args.length < 3) {
						sender.sendMessage(ChatColor.RED + "Usage: /cooldown clear <player> <spawn/home/back/tpa/tpahere/all>");
					} else {
						if (Main.ess3.getUser(args[1]) == null) {
							sender.sendMessage(ChatColor.RED + "Error: Player \"" + ChatColor.YELLOW + args[1] + ChatColor.RED + "\" not found");
						} else {
							if (args[2].toLowerCase().equals("spawn") || args[2].toLowerCase().equals("home") || args[2].toLowerCase().equals("back") || args[2].toLowerCase().equals("tpa") || args[2].toLowerCase().equals("tpahere") || args[2].toLowerCase().equals("all")) {
								List<String> command = new ArrayList<String>();
								User player = Main.ess3.getUser(args[1]);
								if (args[2].toLowerCase().equals("all")) {
									command.add("spawn");
									command.add("home");
									command.add("back");
									command.add("tpa");
									command.add("tpahere");
								} else {
									command.add(args[2]);
								}
								for (String arg : command) {
									for (Entry<Pattern, Long> entry : player.getCommandCooldowns().entrySet()) {
										if (entry.getKey().toString().equals((arg.toLowerCase() + "( .*)?"))) {
											player.clearCommandCooldown(entry.getKey());
											break;
						                }
						            }
									sender.sendMessage(ChatColor.GREEN + "Reset the cooldown on command \"" + ChatColor.YELLOW + arg + ChatColor.GREEN + "\" for player " + ChatColor.YELLOW + player.getName());
								}
							} else {
								sender.sendMessage(ChatColor.RED + "Error: Command \"" + ChatColor.YELLOW + args[2] + ChatColor.RED + "\" is not a valid input");
							}
						}
					}
					return true;
				} else {
					sender.sendMessage(ChatColor.RED + "You do not have permission to use that command!");
				}
			}
			
			if (label.equalsIgnoreCase("randomtp") || label.equalsIgnoreCase("rtp")) {
				int maxSize;
                if (args.length == 0) {
                	if (sender.hasPermission("skmcextra.rtp.self")) {
                		World world = ((Player) sender).getWorld();
                		
                		Location now = ((Player) sender).getLocation();
                		//WorldManager wm = new WorldManager(Main.getMultiverseCore());
                		//MultiverseWorld mWorld = wm.getMVWorld(world);
                		Location spawn = world.getSpawnLocation();
                		
                		int distanceFromSpawn = (int) Math.sqrt((Math.pow(now.getX() - spawn.getX(), 2)) + (Math.pow(now.getZ() - spawn.getZ(), 2)));
                		
                		if (distanceFromSpawn < 201 || sender.hasPermission("skmcextra.rtp.self.anywhere")) {
                			if (sender.hasPermission("skmcextra.rtp.self.anywhere")) {
                				sender.sendMessage(ChatColor.AQUA + "You have bypassed the spawn proximity check");
                			}
                			
                			BorderData border = Config.Border(world.getName());
                    		
                    		if (border != null) {
                    			if (border.getRadiusX() > border.getRadiusZ()) {
                    				maxSize = (int) border.getRadiusZ();
                    			} else {
                    				maxSize = (int) border.getRadiusX();
                    			}
                    		} else {
                    			maxSize = 10000;
                    		}
                    		
                    		int randomLocX = 0;
                    		int randomLocY = 0;
                    		int randomLocZ = 0;
                    		
                    		boolean valid = false;
                    		while (valid == false) {
                        		int okY = 0;
                        		int i = 255;                       	
                        		
                        		randomLocX = (int) ((Math.random() * maxSize * 2) - maxSize + border.getX());
                				randomLocZ = (int) ((Math.random() * maxSize * 2) - maxSize + border.getZ());
                        		
                        		if (world.getEnvironment().equals(Environment.NETHER)) {
                        			i = 100;
                        		}
                				
                				while (okY == 0) {
                					Block block = ((Player) sender).getWorld().getBlockAt(randomLocX, i - 1, randomLocZ);
                					if (i < 50 || block.getType().equals(Material.FIRE) || block.getType().equals(Material.SEAGRASS) || block.getType().equals(Material.TALL_SEAGRASS) || block.getType().equals(Material.LADDER) || block.getType().equals(Material.COBWEB) || block.getType().equals(Material.VINE) || block.getType().equals(Material.WATER) || block.getType().equals(Material.LAVA) || block.getType().equals(Material.SNOW) || block.getType().equals(Material.CACTUS) || block.getType().equals(Material.MAGMA_BLOCK)) {
                						okY = 2;
                					} else if (block.getType().equals(Material.AIR)) {
                						okY = 0;
                						i = i - 1;
                					} else {
                						okY = 1;
                						randomLocY = i;
                					}              	
                				}
                				if (okY == 1) {
                					valid = true;
                				}                
                				if (border != null) {
                					if (border.insideBorder(Math.abs(randomLocX) + Math.round(maxSize / 70), Math.abs(randomLocZ) + Math.round(maxSize / 70)) == false) {
                						sender.getServer().getConsoleSender().sendMessage(randomLocX + " " + randomLocZ + " " + maxSize + " " + (maxSize / 70) + "");
                						valid = false;
                					}
                				}
                				if (world.getEnvironment().equals(Environment.NETHER)) {
            						Block leg = ((Player) sender).getWorld().getBlockAt(randomLocX, i, randomLocZ);
            						Block head = ((Player) sender).getWorld().getBlockAt(randomLocX, i + 1, randomLocZ);
            						if (!leg.getType().equals(Material.AIR) || !head.getType().equals(Material.AIR)) {
            							valid = false;
            						}
                        		}
                    		}
                    		float locX = (float) (randomLocX + 0.5);
                    		float locY = (float) (randomLocY);
                    		float locZ = (float) (randomLocZ + 0.5);
                    		Location loc = new Location(world, locX, locY, locZ);
                    		Location ori = ((Player) sender).getLocation();
                    		int distance = (int) Math.sqrt((Math.pow((loc.getX() - ori.getX()), 2)) + (Math.pow((loc.getY() - ori.getY()), 2)) + (Math.pow((loc.getZ() - ori.getZ()), 2)));
                    		
                    		Main.setBack(((Player) sender));
                    		//((Player) sender).teleport(loc);
                    		try {
								Main.exeTeleport((Player) sender, loc);
							} catch (Throwable e) {
								sender.sendMessage(ChatColor.RED + "Error while teleporting, please report to staff.");
							}
                    		sender.sendMessage(ChatColor.GREEN + "Teleporting to X:" + randomLocX + " Y:" + randomLocY + " Z:" + randomLocZ + " (" + distance + " blocks away)");
                		} else {
                			sender.sendMessage(ChatColor.YELLOW + "You must be within 200 blocks from spawn to use this command!");
                			sender.sendMessage(ChatColor.YELLOW + "Spawn Location of this world: " + ChatColor.GREEN + "X:" + spawn.getBlockX() + " Z:" + spawn.getBlockZ() + ChatColor.YELLOW + " (" + distanceFromSpawn + " blocks away)");
                		}                		
        				
                	} else {
                		sender.sendMessage(ChatColor.DARK_RED + "You do not have permission to run that command!");
                	}
                } else {
                	if (sender.hasPermission("skmcextra.rtp.others")) {
                		World world = ((Player) sender).getWorld();
                		
                		BorderData border = Config.Border(world.getName());
                		
                		if (border != null) {
                			if (border.getRadiusX() > border.getRadiusZ()) {
                				maxSize = (int) border.getRadiusZ();
                			} else {
                				maxSize = (int) border.getRadiusX();
                			}
                		} else {
                			maxSize = 10000;
                		}
                		
                		int randomLocX = 0;
                		int randomLocY = 0;
                		int randomLocZ = 0;
                		
                		boolean valid = false;
                		while (valid == false) {
                    		int okY = 0;
                    		int i = 255;
                    		
                    		randomLocX = (int) ((Math.random() * maxSize * 2) - maxSize + border.getX());
            				randomLocZ = (int) ((Math.random() * maxSize * 2) - maxSize + border.getZ());
                    		
                    		if (world.getEnvironment().equals(Environment.NETHER)) {
                    			i = 100;
                    		}    		
            				
            				while (okY == 0) {
            					Block block = ((Player) sender).getWorld().getBlockAt(randomLocX, i - 1, randomLocZ);
            					if (i < 50 || block.getType().equals(Material.FIRE) || block.getType().equals(Material.SEAGRASS) || block.getType().equals(Material.TALL_SEAGRASS) || block.getType().equals(Material.LADDER) || block.getType().equals(Material.COBWEB) || block.getType().equals(Material.VINE) || block.getType().equals(Material.WATER) || block.getType().equals(Material.LAVA) || block.getType().equals(Material.SNOW) || block.getType().equals(Material.CACTUS) || block.getType().equals(Material.MAGMA_BLOCK)) {
            						okY = 2;
            					} else if (block.getType().equals(Material.AIR)) {
            						okY = 0;
            						i = i - 1;
            					} else {
            						okY = 1;
            						randomLocY = i;
            					}
            				}
            				if (okY == 1) {
            					valid = true;
            				}
            				if (border != null) {
            					if (border.insideBorder(Math.abs(randomLocX) + Math.round(maxSize / 70), Math.abs(randomLocZ) + Math.round(maxSize / 70)) == false) {
            						sender.getServer().getConsoleSender().sendMessage(randomLocX + " " + randomLocZ + " " + maxSize + " " + (maxSize / 70) + "");
            						valid = false;
            					}
            				}
            				if (world.getEnvironment().equals(Environment.NETHER)) {
        						Block leg = ((Player) sender).getWorld().getBlockAt(randomLocX, i, randomLocZ);
        						Block head = ((Player) sender).getWorld().getBlockAt(randomLocX, i + 1, randomLocZ);
        						if (!leg.getType().equals(Material.AIR) || !head.getType().equals(Material.AIR)) {
        							valid = false;
        						}
                    		}
                		}
                		float locX = (float) (randomLocX + 0.5);
                		float locY = (float) (randomLocY);
                		float locZ = (float) (randomLocZ + 0.5);
                		Location loc = new Location(world, locX, locY, locZ);
                		
                		for (String arg : args) {
                			Player player = Bukkit.getServer().getPlayer(arg);
                			if (Bukkit.getOnlinePlayers().contains(player)) {
                				Location ori = player.getLocation();
                        		int distance = (int) Math.sqrt((Math.pow((loc.getX() - ori.getX()), 2)) + (Math.pow((loc.getY() - ori.getY()), 2)) + (Math.pow((loc.getZ() - ori.getZ()), 2)));
                        		Main.setBack(player);
                        		player.teleport(loc);
                        		player.sendMessage(ChatColor.GOLD + "Teleporting...");
                        		sender.sendMessage(ChatColor.GREEN + "Teleported " + ChatColor.AQUA + player.getName() + ChatColor.GREEN + " to X:" + randomLocX + " Y:" + randomLocY + " Z:" + randomLocZ + " (" + distance + " blocks away)");
                			} else {
                				sender.sendMessage(ChatColor.YELLOW + "The player " + ChatColor.RED + arg + ChatColor.YELLOW + " is not online!");
                			}
                		}
                	} else {
                		sender.sendMessage(ChatColor.DARK_RED + "You do not have permission to use that command on other players!");
                	}
                }
				return true;
			}
		} else {
			sender.sendMessage(ChatColor.DARK_RED + "Only players can use this commmand!");
			return true;
		}
		return true;
	}
}