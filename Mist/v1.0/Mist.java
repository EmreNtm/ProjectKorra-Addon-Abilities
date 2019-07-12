package Hiro3;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.waterbending.Torrent;

public class Mist extends WaterAbility implements AddonAbility, ComboAbility {

	private Block source;
	private Vector firstSourceDirection;
	private Location firstSourceLocation;
	private Location secondSourceLocation;
	private Location thirdSourceLocation;
	private Location fourthSourceLocation;
	private LinkedList<Player> affectedPlayers;
	private LinkedList<Player> blindPlayers;
	private long time1;
	private int state;
	private String color;
	
	private BendingPlayer bPlayer;
	
	private double radius;
	private long duration;
	private long durationStart;
	private long cooldown;
	private long chargeTime;
	private boolean blindnessOn;
	private int particlePercentage;
	private boolean mixedMist;
	private boolean blueCharge;
	
	public Mist(Player player) {
		super(player);
		
		bPlayer = BendingPlayer.getBendingPlayer(player);
		
		if (bPlayer.isOnCooldown(this)) {
			return;
		} else if (!bPlayer.canBendIgnoreBindsCooldowns(this)) {
			return;
		}
		
		if (hasAbility(player, Mist.class)) {
			return;
		} 
		
		if (hasAbility(player, Torrent.class)) {
			source = getAbility(player, Torrent.class).getSourceBlock();
			getAbility(player, Torrent.class).remove();
		} else {
			return;
		}
		
		setField();
		start();
	}

	public void setField() {
		affectedPlayers = new LinkedList<Player>();
		blindPlayers = new LinkedList<Player>();
		
		color = "9ECCFF";
		state = -1;
		firstSourceLocation = source.getLocation();
		
		radius = ConfigManager.getConfig().getDouble("ExtraAbilities.Hiro3.Water.Mist.Radius");
		duration = ConfigManager.getConfig().getLong("ExtraAbilities.Hiro3.Water.Mist.Duration");
		durationStart = -1;
		cooldown = ConfigManager.getConfig().getLong("ExtraAbilities.Hiro3.Water.Mist.Cooldown");
		chargeTime = ConfigManager.getConfig().getLong("ExtraAbilities.Hiro3.Water.Mist.ChargeTime");
		blindnessOn = ConfigManager.getConfig().getBoolean("ExtraAbilities.Hiro3.Water.Mist.BlindnessOn");
		particlePercentage = ConfigManager.getConfig().getInt("ExtraAbilities.Hiro3.Water.Mist.ParticlePercentage");
		mixedMist = ConfigManager.getConfig().getBoolean("ExtraAbilities.Hiro3.Water.Mist.MixedMist");
		blueCharge = ConfigManager.getConfig().getBoolean("ExtraAbilities.Hiro3.Water.Mist.BlueCharge");
		if (particlePercentage % 101 == 0)
			particlePercentage = 1;
		else
			particlePercentage %= 101;
	}
	

