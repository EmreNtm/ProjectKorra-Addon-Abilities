package Hiro3;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.ClickType;

public class RagingFire extends FireAbility implements AddonAbility, ComboAbility {

	private long cooldown;
	private long duration;
	private double radius;
	private double height;
	private double damage;
	
	private int blockLimit;
	
	private HashMap<Location, Location> fireLocations;
	
	public RagingFire(Player player) {
		super(player);
		
		if (bPlayer.isOnCooldown(this)) {
			return;
		}
		
		if (!this.bPlayer.canBendIgnoreBinds(this)) {
		      return;
		}
		
		setField();
		findFire();
		if (fireLocations.isEmpty()) {
			player.sendMessage(ChatColor.RED + "There is no fire around you!");
			return;
		}
		this.bPlayer.addCooldown(this);
		start();
	}

	public void setField() {
		cooldown = ConfigManager.getConfig().getLong("ExtraAbilities.Hiro3.Fire.RagingFire.Cooldown");
		duration = ConfigManager.getConfig().getLong("ExtraAbilities.Hiro3.Fire.RagingFire.Duration");
		radius = ConfigManager.getConfig().getDouble("ExtraAbilities.Hiro3.Fire.RagingFire.Radius");
		height = ConfigManager.getConfig().getDouble("ExtraAbilities.Hiro3.Fire.RagingFire.Height");
		damage = ConfigManager.getConfig().getDouble("ExtraAbilities.Hiro3.Fire.RagingFire.Damage");
		blockLimit = ConfigManager.getConfig().getInt("ExtraAbilities.Hiro3.Fire.RagingFire.MaxBlockLimit");
		
		fireLocations = new HashMap<Location, Location>(0);
	}

	@Override
	public void progress() {
		
		if (GeneralMethods.isRegionProtectedFromBuild(this, player.getLocation())) {
			remove();
			return;
		}
		
		if (System.currentTimeMillis() > getStartTime() + duration) {
			clearFire();
			remove();
			return;
		}
		
		for (Location l : fireLocations.keySet()) {
			
			if (!l.getBlock().getType().equals(Material.FIRE)) {
				l.getBlock().setType(Material.FIRE);
			}
			
			Location tmpLoc = l.clone();
			
			for (Entity e : GeneralMethods.getEntitiesAroundPoint(l, height)) {
				if (e instanceof LivingEntity && e.getUniqueId() != player.getUniqueId() 
						&& ((LivingEntity) e).getLocation().getBlockX() == l.getBlockX() 
						&& ((LivingEntity) e).getLocation().getBlockZ() == l.getBlockZ()
						&& ((LivingEntity) e).getLocation().getBlockY() <= fireLocations.get(l).getBlockY()
						&& ((LivingEntity) e).getLocation().getBlockY() >= l.getBlockY()) {
					((LivingEntity) e).damage(damage);
				}
			}
			
			for (double i = 0; i < fireLocations.get(l).distance(l)-1; i+=1) {
				player.getWorld().spawnParticle(Particle.FLAME, tmpLoc.clone().add(0.2, 0.5, 0.5), 0, 0, 0.08, 0);
				player.getWorld().spawnParticle(Particle.FLAME, tmpLoc.clone().add(0.8, 0, 0.2), 0, 0, 0.08, 0);
				player.getWorld().spawnParticle(Particle.FLAME, tmpLoc.clone().add(0.8, 0.5, 0.8), 0, 0, 0.08, 0);
				tmpLoc.add(0, 1, 0);
			}
			
			if (fireLocations.get(l).distance(l) < height && (fireLocations.get(l).getBlock().getType().equals(Material.AIR) || fireLocations.get(l).getBlock().getType().equals(Material.FIRE)))
				fireLocations.replace(l, fireLocations.get(l).clone().add(new Vector(0, 0.4, 0)));
			
		}
		
	}
	
	public void findFire() {
		for (Block b : GeneralMethods.getBlocksAroundPoint(player.getLocation(), radius)) {
			if (b.getType().equals(Material.FIRE)) {
				if (fireLocations.size() < blockLimit) {
					fireLocations.put(b.getLocation(), b.getLocation());
				}
			}
		}
	}
	
	public void clearFire() {
		for (Location l : fireLocations.keySet()) {
			if (l.getBlock().getType().equals(Material.FIRE)) {
				l.getBlock().setType(Material.AIR);
			}
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
		return "RagingFire";
	}

	@Override
	public String getDescription() {
		return "Raise the fire around you.";
	}

	@Override
	public String getInstructions() {
		return "FireBlast (Left Click) -> FireBlast (Left Click) -> Blaze (Left Click)";
	}
	
	@Override
	public String getAuthor() {
		return "Hiro3";
	}

	@Override
	public String getVersion() {
		return "1.1";
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
		return new RagingFire(player);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		ArrayList<AbilityInformation> combination = new ArrayList<>();
		combination.add(new AbilityInformation("FireBlast", ClickType.LEFT_CLICK));
		combination.add(new AbilityInformation("FireBlast", ClickType.LEFT_CLICK));
		combination.add(new AbilityInformation("Blaze", ClickType.LEFT_CLICK));
		
		return combination;
	}

	@Override
	public void load() {
		ProjectKorra.log.info("Succesfully enabled " + getName() + " by " + getAuthor());
		
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Fire.RagingFire.Cooldown", 3000);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Fire.RagingFire.Duration", 4000);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Fire.RagingFire.Radius", 15);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Fire.RagingFire.MaxBlockLimit", 20);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Fire.RagingFire.Damage", 3);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Fire.RagingFire.Height", 3);
		ConfigManager.defaultConfig.save();
	}

	@Override
	public void stop() {
		ProjectKorra.log.info("Successfully disabled " + getName() + " by " + getAuthor());
		super.remove();
	}
	
}