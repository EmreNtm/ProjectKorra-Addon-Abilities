package Hiro3;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
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

import java.util.ArrayList;
import java.util.Random;
import me.xnuminousx.spirits.Methods;
import me.xnuminousx.spirits.ability.api.DarkAbility;
import me.xnuminousx.spirits.ability.dark.combo.Infest;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SoulInfest
  extends DarkAbility
  implements AddonAbility, ComboAbility
{
	
  private Location location;
  private Location location2;
  private Location circleCenter;
  private long time;
  private long cooldown;
  private long duration;
  private int radius;
  private int effectInt;
  private boolean removeWithoutSoul;
  private boolean damageEntities;
  private boolean healDarkSpirits;
  private double damage;
  private int currPoint;
  private Location location3;
  
  private Soul soul;
  private String eye;
  
  public SoulInfest(Player player, Soul soul)
  {
    super(player);
    
    if (!this.bPlayer.canBendIgnoreBinds(this)) {
      return;
    }
    
    if (!CoreAbility.hasAbility(player, SoulSplit.class)) {
    	return;
    }
    
    setFields();
    
    this.soul = soul;
    eye = soul.getEyeColor();
    soul.setEyeColor("800080");
    
    this.location = soul.getLoc().clone();
    this.location2 = soul.getLoc().clone();
    this.location3 = soul.getLoc().clone();
    this.circleCenter = soul.getLoc().clone();
    
    this.time = System.currentTimeMillis();
    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.3F, -1.0F);
    start();
    this.bPlayer.addCooldown(this);
  }
  
  private void setFields()
  {
	this.cooldown = 0;
	this.duration = ConfigManager.getConfig().getLong("ExtraAbilities.Hiro3.Spirit.DarkSpirit.SoulInfest.Duration");
	this.effectInt = ConfigManager.getConfig().getInt("ExtraAbilities.Hiro3.Spirit.DarkSpirit.SoulInfest.EffectInterval");
	this.damage = ConfigManager.getConfig().getDouble("ExtraAbilities.Hiro3.Spirit.DarkSpirit.SoulInfest.Damage");
    this.damageEntities = ConfigManager.getConfig().getBoolean("ExtraAbilities.Hiro3.Spirit.DarkSpirit.SoulInfest.DamageEntities");
    this.healDarkSpirits = ConfigManager.getConfig().getBoolean("ExtraAbilities.Hiro3.Spirit.DarkSpirit.SoulInfest.HealDarkSpirits");
    this.radius = ConfigManager.getConfig().getInt("ExtraAbilities.Hiro3.Spirit.DarkSpirit.SoulInfest.Radius");
    this.removeWithoutSoul = ConfigManager.getConfig().getBoolean("ExtraAbilities.Hiro3.Spirit.DarkSpirit.SoulInfest.RemoveWhenSoulEnds");
  }
  
  public void progress()
  {
    if ((this.player.isDead()) || (!this.player.isOnline()) || (GeneralMethods.isRegionProtectedFromBuild(this, this.location)) || (!this.bPlayer.canBendIgnoreBindsCooldowns(this)))
    {
      remove();
      return;
    }
    
    if(removeWithoutSoul && !soul.isAlive()) {
    	remove();
    	return;
    }
    
    if (soul.isAlive()) {
	    location = soul.getLoc().clone();
	    location2 = soul.getLoc().clone();
	    location3 = soul.getLoc().clone();
	    circleCenter = soul.getLoc().clone();
    }
    
    spawnCircle();
    grabEntities();
    if (System.currentTimeMillis() > this.time + this.duration)
    {
      remove();
      return;
    }
  }
  
  @SuppressWarnings("deprecation")
