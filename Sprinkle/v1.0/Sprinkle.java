package me.hiro3.sprinkle;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.waterbending.WaterManipulation;

import net.md_5.bungee.api.ChatColor;

public class Sprinkle extends WaterAbility implements AddonAbility, ComboAbility {

	private Listener sprinkleListener;
	
	public enum AbilityState {
		SPRINKLE,
		USED
	}
	
	private AbilityState abilityState;
	
	private long cooldown;
	private double searchRadius;
	private long sprinkleDuration;
	private long sprinkleStartDelay;
	private double sprinkleRadius;
	
	private double searchRangeSquared;
	private Location sprinkleStartLocation;
	private long sprinkleStartTime;
	private double currentSprinkleRadius;
	private double sprinkleRadiusIncreseAmount;
	private double sprinkleRadiusSquared;
	
	public Sprinkle(Player player) {
		super(player);
		
		if (!bPlayer.canBendIgnoreBinds(this)) {
			return;
		}
		
		setField();
		
		if (!isReady()) {
			return;
		}
		
		this.abilityState = AbilityState.SPRINKLE;
		bPlayer.addCooldown(this);
		start();
	}
	
	private void setField() {	
		this.searchRadius = 5;
		this.cooldown = ConfigManager.getConfig().getLong("ExtraAbilities.Hiro3.Water.Sprinkle.Cooldown");
		double searchRange = ConfigManager.getConfig().getDouble("ExtraAbilities.Hiro3.Water.Sprinkle.SearchRange");
		this.searchRangeSquared = searchRange * searchRange;
		this.sprinkleDuration = ConfigManager.getConfig().getLong("ExtraAbilities.Hiro3.Water.Sprinkle.Duration");
		this.sprinkleStartDelay = 250;
		this.sprinkleRadius = ConfigManager.getConfig().getDouble("ExtraAbilities.Hiro3.Water.Sprinkle.Radius");
		this.sprinkleRadiusSquared = this.sprinkleRadius * this.sprinkleRadius;
		this.currentSprinkleRadius = 1;
		this.sprinkleRadiusIncreseAmount = (this.sprinkleRadius - 1) / (this.sprinkleDuration / 50.0);
	}

	@Override
	public void progress() {
		
		if (!bPlayer.canBendIgnoreBindsCooldowns(this)) {
			return;
		}
		
		if (System.currentTimeMillis() > this.sprinkleStartTime + this.sprinkleStartDelay) {
			if (System.currentTimeMillis() > getStartTime() + this.sprinkleStartDelay + this.sprinkleDuration) {
				remove();
				return;
			}
			
			Location loc;
			Vector offset;
			for (int i = 0; i < 10; i++) {
				offset = randomPointInSphere().multiply(this.currentSprinkleRadius);
				offset.setY(-Math.random() * 3 + 1 - 3 * Math.max(Math.abs(offset.getX()), Math.abs(offset.getZ())) / this.sprinkleRadius);
				loc = this.sprinkleStartLocation.clone().add(offset);
				player.getWorld().spawnParticle(Particle.FALLING_WATER, loc, 0);
			}
			if (this.currentSprinkleRadius < this.sprinkleRadius - 0.001)
				this.currentSprinkleRadius += this.sprinkleRadiusIncreseAmount;
		}
		
	}
	
	private boolean isReady() {
		Vector direction = player.getLocation().getDirection();
		Location startLoc = player.getEyeLocation();
		Location loc = startLoc.clone();
		Block startingBlock = getSprinkableBlock(loc, this.searchRadius);
		
		while (startingBlock == null && loc.distanceSquared(startLoc) < this.searchRangeSquared) {
			loc.add(direction);
			startingBlock = getSprinkableBlock(loc, this.searchRadius);
		}
		
		if (loc.distanceSquared(startLoc) < this.searchRangeSquared) {
			this.sprinkleStartLocation = startingBlock.getLocation().add(0.5, 0.5, 0.5);
			this.sprinkleStartTime = System.currentTimeMillis();
			Vector offset;
			for (int i = 0; i < 10; i++) {
				offset = randomPointInSphere().multiply(1);
				offset.setY(-Math.random() * 3 + 1 - 2 * Math.max(Math.abs(offset.getX()), Math.abs(offset.getZ())) / this.sprinkleRadius);
				loc = this.sprinkleStartLocation.clone().add(offset);
				player.getWorld().spawnParticle(Particle.WATER_SPLASH, loc, 2);
			}

			return true;
		}
		
		return false;
	}
	
