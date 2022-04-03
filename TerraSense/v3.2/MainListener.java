package me.hiro3.terrasense;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ProjectKorra;

public class MainListener implements Listener {

	@EventHandler
	public void onClick(PlayerAnimationEvent event) {
		Player player = event.getPlayer();
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

		if (event.isCancelled() || bPlayer == null) {
			return;
		}

		if (bPlayer.getBoundAbilityName().equalsIgnoreCase(null)) {
			return;
		}
		
		if (bPlayer.getBoundAbilityName().equalsIgnoreCase("EarthPulse")) {
			new EarthPulse(player);
		}
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if (event.isCancelled())
			return;
		
		if (!TerraSense.canSenseWaves)
			return;
		
		Player player = event.getPlayer();
		if (TerraSense.senseMap.containsKey(player)) {
			UtilityMethods.sendPulse(player, event.getBlock().getLocation().add(0.5, -1, 0.5), 2, 6, 20);
		}
		for (Player p : TerraSense.senseMap.keySet()) {
			if (TerraSense.senseMap.get(p).contains(player)) {
				UtilityMethods.sendPulse(p, event.getBlock().getLocation().add(0.5, -1, 0.5), 2, 6, 20);
			}
		}
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		if (event.isCancelled())
			return;
		
		if (!TerraSense.canSenseWaves)
			return;
		
		Player player = event.getPlayer();
		if (TerraSense.senseMap.containsKey(player)) {
			UtilityMethods.sendPulse(player, event.getBlock().getLocation().add(0.5, 0, 0.5), 2, 6, 20);
		}
		for (Player p : TerraSense.senseMap.keySet()) {
			if (TerraSense.senseMap.get(p).contains(player)) {
				UtilityMethods.sendPulse(p, event.getBlock().getLocation().add(0.5, 0, 0.5), 2, 6, 20);
			}
		}
	}
	
	@EventHandler
	public void onFall(PlayerMoveEvent event) {
		if (event.isCancelled())
			return;
		
		if (!TerraSense.canSenseWaves)
			return;
		
		Player player = event.getPlayer();
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer.getElements().contains(Element.AIR))
			return;
		
		if (player.getFallDistance() < 5)
			return;
		
		Bukkit.getScheduler().scheduleSyncDelayedTask(ProjectKorra.plugin, () -> {
			if (TerraSense.senseMap.containsKey(player)) {
				if (TerraSense.improvedWavesOnFallAndMove)
					UtilityMethods.sendPulseImproved(player, player.getLocation().add(0, -1, 0), TerraSense.ShockwaveRadius, 14, 10);
				else
					UtilityMethods.sendPulse(player, player.getLocation().add(0, -1, 0), 10, 14, 15);
			}
			
			for (Player p : TerraSense.senseMap.keySet()) {
				if (TerraSense.senseMap.get(p).contains(player)) {
					if (TerraSense.improvedWavesOnFallAndMove)
						UtilityMethods.sendPulseImproved(p, player.getLocation().add(0, -1, 0), TerraSense.ShockwaveRadius, 14, 10);
					else
						UtilityMethods.sendPulse(p, player.getLocation().add(0, -1, 0), 10, 14, 15);
				}
			}
        },  1);
	}
	
	@EventHandler
	public void onMove(PlayerMoveEvent event) {
		if (event.isCancelled())
			return;
		
		if (!TerraSense.canSenseWaves)
			return;
		
		Player player = event.getPlayer();
		if (!event.getFrom().getBlock().getLocation().equals(event.getTo().getBlock().getLocation())
				&& Math.random() * 100 < 5) {
			
			if (TerraSense.senseMap.containsKey(player)) {
				if (TerraSense.improvedWavesOnFallAndMove)
					UtilityMethods.sendPulseImproved(player, player.getLocation().add(0, -1, 0), 15, 20, 10);
				else
					UtilityMethods.sendPulse(player, player.getLocation().add(0, -1, 0), 7, 10, 15);
			}
			
			for (Player p : TerraSense.senseMap.keySet()) {
				if (TerraSense.senseMap.get(p).contains(player)) {
					if (TerraSense.improvedWavesOnFallAndMove)
						UtilityMethods.sendPulseImproved(p, player.getLocation().add(0, -1, 0), 15, 20, 10);
					else
						UtilityMethods.sendPulse(p, player.getLocation().add(0, -1, 0), 7, 10, 15);
				}
			}
		}
	}
	
}