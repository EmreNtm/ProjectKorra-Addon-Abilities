package Hiro3;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.ParticleEffect;

import me.xnuminousx.spirits.ability.api.LightAbility;
import net.md_5.bungee.api.ChatColor;

public class Revive extends LightAbility implements AddonAbility {

	private long cooldown;
	private long duration;
	private int buffDuration;
	private int buffPower;
	
	private int state;
	
	private Location projectileStartLoc;
	private Location projectileLoc;
	private Vector projectileDir;
	private double projectileSpeed;
	private double projectileRange;
	private double projectileHitRadius;
	private long projectileHitTime;
	
	private Player target;
	
	private boolean isDirectable;
	private boolean startRevenge;
	
	private int phase;
	
	private Listener RL;
	
	public Revive(Player player) {
		super(player);
		
		if (bPlayer.isOnCooldown(this)) {
			return;
		}

		if (!this.bPlayer.canBendIgnoreBinds(this)) {
		      return;
		}
		
		if (hasAbility(player, Revive.class)) {
			return;
		}
		
		setField();
		
		start();
	}

	public void setField() {
		//34eb71 first green version
		cooldown = ConfigManager.getConfig().getLong("ExtraAbilities.Hiro3.Spirit.LightSpirit.Revive.Cooldown");
		duration = ConfigManager.getConfig().getLong("ExtraAbilities.Hiro3.Spirit.LightSpirit.Revive.Duration");
		buffDuration = ConfigManager.getConfig().getInt("ExtraAbilities.Hiro3.Spirit.LightSpirit.Revive.BuffDuration");
		buffPower = ConfigManager.getConfig().getInt("ExtraAbilities.Hiro3.Spirit.LightSpirit.Revive.BuffPower");
		
		state = 0;
		phase = 0;
		
		projectileLoc = player.getLocation().clone().add(0, 1.25, 0);
		projectileStartLoc = projectileLoc.clone();
		projectileDir = player.getLocation().getDirection().clone();
		projectileSpeed = 1;
		projectileRange = 12;
		projectileHitRadius = 2;
		
		isDirectable = ConfigManager.getConfig().getBoolean("ExtraAbilities.Hiro3.Spirit.LightSpirit.Revive.isDirectable");
		startRevenge = false;
		
		target = null;
		
		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_EVOCATION_ILLAGER_CAST_SPELL, 1, 10);
	}
	
	@Override
	public void progress() {

		if (player.isDead() || !player.isOnline()) {
			remove();
			return;
		}
		
		if (state == 0) {
			
			if (projectileStartLoc.distance(projectileLoc) > projectileRange) {
				bPlayer.addCooldown(this);
				remove();
				return;
			}
			
			if (GeneralMethods.isSolid(projectileLoc.getBlock())) {
				bPlayer.addCooldown(this);
				remove();
				return;
			}
			
			if (isDirectable)
				projectileDir = player.getLocation().getDirection().clone();
			projectileLoc.add(projectileDir.multiply(projectileSpeed));
			GeneralMethods.displayColoredParticle(projectileLoc, "00D639", 0, 0, 0);
			Vector tmpVec = projectileDir.clone().multiply(0.5);
			projectileLoc.add(tmpVec);
			GeneralMethods.displayColoredParticle(projectileLoc, "00D639", 0, 0, 0);
			projectileLoc.subtract(tmpVec);
			
			for (Entity e : GeneralMethods.getEntitiesAroundPoint(projectileLoc, projectileHitRadius)) {
				if (e instanceof Player && !e.getUniqueId().equals(player.getUniqueId())) {
					target = ((Player) e);
					projectileHitTime = System.currentTimeMillis();
					state = 1;
				}
			}
			
			if (target != null) {
				target.sendMessage(ChatColor.AQUA + "Light Spirit " + ChatColor.GREEN + player.getName() + ChatColor.AQUA + " protects you from the death.");
				bPlayer.addCooldown(this);
				player.getWorld().playSound(target.getLocation(), Sound.ENTITY_ELDER_GUARDIAN_CURSE, 1, 10);
			}
			
		} else if (state == 1) {
			
			if (System.currentTimeMillis() > projectileHitTime + duration) {
				target.sendMessage(ChatColor.AQUA + "Protection of " + ChatColor.GREEN + player.getName() + ChatColor.AQUA + " is over!");
				player.getWorld().playSound(target.getLocation(), Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED, 1, 10);
				protectionIsOver(target, 0.8);
				remove();
				return;
			}
			
			if (target.isDead() || !target.isOnline()) {
				remove();
				return;
			}
			
			deathAwaits(target, 0.8, phase);
			phase += 2;
			
			if (startRevenge) {
				state = 2;
			}
			
		} else if (state == 2) {
			
			if (target.isDead() || !target.isOnline()) {
				remove();
				return;
			}
			
			ArrayList<PotionEffect> potions = new ArrayList<PotionEffect>(0);
			potions.add(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0));
			potions.add(new PotionEffect(PotionEffectType.SPEED, buffDuration, buffPower));
			potions.add(new PotionEffect(PotionEffectType.NIGHT_VISION, buffDuration, buffPower));
			potions.add(new PotionEffect(PotionEffectType.REGENERATION, buffDuration, buffPower));
			potions.add(new PotionEffect(PotionEffectType.JUMP, buffDuration, buffPower));
			potions.add(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, buffDuration, buffPower));
			target.addPotionEffects(potions);
			player.getWorld().playSound(target.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1, 10);
			
			target.sendMessage(ChatColor.BOLD + "You came back from grave to avenge!");
			
			new BukkitRunnable() {
				Player t = target;
				long time = System.currentTimeMillis();
				@Override
				public void run() {
					if (t.isDead() || !t.isOnline() || System.currentTimeMillis() > time + buffDuration*50) {
						Bukkit.getScheduler().cancelTask(getTaskId());
					} else {
						revenge(target, 0.8, phase);
						phase += 2;
					}
				}
			}.runTaskTimer(ProjectKorra.plugin, 0, 1);
			
			remove();
			return;
		}
		
	}
	
	public void deathAwaits(Player p, double r, int phase) {
		Location loc;
		
		for (int i = phase; i < 360 + phase; i+=40) {
			double angle = Math.toRadians(i);
			loc = p.getLocation().clone().add(0, 1, 0);
			loc.setX(loc.getX() + r * Math.cos(angle));
			loc.setZ(loc.getZ() + r * Math.sin(angle));
			GeneralMethods.displayColoredParticle(loc, "00D639", 0, 0, 0);
		}
	}
	
	public void revenge(Player p, double r, int phase) {
		Location loc;

		for (int i = phase; i < 360 + phase; i+=40) {
			double angle = Math.toRadians(i);
			loc = p.getLocation().clone().add(0, 1, 0);
			loc.setX(loc.getX() + r * Math.cos(angle));
			loc.setZ(loc.getZ() + r * Math.sin(angle));
			GeneralMethods.displayColoredParticle(loc, "00D639", 0, 0, 0);
		}
		
		for (int i = -phase; i < 360 - phase; i+=40) {
			double angle = Math.toRadians(i);
			loc = p.getLocation().clone().add(0, 0.7, 0);
			loc.setX(loc.getX() + r * Math.cos(angle) * 4/5);
			loc.setZ(loc.getZ() + r * Math.sin(angle) * 4/5);
			GeneralMethods.displayColoredParticle(loc, "00D639", 0, 0, 0);
		}
		
		for (int i = -phase; i < 360 - phase; i+=40) {
			double angle = Math.toRadians(i);
			loc = p.getLocation().clone().add(0, 1.3, 0);
			loc.setX(loc.getX() + r * Math.cos(angle) * 4/5);
			loc.setZ(loc.getZ() + r * Math.sin(angle) * 4/5);
			GeneralMethods.displayColoredParticle(loc, "00D639", 0, 0, 0);
		}
	}
	
	public void protectionIsOver(Player p, double r) {
		Location loc;

		for (int i = 0; i < 360; i+=40) {
			double angle = Math.toRadians(i);
			loc = p.getLocation().clone().add(0, 1, 0);
			loc.setX(loc.getX() + r * Math.cos(angle));
			loc.setZ(loc.getZ() + r * Math.sin(angle));
			ParticleEffect.FALLING_DUST.display(new ParticleEffect.BlockData(Material.EMERALD_BLOCK, (byte)2), 0.2f, 0.2f, 0.2f, 0.2f, 15, loc, 208);
		}
	}
	
	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public Location getLocation() {
		return null;
	}

	@Override
	public String getName() {
		return "Revive";
	}

	@Override
	public boolean isExplosiveAbility() {
		return false;
	}

	@Override
	public boolean isHarmlessAbility() {
		return true;
	}

	@Override
	public boolean isIgniteAbility() {
		return false;
	}

	@Override
	public boolean isSneakAbility() {
		return false;
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
	public String getDescription() {
		return "Awake the vengeful spirit in your target so it can come back from death to get it's revenge.";
	}

	@Override
	public String getInstructions() {
		return "Hold Shift";
	}
	
	public void setStartRevenge(boolean b) {
		startRevenge = b;
	}
	
	public Player getTarget() {
		return target;
	}
	
	@Override
	public void load() {
		RL = new ReviveListener();
		ProjectKorra.plugin.getServer().getPluginManager().registerEvents(RL, ProjectKorra.plugin);
		ProjectKorra.log.info("Succesfully enabled " + getName() + " by " + getAuthor());
		
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Spirit.LightSpirit.Revive.Duration", 15000);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Spirit.LightSpirit.Revive.Cooldown", 10000);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Spirit.LightSpirit.Revive.BuffDuration", 200);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Spirit.LightSpirit.Revive.BuffPower", 1);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Spirit.LightSpirit.Revive.isDirectable", true);
		ConfigManager.defaultConfig.save();
	}

	@Override
	public void stop() {
		ProjectKorra.log.info("Successfully disabled " + getName() + " by " + getAuthor());
		HandlerList.unregisterAll(RL);
		super.remove();
	}

}