public void spawnCircle()
  {
    Methods.createPolygon(this.location, 8, this.radius, 0.2D, ParticleEffect.SMOKE);
    //Methods.createPolygon(this.location, 8, this.radius, 0.2D, ParticleEffect.SPELL_WITCH);
    for (int i = 0; i < 6; i++)
    {
      this.currPoint += 1;
      if (this.currPoint > 360) {
        this.currPoint = 0;
      }
      double angle = this.currPoint * 3.141592653589793D / 180.0D;
      double x = this.radius * Math.cos(angle);
      double x2 = this.radius * Math.sin(angle);
      double z = this.radius * Math.sin(angle);
      double z2 = this.radius * Math.cos(angle);
      this.location2.add(x, 0.0D, z);
      //ParticleEffect.SMOKE.display(this.location2, 0.0F, 0.0F, 0.0F, 0.0F, 1);
      ParticleEffect.RED_DUST.display(location2, 0, 0, 0, 0, 1);
      this.location2.subtract(x, 0.0D, z);
      
      this.location3.add(x2, 0.0D, z2);
      //ParticleEffect.SMOKE.display(this.location3, 0.0F, 0.0F, 0.0F, 0.0F, 1);
      ParticleEffect.RED_DUST.display(location3, 0, 0, 0, 0, 1);
      this.location3.subtract(x2, 0.0D, z2);
    }
    //ParticleEffect.DRAGON_BREATH.display(this.location, this.radius / 2, 0.4F, this.radius / 2, 0.01F, 1);
    ParticleEffect.PORTAL.display(location, radius / 2, 0.4F, radius / 2, 0.01F, 20);
  }
  
  public void grabEntities()
  {
    for (Entity entity : GeneralMethods.getEntitiesAroundPoint(this.circleCenter, this.radius)) {
      if ((entity instanceof LivingEntity)) {
        infestEntities(entity);
      }
    }
  }
  
  public void infestEntities(Entity entity)
  {
    if (new Random().nextInt(this.effectInt) == 0) {
      if ((entity instanceof Player))
      {
        Player ePlayer = (Player)entity;
        BendingPlayer bEntity = BendingPlayer.getBendingPlayer(ePlayer);
        if (bEntity.hasElement(Element.getElement("DarkSpirit")))
        {
          if (this.healDarkSpirits)
          {
            LivingEntity le = (LivingEntity)entity;
            le.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 120, 1));
            ParticleEffect.HEART.display(entity.getLocation().add(0.0D, 2.0D, 0.0D), 1, 0.0D, 0.0D, 0.0D, 0.0D);
          }
        }
        else
        {
          DamageHandler.damageEntity(entity, this.damage, this);
          ParticleEffect.PORTAL.display(entity.getLocation().add(0.0D, 1.0D, 0.0D), 5, 0.0D, 0.0D, 0.0D, 1.5D);
        }
      }
      else if ((entity instanceof Monster))
      {
        LivingEntity le = (LivingEntity)entity;
        le.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 120, 1));
        ParticleEffect.VILLAGER_ANGRY.display(entity.getLocation().add(0.0D, 1.0D, 0.0D), 1, 0.0D, 0.0D, 0.0D, 0.0D);
      }
      else if (((entity instanceof LivingEntity)) && (this.damageEntities))
      {
        DamageHandler.damageEntity(entity, this.damage, this);
        ParticleEffect.PORTAL.display(entity.getLocation().add(0.0D, 1.0D, 0.0D), 5, 0.0D, 0.0D, 0.0D, 1.5D);
      }
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
    return "SoulInfest";
  }
  
  public String getDescription()
  {
    return "Make your souls cast Infest.\n(I give all credits to the xNuminousx)";
  }
  
  public String getInstructions()
  {
    return "Do Infest -> Intoxicate (Tap Sneak) -> Intoxicate (Tap Sneak) -> Intoxicate (Left Click)";
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
  
  @Override
  public void remove() {
	  super.remove();
	  if (soul.isAlive())
		  soul.setEyeColor(eye);
  }
  
  public void load() {
	  ProjectKorra.log.info("Succesfully enabled " + getName() + " by " + "Hiro3");
	  
	  ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Spirit.DarkSpirit.SoulInfest.Duration", 10000);
	  ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Spirit.DarkSpirit.SoulInfest.Radius", 3);
	  ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Spirit.DarkSpirit.SoulInfest.Damage", 1);
	  ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Spirit.DarkSpirit.SoulInfest.EffectInterval", 10);
	  ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Spirit.DarkSpirit.SoulInfest.HealDarkSpirits", true);
	  ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Spirit.DarkSpirit.SoulInfest.DamageEntities", true);
	  ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Spirit.DarkSpirit.SoulInfest.RemoveWhenSoulEnds", false);
	  ConfigManager.defaultConfig.save();
  }
  
  public void stop() {
	  ProjectKorra.log.info("Successfully disabled " + getName() + " by " + "Hiro3");
	  super.remove();
  }

  @Override
  public Object createNewComboInstance(Player player) {
  	if (CoreAbility.hasAbility(player, Infest.class) && CoreAbility.hasAbility(player, SoulSplit.class) && !CoreAbility.hasAbility(player, SoulInfest.class)) {
		if (CoreAbility.getAbility(player, SoulSplit.class).getSoulNo() != 0) {
			for (Soul s : CoreAbility.getAbility(player, SoulSplit.class).getSouls()) {
				//s.setEyeColor("800080");
				new SoulInfest(player, s);
			}
		}
	}
  	return null;
  }

  @Override
  public ArrayList<AbilityInformation> getCombination() {
  	ArrayList<AbilityInformation> combination = new ArrayList<>();
  	combination.add(new AbilityInformation("Intoxicate", ClickType.SHIFT_DOWN));
  	combination.add(new AbilityInformation("Intoxicate", ClickType.SHIFT_UP));
  	combination.add(new AbilityInformation("Intoxicate", ClickType.SHIFT_DOWN));
  	combination.add(new AbilityInformation("Intoxicate", ClickType.SHIFT_UP));
  	combination.add(new AbilityInformation("Intoxicate", ClickType.LEFT_CLICK));

  	return combination;
  }
}
