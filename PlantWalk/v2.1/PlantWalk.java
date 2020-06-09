package com.plantwalk.Hiro3;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.PlantAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.ClickType;

public class PlantWalk extends PlantAbility implements AddonAbility, ComboAbility {

	private Listener PWL;
	
	private long cooldown;
	private double selectRange;
	private long duration;
	
	private Location sourceLocation;
	
	private Tentacle tentacle;
	
	private int abilityState;
	
	public PlantWalk(Player player) {
		super(player);
		
		if (bPlayer.isOnCooldown(this)) {
			return;
		}
		
		if (!bPlayer.canBendIgnoreBindsCooldowns(this)) {
			return;
		}
		
		if (hasAbility(player, PlantWalk.class)) {
			return;
		}
		
		setField();
		if (isPlant(getTargetPlantLocation(player, selectRange).getBlock())) {
			sourceLocation = getTargetPlantLocation(player, selectRange);
			start();
		}
	}

	public void setField() {
		cooldown = ConfigManager.getConfig().getLong("ExtraAbilities.Hiro3.Water.PlantWalk.Cooldown");
		selectRange = ConfigManager.getConfig().getDouble("ExtraAbilities.Hiro3.Water.PlantWalk.SourceSelectRange");
		duration = ConfigManager.getConfig().getLong("ExtraAbilities.Hiro3.Water.PlantWalk.Duration");
		
		abilityState = 0;
	}
	
	@Override
	public void progress() {
		
		if (System.currentTimeMillis() > getStartTime() + duration) {
			remove();
			return;
		}
		
		if (abilityState == 0) {
			this.tentacle = new Tentacle(player, sourceLocation);
			createTentacleSlowly(tentacle);
			abilityState++;
		} else if (abilityState == 1) {
			tentacle.follow(player.getLocation().add(0, 0.5, 0));
		} else if (abilityState == 2) {
			tentacle.follow(player.getLocation().add(0, 0.5, 0));
			if (player.getLocation().add(0, 0.5, 0).distance(sourceLocation) > tentacle.getLength()) {
				if (player.getLocation().add(0, 0.5, 0).distance(sourceLocation) > tentacle.getLength() + 5) {
					remove();
					return;
				}
				Vector tmp = sourceLocation.clone().toVector().subtract(player.getLocation().add(0, 0.5, 0).toVector()).normalize().multiply(0.5);
				player.setVelocity(tmp);
			}
		}
		
	}
	
	public void createTentacleSlowly(Tentacle tentacle) {
		
		new BukkitRunnable() {

			int flag = 0;
			
			@Override
			public void run() {
				if (tentacle.getSize() < tentacle.getMaxSegmentAmount()) {
					tentacle.addNewSegment();
					if (flag == 0 && tentacle.getLastLocation().distance(player.getLocation().add(0, 0.5, 0)) < 2) {
						player.getWorld().playSound(player.getLocation().add(0, 0.5, 0), Sound.ENTITY_PLAYER_ATTACK_CRIT, 2, 0);
						flag++;
					}
				} else {
					Bukkit.getScheduler().cancelTask(getTaskId());
					flightHandler.createInstance(player, "PlantWalk_" + player.getName());
					player.setAllowFlight(true);
					player.setFlying(true);
					abilityState++;
				}
			}
			
		}.runTaskTimer(ProjectKorra.plugin, 0, 1);
		
	}
	
	public Location getTargetPlantLocation(Player player, double range) {
		Vector direction = player.getLocation().getDirection().clone().multiply(0.1);
		Location loc = player.getEyeLocation().clone();
		Location startLoc = loc.clone();
		
		do {
			loc.add(direction);
		} while (startLoc.distance(loc) < range && !PlantAbility.isPlant(loc.getBlock()) && !GeneralMethods.isSolid(loc.getBlock()));
		
		return loc;
	}
	
	public Location getTargetLocation(Player player, double range) {
		Vector direction = player.getLocation().getDirection().clone().multiply(0.1);
		Location loc = player.getEyeLocation().clone();
		Location startLoc = loc.clone();
		
		do {
			loc.add(direction);
		} while (startLoc.distance(loc) < range && !GeneralMethods.isSolid(loc.getBlock()));
		
		return loc;
	}
	
	public int getAbilityState() {
		return this.abilityState;
	}
	
	public Location getSourceLocation() {
		return this.sourceLocation;
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
		return "PlantWalk";
	}
	
	@Override
	public String getDescription() {
		return "Grab yourself with the plant you control and walk on air freely.";
	}

	@Override
	public String getInstructions() {
		return "Torrent (Tap Sneak) > Torrent (Tap Sneak) > WaterSpout (Tap Sneak)\nRight click near the source block to remove PlantWalk.";
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
	public Object createNewComboInstance(Player player) {
		return new PlantWalk(player);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		ArrayList<AbilityInformation> combination = new ArrayList<>();
		combination.add(new AbilityInformation("Torrent", ClickType.SHIFT_DOWN));
		combination.add(new AbilityInformation("Torrent", ClickType.SHIFT_UP));
		combination.add(new AbilityInformation("Torrent", ClickType.SHIFT_DOWN));
		combination.add(new AbilityInformation("Torrent", ClickType.SHIFT_UP));
		combination.add(new AbilityInformation("WaterSpout", ClickType.SHIFT_DOWN));

		return combination;
	}

	@Override
	public String getAuthor() {
		return "Hiro3";
	}

	@Override
	public String getVersion() {
		return "2.1";
	}
	
	@Override
	public void remove() {
		super.remove();
		this.bPlayer.addCooldown(this);
		flightHandler.removeInstance(player, "PlantWalk_" + player.getName());
	}

	@Override
	public void load() {
		PWL = new PlantWalkListener();
		ProjectKorra.plugin.getServer().getPluginManager().registerEvents(PWL, ProjectKorra.plugin);
		ProjectKorra.log.info("Succesfully enabled " + getName() + " by " + getAuthor());
		
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Water.PlantWalk.Cooldown", 4000);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Water.PlantWalk.SourceSelectRange", 15);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Water.PlantWalk.Duration", 20000);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Water.PlantWalk.TentacleLength", 18);
		ConfigManager.defaultConfig.save();
	}

	@Override
	public void stop() {
		ProjectKorra.log.info("Successfully disabled " + getName() + " by " + getAuthor());
		HandlerList.unregisterAll(PWL);
		super.remove();
	}

}