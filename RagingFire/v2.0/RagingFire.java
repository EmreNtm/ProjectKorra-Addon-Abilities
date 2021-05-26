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
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.firebending.FireBlast;
import com.projectkorra.projectkorra.firebending.FireJet;
import com.projectkorra.projectkorra.util.ClickType;

public class RagingFire extends FireAbility implements AddonAbility, ComboAbility {

	private long cooldown;
	private long duration;
	private double radius;
	private double height;
	private double damage;
	
	private int blockLimit;
	
	private HashMap<Location, Location> fireLocations;
	
	private boolean isFireJetUsed = false;
	
	protected boolean isBoostEnabled;
	protected double boostActivationRange;
	protected double boostProjectileSpeed;
	protected double boostProjectileDamage;
	protected long boostFireJetDuration;
	protected double boostFireJetSpeed;
	protected double boostFireBlastChance;
	
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
		
		isBoostEnabled = ConfigManager.getConfig().getBoolean("ExtraAbilities.Hiro3.Fire.RagingFire.SupportFirebending.isEnabled");
		boostActivationRange = ConfigManager.getConfig().getDouble("ExtraAbilities.Hiro3.Fire.RagingFire.SupportFirebending.ActivationRange");
		boostProjectileSpeed = ConfigManager.getConfig().getDouble("ExtraAbilities.Hiro3.Fire.RagingFire.SupportFirebending.ProjectileSpeed");
		boostProjectileDamage = ConfigManager.getConfig().getDouble("ExtraAbilities.Hiro3.Fire.RagingFire.SupportFirebending.ProjectileDamage");
		boostFireJetDuration = ConfigManager.getConfig().getLong("ExtraAbilities.Hiro3.Fire.RagingFire.SupportFirebending.FireJetDurationIncrease");
		boostFireJetSpeed = ConfigManager.getConfig().getDouble("ExtraAbilities.Hiro3.Fire.RagingFire.SupportFirebending.FireJetSpeedIncrease");
		boostFireBlastChance = ConfigManager.getConfig().getDouble("ExtraAbilities.Hiro3.Fire.RagingFire.SupportFirebending.FireBlastExtraProjectileChance");
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
				player.getWorld().spawnParticle(Particle.FLAME, tmpLoc.clone().add(0.5, 0.5, 0.5), 0, Math.random() * 0.1 - 0.05, 0.08, Math.random() * 0.1 - 0.05);
				if (i == 0 && Math.random() * 100 < 70)
					player.getWorld().spawnParticle(Particle.CRIMSON_SPORE, tmpLoc.clone().add(0.5, 0.5, 0.5), 0);
				tmpLoc.add(0, 1, 0);
			}
			
			if (fireLocations.get(l).distance(l) < height && (fireLocations.get(l).getBlock().getType().equals(Material.AIR) || fireLocations.get(l).getBlock().getType().equals(Material.FIRE)))
				fireLocations.replace(l, fireLocations.get(l).clone().add(new Vector(0, 0.4, 0)));
		}
		
		if (this.isBoostEnabled)
			boostProjectiles();
	}
	
	private void boostProjectiles() {
		if (CoreAbility.hasAbility(player, FireBlast.class) && Math.random() < this.boostFireBlastChance) {
			FireBlast fb = CoreAbility.getAbility(player, FireBlast.class);
			new SupportProjectile(this, fb);
		} else if (CoreAbility.hasAbility(player, FireJet.class) && !this.isFireJetUsed && player.getLocation().distance(getRandomLocation()) < this.boostActivationRange) {
			new SupportProjectile(this, CoreAbility.getAbility(player, FireJet.class));
			this.isFireJetUsed = true;
		}
	}
	
	public void findFire() {
		for (Block b : GeneralMethods.getBlocksAroundPoint(player.getLocation(), radius)) {
			if (b.getType().equals(Material.FIRE) && isInLineOfSight(player, b.getLocation())) {
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
	
	public Location getRandomLocation() {
		return (Location) fireLocations.values().toArray()[(int) (Math.random() * fireLocations.size())];
	}
	
	public static boolean isInLineOfSight(Player player, Location location) {
		if (!player.getWorld().equals(location.getWorld())) {
			return false;
		}
		
		Vector dir = player.getLocation().getDirection();
        Vector otherVec = location.toVector().subtract(player.getLocation().toVector());
        double angle = Math.acos( dir.dot(otherVec)  /  (dir.length() * otherVec.length()) );
        angle = Math.toDegrees (angle);
        
        if(angle > 60) {
        	return false;
        }
        
		return true;
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
		return "2.0";
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
		
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Fire.RagingFire.Cooldown", 10000);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Fire.RagingFire.Duration", 10000);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Fire.RagingFire.Radius", 15);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Fire.RagingFire.MaxBlockLimit", 20);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Fire.RagingFire.Damage", 3);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Fire.RagingFire.Height", 3);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Fire.RagingFire.SupportFirebending.isEnabled", true);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Fire.RagingFire.SupportFirebending.ActivationRange", 15);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Fire.RagingFire.SupportFirebending.FireBlastExtraProjectileChance", 0.1);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Fire.RagingFire.SupportFirebending.ProjectileSpeed", 15);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Fire.RagingFire.SupportFirebending.ProjectileDamage", 2);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Fire.RagingFire.SupportFirebending.FireJetDurationIncrease", 3000);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Fire.RagingFire.SupportFirebending.FireJetSpeedIncrease", 0.1);
		ConfigManager.defaultConfig.save();
	}

	@Override
	public void stop() {
		ProjectKorra.log.info("Successfully disabled " + getName() + " by " + getAuthor());
		super.remove();
	}
	
}