package me.vik1395.ProtectionStones;

import java.util.Comparator;
import org.bukkit.OfflinePlayer;








class PlayerComparator
  implements Comparator<OfflinePlayer>
{
  PlayerComparator() {}
  
  public int compare(OfflinePlayer o1, OfflinePlayer o2)
  {
    return o1.getName().compareTo(o2.getName());
  }
}
