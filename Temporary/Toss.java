package Hiro3;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
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
import com.projectkorra.projectkorra.util.ClickType;

public class Toss extends EarthAbility implements AddonAbility, ComboAbility {

	private long cooldown;
	private long radius;
//	private ArrayList<LivingEntity> targets;
	private LivingEntity target;
	@SuppressWarnings("unused")
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
		if (target != null) {
			findTargetLocation();
			start();
		}
	}

	public void setField() {
		cooldown = 0;
		radius = 10;
//		targets = new ArrayList<LivingEntity>(0);
		target = null;
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
		Vector tmpVector = targetLoc.clone().toVector().subtract(target.getLocation().toVector());
		target.setVelocity(new Vector( tmpVector.getX(), 
				(tmpVector.getY() > 4) ? tmpVector.getY() + 0.5 : 4, 
				tmpVector.getZ()).multiply(0.175));
	}
	
	public void findTargets(double radius) {
		for ( Entity e : GeneralMethods.getEntitiesAroundPoint(player.getLocation(), radius) )
			if (e instanceof LivingEntity && e.getUniqueId() != player.getUniqueId())
				target = ((LivingEntity) e);			
	}
	
	public void findTargetLocation() {
		Block tmpBlock = getTargetEarthBlock(player, 15);
		
		while ( !GeneralMethods.isSolid(tmpBlock) ) {
			tmpBlock = tmpBlock.getRelative(BlockFace.DOWN, 1);
			player.sendMessage(tmpBlock.getType() + "");
		}
		
		targetLoc = tmpBlock.getLocation().clone().add(0.5, 0, 0.5);
		
		tmpBlock.setType(Material.GLOWSTONE);
	}
	
//	public Block getTargetEarthBlock2(Player player, double range) {
//		Vector direction = player.getLocation().getDirection().clone().multiply(0.01);
//		Location loc = player.getEyeLocation().clone();
//		Location startLoc = loc.clone();
//		
//		do {
//			loc.add(direction);
//		} while (startLoc.distance(loc) < range && !GeneralMethods.isSolid(loc.getBlock()));
//		
//		if (startLoc.distance(loc) < range && isEarthbendable(loc.getBlock())) {
//			return loc.getBlock();
//		}
//		return null;
//	}
	
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
		
		return combination;
	}

	@Override
	public String getDescription() {
		return "Toss people.";
	}

	@Override
	public String getInstructions() {
		return "EarthBlast (Left Click)";
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
	}

	@Override
	public void stop() {
		ProjectKorra.log.info("Successfully disabled " + getName() + " by " + getAuthor());
		super.remove();
	}

}
