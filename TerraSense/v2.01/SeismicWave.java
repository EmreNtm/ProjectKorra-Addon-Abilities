package me.hiro3.terrasense;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.ClickType;

public class SeismicWave extends EarthAbility implements AddonAbility, ComboAbility {
	
	private ArrayList<ArrayList<Location>> locations;
	private ArrayList<ArrayList<Vector>> directions;
	
	private long cooldown;
	private int range;
	private int amount;
	private int tickDelay;
	private int angleBetweenPoints;
	private int tick;
	
	public SeismicWave(Player player) {
		super(player);
		
		if (bPlayer.isOnCooldown(this)) {
			return;
		} else if (!bPlayer.canBendIgnoreBindsCooldowns(this)) {
			return;
		}
		
		if (player.getPotionEffect(PotionEffectType.BLINDNESS) == null) {
			player.sendMessage(ChatColor.DARK_GREEN + "Close your eyes using EarthPulse first!");
			return;
		}
		 
		if (CoreAbility.hasAbility(player, TerraSense.class)
				&& !CoreAbility.getAbility(player, TerraSense.class).canSense()) {
			return;
		}
		
		setField();
		bPlayer.addCooldown(this);
		start();
	}

	public void setField() {
		locations = new ArrayList<ArrayList<Location>>();
		directions = new ArrayList<ArrayList<Vector>>();
		
		Vector startingVector = new Vector(1, 0, 0);
		Location startingLocation = player.getLocation().add(0, -1, 0);
		
		amount = ConfigManager.getConfig().getInt("ExtraAbilities.Hiro3.Earth.SeismicWave.WaveAmount");
		tickDelay = (int) 10;
		angleBetweenPoints = ConfigManager.getConfig().getInt("ExtraAbilities.Hiro3.Earth.SeismicWave.AngleBetweenPoints");
		
		for (int j = 0; j < amount; j++) {
			ArrayList<Location> l = new ArrayList<Location>();
			ArrayList<Vector> d = new ArrayList<Vector>();
			for (int i = 0 + j*(angleBetweenPoints/2); i < 360 + j*(angleBetweenPoints/2); i += angleBetweenPoints) {
				l.add(startingLocation.clone());
				d.add(UtilityMethods.rotateVectorAroundY(startingVector, i));
			}
			locations.add(l);
			directions.add(d);
		}
		
		cooldown = ConfigManager.getConfig().getLong("ExtraAbilities.Hiro3.Earth.SeismicWave.Cooldown");
		if (CoreAbility.hasAbility(player, TerraSense.class))
			range = (int) CoreAbility.getAbility(player, TerraSense.class).getBlindRadius();
		else
			range = 0;
		tick = 0;
	}
	
	@Override
	public void progress() {
		
		if (tick >= range) {
			if (locations.isEmpty()) {
				remove();
				return;
			} else {
				locations.remove(0);
				tick -= tickDelay;
			}
		}
		
		for (int j = 0; j < ((tick/tickDelay + 1) > locations.size() ? locations.size() : (tick/tickDelay) + 1); j++) {
			ArrayList<Location> l = locations.get(j);
			ArrayList<Vector> d = directions.get(j);
			for (int i = 0; i < l.size(); i++) {
				if (EarthAbility.isEarthbendable(player, l.get(i).getBlock())) {
					UtilityMethods.sendGlowingBlock(player, l.get(i), 1);
				}
				if (EarthAbility.isEarthbendable(player, l.get(i).getBlock().getRelative(BlockFace.UP))) {
					l.get(i).add(0, 1, 0);
				} else {
					l.get(i).add(d.get(i));
					if (EarthAbility.isEarthbendable(player, l.get(i).getBlock().getRelative(BlockFace.UP))) {
						l.get(i).add(0, 1, 0);
					} else {
						l.set(i, getFloor(l.get(i)));
					}
				}
			}
		}
		
		tick++;
	}
	
	public Location getFloor(Location loc) {
		Location f = loc.clone();
		while (!EarthAbility.isEarthbendable(player, loc.getBlock()) && loc.distance(f) < 30) {
			loc.add(0, -1, 0);
		}
		return loc;
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
		return "SeismicWave";
	}

	@Override
	public boolean isHarmlessAbility() {
		return true;
	}

	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public Object createNewComboInstance(Player player) {
		return new SeismicWave(player);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		ArrayList<AbilityInformation> combination = new ArrayList<>();
		combination.add(new AbilityInformation("EarthPulse", ClickType.SHIFT_DOWN));
		combination.add(new AbilityInformation("EarthPulse", ClickType.RIGHT_CLICK_BLOCK));

		return combination;
	}

	@Override
	public String getDescription() {
		return "Send a seismic wave that travels on the surface to see the terrain.";
	}
	
	@Override
	public String getInstructions() {
		return "Become blind -> EarthPulse (Hold Sneak) -> EarthPulse (Right Click on Ground)";
	}
	
	@Override
	public String getAuthor() {
		return "Hiro3";
	}

	@Override
	public String getVersion() {
		return "2.01 (REDONE)";
	}

	@Override
	public void load() {
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Earth.SeismicWave.WaveAmount", 3);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Earth.SeismicWave.AngleBetweenPoints", 10);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Earth.SeismicWave.Cooldown", 5000);
		ConfigManager.defaultConfig.save();
		
		ProjectKorra.log.info("Succesfully enabled " + getName() + " by " + getAuthor());
	}

	@Override
	public void stop() {
		ProjectKorra.log.info("Successfully disabled " + getName() + " by " + getAuthor());
		super.remove();
	}

}