	@Override
	public void progress() {
		
		if (GeneralMethods.isRegionProtectedFromBuild(this, player.getLocation())) {
			remove();
			return;
		}
		
		if (durationStart != -1 && System.currentTimeMillis() > durationStart + duration) {
			this.bPlayer.addCooldown(this);
			remove();
			return;
		}
		
		if (state >= 0 && state < 2 && !player.isSneaking()) {
			remove();
			return;
		}
		
		if (player.isSneaking() && (state == -1 || state == 0)) {
			state = 0;
			if(blueCharge) {
				GeneralMethods.displayColoredParticle(firstSourceLocation, color, 0, 0, 0);
			} else {
				player.getWorld().spawnParticle(Particle.CLOUD, firstSourceLocation, 0);
			}
			firstSourceDirection = player.getLocation().toVector();
			firstSourceDirection.add(new Vector(0, 1, 0));
			firstSourceDirection = firstSourceDirection.subtract(firstSourceLocation.toVector());
			firstSourceDirection.normalize();
			if(firstSourceLocation.distance(player.getLocation()) > 2) {
				firstSourceLocation.add(firstSourceDirection);
			} else {
				time1 = System.currentTimeMillis();
				state = 1;
			}
		} else if (player.isSneaking() && (state == 1 || state == 2) ) {
			for(int i = 0; i <= 360; i+=40) {
				double angle = Math.toRadians(i);
				secondSourceLocation = player.getLocation().clone();
				secondSourceLocation.add(0, 1, 0);
				secondSourceLocation.setX(secondSourceLocation.getX() + 2 * Math.cos(angle));
				secondSourceLocation.setZ(secondSourceLocation.getZ() + 2 * Math.sin(angle));
				if(blueCharge) {
					GeneralMethods.displayColoredParticle(secondSourceLocation, color, 0, 0, 0);
				} else {
					player.getWorld().spawnParticle(Particle.CLOUD, secondSourceLocation, 0);
				}
			}
			if (System.currentTimeMillis() >= time1 + chargeTime) {
				state = 2;
				for(int i = 0; i <= 360; i+=60) {
					double angle = Math.toRadians(i);
					thirdSourceLocation = player.getLocation().clone();
					fourthSourceLocation = player.getLocation().clone();
					thirdSourceLocation.add(0, 2, 0);
					thirdSourceLocation.setX(thirdSourceLocation.getX() + Math.cos(angle));
					thirdSourceLocation.setZ(thirdSourceLocation.getZ() + Math.sin(angle));
					fourthSourceLocation.setX(fourthSourceLocation.getX() + Math.cos(angle));
					fourthSourceLocation.setZ(fourthSourceLocation.getZ() + Math.sin(angle));
					if(blueCharge) {
						GeneralMethods.displayColoredParticle(thirdSourceLocation, color, 0, 0, 0);
						GeneralMethods.displayColoredParticle(fourthSourceLocation, color, 0, 0, 0);
					} else {
						player.getWorld().spawnParticle(Particle.CLOUD, thirdSourceLocation, 0);
						player.getWorld().spawnParticle(Particle.CLOUD, fourthSourceLocation, 0);
					}
				}
			}
		} else if (state == 2) {
			state = 3;
			durationStart = System.currentTimeMillis();
			
			new BukkitRunnable() {
				double r = 0;
				double angle;
				int i = 0;
				@Override
				public void run() {
					if(r <= radius) {
						angle = Math.toRadians(i);
						
						secondSourceLocation = player.getLocation().clone();
						thirdSourceLocation = player.getLocation().clone();
						fourthSourceLocation = player.getLocation().clone();
						
						secondSourceLocation.add(0, 1, 0);
						thirdSourceLocation.add(0, 2, 0);
						
						secondSourceLocation.setX(secondSourceLocation.getX() + (1 + i * radius / 1440) * Math.cos(angle));
						secondSourceLocation.setZ(secondSourceLocation.getZ() + (1 + i * radius / 1440) * Math.sin(angle));
						secondSourceLocation.setY(secondSourceLocation.getY() + 0.01 * i/10);
						
						thirdSourceLocation.setX(thirdSourceLocation.getX() + (i * radius / 1440) * Math.cos(angle));
						thirdSourceLocation.setZ(thirdSourceLocation.getZ() + (i * radius / 1440) * Math.sin(angle));
						thirdSourceLocation.setY(thirdSourceLocation.getY() + 0.02 * i/10);
						
						fourthSourceLocation.setX(fourthSourceLocation.getX() + (i * radius / 1440) * Math.cos(angle));
						fourthSourceLocation.setZ(fourthSourceLocation.getZ() + (i * radius / 1440) * Math.sin(angle));
						
						player.getWorld().spawnParticle(Particle.CLOUD, secondSourceLocation, 0);
						player.getWorld().spawnParticle(Particle.CLOUD, thirdSourceLocation, 0);
						player.getWorld().spawnParticle(Particle.CLOUD, fourthSourceLocation, 0);
						
						i += 10;
						r += radius/144;
					}
					else {
						Bukkit.getScheduler().cancelTask(getTaskId());
					}
				}
			}.runTaskTimer(ProjectKorra.plugin, 0, 0);
			
		} else if (state == 3) { 
			Random generator = new Random();
			for(Block b : GeneralMethods.getBlocksAroundPoint(player.getLocation(), radius)) {
				if(b.getType().equals(Material.AIR) && generator.nextInt((int) (100 * (double) 100/particlePercentage)) == 55) {
					int x = generator.nextInt((int) (100 * (double) 100/particlePercentage));
					if(!mixedMist) {
						player.getWorld().spawnParticle(Particle.CLOUD, b.getLocation(), 0);
					} else if (x % 3 == 0) {
						player.getWorld().spawnParticle(Particle.CLOUD, b.getLocation(), 0);
					} else if (x % 5 == 0) {
						GeneralMethods.displayColoredParticle(b.getLocation(), color, 0, 0, 0);
					} else if (x % 2 == 0) { 
						player.getWorld().spawnParticle(Particle.SPELL, b.getLocation(), 1);
					} else {
						player.getWorld().spawnParticle(Particle.WATER_BUBBLE, b.getLocation(), 1);
					}
				}
			}
			for(Player p : Bukkit.getServer().getOnlinePlayers()) {
				if (p.getUniqueId() != player.getUniqueId() && p.getWorld().equals(player.getWorld())) {
					if (p.getLocation().distance(player.getLocation()) > radius) {
						if (!affectedPlayers.contains(p)) 
							affectedPlayers.add(p);
						p.hidePlayer(ProjectKorra.plugin, player);
						for(Player bp : blindPlayers) {
							p.hidePlayer(ProjectKorra.plugin, bp);
						}
					} else {
						if (!blindPlayers.contains(p))
							blindPlayers.add(p);
						if(blindnessOn) {
							PotionEffect pE = new PotionEffect(PotionEffectType.BLINDNESS, 100000, 0);
							p.addPotionEffect(pE);
						}
					}
				}
			}
			check();
		}
		
	}
	
