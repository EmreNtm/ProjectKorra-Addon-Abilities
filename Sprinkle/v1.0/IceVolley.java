package me.hiro3.sprinkle;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.IceAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.DamageHandler;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class IceVolley extends IceAbility implements AddonAbility {

	public enum AbilityState {
		HOLDINGWATER,
		HOLDINGICE,
		LAUNCH
	}
	
	private AbilityState abilityState;
	
	private long cooldown;
	private double radius;
	private Location centerLocation;
	private int particleAmount;
	private double currentParticleAmount;
	private double particleAmountIncrease;
	private DustOptions iceOptions;
	private long iceStartDelay;
	private int iceAmount;
	private double currentIceAmount;
	private double iceAmountIncrease;
	private double range;
	private double rangeSquared;
	private double speed;
	private double damage;
	private int hitCount;
	private int iceLaunchNoise;
	
	private long waterSphereDuration;
	private long iceSphereDuration;
	private long iceSphereStartTime;
	
	private ArrayList<Location> iceLocations;
	private ArrayList<Location> iceStartLocations;
	private Location targetLocation;
	private HashMap<LivingEntity, Integer> hitCounts;
	private ArrayList<Vector> iceDirections;
	private ArrayList<Integer> tickPhase;
	private int tickCounter;
	
	public IceVolley(Player player, Location location, double radius) {
		super(player);
		
		if(!bPlayer.canBendIgnoreBinds(this)) {
			return;
		}
		
		setField(location, radius);
		
		bPlayer.addCooldown(this);
		start();
	}
	
	private void setField(Location location, double radius) {
		this.abilityState = AbilityState.HOLDINGWATER;
		this.cooldown = ConfigManager.getConfig().getLong("ExtraAbilities.Hiro3.Water.Sprinkle.IceVolley.Cooldown");
		this.radius = radius;
		this.centerLocation = location.clone();
		this.centerLocation.setY(player.getLocation().getY() + 2);
		this.waterSphereDuration = ConfigManager.getConfig().getLong("ExtraAbilities.Hiro3.Water.Sprinkle.IceVolley.WaterSphereDuration");
		this.iceSphereDuration = ConfigManager.getConfig().getLong("ExtraAbilities.Hiro3.Water.Sprinkle.IceVolley.IceSphereDuration");
		this.iceAmount = ConfigManager.getConfig().getInt("ExtraAbilities.Hiro3.Water.Sprinkle.IceVolley.IceAmount");
		this.iceStartDelay = ConfigManager.getConfig().getLong("ExtraAbilities.Hiro3.Water.Sprinkle.IceVolley.IceStartDelay");
		this.currentIceAmount = 0;
		this.iceAmountIncrease = this.iceAmount / ((this.waterSphereDuration - this.iceStartDelay) / 50.0);
		this.particleAmount = ConfigManager.getConfig().getInt("ExtraAbilities.Hiro3.Water.Sprinkle.IceVolley.WaterParticleAmount");
		this.currentParticleAmount = 0;
		this.particleAmountIncrease = this.particleAmount / (this.waterSphereDuration / 50.0);
		this.iceOptions = new DustOptions(Color.fromRGB(193, 241, 255), 0.5f);
		this.iceLocations = new ArrayList<Location>();
		this.iceStartLocations = new ArrayList<Location>();
		this.iceDirections = new ArrayList<Vector>();
		this.tickPhase = new ArrayList<Integer>();
		this.iceLaunchNoise = ConfigManager.getConfig().getInt("ExtraAbilities.Hiro3.Water.Sprinkle.IceVolley.IceLaunchNoise");
		if (this.iceLaunchNoise > 30) {
			this.iceLaunchNoise = 15;
		}
		this.range = ConfigManager.getConfig().getDouble("ExtraAbilities.Hiro3.Water.Sprinkle.IceVolley.Range");
		this.rangeSquared = this.range * this.range;
		this.speed = ConfigManager.getConfig().getDouble("ExtraAbilities.Hiro3.Water.Sprinkle.IceVolley.Speed");
		this.damage = ConfigManager.getConfig().getDouble("ExtraAbilities.Hiro3.Water.Sprinkle.IceVolley.Damage");
		this.tickCounter = 0;
		this.hitCount = ConfigManager.getConfig().getInt("ExtraAbilities.Hiro3.Water.Sprinkle.IceVolley.MaxHitCount");
		this.hitCounts = new HashMap<LivingEntity, Integer>();
	}

	@SuppressWarnings("deprecation")
	@Override
	public void progress() {
		
		if (!bPlayer.canBendIgnoreBindsCooldowns(this)) {
			remove();
			return;
		}
		
		if (System.currentTimeMillis() > getStartTime() + 30000) {
			return;
		}
		
		if (this.abilityState != AbilityState.LAUNCH && !player.isSneaking()) {
			remove();
			return;
		}
		
		if (this.abilityState == AbilityState.LAUNCH && this.iceLocations.isEmpty()) {
			remove();
			return;
		}
		
		if (this.abilityState == AbilityState.HOLDINGWATER) {
			if (System.currentTimeMillis() > getStartTime() + this.waterSphereDuration) {
				this.abilityState = AbilityState.HOLDINGICE;
				this.iceSphereStartTime = System.currentTimeMillis();
			}
			
			Vector offset;
			Location loc;
			for (int i = 0; i < this.currentParticleAmount - (this.iceLocations.size() / 2.0); i++) {
				offset = Sprinkle.randomPointInSphere().normalize().multiply(this.radius / 1.5 + Math.random() - 0.5);
				loc = this.centerLocation.clone().add(offset);
				if (!GeneralMethods.isSolid(loc.getBlock())) {
					player.getWorld().spawnParticle(Particle.WATER_WAKE, loc, 0);
				}
			}
			
			if (this.currentParticleAmount < this.particleAmount)
				this.currentParticleAmount += this.particleAmountIncrease;
			
			if (System.currentTimeMillis() > getStartTime() + this.iceStartDelay) {
				while (this.iceLocations.size() < (int) this.currentIceAmount) {
					offset = Sprinkle.randomPointInSphere().normalize().multiply(this.radius / 1.5 + Math.random() - 0.5);
					loc = this.centerLocation.clone().add(offset);
					this.iceLocations.add(loc);
					this.iceStartLocations.add(loc.clone());
					this.tickPhase.add((int)(Math.random() * this.iceLaunchNoise));
				}
				
				if (this.currentIceAmount < this.iceAmount)
					this.currentIceAmount += this.iceAmountIncrease;
				
				player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.AQUA + "% " 
						+ String.format("%.2f", 100.0 * this.iceLocations.size() / this.iceAmount)));
			}
			
			for (Location location : this.iceLocations) {
				player.getWorld().spawnParticle(Particle.REDSTONE, location, 0, this.iceOptions);
			}
			
		} else if (this.abilityState == AbilityState.HOLDINGICE) {
			if (System.currentTimeMillis() > this.iceSphereStartTime + this.iceSphereDuration) {
				remove();
				return;
			}
			
			for (Location location : this.iceLocations) {
				player.getWorld().spawnParticle(Particle.REDSTONE, location, 0, this.iceOptions);
			}
		} else if (this.abilityState == AbilityState.LAUNCH) {
			Location loc;
			Vector dir;
			for (int i = this.iceLocations.size() - 1; i >= 0; i--) {
				if (this.tickPhase.get(i) < this.tickCounter) {
					loc = this.iceLocations.get(i);
					dir = this.iceDirections.get(i);
					loc.add(dir);
					
					if (GeneralMethods.isSolid(loc.getBlock()) || this.iceStartLocations.get(i).distanceSquared(loc) > this.rangeSquared) {
						this.iceLocations.remove(i);
						this.iceDirections.remove(i);
						this.iceStartLocations.remove(i);
						this.tickPhase.remove(i);
					} else {
						for (Entity e : GeneralMethods.getEntitiesAroundPoint(loc, 1.5)) {
							if (e instanceof LivingEntity && !e.getUniqueId().equals(player.getUniqueId())) {
								if (!this.hitCounts.containsKey(e)) {
									this.hitCounts.put((LivingEntity) e, 0);
								} 
								
								if (this.hitCounts.get(e) < this.hitCount) {
									this.hitCounts.put((LivingEntity) e, this.hitCounts.get(e) + 1);
									DamageHandler.damageEntity(e, this.damage, this);
									((LivingEntity)e).setMaximumNoDamageTicks(0);
									((LivingEntity)e).setNoDamageTicks(0);
								}
								
								this.iceLocations.remove(i);
								this.iceDirections.remove(i);
								this.iceStartLocations.remove(i);
								this.tickPhase.remove(i);
								break;
							}
						}
					}
				}
				
			}
			for (Location location : this.iceLocations) {
				player.getWorld().spawnParticle(Particle.REDSTONE, location, 0, this.iceOptions);
			}
			tickCounter++;
		}
		
	}
	
	public AbilityState getAbilityState() {
		return this.abilityState;
	}
	
	public void launchIceVolley() {
		if (this.abilityState != AbilityState.HOLDINGICE) {
			//return;
		}
		this.abilityState = AbilityState.LAUNCH;
		this.targetLocation = getTargetLocation(player, this.range);
		Vector v;
		for (Location loc : this.iceLocations) {
			v = this.targetLocation.toVector().subtract(loc.toVector()).normalize().multiply(this.speed);
			this.iceDirections.add(v);
		}
	}
	
	public Location getTargetLocation(Player player, double range) {
		Vector direction = player.getLocation().getDirection().clone().multiply(0.1);
		Location loc = player.getEyeLocation().clone();
		Location startLoc = loc.clone();
		
		do {
			loc.add(direction);
		} while (startLoc.distance(loc) < range && !GeneralMethods.isSolid(loc.getBlock()) && !isThereALivingEntity(loc));
		
		return loc;
	}
	
	public boolean isThereALivingEntity(Location loc) {
		for (Entity e : GeneralMethods.getEntitiesAroundPoint(loc, 1.5)) {
			if (e instanceof LivingEntity && !e.getUniqueId().equals(player.getUniqueId()))
				return true;
		}
		return false;
	}
	
	@Override
	public long getCooldown() {
		return this.cooldown;
	}

	@Override
	public Location getLocation() {
		return null;
	}

	@Override
	public String getName() {
		return "IceVolley";
	}


	@Override
	public boolean isHiddenAbility() {
		return true;
	}
	
	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	@Override
	public boolean isSneakAbility() {
		return true;
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
	public void load() {
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Water.Sprinkle.IceVolley.Cooldown", 3000);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Water.Sprinkle.IceVolley.WaterSphereDuration", 5000);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Water.Sprinkle.IceVolley.WaterParticleAmount", 15);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Water.Sprinkle.IceVolley.IceSphereDuration", 5000);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Water.Sprinkle.IceVolley.IceStartDelay", 2000);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Water.Sprinkle.IceVolley.IceAmount", 30);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Water.Sprinkle.IceVolley.IceLaunchNoise", 15);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Water.Sprinkle.IceVolley.Range", 15);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Water.Sprinkle.IceVolley.Speed", 0.8);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Water.Sprinkle.IceVolley.Damage", 2);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Water.Sprinkle.IceVolley.MaxHitCount", 3);
		ConfigManager.defaultConfig.save();
		
		ProjectKorra.log.info("Succesfully enabled " + getName() + " by " + getAuthor());
	}

	@Override
	public void stop() {
		ProjectKorra.log.info("Successfully disabled " + getName() + " by " + getAuthor());
		super.remove();
	}

}
