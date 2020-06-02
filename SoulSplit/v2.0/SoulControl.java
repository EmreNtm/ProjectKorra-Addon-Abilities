package Hiro3;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;

import me.xnuminousx.spirits.ability.api.SpiritAbility;

public class SoulControl extends SpiritAbility implements AddonAbility {

	private SoulSplit split;
	
	private long cooldown;
	private double selectRange;
	private double targetRange;
	private double moveDistance;
	
	private Soul selectedSoul;
	
	private Vector finalDirection;
	private Location finalLocation;
	
	private int abilityState;
	
	public SoulControl(Player player) {
		super(player);
		
		if (!this.bPlayer.canBendIgnoreBinds(this)) {
		      return;
		 }
		
		if (bPlayer.isOnCooldown(this)) {
			return;
		}
		
		if (CoreAbility.getAbility(player, SoulSplit.class) == null) {
			return;
		}
		
		setField();
		
		this.selectedSoul = selectSoul(player, selectRange);
		
		if (selectedSoul != null) {
			selectedSoul.setSelected(true);
			start();
		}
	}

	public void setField() {
		this.split = CoreAbility.getAbility(player, SoulSplit.class);
		this.cooldown = ConfigManager.getConfig().getLong("ExtraAbilities.Hiro3.Spirit.SoulControl.Cooldown");
		this.selectRange = ConfigManager.getConfig().getDouble("ExtraAbilities.Hiro3.Spirit.SoulControl.SelectRange");
		this.targetRange = ConfigManager.getConfig().getDouble("ExtraAbilities.Hiro3.Spirit.SoulControl.TargetRange");
		this.moveDistance = ConfigManager.getConfig().getDouble("ExtraAbilities.Hiro3.Spirit.SoulControl.MoveDistance");
		this.abilityState = 0;
		this.finalDirection = new Vector();
		this.finalLocation = null;
	}
	
	@Override
	public void progress() {
		
		if (split.isRemoved()) {
			remove();
			return;
		}
		
		if (!selectedSoul.isAlive()) {
			remove();
			return;
		}
		
		//Select soul
		if (abilityState == 0) {
			Location targetLoc = getTargetLoc(player, targetRange);
			drawLine(this.selectedSoul.getLoc().clone(), targetLoc.clone());
		} else { //Move soul
			this.selectedSoul.changeDirectionWithAnimation(this.finalDirection);
			this.selectedSoul.moveSoul(this.finalDirection, this.finalLocation);
			bPlayer.addCooldown(this);
			remove();
		}
		
	}
	
	public Soul selectSoul(Player player, double range) {
		Vector direction = player.getLocation().getDirection().clone().multiply(0.1);
		Location loc = player.getEyeLocation().clone();
		Location startLoc = loc.clone();
		
		do {
			loc.add(direction);
			for (Soul s : split.getSouls()) {
				if (s.getLoc().clone().add(0, 1, 0).distance(loc) <= 2) {
					return s;
				}
			}
		} while (startLoc.distance(loc) < range && !GeneralMethods.isSolid(loc.getBlock()));
		
		for (Soul s : split.getSouls()) {
			if (s.getLoc().clone().add(0, 1, 0).distance(loc) <= 2) {
				return s;
			}
		}
		
		return null;
	}
	
	public Location getTargetLoc(Player player, double range) {
		Vector direction = player.getLocation().getDirection().clone().multiply(0.1);
		Location loc = player.getEyeLocation().clone();
		Location startLoc = loc.clone();
		
		do {
			loc.add(direction);
		} while (startLoc.distance(loc) < range && !GeneralMethods.isSolid(loc.getBlock()));
		
		return loc;
	}
	
	public void drawLine(Location from, Location to) {
		Location loc = from.clone();
		Vector dir = to.clone().toVector().subtract(from.clone().toVector()).normalize().multiply(1);
		dir.setY(0);
		this.finalDirection = dir.clone();
		DustOptions result = new DustOptions(Color.fromRGB(178, 178, 178), 1);
		do {
			player.spawnParticle(Particle.REDSTONE, loc, 0, result);
			loc.add(dir);
		} while (loc.distance(from) <= this.moveDistance && loc.distance(from) <= from.distance(to));
		this.finalLocation = loc;
	}
	
	public void setAbilityState(int state) {
		this.abilityState = state;
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
		return "SoulControl";
	}

	@Override
	public String getDescription() {
		return "Select a soul to reposition.";
	}

	@Override
	public String getInstructions() {
		return "Sneak to select a soul. Left click to move it.";
	}
	
	@Override
	public boolean isExplosiveAbility() {
		return false;
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	@Override
	public boolean isIgniteAbility() {
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
		return "2.0";
	}

	@Override
	public void remove() {
		super.remove();
		this.selectedSoul.setSelected(false);
	}
	
	@Override
	public void load() {
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Spirit.SoulControl.Cooldown", 2000);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Spirit.SoulControl.SelectRange", 30);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Spirit.SoulControl.TargetRange", 30);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Spirit.SoulControl.MoveDistance", 5);
		ConfigManager.defaultConfig.save();
	}

	@Override
	public void stop() {
		super.remove();
	}

}
