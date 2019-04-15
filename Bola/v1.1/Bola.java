package me.Hiro.MyAbilities.Bola;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ChiAbility;
import com.projectkorra.projectkorra.chiblocking.passive.ChiPassive;
import com.projectkorra.projectkorra.configuration.ConfigManager;

public class Bola extends ChiAbility implements AddonAbility {

	private double arc;
	private long cooldown;
	private long duration;
	private long delay;
	private double phase;
	private double speed;
	private double range;
	private int charges;
	private int FirstSlowness;
	private int FirstDuration;
	private int FirstDamage;
	private int SecondSlowness;
	private int SecondDuration;
	private int SecondDamage;
	private int ThirdSlowness;
	private int ThirdDuration;
	private int ThirdDamage;
	private int[] hitCount;
	private LivingEntity[] targets;
	private Vector[][] dir;
	private Location[][] loc;
	private Vector[][] pDir;
	private Location[][] pLoc;
	private Location[] origin;

	public Bola(Player player) {
		super(player);

		if (bPlayer.isOnCooldown(this)) {
			return;
		}

		if (!bPlayer.canBend(this)) {
			return;
		}

		if (hasAbility(player, Bola.class)) {
			Bola b = getAbility(player, Bola.class);
			if (b.delay != 0 && System.currentTimeMillis() < b.getStartTime() + b.delay) {
				return;
			}
			if (b.charges > 1) {
				b.charges--;
				player.getWorld().playSound(player.getEyeLocation(), Sound.ENTITY_ARROW_SHOOT, 2, 0);
			}
			if (b.charges == 1) {
				bPlayer.addCooldown(b);
			}
			b.delay *= 2;
			b.pDir[3 - b.charges][3] = player.getEyeLocation().getDirection();
			b.loc[3 - b.charges][0] = player.getEyeLocation();
			b.loc[3 - b.charges][1] = player.getEyeLocation();
			b.loc[3 - b.charges][2] = player.getEyeLocation();
			b.origin[3 - b.charges] = player.getLocation();
			return;
		}

		setFields();
		player.getWorld().playSound(player.getEyeLocation(), Sound.ENTITY_ARROW_SHOOT, 2, 0);
		start();
	}

	public void setFields() {
		arc = 180;
		phase = 0;
		speed = ConfigManager.getConfig().getDouble("ExtraAbilities.Hiro3.Chi.Bola.Speed");
		range = ConfigManager.getConfig().getDouble("ExtraAbilities.Hiro3.Chi.Bola.Range");
		cooldown = ConfigManager.getConfig().getLong("ExtraAbilities.Hiro3.Chi.Bola.Cooldown");
		duration = ConfigManager.getConfig().getLong("ExtraAbilities.Hiro3.Chi.Bola.MaxShotTime");
		delay = ConfigManager.getConfig().getLong("ExtraAbilities.Hiro3.Chi.Bola.TimeBetweenShots");
		duration += 2 * delay;
		charges = 3;
		FirstDamage = ConfigManager.getConfig().getInt("ExtraAbilities.Hiro3.Chi.Bola.FirstDamage");
		FirstSlowness = ConfigManager.getConfig().getInt("ExtraAbilities.Hiro3.Chi.Bola.FirstSlowness");
		FirstDuration = ConfigManager.getConfig().getInt("ExtraAbilities.Hiro3.Chi.Bola.FirstDuration");
		FirstDuration *= 20;
		SecondDamage = ConfigManager.getConfig().getInt("ExtraAbilities.Hiro3.Chi.Bola.SecondDamage");
		SecondSlowness = ConfigManager.getConfig().getInt("ExtraAbilities.Hiro3.Chi.Bola.SecondSlowness");
		SecondDuration = ConfigManager.getConfig().getInt("ExtraAbilities.Hiro3.Chi.Bola.SecondDuration");
		SecondDuration *= 20;
		ThirdDamage = ConfigManager.getConfig().getInt("ExtraAbilities.Hiro3.Chi.Bola.ThirdDamage");
		ThirdSlowness = ConfigManager.getConfig().getInt("ExtraAbilities.Hiro3.Chi.Bola.ThirdSlowness");
		ThirdDuration = ConfigManager.getConfig().getInt("ExtraAbilities.Hiro3.Chi.Bola.ThirdDuration");
		ThirdDuration *= 20;
		hitCount = new int[3];
		targets = new LivingEntity[3];
		origin = new Location[3];
		dir = new Vector[3][3];
		loc = new Location[3][3];
		pDir = new Vector[3][4];
		pLoc = new Location[3][3];
		pDir[0][3] = player.getEyeLocation().getDirection();
		loc[3 - charges][0] = player.getEyeLocation();
		loc[3 - charges][1] = player.getEyeLocation();
		loc[3 - charges][2] = player.getEyeLocation();
		targets[0] = null;
		targets[1] = null;
		targets[2] = null;
		hitCount[0] = 0;
		hitCount[1] = 0;
		hitCount[2] = 0;
		origin[0] = player.getLocation();
	}

