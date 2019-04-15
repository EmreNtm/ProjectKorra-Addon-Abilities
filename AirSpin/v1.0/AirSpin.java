package me.Hiro.MyAbilities.AirSpin;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;

public class AirSpin extends AirAbility implements AddonAbility {
	
	private long cooldown;
	private double arc;
	private double range;
	private int ParticleDivider;
	private Location org;
	private Location[] loc;
	private Vector[] dir;
	private LivingEntity[] entities;

	public AirSpin(Player player) {
		super(player);

		if (bPlayer.isOnCooldown(this)) {
			return;
		}

		if (!bPlayer.canBend(this)) {
			return;
		}

		setFields();
		setArrays();
		start();
		bPlayer.addCooldown(this);
	}

	public void setFields() {
		
		cooldown = ConfigManager.getConfig().getLong("ExtraAbilities.Hiro3.Air.AirSpin.Cooldown");
		range = ConfigManager.getConfig().getDouble("ExtraAbilities.Hiro3.Air.AirSpin.Range");
		arc = ConfigManager.getConfig().getDouble("ExtraAbilities.Hiro3.Air.AirSpin.Angle");
		arc %= 31;
		ParticleDivider = ConfigManager.getConfig().getInt("ExtraAbilities.Hiro3.Air.AirSpin.ParticleDivider");
		if (ParticleDivider == 0) {
			ParticleDivider = 1;
		} else if (ParticleDivider < 0) {
			ParticleDivider = -ParticleDivider;
		}
		
		loc = new Location[70];
		dir = new Vector[70];
		entities = new LivingEntity[70];
		org = player.getEyeLocation();
		
	}
	
	public void setArrays() {
		
		int j;
		double i, angle;
		
		for(i = -arc, j = 0; i <= arc; i+=1, j++) {
			angle = Math.toRadians(i);
			dir[j] = player.getEyeLocation().getDirection();
			dir[j].setX(dir[j].getX() * Math.cos(angle) - dir[j].getZ() * Math.sin(angle));
			dir[j].setZ(dir[j].getX() * Math.sin(angle) + dir[j].getZ() * Math.cos(angle));
		}
		
		for(j = 0; j < (int)2*arc; j++) {
			loc[j] = player.getEyeLocation();
		}
		
	}
	
	public void check() {
		int i;
		int count = 0;
		for(i = 0; i < (int)2*arc; i++) {
			if(loc[i] != null) {
				if(org.distance(loc[i]) > range) {
					remove();
				}
			}else {
				count++;
			}
		}
		if(count == (int)2*arc - 1) {
			remove();
		}
	}
	
	public void particles(LivingEntity entity) {
		double r = 0;
		double tmp = 0.05;
		double angle = 10;
		angle = Math.toRadians(angle);
		Location loc = entity.getLocation();
		loc.setY(loc.getY() - 1);
		while (r >= 0) {
			if (r >= 2) {
				tmp = -tmp;
				r = 2;
			}
			loc.setY(loc.getY() + 0.05);
			loc.setX(loc.getX() + r * Math.cos(angle));
			loc.setZ(loc.getZ() + r * Math.sin(angle));
			getAirbendingParticles().display(loc, 0, 0, 0, 0, 3);
			angle += 10;
			r += tmp;
		}
	}
	
	@Override
	public void progress() {
		int i, j, flag;
		if (player.isDead() || !player.isOnline() || GeneralMethods.isRegionProtectedFromBuild(this, player.getLocation())) {
			remove();
			return;
		}

		for(i = 0; i < (int)2*arc; i++) {
			if(loc[i] != null) {
				loc[i].add(dir[i].multiply(1));
				if(GeneralMethods.isSolid(loc[i].getBlock())) {
					loc[i] = null;
					dir[i] = null;
				}else {
					if(i % ParticleDivider == 0)
						getAirbendingParticles().display(loc[i], 0, 0, 0, 0, 1);
					for(Entity entity : GeneralMethods.getEntitiesAroundPoint(loc[i], 2)) {
						if(entity instanceof LivingEntity && entity.getUniqueId() != player.getUniqueId()) {
							flag = 0;
							for(j = 0; j < (int)2*arc; j++) {
								if (entities[j] == (LivingEntity)entity) {
									flag = 1;
								}
							}
							if (flag == 0) {
								entities[i] = (LivingEntity)entity;
								particles((LivingEntity)entity);
								entity.teleport(entity.getLocation().setDirection(entity.getLocation().getDirection().multiply(-1)));
							}
							loc[i] = null;
							dir[i] = null;
						}
					}
				}
			}
			
		}
		
		check();
	
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
	public boolean isHarmlessAbility() {
		return false;
	}

	@Override
	public boolean isSneakAbility() {
		return false;
	}

	@Override
	public String getName() {
		return "AirSpin";
	}
	
	@Override
	public String getDescription() {
		return "Moves the air around your enemies and makes them spin!";
	}
	
	@Override
	public String getInstructions() {
		return "Left Click";
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
		ProjectKorra.plugin.getServer().getPluginManager().registerEvents(new AirSpinListener(), ProjectKorra.plugin);
		ProjectKorra.log.info("Succesfully enabled " + getName() + " by " + getAuthor());
		
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Air.AirSpin.Cooldown", 2000);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Air.AirSpin.Range", 15);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Air.AirSpin.Angle", 30);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Air.AirSpin.ParticleDivider", 8);
		ConfigManager.defaultConfig.save();
	}

	@Override
	public void stop() {
		ProjectKorra.log.info("Successfully disabled " + getName() + " by " + getAuthor());
		super.remove();
	}

}
