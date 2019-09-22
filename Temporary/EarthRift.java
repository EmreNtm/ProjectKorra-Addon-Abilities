package Hiro3;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.earthbending.RaiseEarth;
import com.projectkorra.projectkorra.util.ClickType;

public class EarthRift extends EarthAbility implements AddonAbility, ComboAbility {

	private long cooldown;
	private int amount;
	private int heightRepeat;
	private double range;
	
	private int currentHeight;
	private Block startBlock;
	private Block headBlock;
	private Vector blockDirection;
	private int amountCount;
	
	public EarthRift(Player player) {
		super(player);
		
		if (bPlayer.isOnCooldown(this)) {
			return;
		}
		
		if (!this.bPlayer.canBendIgnoreBinds(this)) {
		      return;
		}
		
		if (hasAbility(player, RaiseEarth.class)) {
			getAbility(player, RaiseEarth.class).getBlock().setType(Material.AIR);
			getAbility(player, RaiseEarth.class).remove();
		}
		
		setField();
		if (findFirstBlock()) {
			bPlayer.addCooldown(this);
			start();
		}
	}
	
	public void setField() {
		cooldown = ConfigManager.getConfig().getLong("ExtraAbilities.Hiro3.Earth.EarthRift.Cooldown");
		amount = ConfigManager.getConfig().getInt("ExtraAbilities.Hiro3.Earth.EarthRift.Amount");
		heightRepeat = ConfigManager.getConfig().getInt("ExtraAbilities.Hiro3.Earth.EarthRift.HeightRepeat");
		if (heightRepeat < 1)
			heightRepeat = 1;
		currentHeight = 1;
		range = 50;
		
		amountCount = 0;
		
		blockDirection = new Vector(0, 0, 0);
	}

	@Override
	public void progress() {

		if (GeneralMethods.isRegionProtectedFromBuild(this, player.getLocation())) {
			remove();
			return;
		}
		
		if (!player.isSneaking()) {
			remove();
			return;
		}
		
		if (amountCount >= amount) {
			remove();
			return;
		}
		
		currentHeight = amountCount / heightRepeat + 1;
		
		Block targetBlock = getTargetEarthBlock(player, range);
		
		if ( (targetBlock != null && !(targetBlock.getX() == headBlock.getX() && targetBlock.getZ() == headBlock.getZ())) || targetBlock == null ) {
			if (targetBlock != null) 
				blockDirection = targetBlock.getLocation().toVector().subtract(headBlock.getLocation().toVector()).normalize();
			else {
				blockDirection = player.getLocation().clone().add(player.getLocation().getDirection().multiply(25)).subtract(headBlock.getLocation()).toVector().normalize();
			}
			blockDirection.setY(0);
			if (blockDirection.length() < 0.1) {
				blockDirection.setX(1);
			}
			Location tmpLoc = headBlock.getLocation().clone();
			
			do {
				tmpLoc.add(blockDirection);
			} while (tmpLoc.getBlock().equals(headBlock));
			
			while (!GeneralMethods.isSolid(tmpLoc.getBlock())) {
				tmpLoc.setY(tmpLoc.getY() - 1);
			}
			
			new RaiseEarth(player, tmpLoc.getBlock().getLocation(), currentHeight);
			amountCount++;
			headBlock = tmpLoc.getBlock();
		}
		
	}
	
	public boolean findFirstBlock() {
		startBlock = getTargetEarthBlock(player, range);
		if (startBlock == null)
			return false;
		headBlock = startBlock;
		new RaiseEarth(player, headBlock.getLocation(), currentHeight);
		amountCount++;
		return true;
	}
	
	public Block getTargetEarthBlock(Player player, double range) {
		Vector direction = player.getLocation().getDirection().clone().multiply(0.01);
		Location loc = player.getEyeLocation().clone();
		Location startLoc = loc.clone();
		
		do {
			loc.add(direction);
		} while (startLoc.distance(loc) < range && !GeneralMethods.isSolid(loc.getBlock()));
		
		if (startLoc.distance(loc) < range && isEarthbendable(loc.getBlock())) {
			return loc.getBlock();
		}
		return null;
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
		return "EarthRift";
	}

	@Override
	public String getDescription() {
		return "Build a wall.";
	}

	@Override
	public String getInstructions() {
		return "Shockwave (Tap Sneak) -> Shockwave (Hold Sneak) -> RaiseEarth (Left Click)";
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
	public boolean isHarmlessAbility() {
		return true;
	}

	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public Object createNewComboInstance(Player player) {
		return new EarthRift(player);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		ArrayList<AbilityInformation> combination = new ArrayList<>();
		combination.add(new AbilityInformation("Shockwave", ClickType.SHIFT_DOWN));
		combination.add(new AbilityInformation("Shockwave", ClickType.SHIFT_UP));
		combination.add(new AbilityInformation("Shockwave", ClickType.SHIFT_DOWN));
		combination.add(new AbilityInformation("RaiseEarth", ClickType.LEFT_CLICK));
		
		return combination;
	}

	@Override
	public void load() {
		ProjectKorra.log.info("Succesfully enabled " + getName() + " by " + getAuthor());
		
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Earth.EarthRift.Cooldown", 5000);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Earth.EarthRift.Amount", 15);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Earth.EarthRift.HeightRepeat", 2);
		ConfigManager.defaultConfig.save();
	}

	@Override
	public void stop() {
		ProjectKorra.log.info("Successfully disabled " + getName() + " by " + getAuthor());
		super.remove();
	}

}