	public void setArrays(int tmp, Vector v) {
		int j, t;
		double i, angle;

		if (phase < 120) {
			phase += 20;
		} else {
			phase = 0;
		}
		for (i = -arc + phase, j = 0; i < arc + phase; i += 120, j++) {
			angle = Math.toRadians(i);
			dir[tmp][j] = player.getEyeLocation().getDirection();
			dir[tmp][j].setX(dir[tmp][j].getX() * Math.cos(angle) - dir[tmp][j].getZ() * Math.sin(angle));
			dir[tmp][j].setZ(dir[tmp][j].getX() * Math.sin(angle) + dir[tmp][j].getZ() * Math.cos(angle));
		}

		loc[tmp][0].add(v.clone().multiply(speed));
		loc[tmp][1].add(v.clone().multiply(speed));
		loc[tmp][2].add(v.clone().multiply(speed));

		for (i = 0; i < 6; i++) {
			for (j = 0; j < 3; j++) {
				loc[tmp][j].add(dir[tmp][j].clone().multiply(0.2));
				if (GeneralMethods.isSolid(loc[tmp][j].getBlock())) {
					loc[tmp][0] = null;
					dir[tmp][0] = null;
					return;
				}
				for (Entity entity : GeneralMethods.getEntitiesAroundPoint(loc[tmp][j], 1.5)) {
					if (entity instanceof LivingEntity && entity.getUniqueId() != player.getUniqueId()) {
						loc[tmp][0] = null;
						dir[tmp][0] = null;
						for (t = 0; t < 3; t++) {
							if (targets[t] != null && targets[t].equals(entity)) {
								hitCount[t]++;
								if (entity instanceof Player) {
									if (hitCount[t] == 2) {
										((LivingEntity) entity).addPotionEffect(
												new PotionEffect(PotionEffectType.SLOW, SecondDuration, SecondSlowness),
												true);
										if (ChiPassive.willChiBlock(player, (Player) entity)) {
											ChiPassive.blockChi((Player) entity);
										}
										caught((Player) entity, 2);
									} else {
										((LivingEntity) entity).addPotionEffect(
												new PotionEffect(PotionEffectType.SLOW, ThirdDuration, ThirdSlowness),
												true);
										ChiPassive.blockChi((Player) entity);
										caught((Player) entity, 3);
									}
								} else {
									if (hitCount[t] == 2) {
										((LivingEntity) entity).damage(SecondDamage);
									} else {
										((LivingEntity) entity).damage(ThirdDamage);
									}
								}
								return;
							}
						}
						targets[3 - charges] = (LivingEntity) entity;
						hitCount[3 - charges]++;
						if (entity instanceof Player) {
							((LivingEntity) entity).addPotionEffect(
									new PotionEffect(PotionEffectType.SLOW, FirstDuration, FirstSlowness), true);
							caught((Player) entity, 1);
						} else {
							((LivingEntity) entity).damage(FirstDamage);
						}
						return;
					}
				}
				if (i == 5) {
					GeneralMethods.displayColoredParticle(loc[tmp][j], "81c6c8", 0, 0, 0);
				} else {
					GeneralMethods.displayColoredParticle(loc[tmp][j], "4e5c5c", 0, 0, 0);
				}
			}
		}

		loc[tmp][0].subtract(dir[tmp][0].clone().multiply(1.2));
		loc[tmp][1].subtract(dir[tmp][1].clone().multiply(1.2));
		loc[tmp][2].subtract(dir[tmp][2].clone().multiply(1.2));
	}

