package me.vik1395.ProtectionStones;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import com.sk89q.worldguard.protection.flags.RegionGroupFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.FileConfigurationOptions;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;




public class Main
  extends JavaPlugin
{
  public static Plugin plugin;
  public static Plugin wgd;
  public static File psStoneData;
  public static File conf;
  public static FileConfiguration config;
  
  public Main() {}
  
  public static List<String> flags = new ArrayList();
  public static List<String> toggleList = new ArrayList();
  public static List<String> allowedFlags = new ArrayList();
  public static List<String> deniedWorlds = new ArrayList();
  public static Collection<String> mats = new HashSet();
  public static boolean uuid;
  public static int x;
  public static int y;
  
  public static int z;
  public static int priority;
  public void onEnable() { viewTaskList = new HashMap();
    saveDefaultConfig();
    getConfig().options().copyDefaults(true);
    plugin = this;
    conf = new File(getDataFolder() + "/config.yml");
    psStoneData = new File(getDataFolder() + "/hiddenpstones.yml");
    if (!psStoneData.exists()) {
      try {
        psStoneData.createNewFile();
      } catch (IOException ex) {
        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
    getServer().getPluginManager().registerEvents(new ListenerClass(), this);
    if ((getServer().getPluginManager().getPlugin("WorldGuard").isEnabled()) && (getServer().getPluginManager().getPlugin("WorldGuard").isEnabled())) {
      wgd = getServer().getPluginManager().getPlugin("WorldGuard");
    } else {
      getLogger().info("WorldGuard or WorldEdit not enabled! Disabling ProtectionStones...");
      getServer().getPluginManager().disablePlugin(this);
    }
    
    for (String material : getConfig().getString("Blocks").split(",")) {
      String[] split = material.split("-");
      if ((split.length > 1) && (split.length < 3)) {
        if (Material.getMaterial(split[0]) != null) {
          mats.add(material);
        }
      } else {
        mats.add(split[0]);
      }
    }
    flags = getConfig().getStringList("Flags");
    allowedFlags = Arrays.asList(getConfig().getString("Allowed Flags").toLowerCase().split(","));
    deniedWorlds = Arrays.asList(getConfig().getString("Worlds Denied").toLowerCase().split(","));
    uuid = getConfig().getBoolean("UUID");
    
    initConfig();
    
    getLogger().info("ProtectionStones has successfully started!");
    getLogger().info("Created by Vik1395"); }
  
  public Map<CommandSender, Integer> viewTaskList; StoneTypeData StoneTypeData = new StoneTypeData();
  

  public boolean onCommand(CommandSender s, Command cmd, String label, String[] args)
  {
    if ((s instanceof Player)) {
      Player p = (Player)s;
      if (cmd.getName().equalsIgnoreCase("ps")) {
        WorldGuardPlugin wg = (WorldGuardPlugin)wgd;
        RegionManager rgm = wg.getRegionManager(p.getWorld());
        if ((args.length == 0) || (args[0].equalsIgnoreCase("help"))) {
          p.sendMessage(ChatColor.YELLOW + "/ps info members|owners|flags");
          p.sendMessage(ChatColor.YELLOW + "/ps add|remove {playername}");
          p.sendMessage(ChatColor.YELLOW + "/ps addowner|removeowner {playername}");
          p.sendMessage(ChatColor.YELLOW + "/ps count [player]");
          p.sendMessage(ChatColor.YELLOW + "/ps flag {flagname} {setting|null}");
          p.sendMessage(ChatColor.YELLOW + "/ps home {num} - " + ChatColor.GREEN + "{num} has to be within the number of protected regions you own. Use /ps count to check");
          p.sendMessage(ChatColor.YELLOW + "/ps tp {player} {num}");
          p.sendMessage(ChatColor.YELLOW + "/ps hide|unhide");
          p.sendMessage(ChatColor.YELLOW + "/ps toggle");
          p.sendMessage(ChatColor.YELLOW + "/ps view");
          p.sendMessage(ChatColor.YELLOW + "/ps reclaim");
          p.sendMessage(ChatColor.YELLOW + "/ps priority {number|null}");
          p.sendMessage(ChatColor.YELLOW + "/ps region count|list|remove|regen|disown {playername}");
          p.sendMessage(ChatColor.YELLOW + "/ps admin {version|settings|hide|unhide|");
          p.sendMessage(ChatColor.YELLOW + "           cleanup|lastlogon|lastlogons|stats}");
          return true; }
        if (args[0].equalsIgnoreCase("toggle")) {
          if (p.hasPermission("protectionstones.toggle")) {
            if (toggleList != null) {
              if (!toggleList.contains(p.getName())) {
                toggleList.add(p.getName());
                p.sendMessage(ChatColor.YELLOW + "ProtectionStone placement turned off");
              } else {
                toggleList.remove(p.getName());
                p.sendMessage(ChatColor.YELLOW + "ProtectionStone placement turned on");
              }
            } else {
              toggleList.add(p.getName());
              p.sendMessage(ChatColor.YELLOW + "ProtectionStone placement turned off");
            }
          } else {
            p.sendMessage(ChatColor.RED + "You don't have permission to use the toggle command");
          }
          return true;
        }
        
        double x = p.getLocation().getX();
        double y = p.getLocation().getY();
        double z = p.getLocation().getZ();
        Vector v = new Vector(x, y, z);
        String id = "";
        List<String> idList = rgm.getApplicableRegionsIDs(v);
        if (idList.size() == 1) {
          id = idList.toString();
          id = id.substring(1, id.length() - 1);
        } else {
          double distanceToPS = 10000.0D;
          double tempToPS = 0.0D;
          String namePSID = "";
          for (Iterator<String> iterator4 = idList.iterator(); iterator4.hasNext();) {
            String currentID = (String)iterator4.next();
            if (currentID.substring(0, 2).equals("ps")) {
              int indexX = currentID.indexOf("x");
              int indexY = currentID.indexOf("y");
              int indexZ = currentID.length() - 1;
              double psx = Double.parseDouble(currentID.substring(2, indexX));
              double psy = Double.parseDouble(currentID.substring(indexX + 1, indexY));
              double psz = Double.parseDouble(currentID.substring(indexY + 1, indexZ));
              Location psLocation = new Location(p.getWorld(), psx, psy, psz);
              tempToPS = p.getLocation().distance(psLocation);
              if (tempToPS < distanceToPS) {
                distanceToPS = tempToPS;
                namePSID = currentID;
              }
            }
          }
          id = namePSID;
        }
        LocalPlayer localPlayer = wg.wrapPlayer(p);
        final Material bm5; final Material bm7; if ((rgm.getRegion(id) != null) && (
          (rgm.getRegion(id).isOwner(localPlayer)) || (p.hasPermission("protectionstones.superowner")))) {
          if (args[0].equalsIgnoreCase("add")) {
            if (p.hasPermission("protectionstones.members")) {
              if (args.length < 2) {
                p.sendMessage(ChatColor.RED + "This command requires a player name.");
                return true;
              }
              String playerName = args[1];
              UUID uid = Bukkit.getOfflinePlayer(playerName).getUniqueId();
              DefaultDomain members = rgm.getRegion(id).getMembers();
              members.addPlayer(playerName);
              if (uuid) {
                members.addPlayer(uid);
              }
              rgm.getRegion(id).setMembers(members);
              try {
                rgm.save();
              } catch (Exception e) {
                System.out.println("[ProtectionStones] WorldGuard Error [" + e + "] during Region File Save");
              }
              p.sendMessage(ChatColor.YELLOW + playerName + " has been added to your region.");
              return true;
            }
            
            p.sendMessage(ChatColor.RED + "You don't have permission to use Members Commands");
            return true;
          }
          

          if (args[0].equalsIgnoreCase("remove")) {
            if (p.hasPermission("protectionstones.members")) {
              if (args.length < 2) {
                p.sendMessage(ChatColor.RED + "This command requires a player name.");
                return true;
              }
              String playerName = args[1];
              UUID uid = Bukkit.getOfflinePlayer(playerName).getUniqueId();
              DefaultDomain members = rgm.getRegion(id).getMembers();
              members.removePlayer(playerName);
              if (uuid) {
                members.removePlayer(uid);
              }
              rgm.getRegion(id).setMembers(members);
              try {
                rgm.save();
              } catch (Exception e) {
                System.out.println("[ProtectionStones] WorldGuard Error [" + e + "] during Region File Save");
              }
              p.sendMessage(ChatColor.YELLOW + playerName + " has been removed from region.");
            } else {
              p.sendMessage(ChatColor.RED + "You don't have permission to use Members Commands");
            }
            return true;
          }
          
          if (args[0].equalsIgnoreCase("addowner")) {
            if (p.hasPermission("protectionstones.owners")) {
              if (args.length < 2) {
                p.sendMessage(ChatColor.RED + "This command requires a player name.");
                return true;
              }
              String playerName = args[1];
              UUID uid = Bukkit.getOfflinePlayer(playerName).getUniqueId();
              DefaultDomain owners = rgm.getRegion(id).getOwners();
              owners.addPlayer(playerName);
              if (uuid) {
                owners.addPlayer(uid);
              }
              rgm.getRegion(id).setOwners(owners);
              try {
                rgm.save();
              } catch (Exception e) {
                System.out.println("[ProtectionStones] WorldGuard Error [" + e + "] during Region File Save");
              }
              p.sendMessage(ChatColor.YELLOW + playerName + " has been added to your region.");
              return true;
            }
            
            p.sendMessage(ChatColor.RED + "You don't have permission to use Owners Commands");
            return true;
          }
          

          if (args[0].equalsIgnoreCase("removeowner")) {
            if (p.hasPermission("protectionstones.owners")) {
              if (args.length < 2) {
                p.sendMessage(ChatColor.RED + "This command requires a player name.");
                return true;
              }
              String playerName = args[1];
              UUID uid = Bukkit.getOfflinePlayer(playerName).getUniqueId();
              DefaultDomain owners = rgm.getRegion(id).getOwners();
              owners.removePlayer(playerName);
              if (uuid) {
                owners.addPlayer(uid);
              }
              rgm.getRegion(id).setOwners(owners);
              try {
                rgm.save();
              } catch (Exception e) {
                System.out.println("[ProtectionStones] WorldGuard Error [" + e + "] during Region File Save");
              }
              p.sendMessage(ChatColor.YELLOW + playerName + " has been removed from region.");
            } else {
              p.sendMessage(ChatColor.RED + "You don't have permission to use Owners Commands");
            }
            return true;
          }
          
          if (args[0].equalsIgnoreCase("view")) {
            if (p.hasPermission("protectionstones.view")) {
              if (!viewTaskList.isEmpty()) {
                int playerTask = 0;
                try {
                  playerTask = ((Integer)viewTaskList.get(p)).intValue();
                } catch (Exception e) {
                  playerTask = 0;
                }
                if ((playerTask != 0) && (Bukkit.getScheduler().isQueued(playerTask))) {
                  return true;
                }
              }
              Vector minVector = rgm.getRegion(id).getMinimumPoint();
              Vector maxVector = rgm.getRegion(id).getMaximumPoint();
              final int minX = minVector.getBlockX();
              final int minY = minVector.getBlockY();
              final int minZ = minVector.getBlockZ();
              final int maxX = maxVector.getBlockX();
              final int maxY = maxVector.getBlockY();
              final int maxZ = maxVector.getBlockZ();
              double px = p.getLocation().getX();
              double py = p.getLocation().getY();
              double pz = p.getLocation().getZ();
              Vector playerVector = new Vector(px, py, pz);
              final int playerY = playerVector.getBlockY();
              final World theWorld = p.getWorld();
              
              final Material bm1 = getBlock(theWorld, minX, playerY, minZ);
              final Material bm2 = getBlock(theWorld, maxX, playerY, minZ);
              final Material bm3 = getBlock(theWorld, minX, playerY, maxZ);
              final Material bm4 = getBlock(theWorld, maxX, playerY, maxZ);
              bm5 = getBlock(theWorld, minX, maxY, minZ);
              final Material bm6 = getBlock(theWorld, maxX, maxY, minZ);
              bm7 = getBlock(theWorld, minX, maxY, maxZ);
              final Material bm8 = getBlock(theWorld, maxX, maxY, maxZ);
              final Material bm9 = getBlock(theWorld, minX, minY, minZ);
              final Material bm10 = getBlock(theWorld, maxX, minY, minZ);
              final Material bm11 = getBlock(theWorld, minX, minY, maxZ);
              final Material bm12 = getBlock(theWorld, maxX, minY, maxZ);
              
              setBlock(theWorld, minX, playerY, minZ, Material.GLASS);
              setBlock(theWorld, maxX, playerY, minZ, Material.GLASS);
              setBlock(theWorld, minX, playerY, maxZ, Material.GLASS);
              setBlock(theWorld, maxX, playerY, maxZ, Material.GLASS);
              
              setBlock(theWorld, minX, maxY, minZ, Material.GLASS);
              setBlock(theWorld, maxX, maxY, minZ, Material.GLASS);
              setBlock(theWorld, minX, maxY, maxZ, Material.GLASS);
              setBlock(theWorld, maxX, maxY, maxZ, Material.GLASS);
              
              setBlock(theWorld, minX, minY, minZ, Material.GLASS);
              setBlock(theWorld, maxX, minY, minZ, Material.GLASS);
              setBlock(theWorld, minX, minY, maxZ, Material.GLASS);
              setBlock(theWorld, maxX, minY, maxZ, Material.GLASS);
              
              int taskID = getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
                public void run() {
                  setBlock(theWorld, minX, playerY, minZ, bm1);
                  setBlock(theWorld, maxX, playerY, minZ, bm2);
                  setBlock(theWorld, minX, playerY, maxZ, bm3);
                  setBlock(theWorld, maxX, playerY, maxZ, bm4);
                  setBlock(theWorld, minX, maxY, minZ, bm5);
                  setBlock(theWorld, maxX, maxY, minZ, bm6);
                  setBlock(theWorld, minX, maxY, maxZ, bm7);
                  setBlock(theWorld, maxX, maxY, maxZ, bm8);
                  setBlock(theWorld, minX, minY, minZ, bm9);
                  setBlock(theWorld, maxX, minY, minZ, bm10);
                  setBlock(theWorld, minX, minY, maxZ, bm11);
                  setBlock(theWorld, maxX, minY, maxZ, bm12);
                }
              }, 600L);
              viewTaskList.put(p, Integer.valueOf(taskID));
            } else {
              p.sendMessage(ChatColor.RED + "You don't have permission to use that command");
            }
            return true;
          }
          
          if (args[0].equalsIgnoreCase("unhide")) {
            if (p.hasPermission("protectionstones.unhide")) {
              if (id.substring(0, 2).equals("ps")) {
                int indexX = id.indexOf("x");
                int indexY = id.indexOf("y");
                int indexZ = id.length() - 1;
                int psx = Integer.parseInt(id.substring(2, indexX));
                int psy = Integer.parseInt(id.substring(indexX + 1, indexY));
                int psz = Integer.parseInt(id.substring(indexY + 1, indexZ));
                Block blockToUnhide = p.getWorld().getBlockAt(psx, psy, psz);
                YamlConfiguration hideFile = YamlConfiguration.loadConfiguration(psStoneData);
                String entry = (int)blockToUnhide.getLocation().getX() + "x";
                entry = entry + (int)blockToUnhide.getLocation().getY() + "y";
                entry = entry + (int)blockToUnhide.getLocation().getZ() + "z";
                String setmat = hideFile.getString(entry);
                String subtype = null;
                if (blockToUnhide.getType() == Material.AIR) {
                  if (setmat.contains("-")) {
                    String[] str = setmat.split("-");
                    setmat = str[0];
                    subtype = str[1];
                  }
                  hideFile.set(entry, null);
                  try {
                    hideFile.save(psStoneData);
                  } catch (IOException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                  }
                  blockToUnhide.setType(Material.getMaterial(setmat));
                  if (subtype != null) {
                    blockToUnhide.setData((byte)Integer.parseInt(subtype));
                  }
                } else {
                  p.sendMessage(ChatColor.YELLOW + "This PStone doesn't appear hidden...");
                }
              } else {
                p.sendMessage(ChatColor.YELLOW + "Not a ProtectionStones Region");
              }
            } else {
              p.sendMessage(ChatColor.RED + "You don't have permission to use that command");
            }
            return true;
          }
          
          if (args[0].equalsIgnoreCase("hide")) {
            if (p.hasPermission("protectionstones.hide")) {
              if (id.substring(0, 2).equals("ps")) {
                int indexX = id.indexOf("x");
                int indexY = id.indexOf("y");
                int indexZ = id.length() - 1;
                int psx = Integer.parseInt(id.substring(2, indexX));
                int psy = Integer.parseInt(id.substring(indexX + 1, indexY));
                int psz = Integer.parseInt(id.substring(indexY + 1, indexZ));
                Block blockToHide = p.getWorld().getBlockAt(psx, psy, psz);
                YamlConfiguration hideFile = YamlConfiguration.loadConfiguration(psStoneData);
                String entry = (int)blockToHide.getLocation().getX() + "x";
                entry = entry + (int)blockToHide.getLocation().getY() + "y";
                entry = entry + (int)blockToHide.getLocation().getZ() + "z";
                if (blockToHide.getType() != Material.AIR) {
                  hideFile.set(entry, blockToHide.getType().toString() + "-" + blockToHide.getData());
                  try {
                    hideFile.save(psStoneData);
                  } catch (IOException ex) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                  }
                  blockToHide.setType(Material.AIR);
                } else {
                  p.sendMessage(ChatColor.YELLOW + "This PStone appears to already be hidden...");
                }
              } else {
                p.sendMessage(ChatColor.YELLOW + "Not a ProtectionStones Region");
              }
            } else {
              p.sendMessage(ChatColor.RED + "You don't have permission to use that command");
            }
            return true;
          }
          if (args[0].equalsIgnoreCase("priority")) {
            if (p.hasPermission("protectionstones.priority")) {
              if (args.length < 2) {
                int priority = rgm.getRegion(id).getPriority();
                p.sendMessage(ChatColor.YELLOW + "Priority: " + priority);
                return true;
              }
              int priority = Integer.valueOf(Integer.parseInt(args[1])).intValue();
              rgm.getRegion(id).setPriority(priority);
              try {
                rgm.save();
              } catch (Exception e) {
                System.out.println("[ProtectionStones] WorldGuard Error [" + e + "] during Region File Save");
              }
              p.sendMessage(ChatColor.YELLOW + "Priority has been set.");
            } else {
              p.sendMessage(ChatColor.RED + "You don't have permission to use Priority Commands");
            }
            return true;
          }
          
          if (args[0].equalsIgnoreCase("flag")) {
            if (p.hasPermission("protectionstones.flags")) {
              if (args.length >= 3) {
                if ((allowedFlags.contains(args[1].toLowerCase())) || (p.hasPermission("protectionstones.flag." + args[1].toLowerCase())) || (p.hasPermission("protectionstones.flag.*"))) {
                  FlagHandler fh = new FlagHandler();
                  fh.setFlag(args, rgm.getRegion(id), p);
                } else {
                  p.sendMessage(ChatColor.RED + "You don't have permission to set that flag");
                }
              } else {
                p.sendMessage(ChatColor.RED + "Use:  /ps flag {flagname} {flagvalue}");
              }
            } else {
              p.sendMessage(ChatColor.RED + "You don't have permission to use flag commands");
            }
            return true;
          }
          
          if (args[0].equalsIgnoreCase("info")) {
            if (args.length == 1) {
              if (p.hasPermission("protectionstones.info")) {
                if (id != "") {
                  ProtectedRegion region = rgm.getRegion(id);
                  if (region != null) {
                    p.sendMessage(ChatColor.GRAY + "================ PS Info ================");
                    p.sendMessage(ChatColor.BLUE + "Region:" + ChatColor.YELLOW + id + ChatColor.BLUE + ", Priority: " + ChatColor.YELLOW + rgm.getRegion(id).getPriority());
                    String myFlag = "";
                    String myFlagValue = "";
                    int n = DefaultFlag.flagsList.length;
                    for (int i = 0; i < n; i++) {
                      Flag<?> flag = DefaultFlag.flagsList[i];
                      if (region.getFlag(flag) != null) {
                        myFlagValue = region.getFlag(flag).toString();
                        RegionGroupFlag groupFlag = flag.getRegionGroupFlag();
                        RegionGroup group = null;
                        if (groupFlag != null) {
                          group = (RegionGroup)region.getFlag(groupFlag);
                        }
                        if (group != null) {
                          myFlag = myFlag + flag.getName() + " -g " + region.getFlag(groupFlag) + " " + myFlagValue + ", ";
                        } else {
                          myFlag = myFlag + flag.getName() + ": " + myFlagValue + ", ";
                        }
                      }
                    }
                    
                    if (myFlag.length() > 2) {
                      myFlag = myFlag.substring(0, myFlag.length() - 2) + ".";
                      p.sendMessage(ChatColor.BLUE + "Flags: " + ChatColor.YELLOW + myFlag);
                    } else {
                      p.sendMessage(ChatColor.BLUE + "Flags: " + ChatColor.RED + "(none)");
                    }
                    DefaultDomain owners = region.getOwners();
                    String ownerNames = owners.getPlayers().toString();
                    if (ownerNames != "[]") {
                      ownerNames = ownerNames.substring(1, ownerNames.length() - 1);
                      p.sendMessage(ChatColor.BLUE + "Owners: " + ChatColor.YELLOW + ownerNames);
                    } else {
                      p.sendMessage(ChatColor.BLUE + "Owners: " + ChatColor.RED + "(no owners)");
                    }
                    DefaultDomain members = region.getMembers();
                    String memberNames = members.getPlayers().toString();
                    if (memberNames != "[]") {
                      memberNames = memberNames.substring(1, memberNames.length() - 1);
                      p.sendMessage(ChatColor.BLUE + "Members: " + ChatColor.YELLOW + memberNames);
                    } else {
                      p.sendMessage(ChatColor.BLUE + "Members: " + ChatColor.RED + "(no members)");
                    }
                    BlockVector min = region.getMinimumPoint();
                    BlockVector max = region.getMaximumPoint();
                    p.sendMessage(ChatColor.BLUE + "Bounds: " + ChatColor.YELLOW + "(" + min.getBlockX() + "," + min.getBlockY() + "," + min.getBlockZ() + ") -> (" + max.getBlockX() + "," + max.getBlockY() + "," + max.getBlockZ() + ")");
                    return true;
                  }
                  p.sendMessage(ChatColor.YELLOW + "Region does not exist");
                } else {
                  p.sendMessage(ChatColor.YELLOW + "No region found");
                }
              } else {
                p.sendMessage(ChatColor.RED + "You don't have permission to use the region info command");
              }
            } else if (args.length == 2) {
              if (args[1].equalsIgnoreCase("members")) {
                if (p.hasPermission("protectionstones.members")) {
                  DefaultDomain members = rgm.getRegion(id).getMembers();
                  String memberNames = members.getPlayers().toString();
                  if (memberNames != "[]") {
                    memberNames = memberNames.substring(1, memberNames.length() - 1);
                    p.sendMessage(ChatColor.BLUE + "Members: " + ChatColor.YELLOW + memberNames);
                  } else {
                    p.sendMessage(ChatColor.BLUE + "Members: " + ChatColor.RED + "(no members)");
                  }
                } else {
                  p.sendMessage(ChatColor.RED + "You don't have permission to use Members Commands");
                }
              } else if (args[1].equalsIgnoreCase("owners")) {
                if (p.hasPermission("protectionstones.owners")) {
                  DefaultDomain owners = rgm.getRegion(id).getOwners();
                  String ownerNames = owners.getPlayers().toString();
                  if (ownerNames != "[]") {
                    ownerNames = ownerNames.substring(1, ownerNames.length() - 1);
                    p.sendMessage(ChatColor.BLUE + "Owners: " + ChatColor.YELLOW + ownerNames);
                  } else {
                    p.sendMessage(ChatColor.BLUE + "Owners: " + ChatColor.RED + "(no owners)");
                  }
                } else {
                  p.sendMessage(ChatColor.RED + "You don't have permission to use Owners Commands");
                }
              } else if (args[1].equalsIgnoreCase("flags")) {
                if (p.hasPermission("protectionstones.flags")) {
                  String myFlag = "";
                  String myFlagValue = "";
                  int n = DefaultFlag.flagsList.length;
                  for (int i = 0; i < n; i++) {
                    Flag<?> flag = DefaultFlag.flagsList[i];
                    if (rgm.getRegion(id).getFlag(flag) != null) {
                      myFlagValue = rgm.getRegion(id).getFlag(flag).toString();
                      myFlag = myFlag + flag.getName() + ": " + myFlagValue + ", ";
                    }
                  }
                  if (myFlag.length() > 2) {
                    myFlag = myFlag.substring(0, myFlag.length() - 2) + ".";
                    p.sendMessage(ChatColor.BLUE + "Flags: " + ChatColor.YELLOW + myFlag);
                  } else {
                    p.sendMessage(ChatColor.BLUE + "Flags: " + ChatColor.RED + "(none)");
                  }
                } else {
                  p.sendMessage(ChatColor.RED + "You don't have permission to use Flags Commands");
                }
              } else {
                p.sendMessage(ChatColor.RED + "Use:  /ps info members|owners|flags");
              }
            } else {
              p.sendMessage(ChatColor.RED + "Use:  /ps info members|owners|flags");
            }
            return true;
          }
        }
        

        if (args[0].equalsIgnoreCase("count")) {
          int count = 0;
          String playerName = null;
          UUID playerid = null;
          
          if (args.length == 1) {
            if (!p.hasPermission("protectionstones.count")) {
              p.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
            }
            playerName = wg.wrapPlayer(p).getName();
            if (uuid) {
              playerid = wg.wrapPlayer(p).getUniqueId();
            }
            try {
              Map<String, ProtectedRegion> regions = rgm.getRegions();
              for (Iterator<String> region = regions.keySet().iterator(); region.hasNext();) {
                String selected = (String)region.next();
                if (uuid) {
                  if ((((ProtectedRegion)regions.get(selected)).getOwners().contains(playerid)) && 
                    (((ProtectedRegion)regions.get(selected)).getId().startsWith("ps"))) {
                    count++;
                  }
                  
                }
                else if ((((ProtectedRegion)regions.get(selected)).getOwners().contains(playerName)) && 
                  (((ProtectedRegion)regions.get(selected)).getId().startsWith("ps"))) {
                  count++;
                }
              }
            }
            catch (Exception localException1) {}
            
            p.sendMessage(ChatColor.YELLOW + "Your region count: " + count);
            return true; }
          if (args.length == 2) {
            if (!p.hasPermission("protectionstones.count.others")) {
              p.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
            }
            
            playerName = wg.wrapOfflinePlayer(Bukkit.getOfflinePlayer(args[1])).getName();
            if (uuid)
              playerid = wg.wrapOfflinePlayer(Bukkit.getOfflinePlayer(args[1])).getUniqueId();
            try {
              Map<String, ProtectedRegion> regions = rgm.getRegions();
              for (Iterator<String> region = regions.keySet().iterator(); region.hasNext();) {
                String selected = (String)region.next();
                if (uuid) {
                  if ((((ProtectedRegion)regions.get(selected)).getOwners().contains(playerid)) && 
                    (((ProtectedRegion)regions.get(selected)).getId().startsWith("ps"))) {
                    count++;
                  }
                  
                }
                else if ((((ProtectedRegion)regions.get(selected)).getOwners().contains(playerName)) && 
                  (((ProtectedRegion)regions.get(selected)).getId().startsWith("ps"))) {
                  count++;
                }
              }
            }
            catch (Exception localException2) {}
            
            p.sendMessage(ChatColor.YELLOW + args[1] + "'s region count: " + count);
            return true;
          }
          p.sendMessage(ChatColor.RED + "Usage: /ps count, /ps count [player]");
          return true;
        }
        


        if (args[0].equalsIgnoreCase("region")) {
          if (args.length >= 3) {
            if (p.hasPermission("protectionstones.region")) {
              OfflinePlayer p2 = Bukkit.getOfflinePlayer(args[2]);
              if (args[1].equalsIgnoreCase("count")) {
                String playerName = null;
                UUID playerId = null;
                int count = 0;
                try {
                  if (uuid) {
                    playerId = wg.wrapPlayer(p2.getPlayer()).getUniqueId();
                  } else {
                    playerName = wg.wrapPlayer(p2.getPlayer()).getName();
                  }
                  Map<String, ProtectedRegion> regions = rgm.getRegions();
                  for (Iterator<String> region = regions.keySet().iterator(); region.hasNext();) {
                    String selected = (String)region.next();
                    if (uuid) {
                      if ((((ProtectedRegion)regions.get(selected)).getOwners().contains(playerId)) && 
                        (((ProtectedRegion)regions.get(selected)).getId().startsWith("ps"))) {
                        count++;
                      }
                      
                    }
                    else if ((((ProtectedRegion)regions.get(selected)).getOwners().contains(playerName)) && 
                      (((ProtectedRegion)regions.get(selected)).getId().startsWith("ps"))) {
                      count++;
                    }
                  }
                }
                catch (Exception localException3) {}
                

                p.sendMessage(ChatColor.YELLOW + args[2] + "'s region count: " + count);
                return true;
              }
              Iterator<String> playerCount;
              if (args[1].equalsIgnoreCase("list"))
              {
                Map<String, ProtectedRegion> regions = rgm.getRegions();
                String name = args[2].toLowerCase();
                UUID playerid = null;
                if (uuid) {
                  playerid = Bukkit.getOfflinePlayer(args[2]).getUniqueId();
                }
                int size = regions.size();
                String[] regionIDList = new String[size];
                String regionMessage = "";
                int index = 0;
                for (playerCount = regions.keySet().iterator(); playerCount.hasNext();) {
                  String idname = (String)playerCount.next();
                  try {
                    if (idname.startsWith("ps")) {
                      if (uuid) {
                        if (((ProtectedRegion)regions.get(idname)).getOwners().contains(playerid)) {
                          regionIDList[index] = idname;
                          regionMessage = regionMessage + regionIDList[index] + ", ";
                          index++;
                        }
                      }
                      else if (((ProtectedRegion)regions.get(idname)).getOwners().contains(name)) {
                        regionIDList[index] = idname;
                        regionMessage = regionMessage + regionIDList[index] + ", ";
                        index++;
                      }
                    }
                  }
                  catch (Exception localException4) {}
                }
                if (index == 0) {
                  p.sendMessage(ChatColor.YELLOW + "No regions found for " + name);
                } else {
                  regionMessage = regionMessage.substring(0, regionMessage.length() - 2) + ".";
                  p.sendMessage(ChatColor.YELLOW + args[2] + "'s regions: " + regionMessage);
                }
                return true;
              }
              if ((args[1].equalsIgnoreCase("remove")) || (args[1].equalsIgnoreCase("regen")) || (args[1].equalsIgnoreCase("disown"))) {
                RegionManager mgr = wg.getRegionManager(p.getWorld());
                Map<String, ProtectedRegion> regions = mgr.getRegions();
                String name = args[2].toLowerCase();
                UUID playerid = null;
                if (uuid) {
                  playerid = Bukkit.getOfflinePlayer(name).getUniqueId();
                }
                int size = regions.size();
                String[] regionIDList = new String[size];
                int index = 0;
                for (String idname : regions.keySet()) {
                  try {
                    if (idname.startsWith("ps")) {
                      if (uuid) {
                        if (((ProtectedRegion)regions.get(idname)).getOwners().contains(playerid)) {
                          regionIDList[index] = idname;
                          index++;
                        }
                      }
                      else if (((ProtectedRegion)regions.get(idname)).getOwners().getPlayers().contains(name)) {
                        regionIDList[index] = idname;
                        index++;
                      }
                    }
                  }
                  catch (Exception localException5) {}
                }
                if (index == 0) {
                  p.sendMessage(ChatColor.YELLOW + "No regions found for " + args[2]);
                } else {
                  for (int i = 0; i < index; i++) {
                    if (args[1].equalsIgnoreCase("disown")) {
                      DefaultDomain owners = rgm.getRegion(regionIDList[i]).getOwners();
                      owners.removePlayer(name);
                      if (uuid) {
                        owners.removePlayer(playerid);
                      }
                      rgm.getRegion(regionIDList[i]).setOwners(owners);
                    } else {
                      if (args[1].equalsIgnoreCase("regen")) {
                        if (getServer().getPluginManager().getPlugin("WorldEdit") != null) {
                          Bukkit.dispatchCommand(p, "region select " + regionIDList[i]);
                          Bukkit.dispatchCommand(p, "/regen");
                        }
                      } else if (regionIDList[i].substring(0, 2).equals("ps")) {
                        int indexX = regionIDList[i].indexOf("x");
                        int indexY = regionIDList[i].indexOf("y");
                        int indexZ = regionIDList[i].length() - 1;
                        int psx = Integer.parseInt(regionIDList[i].substring(2, indexX));
                        int psy = Integer.parseInt(regionIDList[i].substring(indexX + 1, indexY));
                        int psz = Integer.parseInt(regionIDList[i].substring(indexY + 1, indexZ));
                        Block blockToRemove = p.getWorld().getBlockAt(psx, psy, psz);
                        blockToRemove.setType(Material.AIR);
                      }
                      mgr.removeRegion(regionIDList[i]);
                    }
                  }
                  p.sendMessage(ChatColor.YELLOW + name + "'s regions have been removed");
                  try {
                    rgm.save();
                  } catch (Exception e) {
                    System.out.println("[ProtectionStones] WorldGuard Error [" + e + "] during Region File Save");
                  }
                }
                return true;
              }
            } else {
              p.sendMessage(ChatColor.RED + "You don't have permission to use Region Commands");
            }
          } else {
            p.sendMessage(ChatColor.YELLOW + "/ps region {count|list|remove|regen|disown} {playername}");
            return true;
          }
        }
        String region;
        if (((args[0].equalsIgnoreCase("tp")) && (p.hasPermission("protectionstones.tp"))) || ((args[0].equalsIgnoreCase("home")) && (p.hasPermission("protectionstones.home")))) {
          String name = p.getName().toLowerCase();
          UUID playerid = null;int rgnum = 0;
          int index = 0;
          
          Map<Integer, String> playerRegions = new HashMap();
          if (args[0].equalsIgnoreCase("tp")) {
            if (args.length != 3) {
              p.sendMessage(ChatColor.RED + "Usage: /ps tp [player] [num]");
              return true;
            }
            rgnum = Integer.parseInt(args[2]);
          } else {
            if (args.length != 2) {
              p.sendMessage(ChatColor.RED + "Usage: /ps home [num]");
              p.sendMessage(ChatColor.YELLOW + "To see your ps count, type /ps count. Use any number within the range to teleport to that ps");
              return true;
            }
            rgnum = Integer.parseInt(args[1]);
          }
          if (uuid) {
            playerid = p.getUniqueId();
          }
          try {
            Map<String, ProtectedRegion> regions = rgm.getRegions();
            for (Iterator<String> region = regions.keySet().iterator(); region.hasNext();) {
              String selected = (String)region.next();
              if (selected.startsWith("ps")) {
                if (uuid) {
                  if (((ProtectedRegion)regions.get(selected)).getOwners().contains(playerid)) {
                    index++;
                    playerRegions.put(Integer.valueOf(index), selected);
                  }
                }
                else if (((ProtectedRegion)regions.get(selected)).getOwners().contains(name)) {
                  index++;
                  playerRegions.put(Integer.valueOf(index), selected);
                }
              }
            }
          } catch (Exception localException6) {}
          LocalPlayer lp;
          if (args[0].equalsIgnoreCase("tp"))
          {
            try {
              lp = wg.wrapOfflinePlayer(Bukkit.getOfflinePlayer(args[1]));
            } catch (Exception e) { LocalPlayer lp;
              p.sendMessage(ChatColor.RED + "Error while searching for " + args[1] + "'s regions. Please make sure you have entered the correct name.");
              return true;
            }
            if (rgnum <= 0) {
              p.sendMessage(ChatColor.RED + "Please enter a number above 0.");
              return true;
            }
            if (index <= 0) {
              p.sendMessage(ChatColor.RED + lp.getName() + " doesn't own any protected regions!");
              return true;
            }
            if (rgnum > index) {
              p.sendMessage(ChatColor.RED + lp.getName() + " only has " + index + " protected regions!");
              return true;
            }
          } else if (args[0].equalsIgnoreCase("home")) {
            try {
              lp = wg.wrapPlayer(p);
            } catch (Exception e) {
              p.sendMessage(ChatColor.RED + "Error while searching for your regions.");
              return true;
            }
            if (rgnum <= 0) {
              p.sendMessage(ChatColor.RED + "Please enter a number above 0.");
              return true;
            }
            if (index <= 0) {
              p.sendMessage(ChatColor.RED + "You don't own any protected regions!");
            }
            if (rgnum > index) {
              p.sendMessage(ChatColor.RED + "You only have " + index + " total regions!");
              return true;
            }
          } else {
            p.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
          }
          if (rgnum <= index) {
            region = rgm.getRegion((String)playerRegions.get(Integer.valueOf(rgnum))).getId();
            System.out.print(region);
            String[] pos = region.split("x|y|z");
            System.out.print(pos.toString());
            if (pos.length == 3) {
              pos[0] = pos[0].substring(2);
              p.sendMessage(ChatColor.GREEN + "Teleporting...");
              int tpx = Integer.parseInt(pos[0]);
              int tpy = Integer.parseInt(pos[1]);
              int tpz = Integer.parseInt(pos[2]);
              Location tploc = new Location(p.getWorld(), tpx, tpy, tpz);
              p.teleport(tploc);
            } else {
              p.sendMessage(ChatColor.RED + "Error in teleporting to protected region!");
            }
            return true;
          }
          p.sendMessage(ChatColor.RED + "Error in finding the region to teleport to!");

        }
        else if (args[0].equalsIgnoreCase("admin")) {
          if (!p.hasPermission("protectionstones.admin")) {
            p.sendMessage(ChatColor.RED + "You do not have permission to use that command.");
          }
          else if (args.length < 2) {
            p.sendMessage(ChatColor.RED + "Correct usage: /ps admin {version|settings|hide|unhide|");
            p.sendMessage(ChatColor.RED + "                          cleanup|lastlogon|lastlogons|stats}");
          } else if (args.length > 1)
          {
            if (args[1].equalsIgnoreCase("version")) {
              p.sendMessage(ChatColor.YELLOW + "ProtectionStones " + getDescription().getVersion());
              p.sendMessage(ChatColor.YELLOW + "CraftBukkit  " + Bukkit.getVersion());
            } else if (args[1].equalsIgnoreCase("settings")) {
              p.sendMessage(getConfig().saveToString().split("\n")); }
            int psz;
            if ((args[1].equalsIgnoreCase("hide")) || (args[1].equalsIgnoreCase("unhide"))) {
              RegionManager mgr = wg.getRegionManager(p.getWorld());
              Map<String, ProtectedRegion> regions = mgr.getRegions();
              if (regions.isEmpty()) {
                p.sendMessage(ChatColor.YELLOW + "No ProtectionStones Regions Found");
              }
              int regionSize = regions.size();
              String[] regionIDList = new String[regionSize];
              String blockMaterial = "AIR";
              String hMessage = "hidden";
              int index = 0;
              for (String idname : regions.keySet()) {
                try {
                  if (idname.substring(0, 2).equals("ps")) {
                    regionIDList[index] = idname;
                    index++;
                  }
                } catch (Exception localException7) {}
              }
              if (index == 0) {
                p.sendMessage(ChatColor.YELLOW + "No ProtectionStones Regions Found");
              } else {
                for (int i = 0; i < index; i++) {
                  int indexX = regionIDList[i].indexOf("x");
                  int indexY = regionIDList[i].indexOf("y");
                  int indexZ = regionIDList[i].length() - 1;
                  int psx = Integer.parseInt(regionIDList[i].substring(2, indexX));
                  int psy = Integer.parseInt(regionIDList[i].substring(indexX + 1, indexY));
                  psz = Integer.parseInt(regionIDList[i].substring(indexY + 1, indexZ));
                  Block blockToChange = p.getWorld().getBlockAt(psx, psy, psz);
                  blockMaterial = "AIR";
                  String entry = (int)blockToChange.getLocation().getX() + "x";
                  entry = entry + (int)blockToChange.getLocation().getY() + "y";
                  entry = entry + (int)blockToChange.getLocation().getZ() + "z";
                  String subtype = null;
                  if (args[1].equalsIgnoreCase("unhide")) {
                    if (blockToChange.getType() == Material.getMaterial(blockMaterial)) {
                      YamlConfiguration hideFile = YamlConfiguration.loadConfiguration(psStoneData);
                      blockMaterial = hideFile.getString(entry);
                      if (blockMaterial.contains("-")) {
                        String[] str = blockMaterial.split("-");
                        blockMaterial = str[0];
                        subtype = str[1];
                      }
                      hideFile.set(entry, null);
                      try {
                        hideFile.save(psStoneData);
                      } catch (IOException ex) {
                        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                      }
                    }
                  } else if (args[1].equalsIgnoreCase("hide")) {
                    if (blockToChange.getType() != Material.getMaterial(blockMaterial)) {
                      YamlConfiguration hideFile = YamlConfiguration.loadConfiguration(psStoneData);
                      hideFile.set(entry, blockToChange.getType().toString());
                      try {
                        hideFile.save(psStoneData);
                      } catch (IOException ex) {
                        Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                      }
                    }
                    else if ((subtype == null) || (blockToChange.getData() == (byte)Integer.parseInt(subtype))) {}
                  }
                  
                  blockToChange.setType(Material.getMaterial(blockMaterial));
                  if (subtype != null) {
                    blockToChange.setData((byte)Integer.parseInt(subtype));
                  }
                }
              }
              
              if (args[1].equalsIgnoreCase("unhide")) {
                hMessage = "unhidden";
              }
              p.sendMessage(ChatColor.YELLOW + "All ProtectionStones have been " + hMessage);
            } else if (args[1].equalsIgnoreCase("cleanup")) {
              if (args.length >= 3) {
                if ((args[2].equalsIgnoreCase("remove")) || (args[2].equalsIgnoreCase("regen")) || (args[2].equalsIgnoreCase("disown"))) {
                  int days = 30;
                  if (args.length > 3) {
                    days = Integer.parseInt(args[3]);
                  }
                  p.sendMessage(ChatColor.YELLOW + "Cleanup " + args[2] + " " + days + " days");
                  p.sendMessage(ChatColor.YELLOW + "================");
                  RegionManager mgr = wg.getRegionManager(p.getWorld());
                  Map<String, ProtectedRegion> regions = mgr.getRegions();
                  int size = regions.size();
                  String name = "";
                  int index = 0;
                  String[] regionIDList = new String[size];
                  OfflinePlayer[] offlinePlayerList = getServer().getOfflinePlayers();
                  int playerCount = offlinePlayerList.length;
                  for (int iii = 0; iii < playerCount; iii++) {
                    long lastPlayed = (System.currentTimeMillis() - offlinePlayerList[iii].getLastPlayed()) / 86400000L;
                    if (lastPlayed >= days) {
                      index = 0;
                      name = offlinePlayerList[iii].getName().toLowerCase();
                      for (String idname : regions.keySet()) {
                        try {
                          if (((ProtectedRegion)regions.get(idname)).getOwners().getPlayers().contains(name)) {
                            regionIDList[index] = idname;
                            index++;
                          }
                        } catch (Exception localException8) {}
                      }
                      if (index == 0) {
                        p.sendMessage(ChatColor.YELLOW + "No regions found for " + name);
                      } else {
                        p.sendMessage(ChatColor.YELLOW + args[2] + ": " + name);
                        for (int i = 0; i < index; i++) {
                          if (args[2].equalsIgnoreCase("disown")) {
                            DefaultDomain owners = rgm.getRegion(regionIDList[i]).getOwners();
                            owners.removePlayer(name);
                            rgm.getRegion(regionIDList[i]).setOwners(owners);
                          } else {
                            if (args[2].equalsIgnoreCase("regen")) {
                              if (getServer().getPluginManager().getPlugin("WorldEdit") != null) {
                                Bukkit.dispatchCommand(p, "region select " + regionIDList[i]);
                                Bukkit.dispatchCommand(p, "/regen");
                              }
                            } else if (regionIDList[i].substring(0, 2).equals("ps")) {
                              int indexX = regionIDList[i].indexOf("x");
                              int indexY = regionIDList[i].indexOf("y");
                              int indexZ = regionIDList[i].length() - 1;
                              int psx = Integer.parseInt(regionIDList[i].substring(2, indexX));
                              int psy = Integer.parseInt(regionIDList[i].substring(indexX + 1, indexY));
                              int psz = Integer.parseInt(regionIDList[i].substring(indexY + 1, indexZ));
                              Block blockToRemove = p.getWorld().getBlockAt(psx, psy, psz);
                              blockToRemove.setType(Material.AIR);
                            }
                            mgr.removeRegion(regionIDList[i]);
                          }
                        }
                      }
                    }
                  }
                  try {
                    rgm.save();
                  } catch (Exception e) {
                    System.out.println("[ProtectionStones] WorldGuard Error [" + e + "] during Region File Save");
                  }
                  p.sendMessage(ChatColor.YELLOW + "================");
                  p.sendMessage(ChatColor.YELLOW + "Completed " + args[2] + " cleanup");
                  return true;
                }
              }
              else {
                p.sendMessage(ChatColor.YELLOW + "/ps admin cleanup {remove|regen|disown} {days}");
                return true;
              }
            } else if (args[1].equalsIgnoreCase("lastlogon")) {
              System.out.print("ProtectionStones LastLogon debug #0");
              if (args.length > 2) {
                String playerName = args[2];
                System.out.print("ProtectionStones LastLogon debug #1");
                if (Bukkit.getOfflinePlayer(playerName).getFirstPlayed() > 0L) {
                  System.out.print("ProtectionStones LastLogon debug #2");
                  long lastPlayed = (System.currentTimeMillis() - Bukkit.getOfflinePlayer(playerName).getLastPlayed()) / 86400000L;
                  p.sendMessage(ChatColor.YELLOW + playerName + " last played " + lastPlayed + " days ago.");
                  if (Bukkit.getOfflinePlayer(playerName).isBanned()) {
                    System.out.print("ProtectionStones LastLogon debug #3");
                    p.sendMessage(ChatColor.YELLOW + playerName + " is banned.");
                  }
                } else {
                  p.sendMessage(ChatColor.YELLOW + "Player name not found.");
                }
              } else {
                p.sendMessage(ChatColor.YELLOW + "A player name is required.");
              }
            } else if (args[1].equalsIgnoreCase("lastlogons")) {
              int days = 0;
              if (args.length > 2) {
                days = Integer.parseInt(args[2]);
              }
              OfflinePlayer[] offlinePlayerList = getServer().getOfflinePlayers();
              int playerCount = offlinePlayerList.length;
              int playerCounter = 0;
              p.sendMessage(ChatColor.YELLOW + days + " Days Plus:");
              p.sendMessage(ChatColor.YELLOW + "================");
              Arrays.sort(offlinePlayerList, new PlayerComparator());
              for (int iii = 0; iii < playerCount; iii++) {
                long lastPlayed = (System.currentTimeMillis() - offlinePlayerList[iii].getLastPlayed()) / 86400000L;
                if (lastPlayed >= days) {
                  playerCounter++;
                  p.sendMessage(ChatColor.YELLOW + offlinePlayerList[iii].getName() + " " + lastPlayed + " days");
                }
              }
              p.sendMessage(ChatColor.YELLOW + "================");
              p.sendMessage(ChatColor.YELLOW + playerCounter + " Total Players Shown");
              p.sendMessage(ChatColor.YELLOW + playerCount + " Total Players Checked");
            } else if (args[1].equalsIgnoreCase("stats")) {
              if (args.length > 2) {
                String playerName = args[2];
                if (Bukkit.getOfflinePlayer(playerName).getFirstPlayed() > 0L) {
                  p.sendMessage(ChatColor.YELLOW + playerName + ":");
                  p.sendMessage(ChatColor.YELLOW + "================");
                  long firstPlayed = (System.currentTimeMillis() - Bukkit.getOfflinePlayer(playerName).getFirstPlayed()) / 86400000L;
                  p.sendMessage(ChatColor.YELLOW + "First played " + firstPlayed + " days ago.");
                  long lastPlayed = (System.currentTimeMillis() - Bukkit.getOfflinePlayer(playerName).getLastPlayed()) / 86400000L;
                  p.sendMessage(ChatColor.YELLOW + "Last played " + lastPlayed + " days ago.");
                  String banMessage = "Not Banned";
                  if (Bukkit.getOfflinePlayer(playerName).isBanned())
                  {
                    banMessage = "Banned";
                  }
                  p.sendMessage(ChatColor.YELLOW + banMessage);
                  int count = 0;
                  try {
                    LocalPlayer thePlayer = null;
                    thePlayer = wg.wrapOfflinePlayer(Bukkit.getOfflinePlayer(args[2]));
                    count = rgm.getRegionCountOfPlayer(thePlayer);
                  } catch (Exception localException9) {}
                  p.sendMessage(ChatColor.YELLOW + "Regions: " + count);
                  p.sendMessage(ChatColor.YELLOW + "================");
                } else {
                  p.sendMessage(ChatColor.YELLOW + "Player name not found.");
                }
                return true;
              }
              p.sendMessage(ChatColor.YELLOW + "World:");
              p.sendMessage(ChatColor.YELLOW + "================");
              int count = 0;
              try {
                count = rgm.size();
              } catch (Exception localException10) {}
              p.sendMessage(ChatColor.YELLOW + "Regions: " + count);
              p.sendMessage(ChatColor.YELLOW + "================");
            }
          }
        } else {
          if (args[0].equalsIgnoreCase("reclaim")) {
            if (p.hasPermission("protectionstones.reclaim")) {
              ProtectedRegion region = rgm.getRegion(id);
              if (region != null) {
                if (id.substring(0, 2).equals("ps")) {
                  int indexX = id.indexOf("x");
                  int indexY = id.indexOf("y");
                  int indexZ = id.length() - 1;
                  int psx = Integer.parseInt(id.substring(2, indexX));
                  int psy = Integer.parseInt(id.substring(indexX + 1, indexY));
                  int psz = Integer.parseInt(id.substring(indexY + 1, indexZ));
                  Block blockToUnhide = p.getWorld().getBlockAt(psx, psy, psz);
                  String entry = null;
                  String setmat = null;
                  if (blockToUnhide.getType() == Material.AIR) {
                    YamlConfiguration hideFile = YamlConfiguration.loadConfiguration(psStoneData);
                    entry = (int)blockToUnhide.getLocation().getX() + "x";
                    entry = entry + (int)blockToUnhide.getLocation().getY() + "y";
                    entry = entry + (int)blockToUnhide.getLocation().getZ() + "z";
                    setmat = hideFile.getString(entry);
                    hideFile.set(entry, null);
                    try {
                      hideFile.save(psStoneData);
                    } catch (IOException ex) {
                      Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                    }
                  }
                  int type = 0;
                  String blocktypedata = blockToUnhide.getType().toString() + "-" + blockToUnhide.getData();
                  if (mats.contains(blocktypedata)) {
                    type = 1;
                  } else if (mats.contains(blockToUnhide.getType().toString())) {
                    type = 2;
                  }
                  if (setmat != null) blockToUnhide.setType(Material.getMaterial(setmat));
                  BlockVector max = region.getMaximumPoint();
                  BlockVector min = region.getMinimumPoint();
                  Vector middle = max.add(min).multiply(0.5D);
                  Collection<Block> blocks = new HashSet();
                  if (type == 2) blocktypedata = blockToUnhide.getType().toString();
                  Block block; if (StoneTypeData.RegionY(blocktypedata) == 0) {
                    double xx = middle.getX();
                    double zz = middle.getZ();
                    for (double yy = 0.0D; yy <= p.getWorld().getMaxHeight(); yy += 1.0D) {
                      block = new Location(p.getWorld(), xx, yy, zz).getBlock();
                      if (mats.contains(block.getType().toString() + "-" + block.getData())) {
                        blocks.add(new Location(p.getWorld(), xx, yy, zz).getBlock());
                      } else if (mats.contains(block.getType())) {
                        blocks.add(new Location(p.getWorld(), xx, yy, zz).getBlock());
                      }
                    }
                  }
                  
                  localPlayer = wg.wrapPlayer(p);
                  if ((region.isOwner(localPlayer)) || (p.hasPermission("protectionstones.superowner"))) {
                    Block middleblock = null;
                    Block it = null;
                    if (!blocks.isEmpty()) it = (Block)blocks.iterator().next();
                    if ((it != null) && (StoneTypeData.RegionY(it.getType().toString() + "-" + it.getData()) == 0)) {
                      middleblock = it;
                    } else if ((it != null) && (StoneTypeData.RegionY(it.getType().toString()) == 0)) {
                      middleblock = it;
                    } else {
                      middleblock = p.getWorld().getBlockAt((int)middle.getX(), (int)middle.getY(), (int)middle.getZ());
                    }
                    
                    if ((!StoneTypeData.NoDrop(middleblock.getType().toString() + "-" + middleblock.getData())) && (!StoneTypeData.NoDrop(middleblock.getType().toString()))) {
                      ItemStack oreblock = new ItemStack(middleblock.getType(), 1, middleblock.getData());
                      boolean freeSpace = false;
                      
                      block = (bm7 = p.getInventory().getContents()).length; for (bm5 = 0; bm5 < block; bm5++) { ItemStack is = bm7[bm5];
                        if ((!freeSpace) && 
                          (is == null)) {
                          freeSpace = true;
                          break;
                        }
                      }
                      


                      if (freeSpace) {
                        PlayerInventory inventory = p.getInventory();
                        inventory.addItem(new ItemStack[] {
                          oreblock });
                        
                        middleblock.setType(Material.AIR);
                        rgm.removeRegion(id);
                        try {
                          rgm.save();
                        } catch (Exception e1) {
                          System.out.println("[ProtectionStones] WorldGuard Error [" + e1 + "] during Region File Save");
                        }
                        p.sendMessage(ChatColor.YELLOW + "This area is no longer protected.");
                      } else {
                        p.sendMessage(ChatColor.RED + "You don't have enough room in your inventory.");
                      }
                    } else {
                      middleblock.setType(Material.AIR);
                      rgm.removeRegion(id);
                      try {
                        rgm.save();
                      } catch (Exception e1) {
                        System.out.println("[ProtectionStones] WorldGuard Error [" + e1 + "] during Region File Save");
                      }
                      p.sendMessage(ChatColor.YELLOW + "This area is no longer protected.");
                    }
                  } else {
                    p.sendMessage(ChatColor.YELLOW + "You are not the owner of this region.");
                  }
                } else {
                  p.sendMessage(ChatColor.YELLOW + "Not a ProtectionStones Region");
                }
              }
            } else {
              p.sendMessage(ChatColor.RED + "You don't have permission to use the Reclaim Command");
            }
            return true;
          }
          p.sendMessage(ChatColor.RED + "No such command. please type /ps help for more info");
        }
      }
    } else {
      s.sendMessage(ChatColor.RED + "PS cannot be used from Console");
    }
    return true;
  }
  
  public static Object getFlagValue(Flag<?> flag, Object value) {
    if (value == null) { return null;
    }
    String valueString = value.toString().trim();
    
    if ((flag instanceof StateFlag)) {
      if (valueString.equalsIgnoreCase("allow")) return StateFlag.State.ALLOW;
      if (valueString.equalsIgnoreCase("deny")) return StateFlag.State.DENY;
      return null;
    }
    return null;
  }
  
  protected void setBlock(World theWorld, int x, int y, int z, Material mat) {
    Block blockToChange = theWorld.getBlockAt(x, y, z);
    blockToChange.setType(mat);
  }
  
  protected Material getBlock(World theWorld, int x, int y, int z) {
    Block blockToReturn = theWorld.getBlockAt(x, y, z);
    return blockToReturn.getType();
  }
  
  private boolean initConfig() {
    config = new YamlConfiguration();
    try {
      config.load(conf);
    } catch (IOException|InvalidConfigurationException ex) {
      Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
    }
    System.out.print("[ProtectionStones] Checking Configuration Version");
    if (getConfig().get("ConfVer") == null) {
      System.out.print("Config is outdated, this WILL generate errors, please refresh it!");
    } else {
      if (config.getInt("ConfVer") == 1) {
        System.out.print("Config is correct version, continuing start-up");
        return true; }
      if (config.getInt("ConfVer") > 1) {
        System.out.print("Config version is higher than required version, this might cause trouble");
        return true;
      }
      fixInitialHidden(config.get("Block"));
      System.out.print("Config is outdated, this WILL generate errors, please refresh it!");
      return true;
    }
    
    return false;
  }
  
  private void fixInitialHidden(Object block) { YamlConfiguration hideFile = YamlConfiguration.loadConfiguration(psStoneData);
    WorldGuardPlugin wg = (WorldGuardPlugin)wgd;
    System.out.print("Patching initial hiddenpstones.yml");
    hideFile.set("InitialHideDone", Boolean.valueOf(true));
    Iterator<String> region; for (Iterator localIterator = Bukkit.getWorlds().iterator(); localIterator.hasNext(); 
        

        region.hasNext())
    {
      World world = (World)localIterator.next();
      RegionManager rgm = wg.getRegionManager(world);
      Map<String, ProtectedRegion> regions = rgm.getRegions();
      region = regions.keySet().iterator(); continue;
      String selected = (String)region.next();
      if (selected.startsWith("ps")) {
        Material mat = Material.valueOf(block.toString());
        String sub = null;
        if (block.toString().contains("-")) {
          sub = block.toString().split("-")[1];
        }
        if (sub != null) {
          hideFile.set(selected, mat.toString() + "-" + sub);
        } else {
          hideFile.set(selected, mat.toString() + "-0");
        }
      }
    }
    try
    {
      hideFile.save(psStoneData);
    } catch (IOException ex) {
      Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
}
