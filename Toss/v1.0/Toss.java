package Hiro3;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
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

public class Toss extends EarthAbility implements AddonAbility, ComboAbility {

	private long cooldown;
	private long radius;
	private int range;
	private boolean isLimited;
	private int maxEntity;
	
	private ArrayList<LivingEntity> targets;
	private Location targetLoc;
	
	public Toss(Player player) {
		super(player);
		
		if (bPlayer.isOnCooldown(this)) {
			return;
		}
		
		if (!this.bPlayer.canBendIgnoreBinds(this)) {
		      return;
		}
		
		setField();
		findTargets(radius);
		if (!targets.isEmpty()) {
			findTargetLocation();
			bPlayer.addCooldown(this);
			start();
		} else {
			player.sendMessage(ChatColor.GREEN + "There is no one around you standing on earth!");
		}
	}

	public void setField() {
		cooldown = ConfigManager.getConfig().getLong("ExtraAbilities.Hiro3.Earth.Toss.Cooldown");
		radius = ConfigManager.getConfig().getLong("ExtraAbilities.Hiro3.Earth.Toss.EntitySelectRadius");
		range = ConfigManager.getConfig().getInt("ExtraAbilities.Hiro3.Earth.Toss.BlockSelectRange");
		isLimited = ConfigManager.getConfig().getBoolean("ExtraAbilities.Hiro3.Earth.Toss.LimitedAmountOfEntities.isEnabled");
		maxEntity = ConfigManager.getConfig().getInt("ExtraAbilities.Hiro3.Earth.Toss.LimitedAmountOfEntities.MaxEntityNumber");
		
		targets = new ArrayList<LivingEntity>(0);
		targetLoc = null;
	}
	
	@Override
	public void progress() {
		
		if (GeneralMethods.isRegionProtectedFromBuild(this, player.getLocation())) {
			remove();
			return;
		}
		
		launch();
		remove();
	}
	
	public void launch() {
		for (LivingEntity target : targets) {
			Vector tmpVector = targetLoc.clone().toVector().subtract(target.getLocation().toVector());
			target.setVelocity(new Vector( tmpVector.getX(), 
					(tmpVector.getY() < 4) ? 4 : tmpVector.getY() + 1.5, 
					tmpVector.getZ()).multiply(0.175));
			shape(target, target.getVelocity());
		}
	}
	
	public void shape(LivingEntity entity, Vector velocity) {
		Vector reverseVelocity = velocity.clone().setY(0).normalize().multiply(-1);
		new RaiseEarth(player, entity.getLocation().add(0, -1, 0).add(reverseVelocity), 2);
		new RaiseEarth(player, entity.getLocation().add(0, -1, 0).add(reverseVelocity).add(reverseVelocity), 1);
	}
	
	public void findTargets(double radius) {
		for ( Entity e : GeneralMethods.getEntitiesAroundPoint(player.getLocation(), radius) )
			if (e instanceof LivingEntity && e.getUniqueId() != player.getUniqueId() && isEarthbendable(((LivingEntity) e).getLocation().add(0, -1, 0).getBlock())) {
				if ( (isLimited && targets.size() < maxEntity) || !isLimited)
					targets.add((LivingEntity) e);
			}		
	}
	
	public void findTargetLocation() {
		Block tmpBlock = getTargetEarthBlock(player, range);
		
		while ( !GeneralMethods.isSolid(tmpBlock) ) {
			tmpBlock = tmpBlock.getRelative(BlockFace.DOWN, 1);
		}
		
		targetLoc = tmpBlock.getLocation().clone().add(0.5, 0, 0.5);
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
		return "Toss";
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
		return new Toss(player);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		ArrayList<AbilityInformation> combination = new ArrayList<>();
		combination.add(new AbilityInformation("EarthBlast", ClickType.LEFT_CLICK));
		combination.add(new AbilityInformation("EarthBlast", ClickType.SHIFT_DOWN));
		combination.add(new AbilityInformation("Catapult", ClickType.SHIFT_UP));
		
		return combination;
	}

	@Override
	public String getDescription() {
		return "Throw creatures to which block you're looking at.";
	}

	@Override
	public String getInstructions() {
		return "EarthBlast (Left Click) -> EarthBlast (Hold Sneak) -> Catapult (Release Sneak)";
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
		ProjectKorra.log.info("Succesfully enabled " + getName() + " by " + getAuthor());
		
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Earth.Toss.Cooldown", 5000);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Earth.Toss.BlockSelectRange", 15);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Earth.Toss.EntitySelectRadius", 10);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Earth.Toss.LimitedAmountOfEntities.isEnabled", false);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Earth.Toss.LimitedAmountOfEntities.MaxEntityNumber", 10);
		ConfigManager.defaultConfig.save();
	}

	@Override
	public void stop() {
		ProjectKorra.log.info("Successfully disabled " + getName() + " by " + getAuthor());
		super.remove();
	}

}
