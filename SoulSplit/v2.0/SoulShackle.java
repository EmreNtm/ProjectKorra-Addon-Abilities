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
import me.xnuminousx.spirits.ability.api.DarkAbility;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class SoulShackle
  extends DarkAbility
  implements AddonAbility, ComboAbility
{
  private LivingEntity target = null;
  private Location targetLoc;
  private Location location;
  private int range;
  private long time;
  private long duration;
  private Location origin;
  private Vector direction;
  private double radius;
  private int currPoint;
  private boolean progress;
  private boolean registerLoc;
  private long cooldown;
  
  private Soul soul;
  private String eye;
  
  public SoulShackle(Player player, Soul soul)
  {
    super(player);
    
    if (bPlayer.isOnCooldown(this))
		return;
    
    if (!this.bPlayer.canBendIgnoreBinds(this)) {
        return;
    }
    setFields();
    
    this.soul = soul;
    eye = soul.getEyeColor();
    soul.setEyeColor("800080");
    
    this.origin = soul.getLoc().clone().add(0.0D, 1.0D, 0.0D);
    this.location = this.origin.clone();
    this.direction = this.soul.getDir().setY(0);
    
    this.time = System.currentTimeMillis();
    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDER_EYE_DEATH, 1.0F, -1.0F);
    start();
  }
  
  private void setFields()
  {
    this.cooldown = ConfigManager.getConfig().getLong("ExtraAbilities.Hiro3.Spirit.DarkSpirit.SoulShackle.Cooldown");
    this.duration = ConfigManager.getConfig().getLong("ExtraAbilities.Hiro3.Spirit.DarkSpirit.SoulShackle.Duration");
    this.range = ConfigManager.getConfig().getInt("ExtraAbilities.Hiro3.Spirit.DarkSpirit.SoulShackle.Range");
    this.radius = ConfigManager.getConfig().getDouble("ExtraAbilities.Hiro3.Spirit.DarkSpirit.SoulShackle.Radius");
    this.progress = true;
    this.registerLoc = false;
  }
  
  public void progress()
  {
    if ((this.player.isDead()) || (!this.player.isOnline()) || (GeneralMethods.isRegionProtectedFromBuild(this, this.origin)))
    {
      remove();
      return;
    }
    if ((this.origin.distanceSquared(this.location) > this.range * this.range) && (this.target == null))
    {
      remove();
      return;
    }
    bind();
  }
  
  public void bind()
  {
    if (this.progress)
    {
      this.location.add(this.direction.multiply(1));
      
      blastSpiral(200, 0.04F, this.location);
    }
    if (this.target == null)
    {
      for (Entity entity : GeneralMethods.getEntitiesAroundPoint(this.location, this.radius)) {
        if (((entity instanceof LivingEntity)) && (entity.getUniqueId() != this.player.getUniqueId()))
        {
          this.target = ((LivingEntity)entity);
          this.registerLoc = true;
        }
      }
    }
    else
    {
      if (this.registerLoc)
      {
        this.targetLoc = this.target.getLocation();
        this.registerLoc = false;
      }
      if ((this.target.isDead()) || (this.target.getWorld() != this.player.getWorld()))
      {
        remove();
        return;
      }
      if (System.currentTimeMillis() > this.time + this.duration)
      {
    	  ParticleEffect.CLOUD.display(this.targetLoc, 5, 0.0D, 0.0D, 0.0D, 0.07999999821186066D);
        this.player.getWorld().playSound(this.targetLoc, Sound.BLOCK_IRON_TRAPDOOR_CLOSE, 0.5F, 1.5F);
        remove();
        return;
      }
      for (Entity entity : GeneralMethods.getEntitiesAroundPoint(this.targetLoc, 2.0D)) {
        if ((entity != this.target) || (entity == null))
        {
          remove();
          return;
        }
      }
      this.progress = false;
      Vector vec = this.targetLoc.getDirection().normalize().multiply(0);
      this.target.setVelocity(vec);
      this.targetLoc.setPitch(this.targetLoc.getPitch());
      this.targetLoc.setYaw(this.targetLoc.getYaw());
      
      holdSpiral(30, 0.04F, this.targetLoc);
    }
  }
  
  public void blastSpiral(int points, float size, Location location)
  {
    for (int i = 0; i < 6; i++)
    {
      this.currPoint += 360 / points;
      if (this.currPoint > 360) {
        this.currPoint = 0;
      }
      double angle = this.currPoint * 3.141592653589793D / 180.0D * Math.cos(3.141592653589793D);
      double x = size * (12.566370614359172D - angle) * Math.cos(angle + i);
      double z = size * (12.566370614359172D - angle) * Math.sin(angle + i);
      location.add(x, 0.10000000149011612D, z);
      ParticleEffect.SPELL_WITCH.display(location, 1, 0.0D, 0.0D, 0.0D, 0.0D);
      location.subtract(x, 0.10000000149011612D, z);
    }
  }
  
  public void holdSpiral(int points, float size, Location location)
  {
    for (int t = 0; t < 2; t++)
    {
      this.currPoint += 360 / points;
      if (this.currPoint > 360) {
        this.currPoint = 0;
      }
      double angle2 = this.currPoint * 3.141592653589793D / 180.0D * Math.cos(3.141592653589793D);
      double x2 = size * (15.707963267948966D - angle2) * Math.cos(angle2 + t);
      double z2 = size * (15.707963267948966D - angle2) * Math.sin(angle2 + t);
      location.add(x2, 0.10000000149011612D, z2);
      ParticleEffect.SPELL_WITCH.display(location, 1, 0.0D, 0.0D, 0.0D, 0.0D);
      location.subtract(x2, 0.10000000149011612D, z2);
    }
  }
  
  public long getCooldown()
  {
    return this.cooldown;
  }
  
  public Location getLocation()
  {
    return null;
  }
  
  public String getName()
  {
    return "SoulShackle";
  }
  
  public String getDescription()
  {
    return "Make your souls cast Shackle.\n(I give all credits to the xNuminousx)";
  }
  
  public String getInstructions()
  {
    return "Shackle (Tap Sneak) -> Shackle (Tap Sneak) -> Shackle (Left Click)";
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
	  
	  ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Spirit.DarkSpirit.SoulShackle.Duration", 2500);
	  ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Spirit.DarkSpirit.SoulShackle.Range", 10);
	  ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Spirit.DarkSpirit.SoulShackle.Radius", 2);
	  ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Spirit.DarkSpirit.SoulShackle.Cooldown", 7000);
	  ConfigManager.defaultConfig.save();
  }
  
  public void stop() {
	  ProjectKorra.log.info("Successfully disabled " + getName() + " by " + getAuthor());
	  super.remove();
  }

  @Override
  public void remove() {
	  super.remove();
	  bPlayer.addCooldown(this);
	  if (soul.isAlive())
		  soul.setEyeColor(eye);
  }
  
	@Override
	public Object createNewComboInstance(Player player) {
		if (hasAbility(player, SoulSplit.class) && getAbility(player, SoulSplit.class).getSoulNo() != 0 && !CoreAbility.hasAbility(player, SoulShackle.class)) {
			for (Soul s : getAbility(player, SoulSplit.class).getSouls()) {
				new SoulShackle(player, s);
			}
		}
		return null;
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		ArrayList<AbilityInformation> combination = new ArrayList<>();
		combination.add(new AbilityInformation("Shackle", ClickType.SHIFT_DOWN));
		combination.add(new AbilityInformation("Shackle", ClickType.SHIFT_UP));
		combination.add(new AbilityInformation("Shackle", ClickType.SHIFT_DOWN));
		combination.add(new AbilityInformation("Shackle", ClickType.SHIFT_UP));
		combination.add(new AbilityInformation("Shackle", ClickType.LEFT_CLICK));

		return combination;
	}
}
