package me.hiro.safelanding;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.PassiveAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;

public class SafeLandingAir extends AirAbility implements AddonAbility, PassiveAbility {
	
	private long duration;
	private boolean isActive;
	private long lastActivateTime;
	
	private int flag = 0;
	
	public SafeLandingAir(Player player) {
		super(player);
		
		setField();
	}

	public void setField() {
		duration = ConfigManager.getConfig().getLong("ExtraAbilities.Hiro3.Air.SafeLandingAir.Duration");
		isActive = false;
	}
	
	@Override
	public void progress() {
		if (isActive) {
			
			if (player.isOnGround() && flag == 0) {
				flag = 1;
				new BukkitRunnable() {
					@Override
					public void run() {
						isActive = false;
						flag = 0;
					}
				}.runTaskLater(ProjectKorra.plugin, 1);
			}
			
			if (System.currentTimeMillis() > lastActivateTime + duration) {
				isActive = false;
			}
			
		}
	}
	
	public void activate() {
		isActive = true;
		lastActivateTime = System.currentTimeMillis();
	}
	
	public void deactivate() {
		isActive = false;
	}
	
	public boolean isActive() {
		return this.isActive;
	}
	
	@Override
	public long getCooldown() {
		return 0;
	}

	@Override
	public Location getLocation() {
		return null;
	}

	@Override
	public String getName() {
		return "SafeLandingAir";
	}

	@Override
	public boolean isHarmlessAbility() {
		return true;
	}

	@Override
	public boolean isSneakAbility() {
		return false;
	}

	@Override
	public boolean isInstantiable() {
		return true;
	}

	@Override
	public boolean isProgressable() {
		return true;
	}
	
	@Override
	public String getDescription() {
		return "Land safely after WallRun.";
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
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Air.SafeLandingAir.Duration", 5000);
		ConfigManager.defaultConfig.save();
		
		ProjectKorra.log.info("Succesfully enabled " + getName() + " by " + getAuthor());
	}

	@Override
	public void stop() {
		ProjectKorra.log.info("Successfully disabled " + getName() + " by " + getAuthor());
		super.remove();
	}

}