	public void check() {
		LinkedList<Player> tmpAffected = new LinkedList<Player>();
		LinkedList<Player> tmpBlind = new LinkedList<Player>();
		
		for(Player p : affectedPlayers) {
			if(p.getLocation().distance(player.getLocation()) <= radius) {
				p.showPlayer(ProjectKorra.plugin, player);
				for(Player bp : blindPlayers) {
					p.showPlayer(ProjectKorra.plugin, bp);
				}
				tmpAffected.add(p);
			}
		}
		
		for(Player p : blindPlayers) {
			if(p.getLocation().distance(player.getLocation()) > radius) {
				for(Player ap : affectedPlayers) {
					ap.showPlayer(ProjectKorra.plugin, p);
				}
				for(PotionEffect pE : p.getActivePotionEffects()) {
					if (pE.getType().equals(PotionEffectType.BLINDNESS) && pE.getDuration() > 10000) {	
						if(blindnessOn)
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
			p.showPlayer(ProjectKorra.plugin, player);
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
					if(blindnessOn)
						p.removePotionEffect(pE.getType());
				}
			}
		}
		affectedPlayers.clear();
		blindPlayers.clear();
	}
	
	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public Location getLocation() {
		return player != null ? player.getLocation() : null;
	}

	@Override
	public String getName() {
		return "Mist";
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	@Override
	public boolean isSneakAbility() {
		return false;
	}

	@Override
	public Object createNewComboInstance(Player player) {
		return new Mist(player);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		ArrayList<AbilityInformation> combination = new ArrayList<>();
		combination.add(new AbilityInformation("PhaseChange", ClickType.SHIFT_DOWN));
		combination.add(new AbilityInformation("PhaseChange", ClickType.SHIFT_UP));
		combination.add(new AbilityInformation("PhaseChange", ClickType.SHIFT_DOWN));
		combination.add(new AbilityInformation("Torrent", ClickType.LEFT_CLICK));

		return combination;
	}

	@Override
	public String getDescription() {
		return " A waterbender is able to change water's phase from liquid to gas. Use this ability to create a mist and "
				+ "hide yourself in it as Katara did on the \"The Painted Lady\" episode.";
	}

	@Override
	public String getInstructions() {
		return "PhaseChange (Tap Sneak) > PhaseChange (Hold Sneak) > Torrent (Left Click to a water source)";
	}
	
	@Override
	public String getAuthor() {
		return "Hiro3";
	}

	@Override
	public String getVersion() {
		return "1.0";
	}
	
	@Override
	public void remove() {
		super.remove();
		lastCheck();
	}
	
	@Override
	public void load() {
		ProjectKorra.log.info("Succesfully enabled " + getName() + " by " + getAuthor());
		
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Water.Mist.Radius", 7);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Water.Mist.Duration", 14000);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Water.Mist.Cooldown", 5000);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Water.Mist.ChargeTime", 2000);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Water.Mist.BlindnessOn", true);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Water.Mist.ParticlePercentage", 50);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Water.Mist.MixedMist", false);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Water.Mist.BlueCharge", false);
		ConfigManager.defaultConfig.save();
	}

	@Override
	public void stop() {
		ProjectKorra.log.info("Successfully disabled " + getName() + " by " + getAuthor());
		super.remove();
	}

}

