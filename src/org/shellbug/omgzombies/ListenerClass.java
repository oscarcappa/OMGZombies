package org.shellbug.omgzombies;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import com.earth2me.essentials.Essentials;

import net.ess3.api.MaxMoneyException;

public class ListenerClass implements Listener {

	final boolean DEBUG = false;
	MainClass mainclass;
	Integer iReward;
	World world;
	Logger logger;
//	Boolean bInvasion;
	Boolean bEssentials;
	HashMap<String, Boolean> bLand;
	HashMap<String, Integer> iZombieCount;
	private Essentials essentials;

	public ListenerClass(MainClass plugin) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		logger = plugin.getLogger();
		mainclass = plugin;
//		bInvasion = plugin.bInvasion;
		bEssentials = plugin.bEssentials;
		bLand = plugin.bLand;
		iZombieCount = plugin.iZombieCount;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player player = e.getPlayer();
		// Se entro ed � attiva l'invasione
		if(DEBUG){
			logger.warning("bInvasion: " + mainclass.bInvasion +
					"; onPlayerJoin player.getWorld().getName(): " + player.getWorld().getName() +
					"; sServerName: " + mainclass.sServerName +
					"; AllowFly: " + mainclass.getConfig().getBoolean("AllowFly")
					);
		}
		
		if (mainclass.bInvasion) {
			if (player.getWorld().getName().compareTo(mainclass.sServerName) == 0) {
				// Se nella configurazione c'e' scritto che � possibile volare
				if (mainclass.getConfig().getBoolean("AllowFly")) {
					player.setAllowFlight(true);
					player.setFlying(true);
				}
				player.playSound(player.getLocation(), Sound.AMBIENCE_THUNDER, 2f, 2f);
			}
		}

		if (!bLand.containsKey(player.getName())) {
			bLand.put(player.getName(), false);
		}
		if (!iZombieCount.containsKey(player.getName())) {
			iZombieCount.put(player.getName(), 0);
		}
	}

	@EventHandler
	public void killZombie(EntityDeathEvent e) {
		Entity dead = e.getEntity();
		Entity killer = e.getEntity().getKiller();
		if (killer instanceof Player && dead instanceof Zombie) {
			Player player = (Player) killer;
			if (iZombieCount.containsKey(player.getName())) {
				iZombieCount.put(player.getName(), iZombieCount.get(player.getName()) + 1);
				if(DEBUG){
					logger.warning(player.getName()+":"+iZombieCount.get(player.getName()));
				}
			}
			iReward = mainclass.getConfig().getInt("Reward");
			if ((iReward > 0) && (bEssentials) ) {
				essentials = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
				BigDecimal iMoney = new BigDecimal(iReward);
				try {
					essentials.getUser(player.getName()).giveMoney(iMoney);
					iMoney = essentials.getUser(player.getName()).getMoney();
					player.sendMessage(ChatColor.GREEN + "You receive " + ChatColor.RED + iReward.toString() + "$ "
							+ ChatColor.GREEN + "(" + ChatColor.RED + iMoney.toString() + "$" + ChatColor.GREEN
							+ ")");
					if(DEBUG){
						logger.warning(player.getName()+" receives " + iReward.toString() + "$ (" + iMoney.toString() + "$)");
					}
				} catch (MaxMoneyException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
/*
				try {
					essentials.getUser(player.getName()).giveMoney(iMoney);
					iMoney = essentials.getUser(player.getName()).getMoney();
					player.sendMessage(ChatColor.GREEN + "You receive " + ChatColor.RED + iReward.toString() + "$ "
							+ ChatColor.GREEN + "(" + ChatColor.RED + iMoney.toString() + "$" + ChatColor.GREEN
							+ ")");
				} catch (MaxMoneyException e1) {
					e1.printStackTrace();
				}
*/
			}
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		Location location = player.getLocation();
		if (!player.isFlying()) {
			Block block = location.getBlock().getRelative(BlockFace.DOWN);
			if (block.getType() != Material.AIR) {
				world = (World) player.getWorld();
				// Questo quando tolgo il fly
				Integer land = world.getHighestBlockYAt(location);
				Integer pgy = (int) location.getY();
				if (land == pgy) {
					if (bLand.get(player.getName())) {
						location.getWorld().createExplosion(location.getX(), location.getY(), location.getZ(), 4.0F,
								false, false);
						bLand.put(player.getName(), false);
					}
				}
			}
		}
	}

	@EventHandler
	public void onEntityDamage(EntityDamageEvent e) {
		if (e.getEntity() instanceof Player && e.getCause().equals(DamageCause.BLOCK_EXPLOSION)) {
			e.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
		Player player = event.getPlayer();
		Location location = player.getLocation();
		if (!player.isFlying()) {
			Block block = location.getBlock().getRelative(BlockFace.DOWN);
			if (block.getType() != Material.AIR) {
				world = (World) player.getWorld();
				// Questo quando tolgo il fly
				Integer land = world.getHighestBlockYAt(location);
				Integer pgy = (int) location.getY();
				if (land != pgy) {
					// Parto dall'acqua
					bLand.put(player.getName(), true);
				}
			} else {
				world = (World) player.getWorld();
				// Questo quando tolgo il fly
				Integer land = world.getHighestBlockYAt(location);
				Integer pgy = (int) location.getY();
				if (land != pgy) {
					// Parto dalla terra
					bLand.put(player.getName(), true);
				}
			}

		}

	}

}
