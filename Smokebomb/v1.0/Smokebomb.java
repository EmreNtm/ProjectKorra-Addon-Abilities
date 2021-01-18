package me.hiro3.smokebomb;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.metadata.FixedMetadataValue;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ChiAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;

public class Smokebomb extends ChiAbility implements AddonAbility {

	private Listener SL;
	
	public static double smokeRadius;
	public static long duration;
	public static double particlePercentage;
	
	private long cooldown;
	
	public Smokebomb(Player player) {
		super(player);
		
		if (!bPlayer.canBend(this))
			return;
		
		setField();
		start();
	}
	
	public void setField() {
		cooldown = ConfigManager.getConfig().getLong("ExtraAbilities.Hiro3.Chi.Smokebomb.Cooldown");
		duration = ConfigManager.getConfig().getLong("ExtraAbilities.Hiro3.Chi.Smokebomb.Duration");
		smokeRadius = ConfigManager.getConfig().getDouble("ExtraAbilities.Hiro3.Chi.Smokebomb.Radius");
		particlePercentage = ConfigManager.getConfig().getDouble("ExtraAbilities.Hiro3.Chi.Smokebomb.ParticlePercentage");
	}

	@Override
	public void progress() {
		player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_WEAK, 5, 0);
		Snowball bomb = player.launchProjectile(Snowball.class);
		bomb.setBounce(false);
		bomb.setMetadata("Smokebomb", new FixedMetadataValue(ProjectKorra.plugin, "1"));
		bPlayer.addCooldown(this);
		remove();
	}
	
	public static void createSmokeArea(Location location) {
		SmokebombArea sa = new SmokebombArea(location, smokeRadius, duration);
		sa.runTaskTimer(ProjectKorra.plugin, 0, 1);
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
		return "Smokebomb";
	}

	@Override
	public String getDescription() {
		return "Throw a smokebomb to create a hidden area. People outside of the area can't see people inside of the area."
				+ " People inside of the area get blindness.";
	}

	@Override
	public String getInstructions() {
		return "Left Click";
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
		return false;
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
		SL = new SmokebombListener();
		ProjectKorra.plugin.getServer().getPluginManager().registerEvents(SL, ProjectKorra.plugin);
		ProjectKorra.log.info("Succesfully enabled " + getName() + " by " + getAuthor());
		
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Chi.Smokebomb.Cooldown", 10000);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Chi.Smokebomb.Duration", 15000);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Chi.Smokebomb.Radius", 7);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Chi.Smokebomb.ParticlePercentage", 0.01);
		ConfigManager.defaultConfig.save();
	}

	@Override
	public void stop() {
		ProjectKorra.log.info("Successfully disabled " + getName() + " by " + getAuthor());
		HandlerList.unregisterAll(SL);
		super.remove();
	}

}
