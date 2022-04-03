package me.hiro3.plantwalk;

import java.util.ArrayList;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
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
	
	private float firstFlySpeed;
	private float flySpeed;
	
	private DustOptions dustOptions;
	private double length;
	private int segmentAmount;
	private int segmentParticleAmount;
	private double segmentLength;
	private double lengthSquared;
	private boolean soundFlag;
	
	public enum AbilityState {
		STARTED,
		GROWING,
		GROWN
	}
	private AbilityState abilityState;
	
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
			this.sourceLocation = getTargetPlantLocation(player, this.selectRange);
			this.tentacle = new Tentacle(this.sourceLocation, this.segmentLength, 1, this.segmentParticleAmount, this.dustOptions);
			this.tentacle.setLength(0.1);
			this.tentacle.setAttached(true);
			this.abilityState = AbilityState.GROWING;
			start();
		}
	}

	public void setField() {
		this.cooldown = ConfigManager.getConfig().getLong("ExtraAbilities.Hiro3.Water.PlantWalk.Cooldown");
		this.selectRange = ConfigManager.getConfig().getDouble("ExtraAbilities.Hiro3.Water.PlantWalk.SourceSelectRange");
		this.duration = ConfigManager.getConfig().getLong("ExtraAbilities.Hiro3.Water.PlantWalk.Duration");
		this.firstFlySpeed = player.getFlySpeed();
		this.flySpeed = (float) ConfigManager.getConfig().getDouble("ExtraAbilities.Hiro3.Water.PlantWalk.FlySpeed");
		
		this.dustOptions = new DustOptions(Color.fromRGB(57, 138, 44), 0.5f);
		this.length = ConfigManager.getConfig().getDouble("ExtraAbilities.Hiro3.Water.PlantWalk.TentacleLength");
		this.length = this.length < 1 ? 1 : this.length;
		this.segmentAmount = ConfigManager.getConfig().getInt("ExtraAbilities.Hiro3.Water.PlantWalk.SegmentAmount");
		this.segmentParticleAmount = ConfigManager.getConfig().getInt("ExtraAbilities.Hiro3.Water.PlantWalk.SegmentParticleAmount");
		this.segmentAmount = this.segmentAmount < 1 ? 1 : this.segmentAmount;
		this.segmentLength = this.length / this.segmentAmount;
		this.lengthSquared = this.length * this.length;
		this.soundFlag = false;
		
		this.abilityState = AbilityState.STARTED;
	}
	
	public AbilityState getAbilityState() {
		return this.abilityState;
	}
	
	@Override
	public void progress() {
		
		if (System.currentTimeMillis() > getStartTime() + duration) {
			remove();
			return;
		}
		
		if (this.abilityState == AbilityState.GROWING) {
			if (this.tentacle.getLength() < this.length) {
				this.tentacle.setLength( (this.tentacle.getLength() + 1) < this.length ? (this.tentacle.getLength() + 1) : this.length );
				this.tentacle.reach(player.getLocation().add(0, 0.5, 0));
				this.tentacle.display();
				
				if (this.soundFlag == false && this.tentacle.getLastLocation().distanceSquared(player.getLocation().add(0, 0.5, 0)) < 4) {
					player.getWorld().playSound(player.getLocation().add(0, 0.5, 0), Sound.ENTITY_PLAYER_ATTACK_CRIT, 2, 0);
					this.soundFlag = true;
				}
			} else {
				this.flightHandler.createInstance(player, "PlantWalk_" + player.getName());
				allowFlight();
				this.abilityState = AbilityState.GROWN;
			}
		} else if (this.abilityState == AbilityState.GROWN) {
			this.tentacle.reach(player.getLocation().add(0, 0.5, 0));
			this.tentacle.display();
			allowFlight();
			if (player.getLocation().add(0, 0.5, 0).distanceSquared(sourceLocation) > this.lengthSquared + 2 * this.length + 1) {
				if (player.getLocation().add(0, 0.5, 0).distanceSquared(sourceLocation) > this.lengthSquared + 10 * this.length + 25) {
					remove();
					return;
				}
				Vector tmp = sourceLocation.clone().toVector().subtract(player.getLocation().add(0, 0.5, 0).toVector()).normalize().multiply(0.5);
				player.setVelocity(tmp);
			}
		}
		
	}
	
	private void allowFlight() {
		if (!this.player.getAllowFlight()) {
			this.player.setAllowFlight(true);
		}
		if (!this.player.isFlying()) {
			this.player.setFlying(true);
		}
		if (this.player.getFlySpeed() != this.flySpeed) {
			this.player.setFlySpeed(flySpeed);
		}
	}

	private void removeFlight() {
		if (this.player.isFlying()) {
			this.player.setFlying(false);
		}
		if (this.player.getAllowFlight()) {
			this.player.setAllowFlight(false);
		}
		if (this.player.getFlySpeed() != this.firstFlySpeed) {
			this.player.setFlySpeed(firstFlySpeed);
		}
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
		return "4.0";
	}
	
	@Override
	public void remove() {
		super.remove();
		this.bPlayer.addCooldown(this);
		removeFlight();
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
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Water.PlantWalk.FlySpeed", 0.1);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Water.PlantWalk.SegmentAmount", 180);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Water.PlantWalk.SegmentParticleAmount", 1);
		ConfigManager.defaultConfig.save();
	}

	@Override
	public void stop() {
		ProjectKorra.log.info("Successfully disabled " + getName() + " by " + getAuthor());
		HandlerList.unregisterAll(PWL);
		super.remove();
	}

}