	public void caught(Player entity, int level) {

		if (level == 1) {

			player.getWorld().playSound(entity.getEyeLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 2, 0);
			new BukkitRunnable() {
				int j, flag = 0;
				double i, angle, p = 1;
				double phase = 0;

				@Override
				public void run() {

					if (phase < 120) {
						phase += 20;
					} else {
						phase = 0;
					}

					for (i = -arc + phase, j = 0; i < arc + phase; i += 120, j++) {
						angle = Math.toRadians(i);
						pDir[0][j] = player.getEyeLocation().getDirection();
						pDir[0][j].setX(pDir[0][j].getX() * Math.cos(angle) - pDir[0][j].getZ() * Math.sin(angle));
						pDir[0][j].setZ(pDir[0][j].getX() * Math.sin(angle) + pDir[0][j].getZ() * Math.cos(angle));
					}

					pLoc[0][0] = entity.getLocation();
					pLoc[0][1] = entity.getLocation();
					pLoc[0][2] = entity.getLocation();
					pLoc[0][0].setY(pLoc[0][0].getY() + 0.2);
					pLoc[0][1].setY(pLoc[0][1].getY() + 0.2);
					pLoc[0][2].setY(pLoc[0][2].getY() + 0.2);

					for (i = 0; i < 6; i++) {
						for (j = 0; j < 3; j++) {
							pLoc[0][j].add(pDir[0][j].clone().multiply(0.2 * p));
							if (i == 5) {
								GeneralMethods.displayColoredParticle(pLoc[0][j], "81c6c8", 0, 0, 0);
							} else {
								GeneralMethods.displayColoredParticle(pLoc[0][j], "4e5c5c", 0, 0, 0);
							}
						}
					}
					if (p >= 0.2) {
						p -= 0.05;
					} else {
						phase = -20;
					}
					for (PotionEffect effect : entity.getActivePotionEffects()) {
						if (effect.getType().getName() == "SLOW") {
							flag = 1;
						}
					}
					if (flag == 0)
						this.cancel();
					flag = 0;
				}
			}.runTaskTimer(ProjectKorra.plugin, 0, 0);

		} else if (level == 2) {

			player.getWorld().playSound(entity.getEyeLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 2, 0);
			new BukkitRunnable() {
				int j, flag = 0;
				double i, angle, p = 1;
				double phase = 0;

				@Override
				public void run() {

					if (phase < 120) {
						phase += 20;
					} else {
						phase = 0;
					}

					for (i = -arc + phase, j = 0; i < arc + phase; i += 120, j++) {
						angle = Math.toRadians(i);
						pDir[0][j] = player.getEyeLocation().getDirection();
						pDir[0][j].setX(pDir[0][j].getX() * Math.cos(angle) - pDir[0][j].getZ() * Math.sin(angle));
						pDir[0][j].setZ(pDir[0][j].getX() * Math.sin(angle) + pDir[0][j].getZ() * Math.cos(angle));
					}

					pLoc[0][0] = entity.getLocation();
					pLoc[0][1] = entity.getLocation();
					pLoc[0][2] = entity.getLocation();
					pLoc[0][0].setY(pLoc[0][0].getY() + 0.9);
					pLoc[0][1].setY(pLoc[0][1].getY() + 0.9);
					pLoc[0][2].setY(pLoc[0][2].getY() + 0.9);

					for (i = 0; i < 6; i++) {
						for (j = 0; j < 3; j++) {
							pLoc[0][j].add(pDir[0][j].clone().multiply(0.2 * p));
							if (i == 5) {
								GeneralMethods.displayColoredParticle(pLoc[0][j], "81c6c8", 0, 0, 0);
							} else {
								GeneralMethods.displayColoredParticle(pLoc[0][j], "4e5c5c", 0, 0, 0);
							}
						}
					}
					if (p >= 0.4) {
						p -= 0.05;
					} else {
						phase = -20;
					}
					for (PotionEffect effect : entity.getActivePotionEffects()) {
						if (effect.getType().getName() == "SLOW") {
							flag = 1;
						}
					}
					if (flag == 0)
						this.cancel();
					flag = 0;
				}
			}.runTaskTimer(ProjectKorra.plugin, 0, 0);

		} else if (level == 3) {

			player.getWorld().playSound(entity.getEyeLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 2, 0);
			new BukkitRunnable() {
				int j, flag = 0;
				double i, angle, p = 1;
				double phase = 0;

				@Override
				public void run() {

					if (phase < 120) {
						phase += 20;
					} else {
						phase = 0;
					}

					for (i = -arc + phase, j = 0; i < arc + phase; i += 120, j++) {
						angle = Math.toRadians(i);
						pDir[0][j] = player.getEyeLocation().getDirection();
						pDir[0][j].setX(pDir[0][j].getX() * Math.cos(angle) - pDir[0][j].getZ() * Math.sin(angle));
						pDir[0][j].setZ(pDir[0][j].getX() * Math.sin(angle) + pDir[0][j].getZ() * Math.cos(angle));
					}

					pLoc[0][0] = entity.getLocation();
					pLoc[0][1] = entity.getLocation();
					pLoc[0][2] = entity.getLocation();
					pLoc[0][0].setY(pLoc[0][0].getY() + 1.3);
					pLoc[0][1].setY(pLoc[0][1].getY() + 1.3);
					pLoc[0][2].setY(pLoc[0][2].getY() + 1.3);

					for (i = 0; i < 6; i++) {
						for (j = 0; j < 3; j++) {
							pLoc[0][j].add(pDir[0][j].clone().multiply(0.2 * p));
							if (i == 5) {
								GeneralMethods.displayColoredParticle(pLoc[0][j], "81c6c8", 0, 0, 0);
							} else {
								GeneralMethods.displayColoredParticle(pLoc[0][j], "4e5c5c", 0, 0, 0);
							}
						}
					}
					if (p >= 0.4) {
						p -= 0.05;
					} else {
						phase = -20;
					}
					for (PotionEffect effect : entity.getActivePotionEffects()) {
						if (effect.getType().getName() == "SLOW") {
							flag = 1;
						}
					}
					if (flag == 0)
						this.cancel();
					flag = 0;
				}
			}.runTaskTimer(ProjectKorra.plugin, 0, 0);
		}

	}

