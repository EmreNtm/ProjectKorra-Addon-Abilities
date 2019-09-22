package Hiro3;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;

import java.util.ArrayList;
import me.xnuminousx.spirits.ability.api.SpiritAbility;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class SoulSplit extends SpiritAbility implements AddonAbility {
	
	private SoulSplitListener SSL;
  
	private long duration;
	private long cooldown;
	private long chargeTime;
	private ArrayList<Soul> souls;
  
	private int soulNo;
	private int state;
	private long soulStartTime;
	private long durationStartTime;
	private long soulWaitTime;
  
	public SoulSplit(Player player) {
		super(player);
    
		 if (!this.bPlayer.canBendIgnoreBinds(this)) {
		      return;
		 }
		
		setfield();
		start();
	}
  
	public void setfield() {
		duration = ConfigManager.getConfig().getLong("ExtraAbilities.Hiro3.Spirit.SoulSplit.Duration");
		cooldown = ConfigManager.getConfig().getLong("ExtraAbilities.Hiro3.Spirit.SoulSplit.Cooldown");
		chargeTime = ConfigManager.getConfig().getLong("ExtraAbilities.Hiro3.Spirit.SoulSplit.ChargeTime");
		souls = new ArrayList<Soul>(0);
		setSoulNo(0);
		state = 0;
		soulStartTime = System.currentTimeMillis();
		durationStartTime = System.currentTimeMillis();
		soulWaitTime = ConfigManager.getConfig().getLong("ExtraAbilities.Hiro3.Spirit.SoulSplit.InactiveSoulTime");
	}
  
	public void progress() {
	  
		if (soulNo != 0 && System.currentTimeMillis() > durationStartTime + duration) {
			bPlayer.addCooldown(this);
			remove();
		  	return;
		}
    
		if (soulNo != 4) {
			if (bPlayer.getBoundAbilityName().equalsIgnoreCase("SoulSplit")) {
				tryToCreateSoul();
			} else if (state == 1 || state == 3 || state == 5 || state == 7) {
				state--;
			}
		}
    
		if (soulNo != 0) {
			for (Soul s : souls) {
				s.display();
			}
			collectSoul();
			hitSoul();
		}
	  
	}
  
	@SuppressWarnings("deprecation")
	public void hitSoul() {
		Soul tmpSoul = null;
		for (Soul s : souls) {
			for (Entity p : GeneralMethods.getEntitiesAroundPoint(s.getLoc(), 2)) {
				if (p instanceof Player && p.getUniqueId() != player.getUniqueId() && System.currentTimeMillis() > s.getStartTime() + soulWaitTime) {
					tmpSoul = s;
					s.setAlive(false);
					soulNo -= 1;
					state -= 2;
					player.setHealthScale(player.getHealthScale() + 4);
					player.setMaxHealth(player.getMaxHealth() + 4);
					player.setHealth(player.getHealth() + 4);
					//A solution to damage doesn't stack problem.
					((Player) p).damage(0.01);
					if (((Player) p).getHealth() - 4 < 0)
						((Player) p).setHealth(0);
					else
						((Player) p).setHealth(((Player) p).getHealth() - 4);
					p.getWorld().playSound(s.getLoc(), Sound.ENTITY_GENERIC_EXTINGUISH_FIRE, 1, 10);
					PotionEffect pe = new PotionEffect(PotionEffectType.NIGHT_VISION, 20, 1);
	  	  			player.addPotionEffect(pe);
					explode(p.getWorld(), s.getLoc(), Particle.CLOUD);
					if (soulNo == 0) {
						bPlayer.addCooldown(this);
						remove();
					}
				}
			}
		}
    
		if (tmpSoul != null) {
			souls.remove(tmpSoul);
		}  
    }
  
	@SuppressWarnings("deprecation")
	public void collectSoul() {
		Soul tmpSoul = null;
  	  	for (Soul s : souls) {
  	  		if (player.getLocation().getWorld().equals(s.getLoc().getWorld()) && player.getLocation().distance(s.getLoc()) < 1 && System.currentTimeMillis() > s.getStartTime() + soulWaitTime) {
  	  			tmpSoul = s;
  	  			s.setAlive(false);
  	  			soulNo -= 1;
  	  			state -= 2;
  	  			player.setHealthScale(player.getHealthScale() + 4);
  	  			player.setMaxHealth(player.getMaxHealth() + 4);
  	  			if (player.getHealth() != 0)
  	  				player.setHealth(player.getHealth() + 4);
  	  			//player.getWorld().playSound(s.getLoc(), Sound.ENTITY_ILLUSION_ILLAGER_PREPARE_MIRROR, 1, 10);
  	  			player.getWorld().playSound(s.getLoc(), Sound.BLOCK_CHORUS_FLOWER_GROW, 1, 10);
  	  			PotionEffect pe = new PotionEffect(PotionEffectType.NIGHT_VISION, 20, 1);
  	  			player.addPotionEffect(pe);
  	  			if (soulNo == 0) {
  	  				bPlayer.addCooldown(this);
  	  				remove();
  	  			}
  	  		}
  	  	}
    
  	  	if (tmpSoul != null) {
  	  		souls.remove(tmpSoul);
  	  	}
  	  
    }
  
	@SuppressWarnings("deprecation")
	public void tryToCreateSoul() {
	  
		if (soulNo == 0) {
			if (!player.isSneaking() && state == 0) {
				soulStartTime = System.currentTimeMillis();
				return;
			}
			if (player.isSneaking() && state == 0) {
				if (System.currentTimeMillis() > soulStartTime + chargeTime) {
					state += 1;
				}
			} else if (state == 1) {
				player.getWorld().spawnParticle(Particle.CLOUD, player.getEyeLocation().add(player.getLocation().getDirection().multiply(0.4)), 0);
				if (!player.isSneaking()) {
					Soul soul = new Soul(player, player.getLocation(), 4);
					souls.add(soul);
					//player.getWorld().playSound(soul.getLoc(), Sound.ENTITY_ILLUSION_ILLAGER_CAST_SPELL, 1, 10);
					player.getWorld().playSound(soul.getLoc(), Sound.BLOCK_CHORUS_FLOWER_GROW, 1, 10);
					PotionEffect pe = new PotionEffect(PotionEffectType.BLINDNESS, 20, 1);
					player.addPotionEffect(pe);
					player.setHealthScale(20 - soul.getHearts());
					player.setMaxHealth(20 - soul.getHearts());
					durationStartTime = System.currentTimeMillis();
					state += 1;
					setSoulNo(getSoulNo() + 1);
				}
			}
		}
	    
		if (soulNo == 1) {
			if (!player.isSneaking() && state == 2) {
				soulStartTime = System.currentTimeMillis();
			}
			if (player.isSneaking() && state == 2) {
				if (System.currentTimeMillis() > soulStartTime + chargeTime) {
					state += 1;
				}
			} else if (state == 3) {
				player.getWorld().spawnParticle(Particle.CLOUD, player.getEyeLocation().add(player.getLocation().getDirection().multiply(0.4D)), 0);
				if (!player.isSneaking()) {
					Soul soul = new Soul(player, player.getLocation(), 4);
					souls.add(soul);
					//player.getWorld().playSound(soul.getLoc(), Sound.ENTITY_ILLUSION_ILLAGER_CAST_SPELL, 1, 10);
					player.getWorld().playSound(soul.getLoc(), Sound.BLOCK_CHORUS_FLOWER_GROW, 1, 10);
					PotionEffect pe = new PotionEffect(PotionEffectType.BLINDNESS, 20, 1);
					player.addPotionEffect(pe);
		          	player.setHealthScale(20 - soul.getHearts() * 2);
		          	player.setMaxHealth(20 - soul.getHearts() * 2);
		          	durationStartTime = System.currentTimeMillis();
		          	state += 1;
		          	setSoulNo(getSoulNo() + 1);
				}
			}
		}
	    
	    
		if (soulNo == 2) {
			if (!player.isSneaking() && state == 4) {
				soulStartTime = System.currentTimeMillis();
			}
			if (player.isSneaking() && state == 4) {
				if (System.currentTimeMillis() > soulStartTime + chargeTime) {
					state += 1;
				}
			} else if (state == 5) {
				player.getWorld().spawnParticle(Particle.CLOUD, player.getEyeLocation().add(player.getLocation().getDirection().multiply(0.4D)), 0);
				if (!player.isSneaking()) {
					Soul soul = new Soul(player, player.getLocation(), 4);
					souls.add(soul);
					//player.getWorld().playSound(soul.getLoc(), Sound.ENTITY_ILLUSION_ILLAGER_CAST_SPELL, 1, 10);
					player.getWorld().playSound(soul.getLoc(), Sound.BLOCK_CHORUS_FLOWER_GROW, 1, 10);
					PotionEffect pe = new PotionEffect(PotionEffectType.BLINDNESS, 20, 1);
					player.addPotionEffect(pe);
				  	player.setHealthScale(20 - soul.getHearts() * 3);
				  	player.setMaxHealth(20 - soul.getHearts() * 3);
				  	durationStartTime = System.currentTimeMillis();
				  	state += 1;
				  	setSoulNo(getSoulNo() + 1);
				}
			}
		}
	    
	    
		if (soulNo == 3) {
			if (!player.isSneaking() && state == 6) {
				soulStartTime = System.currentTimeMillis();
			}
			if (player.isSneaking() && state == 6) {
				if (System.currentTimeMillis() > soulStartTime + chargeTime) {
					state += 1;
				}
			} else if (state == 7) {
				player.getWorld().spawnParticle(Particle.CLOUD, player.getEyeLocation().add(player.getLocation().getDirection().multiply(0.4D)), 0);
				if (!player.isSneaking()) {
					Soul soul = new Soul(player, player.getLocation(), 4);
					souls.add(soul);
					//player.getWorld().playSound(soul.getLoc(), Sound.ENTITY_ILLUSION_ILLAGER_CAST_SPELL, 1, 10);
					player.getWorld().playSound(soul.getLoc(), Sound.BLOCK_CHORUS_FLOWER_GROW, 1, 10);
					PotionEffect pe = new PotionEffect(PotionEffectType.BLINDNESS, 20, 1);
					player.addPotionEffect(pe);
					player.setHealthScale(20 - soul.getHearts() * 4);
					player.setMaxHealth(20 - soul.getHearts() * 4);
					durationStartTime = System.currentTimeMillis();
					state += 1;
				  	setSoulNo(getSoulNo() + 1);
				  	player.sendMessage(ChatColor.AQUA + "You have reached the maximum soul number.");
				}
			}
		}
	}
    
  
	public void explode(World world, Location center, Particle particle) {
		new BukkitRunnable() {
    	  
			Location loc = center.clone();
			double radius = 2.0;
			double tmpRadius;
			int count = 1;
			
			public void run() {
				tmpRadius = 0.0;
				for (double i = -0.5 * count; i <= 0.5 * count; i += 0.5) {
					for (int j = 0; j <= 360; j += 30) {
						double angle = Math.toRadians(j);
						loc.setX(center.getX() + tmpRadius * Math.cos(angle));
						loc.setZ(center.getZ() + tmpRadius * Math.sin(angle));
						loc.setY(center.getY() + i);
						world.spawnParticle(particle, loc, 0);
					}
					if (i < 0) {
						tmpRadius += radius / (1 * count) * -i / 5;
					} else {
						tmpRadius -= radius / (1 * count) * i / 5;
					}
				}
				radius += 1;
				count += 1;
				if (count > 8) {
					Bukkit.getScheduler().cancelTask(getTaskId());
				}
			}  
		}.runTaskTimer(ProjectKorra.plugin, 0, 0);
    }
  
	public int getSoulNo() {
		return this.soulNo;
	}
  
	public void setSoulNo(int soulNo) {
		this.soulNo = soulNo;
	}
	
	public ArrayList<Soul> getSouls() {
		return this.souls;
	}
  
	public long getCooldown() {
		return cooldown;
	}
  
	public Location getLocation() {
		return null;
	}
  
	public String getName() {
		return "SoulSplit";
	}
  
	@Override
	public String getDescription() {
		return "Split your soul by sacrificing your maximum health and cast abilities with these spiritual companions.\n"
				+ "(Interactive with all other soul moves.)";
	}

	@Override
	public String getInstructions() {
		return "Hold sneak untill you see the particles.";
	}
	
	public boolean isExplosiveAbility() {
		return false;
	}
  
	public boolean isHarmlessAbility() {
		return false;
	}
  
	public boolean isIgniteAbility() {
		return false;
	}
  
	public boolean isSneakAbility() {
		return false;
	}
  
	public String getAuthor() {
		return "Hiro3";
	}	
  
	public String getVersion() {
		return "1.0";
	}
  
	@SuppressWarnings("deprecation")
	public void remove() {
		super.remove();
		player.setHealthScale(20);
		player.setMaxHealth(20);
		if(player.getHealth() != 0 && soulNo != 0) {
			for (Soul s : souls) {
				player.getWorld().playSound(s.getLoc(), Sound.BLOCK_CHORUS_FLOWER_GROW, 1, 10);
				PotionEffect pe = new PotionEffect(PotionEffectType.NIGHT_VISION, 20, 1);
				player.addPotionEffect(pe);
				player.setHealth(player.getHealth() + 4);
			}
		}
	}
  
	public void load() {
		SSL = new SoulSplitListener();
		ProjectKorra.plugin.getServer().getPluginManager().registerEvents(SSL, ProjectKorra.plugin);
		ProjectKorra.log.info("Succesfully enabled " + getName() + " by " + getAuthor());
		
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Spirit.SoulSplit.Cooldown", 3000);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Spirit.SoulSplit.Duration", 15000);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Spirit.SoulSplit.ChargeTime", 1000);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Spirit.SoulSplit.InactiveSoulTime", 2000);
		ConfigManager.defaultConfig.save();
	}
  
	@SuppressWarnings("deprecation")
	public void stop()  {
		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
			if (p.getHealthScale() != 20) {
				p.setHealthScale(20);
				p.setMaxHealth(20);
			}
		}
		ProjectKorra.log.info("Successfully disabled " + getName() + " by " + getAuthor());
		HandlerList.unregisterAll(SSL);
		super.remove();
  	}
}

