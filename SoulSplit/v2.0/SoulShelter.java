package Hiro3;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.ClickType;
import com.projectkorra.projectkorra.util.ParticleEffect;

import java.util.ArrayList;
import me.xnuminousx.spirits.ability.api.LightAbility;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class SoulShelter
  extends LightAbility
  implements AddonAbility, ComboAbility
{
  public ShelterType shelterType;
  private boolean isDamaged;
  private Location location;
  private long time;
  private long duration;
  private Location origin;
  private float shieldSize;
  private long knockDis;
  private double currPoint;
  
  private Soul soul;
  private String eye;
  
  public static enum ShelterType
  {
    CLICK,  SHIFT;
  }
  
  public SoulShelter(Player player, ShelterType shelterType, Soul soul)
  {
    super(player);
	  
	if (!this.bPlayer.canBendIgnoreBinds(this)) {
		return;
	}
	  
    setFields();
    
    this.soul = soul;
    eye = soul.getEyeColor();
    soul.setEyeColor("42f57b");
    this.origin = soul.getLoc().clone().add(0.0D, 1.0D, 0.0D);
    this.location = this.origin.clone();
    
    this.time = System.currentTimeMillis();
    this.shelterType = shelterType;
    player.getWorld().playSound(player.getLocation(), Sound.BLOCK_PORTAL_TRAVEL, 0.1F, 2.0F);
    
    start();
  }
  
  private void setFields()
  {
    this.duration = ConfigManager.getConfig().getLong("ExtraAbilities.Hiro3.Spirit.LightSpirit.SoulShelter.Duration");
    this.shieldSize = ConfigManager.getConfig().getInt("ExtraAbilities.Hiro3.Spirit.LightSpirit.SoulShelter.ShieldSize");
    this.knockDis = ConfigManager.getConfig().getLong("ExtraAbilities.Hiro3.Spirit.LightSpirit.SoulShelter.KnockbackPower");
    this.isDamaged = false;
  }
  
  public void progress()
  {
    if ((this.player.isDead()) || (!this.player.isOnline()) || (GeneralMethods.isRegionProtectedFromBuild(this, this.location)))
    {
      remove();
      return;
    }

    if (!CoreAbility.hasAbility(player, SoulSplit.class)) {
    	remove();
    	return;
    }
    
    if(!soul.isAlive()) {
    	remove();
    	return;
    }
    
    if (this.shelterType == ShelterType.CLICK) {
      shieldOther();
    }
  }
  
  public void shieldOther()
  {


        if (System.currentTimeMillis() > this.time + this.duration)
        {
          remove();
          return;
        }

        this.location = soul.getLoc().clone().add(0.0D, 1.0D, 0.0D);
        if (this.isDamaged)
        {
          remove();
          return;
        }
        for (Entity target2 : GeneralMethods.getEntitiesAroundPoint(this.location, this.shieldSize)) {
          if ((target2 instanceof LivingEntity) && (!target2.getUniqueId().equals(player.getUniqueId())))
          {
            Vector vec = target2.getLocation().getDirection().normalize().multiply((float)-this.knockDis);
            vec.setY(1);
            target2.setVelocity(vec);
          }
        }
        rotateShield(this.location, 100, this.shieldSize);

    
  }
  
  public void rotateShield(Location location, int points, float size)
  {
    for (int t = 0; t < 6; t++)
    {
      this.currPoint += 360 / points;
      if (this.currPoint > 360) {
        this.currPoint = 0;
      }
      double angle = this.currPoint * 3.141592653589793D / 180.0D * Math.cos(3.141592653589793D);
      double x2 = size * Math.cos(angle);
      double y = 0.9D * (15.707963267948966D - t) - 10.0D;
      double z2 = size * Math.sin(angle);
      location.add(x2, y, z2);
      ParticleEffect.SPELL_INSTANT.display(location, 1, 0.5D, 0.5D, 0.5D, 0.0D);
      location.subtract(x2, y, z2);
    }
  }
  
  public long getCooldown()
  {
    return 0L;
  }
  
  public Location getLocation()
  {
    return null;
  }
  
  public String getName()
  {
    return "SoulShelter";
  }
  
  public String getDescription()
  {
    return "Make your souls cast Shelter.\n(I give all credits to the xNuminousx)";
  }
  
  public String getInstructions()
  {
    return "Shelter (Tap Sneak) -> Shelter (Tap Sneak) -> Shelter (Left Click)";
  }
  
  public String getAuthor()
  {
    return "Hiro3";
  }
  
  public String getVersion()
  {
	  return "2.0";
  }
  
  public boolean isEnabled()
  {
    return true;
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
	  
	  ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Spirit.LightSpirit.SoulShelter.Duration", 7000);
	  ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Spirit.LightSpirit.SoulShelter.ShieldSize", 5);
	  ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Spirit.LightSpirit.SoulShelter.KnockbackPower", 1);
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
		if (hasAbility(player, SoulSplit.class) && getAbility(player, SoulSplit.class).getSoulNo() != 0 && !CoreAbility.hasAbility(player, SoulShelter.class)) {
			for (Soul s : getAbility(player, SoulSplit.class).getSouls()) {
				new SoulShelter(player, ShelterType.CLICK, s);
			}
		}
		return null;
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		ArrayList<AbilityInformation> combination = new ArrayList<>();
		combination.add(new AbilityInformation("Shelter", ClickType.SHIFT_DOWN));
		combination.add(new AbilityInformation("Shelter", ClickType.SHIFT_UP));
		combination.add(new AbilityInformation("Shelter", ClickType.SHIFT_DOWN));
		combination.add(new AbilityInformation("Shelter", ClickType.SHIFT_UP));
		combination.add(new AbilityInformation("Shelter", ClickType.LEFT_CLICK));

		return combination;
	}
}