	private Block getSprinkableBlock(Location loc, double radius) {
		for (Block b : GeneralMethods.getBlocksAroundPoint(loc, radius)) {
			if (WaterAbility.isWater(b)) {
				for (WaterManipulation wm : getAbilities(WaterManipulation.class)) {
					if (wm.isProgressing() && wm.getLocation().getBlock().equals(b)) {
						wm.remove();
						return b;
					}
				}
			}
		}
		
		return null;
	}
	
	public static Vector randomPointInSphere() {
	    double u = Math.random();
	    double v = Math.random();
	    double theta = 2 * Math.PI * u;
	    double phi = Math.acos(2 * v - 1);
	    double x = (Math.sin(phi) * Math.cos(theta));
	    double y = (Math.sin(phi) * Math.sin(theta));
	    double z = (Math.cos(phi));
	    if (x == 0 && y == 0 && z == 0) {
	    	return new Vector (0, 1, 0);
	    }
	    return new Vector(x, y, z);
	}
	
	public void startIceVolley(Player player) {
		new IceVolley(player, this.sprinkleStartLocation, this.sprinkleRadius);
		this.abilityState = AbilityState.USED;
	}
	
	public boolean canUseIceVolley(Player player) {
		if (this.abilityState == AbilityState.USED) {
			return false;
		}
		
		Location playerLoc = player.getLocation();
		Location sprinkleLoc = this.sprinkleStartLocation.clone();
		
		if (sprinkleLoc.toVector().setY(0).distanceSquared(playerLoc.toVector().setY(0)) <= this.sprinkleRadiusSquared
				&& playerLoc.getY() < sprinkleLoc.getY()) {
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
		return "Sprinkle";
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
		return new Sprinkle(player);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		ArrayList<AbilityInformation> combination = new ArrayList<>();
		combination.add(new AbilityInformation("PhaseChange", ClickType.SHIFT_DOWN));
		combination.add(new AbilityInformation("PhaseChange", ClickType.LEFT_CLICK));
		combination.add(new AbilityInformation("PhaseChange", ClickType.SHIFT_UP));

		return combination;
	}

	@Override
	public String getDescription() {
		return ChatColor.DARK_AQUA + "Sprinkle: " + ChatColor.AQUA + "Split 'WaterManipulation' into small rain drops.\n"
				+ ChatColor.DARK_AQUA +  "IceVolley: " + ChatColor.AQUA + " Bend the sprinkle around you to create a water sphere. Then turn the rain drops"
						+ " into ice spikes to throw.\n";
	}

	@Override
	public String getInstructions() {
		return ChatColor.DARK_AQUA + "\nSprinkle: " + ChatColor.AQUA + "PhaseChange (Hold Sneak) -> PhaseChange (Left Click) -> PhaseChange (Aim Towards 'WaterManipulation' and Release Sneak)\n"
				+ ChatColor.DARK_AQUA + "IceVolley: " + ChatColor.AQUA + " Under a sprinkle, hold sneak with IceBlast to form a water sphere. Left click to launch an IceVolley.\n";
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
		sprinkleListener = new SprinkleListener();
		ProjectKorra.plugin.getServer().getPluginManager().registerEvents(sprinkleListener, ProjectKorra.plugin);
		
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Water.Sprinkle.Cooldown", 1000);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Water.Sprinkle.SearchRange", 20);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Water.Sprinkle.Duration", 2000);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Water.Sprinkle.Radius", 5);
		ConfigManager.defaultConfig.save();
		
		ProjectKorra.log.info("Succesfully enabled " + getName() + " by " + getAuthor());
	}

	@Override
	public void stop() {
		ProjectKorra.log.info("Successfully disabled " + getName() + " by " + getAuthor());
		super.remove();
	}

}
