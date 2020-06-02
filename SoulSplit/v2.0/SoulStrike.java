package Hiro3;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;

import me.xnuminousx.spirits.ability.api.DarkAbility;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class SoulStrike
  extends DarkAbility
  implements AddonAbility, ComboAbility
{
  private long cooldown;
  private Location origin;
  private Location location;
  private int range;
  private Vector direction;
  private boolean progress;
  private double damage;
  
  private String eye;
  private Soul soul;
  
  public SoulStrike(Player player, Soul soul)
  {
    super(player);
    
    if (!this.bPlayer.canBendIgnoreBinds(this)) {
        return;
    }
    
    setFields();
    
    this.soul = soul;
    eye = soul.getEyeColor();
    soul.setEyeColor("800080");
    
    this.origin = this.soul.getLoc().clone().add(0.0D, 1.0D, 0.0D);
    this.location = this.origin.clone();
    this.direction = this.soul.getDir().setY(0);
    
    start();
  }
  
  private void setFields()
  {
    this.cooldown = 0;
    this.damage = ConfigManager.getConfig().getDouble("ExtraAbilities.Hiro3.Spirit.DarkSpirit.SoulStrike.Damage");
    this.range = ConfigManager.getConfig().getInt("ExtraAbilities.Hiro3.Spirit.DarkSpirit.SoulStrike.Range");
    this.progress = true;
  }
  
  public void progress()
  {
    if ((this.player.isDead()) || (!this.player.isOnline()) || (GeneralMethods.isRegionProtectedFromBuild(this, this.soul.getLoc())) || (this.origin.distanceSquared(this.location) > this.range * this.range))
    {
      remove();
      return;
    }
    
    if (GeneralMethods.isSolid(location.getBlock())) {
        remove();
        return;
    }
    
    strike();
  }
  
  public void strike()
  {
    if (this.progress)
    {
      this.location.add(this.direction.multiply(1));
      ParticleEffect.CRIT.display(this.location, 0, 0.0D, 0.0D, 0.0D, 1.0D);
    }
    for (Entity target : GeneralMethods.getEntitiesAroundPoint(this.location, 1.5D)) {
      if (((target instanceof LivingEntity)) && (target.getUniqueId() != this.player.getUniqueId()))
      {
        Location location = this.soul.getLoc().clone();
        this.progress = false;
        LivingEntity le = (LivingEntity)target;
        Location tLoc = le.getLocation().clone();
        tLoc.setPitch(location.getPitch());
        tLoc.setYaw(location.getYaw());
        this.soul.setLoc(tLoc);
        DamageHandler.damageEntity(target, this.damage, this);
        this.player.getWorld().playSound(location, Sound.ENTITY_PLAYER_BURP, 0.2F, 0.2F);
        
        this.bPlayer.addCooldown(this);
        remove();
        return;
      }
    }
  }
  
  public long getCooldown()
  {
    return cooldown;
  }
  
  public Location getLocation()
  {
    return null;
  }
  
  public String getName()
  {
    return "SoulStrike";
  }
  
  public String getDescription()
  {
    return "Make your souls cast Strike.\n(I give all credits to the xNuminousx)";
  }
  
  public String getInstructions()
  {
    return "Strike (Tap Sneak) -> Strike (Tap Sneak) -> Strike (Left Click)";
  }
  
  public String getAuthor()
  {
    return "Hiro3";
  }
  
  public String getVersion()
  {
	  return "2.0";
  }
  
  public boolean isExplosiveAbility()
  {
    return false;
  }
  
  public boolean isHarmlessAbility()
  {
    return false;
  }
  
  public boolean isIgniteAbility()
  {
    return false;
  }
  
  public boolean isSneakAbility()
  {
    return false;
  }
  
  public void load() {
	  ProjectKorra.log.info("Succesfully enabled " + getName() + " by " + getAuthor());
	  
	  ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Spirit.DarkSpirit.SoulStrike.Damage", 3);
	  ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Spirit.DarkSpirit.SoulStrike.Range", 5);
	  ConfigManager.defaultConfig.save();
  }
  
  public void stop() {
	  ProjectKorra.log.info("Successfully disabled " + getName() + " by " + getAuthor());
	  super.remove();
  }

  @Override
  public void remove() {
	  super.remove();
	  if (soul.isAlive())
		  soul.setEyeColor(eye);
  }
  
	@Override
	public Object createNewComboInstance(Player player) {
		if (hasAbility(player, SoulSplit.class) && getAbility(player, SoulSplit.class).getSoulNo() != 0 && !CoreAbility.hasAbility(player, SoulStrike.class)) {
			for (Soul s : getAbility(player, SoulSplit.class).getSouls()) {
				new SoulStrike(player, s);
			}
		}
		return null;
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		ArrayList<AbilityInformation> combination = new ArrayList<>();
		combination.add(new AbilityInformation("Strike", ClickType.SHIFT_DOWN));
		combination.add(new AbilityInformation("Strike", ClickType.SHIFT_UP));
		combination.add(new AbilityInformation("Strike", ClickType.SHIFT_DOWN));
		combination.add(new AbilityInformation("Strike", ClickType.SHIFT_UP));
		combination.add(new AbilityInformation("Strike", ClickType.LEFT_CLICK));

		return combination;
	}
}
