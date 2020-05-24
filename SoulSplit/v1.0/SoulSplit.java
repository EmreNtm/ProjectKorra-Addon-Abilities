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
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SoulSplit extends SpiritAbility implements AddonAbility {
	
	private SoulSplitListener SSL;
  
	private long duration;
	private long cooldown;
	private long chargeTime;
	private double startingMaxHealth;
	private int maxSoulNumber;
	private int soulHeart;
	private ArrayList<Soul> souls;
	private int soulNo;
	private int state;
	private long soulStartTime;
	private long durationStartTime;
	
	private boolean isClicked;
	private double soulThrowDistance;
	
	public SoulSplit(Player player) {
		super(player);
    
		 if (!this.bPlayer.canBendIgnoreBinds(this)) {
		      return;
		 }
		
		setfield();
		start();
	}
  
	@SuppressWarnings("deprecation")
	public void setfield() {
		duration = ConfigManager.getConfig().getLong("ExtraAbilities.Hiro3.Spirit.SoulSplit.Duration");
		cooldown = ConfigManager.getConfig().getLong("ExtraAbilities.Hiro3.Spirit.SoulSplit.Cooldown");
		chargeTime = ConfigManager.getConfig().getLong("ExtraAbilities.Hiro3.Spirit.SoulSplit.ChargeTime");
		souls = new ArrayList<Soul>(0);
		setSoulNo(0);
		state = 0;
		soulStartTime = System.currentTimeMillis();
		durationStartTime = System.currentTimeMillis();
		
		isClicked = false;
		soulThrowDistance = ConfigManager.getConfig().getDouble("ExtraAbilities.Hiro3.Spirit.SoulSplit.SoulThrowDistance");
		
		startingMaxHealth = player.getMaxHealth();
		maxSoulNumber = ConfigManager.getConfig().getInt("ExtraAbilities.Hiro3.Spirit.SoulSplit.MaxSoulNumber");
		soulHeart = ConfigManager.getConfig().getInt("ExtraAbilities.Hiro3.Spirit.SoulSplit.SoulHeart");
		
		if (maxSoulNumber * soulHeart > startingMaxHealth-1) {
			soulHeart = (int) (startingMaxHealth / 4);
			maxSoulNumber = (int) (startingMaxHealth / soulHeart);
			if (startingMaxHealth % 2 == 0)
				maxSoulNumber--;
		}
	}
  
	public void progress() {
	  
		if (soulNo != 0 && System.currentTimeMillis() > durationStartTime + duration) {
			bPlayer.addCooldown(this);
			remove();
		  	return;
		}
    
		if (soulNo != maxSoulNumber) {
			if (bPlayer.getBoundAbilityName().equalsIgnoreCase("SoulSplit")) {
				tryToCreateSoul();
			} else if (state % 2 == 1) {
				state--;
			}
		}
    
		if (soulNo != 0) {
			for (Soul s : souls) {
				s.display();
			}
			collectForm();
			hitForm();
		}
	  
	}
	
	public void hitForm() {
		ArrayList<Soul> tmpSouls = new ArrayList<Soul>(0);
		for (Soul s : souls) {
			for (Entity p : GeneralMethods.getEntitiesAroundPoint(s.getLoc(), 2)) {
				if (p instanceof Player && s.hitSoul((Player) p)) {
					tmpSouls.add(s);
					soulNo -= 1;
					state -= 2;
				}
			}
		}
		
		if (soulNo == 0) {
			bPlayer.addCooldown(this);
			remove();
		}
		
		for (Soul s : tmpSouls) {
			souls.remove(s);
		}
	}
	
	public void collectForm() {
		ArrayList<Soul> tmpSouls = new ArrayList<Soul>(0);
		for (Soul s : souls) {
			if (s.collectSoul()) {
				tmpSouls.add(s);
				soulNo -= 1;
				state -= 2;
			}
		}
		
		if (soulNo == 0) {
			bPlayer.addCooldown(this);
			remove();
		}
		
		for (Soul s : tmpSouls) {
			souls.remove(s);
		}
	}
    
	@SuppressWarnings("deprecation")
	public void tryToCreateSoul() {
		for (int i = 0; i < maxSoulNumber; i++) {
			if (soulNo == i) { 
				if (!player.isSneaking() && state == 2*i) { 
					soulStartTime = System.currentTimeMillis();
					return;
				}
				if (player.isSneaking() && state == 2*i) {
					if (System.currentTimeMillis() > soulStartTime + chargeTime) {
						state += 1;
					}
				} else if (state == 2*i+1) {
					player.getWorld().spawnParticle(Particle.CLOUD, player.getEyeLocation().add(player.getLocation().getDirection().multiply(0.4)), 0);
					if (!player.isSneaking() || this.isClicked) {
						double tmpHealth = player.getHealth() - (startingMaxHealth - soulHeart * (i+1));
						if (tmpHealth < 0)
							tmpHealth = 0;
						else if (tmpHealth > soulHeart)
							tmpHealth = soulHeart;
						Soul soul = new Soul(this, player, player.getLocation(), soulHeart, tmpHealth);
						souls.add(soul);
						//player.getWorld().playSound(soul.getLoc(), Sound.ENTITY_ILLUSION_ILLAGER_CAST_SPELL, 1, 10);
						player.getWorld().playSound(soul.getLoc(), Sound.BLOCK_CHORUS_FLOWER_GROW, 1, 10);
						PotionEffect pe = new PotionEffect(PotionEffectType.BLINDNESS, 20, 1);
						player.addPotionEffect(pe);
						player.setHealthScale(startingMaxHealth - soul.getHearts() * (i+1));
						player.setMaxHealth(startingMaxHealth - soul.getHearts() * (i+1));
						durationStartTime = System.currentTimeMillis();
						/* */
						if (this.isClicked) {
							soul.moveSoul(this.soulThrowDistance);
							this.isClicked = false;
							soulStartTime = System.currentTimeMillis();
							soul.setSoulWaitTime(250);
						}
						/* */
						state += 1;
						setSoulNo(getSoulNo() + 1);
						if (getSoulNo() == maxSoulNumber)
							player.sendMessage(ChatColor.AQUA + "You have reached the maximum soul number.");
					}
				}
			}
		}
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
  
	public boolean isClicked() {
		return this.isClicked;
	}
	
	public void setIsClicked(boolean isClicked) {
		this.isClicked = isClicked;
	}
	
	public int getState() {
		return this.state;
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
		return "Hold sneak untill you see the particles. \nLeft click to throw the soul or release sneak to create the soul on where you stand.";
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
		player.setHealthScale(startingMaxHealth);
		player.setMaxHealth(startingMaxHealth);
		if(player.getHealth() != 0 && soulNo != 0) {
			for (Soul s : souls) {
				player.getWorld().playSound(s.getLoc(), Sound.BLOCK_CHORUS_FLOWER_GROW, 1, 10);
				PotionEffect pe = new PotionEffect(PotionEffectType.NIGHT_VISION, 20, 1);
				player.addPotionEffect(pe);
				player.setHealth(player.getHealth() + s.getHealth());
			}
		}

		for (Soul s : souls) {
			s.setAlive(false);
		}
	}
  
	public void load() {
		SSL = new SoulSplitListener();
		ProjectKorra.plugin.getServer().getPluginManager().registerEvents(SSL, ProjectKorra.plugin);
		ProjectKorra.log.info("Succesfully enabled " + getName() + " by " + getAuthor());
		
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Spirit.SoulSplit.Cooldown", 3000);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Spirit.SoulSplit.Duration", 30000);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Spirit.SoulSplit.ChargeTime", 1000);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Spirit.SoulSplit.InactiveSoulTime", 2000);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Spirit.SoulSplit.MaxSoulNumber", 4);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Spirit.SoulSplit.SoulHeart", 4);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Spirit.SoulSplit.isStackable", false);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Spirit.SoulSplit.SoulThrowDistance", 5);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Spirit.SoulSplit.SoulThrowSpeed", 0.5);		
		ConfigManager.defaultConfig.save();
	}
  
	@SuppressWarnings("deprecation")
	public void stop()  {
		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
			if (p.getHealthScale() != startingMaxHealth) {
				p.setHealthScale(startingMaxHealth);
				p.setMaxHealth(startingMaxHealth);
			}
		}
		ProjectKorra.log.info("Successfully disabled " + getName() + " by " + getAuthor());
		HandlerList.unregisterAll(SSL);
		super.remove();
  	}
}

