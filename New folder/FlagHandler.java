package me.vik1395.ProtectionStones;

import com.sk89q.worldguard.protection.flags.RegionGroup;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class FlagHandler
{
  public FlagHandler() {}
  
  com.sk89q.worldguard.bukkit.WorldGuardPlugin wg = (com.sk89q.worldguard.bukkit.WorldGuardPlugin)Main.wgd;
  
  public void setFlag(String[] args, ProtectedRegion region, Player p) {
    com.sk89q.worldguard.protection.flags.Flag<?> rawFlag = com.sk89q.worldguard.protection.flags.DefaultFlag.fuzzyMatchFlag(args[1]);
    if ((rawFlag instanceof StateFlag)) {
      StateFlag flag = (StateFlag)rawFlag;
      if (args[2].equalsIgnoreCase("default")) {
        region.setFlag(flag, flag.getDefault());
        region.setFlag(flag.getRegionGroupFlag(), null);
        p.sendMessage(ChatColor.YELLOW + args[1] + " flag has been set.");
      } else {
        RegionGroup group = null;
        if (java.util.Arrays.toString(args).contains("-g")) {
          int i = 0;
          for (String s : args) {
            i++;
            if (s.equalsIgnoreCase("-g")) {
              group = getRegionGroup(args[i]);
            }
          }
        }
        if (java.util.Arrays.toString(args).contains("allow")) {
          region.setFlag(flag, com.sk89q.worldguard.protection.flags.StateFlag.State.ALLOW);
          if (group != null) {
            region.setFlag(flag.getRegionGroupFlag(), group);
          }
          p.sendMessage(ChatColor.YELLOW + args[1] + " flag has been set.");
        } else if (java.util.Arrays.toString(args).contains("deny")) {
          region.setFlag(flag, com.sk89q.worldguard.protection.flags.StateFlag.State.DENY);
          if (group != null) {
            region.setFlag(flag.getRegionGroupFlag(), group);
          }
          p.sendMessage(ChatColor.YELLOW + args[1] + " flag has been set.");
        }
        else if (group != null) {
          region.setFlag(flag.getRegionGroupFlag(), group);
          p.sendMessage(ChatColor.YELLOW + args[1] + " flag has been set.");
        } else {
          p.sendMessage(ChatColor.YELLOW + args[1] + " flag has " + ChatColor.RED + "not" + ChatColor.YELLOW + " been set.");
        }
      }
    }
    else if ((rawFlag instanceof com.sk89q.worldguard.protection.flags.DoubleFlag)) {
      com.sk89q.worldguard.protection.flags.DoubleFlag flag = (com.sk89q.worldguard.protection.flags.DoubleFlag)rawFlag;
      if (args[2].equalsIgnoreCase("default")) {
        region.setFlag(flag, (Double)flag.getDefault());
        region.setFlag(flag.getRegionGroupFlag(), null);
      } else {
        region.setFlag(flag, Double.valueOf(Double.parseDouble(args[1])));
      }
      p.sendMessage(ChatColor.YELLOW + args[1] + " flag has been set.");
    } else if ((rawFlag instanceof com.sk89q.worldguard.protection.flags.IntegerFlag)) {
      com.sk89q.worldguard.protection.flags.IntegerFlag flag = (com.sk89q.worldguard.protection.flags.IntegerFlag)rawFlag;
      if (args[2].equalsIgnoreCase("default")) {
        region.setFlag(flag, (Integer)flag.getDefault());
        region.setFlag(flag.getRegionGroupFlag(), null);
      } else {
        region.setFlag(flag, Integer.valueOf(Integer.parseInt(args[1])));
      }
      p.sendMessage(ChatColor.YELLOW + args[1] + " flag has been set.");
    } else if ((rawFlag instanceof com.sk89q.worldguard.protection.flags.StringFlag)) {
      com.sk89q.worldguard.protection.flags.StringFlag flag = (com.sk89q.worldguard.protection.flags.StringFlag)rawFlag;
      if (args[2].equalsIgnoreCase("default")) {
        region.setFlag(flag, flag.getDefault());
        region.setFlag(flag.getRegionGroupFlag(), null);
      } else {
        String flagValue = com.google.common.base.Joiner.on(" ").join(args).substring(args[0].length() + args[1].length() + 2);
        String msg = flagValue.replaceAll("%player%", p.getName());
        region.setFlag(flag, msg);
      }
      p.sendMessage(ChatColor.YELLOW + args[1] + " flag has been set.");
    } else if ((rawFlag instanceof com.sk89q.worldguard.protection.flags.BooleanFlag)) {
      com.sk89q.worldguard.protection.flags.BooleanFlag flag = (com.sk89q.worldguard.protection.flags.BooleanFlag)rawFlag;
      if (args[2].equalsIgnoreCase("default")) {
        region.setFlag(flag, (Boolean)flag.getDefault());
        region.setFlag(flag.getRegionGroupFlag(), null);
        p.sendMessage(ChatColor.YELLOW + args[1] + " flag has been set.");
      }
      else if (args[2].equalsIgnoreCase("true")) {
        region.setFlag(flag, Boolean.valueOf(true));
        p.sendMessage(ChatColor.YELLOW + args[1] + " flag has been set.");
      } else if (args[2].equalsIgnoreCase("false")) {
        region.setFlag(flag, Boolean.valueOf(false));
        p.sendMessage(ChatColor.YELLOW + args[1] + " flag has been set.");
      }
    }
  }
  












  private RegionGroup getRegionGroup(String arg)
  {
    if ((arg.equalsIgnoreCase("member")) || (arg.equalsIgnoreCase("members")))
      return RegionGroup.MEMBERS;
    if ((arg.equalsIgnoreCase("nonmembers")) || (arg.equalsIgnoreCase("nonmember")) || 
      (arg.equalsIgnoreCase("nomember")) || (arg.equalsIgnoreCase("nomembers")) || 
      (arg.equalsIgnoreCase("non_members")) || (arg.equalsIgnoreCase("non_member")) || 
      (arg.equalsIgnoreCase("no_member")) || (arg.equalsIgnoreCase("no_members")))
      return RegionGroup.NON_MEMBERS;
    if ((arg.equalsIgnoreCase("nonowners")) || (arg.equalsIgnoreCase("nonowner")) || 
      (arg.equalsIgnoreCase("noowner")) || (arg.equalsIgnoreCase("noowners")) || 
      (arg.equalsIgnoreCase("non_owners")) || (arg.equalsIgnoreCase("non_owner")) || 
      (arg.equalsIgnoreCase("no_owner")) || (arg.equalsIgnoreCase("no_owners")))
      return RegionGroup.NON_OWNERS;
    if ((arg.equalsIgnoreCase("owner")) || (arg.equalsIgnoreCase("owners")))
      return RegionGroup.OWNERS;
    if ((arg.equalsIgnoreCase("none")) || (arg.equalsIgnoreCase("noone")))
      return RegionGroup.NONE;
    if ((arg.equalsIgnoreCase("all")) || (arg.equalsIgnoreCase("everyone")))
      return RegionGroup.ALL;
    if (arg.endsWith("empty")) {
      return null;
    }
    
    return null;
  }
}
