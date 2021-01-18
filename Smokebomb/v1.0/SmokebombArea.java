package me.hiro3.smokebomb;

import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AirAbility;

public class SmokebombArea extends BukkitRunnable {

	private Location location;
	private double radius;
	private double maxRadius;
	private long startTime;
	private long duration;
	private long growTime;
	
	private HashSet<Player> affectedPlayers;
	private HashSet<Player> blindPlayers;
	
	public SmokebombArea(Location location, double radius, long duration) {
		this.location = location;
		this.maxRadius = radius;
		this.radius = this.maxRadius / 5;
		this.startTime = System.currentTimeMillis();
		this.duration = duration;
		
		this.growTime = duration / 3;
		this.growTime *= 4.0/5;
		
		this.affectedPlayers = new HashSet<Player>();
		this.blindPlayers = new HashSet<Player>();
		AirAbility.playAirbendingSound(location);
	}
	
	@Override
	public void run() {
		
		if (System.currentTimeMillis() > startTime + duration) {
			lastCheck();
			this.cancel();
			return;
		}
		
		if (Math.random() < 0.25)
			AirAbility.playAirbendingSound(location);
		
		if (this.radius < this.maxRadius) {
			this.radius += this.maxRadius / ((int) this.growTime/50);
		} else {
			this.radius = this.maxRadius;
		}
		
		for(Block b : GeneralMethods.getBlocksAroundPoint(location, radius)) {
			if(b.getType().equals(Material.AIR) && Math.random() < Smokebomb.particlePercentage) { //0.025, //0,0125
				location.getWorld().spawnParticle(Particle.CLOUD, b.getLocation().add(0.5, 0.5, 0.5), 0);
			}
		}
		
		for(Player p : Bukkit.getServer().getOnlinePlayers()) {
			if (p.getWorld().equals(location.getWorld())) {
				if (p.getLocation().distance(location) > radius) {
					if (!affectedPlayers.contains(p)) 
						affectedPlayers.add(p);
					for(Player bp : blindPlayers) {
						p.hidePlayer(ProjectKorra.plugin, bp);
					}
				} else {
					if (!blindPlayers.contains(p))
						blindPlayers.add(p);
					PotionEffect pE = new PotionEffect(PotionEffectType.BLINDNESS, 100000, 0);
					p.addPotionEffect(pE);
				}
			}
		}
		
		check();
	}
	
	public void check() {
		HashSet<Player> tmpAffected = new HashSet<Player>();
		HashSet<Player> tmpBlind = new HashSet<Player>();
		
		for(Player p : affectedPlayers) {
			if(p.getWorld().equals(location.getWorld()) && p.getLocation().distance(location) <= radius) {
				for(Player bp : blindPlayers) {
					p.showPlayer(ProjectKorra.plugin, bp);
				}
				tmpAffected.add(p);
			}
		}
		
		for(Player p : blindPlayers) {
			if(!p.getWorld().equals(location.getWorld()) || p.getLocation().distance(location) > radius) {
				for(Player ap : affectedPlayers) {
					ap.showPlayer(ProjectKorra.plugin, p);
				}
				for(PotionEffect pE : p.getActivePotionEffects()) {
					if (pE.getType().equals(PotionEffectType.BLINDNESS) && pE.getDuration() > 10000) {	
						p.removePotionEffect(pE.getType());
						tmpBlind.add(p);
					}
				}
			}
		}
		
		for(Player p : tmpAffected) {
			if (affectedPlayers.contains(p))
				affectedPlayers.remove(p);
		}
		
		for(Player p : tmpBlind) {
			if (tmpBlind.contains(p))
				blindPlayers.remove(p);
		}
		
		tmpAffected.clear();
		tmpBlind.clear();
	}
	
	public void lastCheck() {
		for(Player p : affectedPlayers) {
			for(Player bp : blindPlayers) {
				p.showPlayer(ProjectKorra.plugin, bp);
			}
		}
		for(Player p : blindPlayers) {
			for(Player ap : affectedPlayers) {
				ap.showPlayer(ProjectKorra.plugin, p);
			}
			for(PotionEffect pE : p.getActivePotionEffects()) {
				if (pE.getType().equals(PotionEffectType.BLINDNESS) && pE.getDuration() > 10000) {
					p.removePotionEffect(pE.getType());
				}
			}
		}
		affectedPlayers.clear();
		blindPlayers.clear();
	}

}