	@Override
	public void progress() {
		int i;

		if (player.isDead() || !player.isOnline()
				|| GeneralMethods.isRegionProtectedFromBuild(this, player.getLocation())) {
			remove();
			return;
		}

		if (loc[0][0] != null && origin[0].distance(loc[0][0]) > range) {
			loc[0][0] = null;
		}

		if (loc[1][0] != null && origin[1].distance(loc[1][0]) > range) {
			loc[1][0] = null;
		}

		if (loc[2][0] != null && origin[2].distance(loc[2][0]) > range) {
			loc[2][0] = null;
		}

		if (duration != 0 && System.currentTimeMillis() >= getStartTime() + duration) {
			if (!bPlayer.isOnCooldown(this)) {
				bPlayer.addCooldown(this);
			}
			if(charges == 1 && loc[2][0] == null) {
				remove();
				return;
			} else if (charges != 1) {
				remove();
				return;
			}
		}

		if (charges >= 1) {
			for (i = 0; i < 4 - charges; i++) {
				if (loc[i][0] != null) {
					setArrays(i, pDir[i][3]);
				}
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
		return "Bola";
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
	public String getDescription() {
		return "Throw your bola at your targets to slow and catch them!";
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
		return "1.1";
	}

	@Override
	public void load() {
		ProjectKorra.plugin.getServer().getPluginManager().registerEvents(new BolaListener(), ProjectKorra.plugin);
		ProjectKorra.log.info("Succesfully enabled " + getName() + " by " + getAuthor());

		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Chi.Bola.Cooldown", 4000);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Chi.Bola.MaxShotTime", 4000);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Chi.Bola.TimeBetweenShots", 500);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Chi.Bola.Range", 20);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Chi.Bola.Speed", 1.25);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Chi.Bola.FirstSlowness", 1);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Chi.Bola.FirstDuration", 5);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Chi.Bola.FirstDamage", 2);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Chi.Bola.SecondSlowness", 3);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Chi.Bola.SecondDuration", 5);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Chi.Bola.SecondDamage", 4);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Chi.Bola.ThirdSlowness", 6);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Chi.Bola.ThirdDuration", 5);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Chi.Bola.ThirdDamage", 6);
		ConfigManager.defaultConfig.save();
	}

	@Override
	public void stop() {
		ProjectKorra.log.info("Successfully disabled " + getName() + " by " + getAuthor());
		super.remove();
	}

}
