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
import me.xnuminousx.spirits.ability.api.LightAbility;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SoulOrb
  extends LightAbility
  implements AddonAbility, ComboAbility
{
  private long time;
  private long cooldown;
  private Location location;
  private Location targetLoc;
  private boolean checkEntities;
  private boolean registerOrbLoc;
  private boolean progressExplosion;
  private boolean playDormant;
  private boolean requireGround;
  private long duration;
  private int blindDuration;
  private int nauseaDuration;
  private int potionAmp;
  private double detonateRange;
  private double effectRange;
  private double damage;
  
  private Soul soul;
  private String eye;
  
  public SoulOrb(Player player, Soul soul)
  {
    super(player);
    
    if (!this.bPlayer.canBendIgnoreBinds(this)) {
        return;
    }
    
    setFields();

    this.soul = soul;
    eye = soul.getEyeColor();
    soul.setEyeColor("00FFFF");
    
    this.location = soul.getLoc().clone();
    
    this.time = System.currentTimeMillis();
    
    start();
  }
  
  private void setFields()
  {
    this.cooldown = 0;
    this.duration = 30000;
    this.damage = ConfigManager.getConfig().getDouble("ExtraAbilities.Hiro3.Spirit.LightSpirit.SoulOrb.Damage");
    this.detonateRange = 2;
    this.effectRange = ConfigManager.getConfig().getInt("ExtraAbilities.Hiro3.Spirit.LightSpirit.SoulOrb.EffectRange");
    this.blindDuration = ConfigManager.getConfig().getInt("ExtraAbilities.Hiro3.Spirit.LightSpirit.SoulOrb.BlindDuration");
    this.nauseaDuration = ConfigManager.getConfig().getInt("ExtraAbilities.Hiro3.Spirit.LightSpirit.SoulOrb.NauseaDuration");
    this.potionAmp = ConfigManager.getConfig().getInt("ExtraAbilities.Hiro3.Spirit.LightSpirit.SoulOrb.PotionPower");
    this.requireGround = false;
    this.checkEntities = false;
    this.registerOrbLoc = true;
    this.progressExplosion = false;
    this.playDormant = false;
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
    
    this.location = soul.getLoc().clone();
    this.targetLoc = soul.getLoc().clone();
    

        this.playDormant = true;
        if (this.registerOrbLoc)
        {
          if ((this.requireGround) && 
            (!canSpawn(this.targetLoc)))
          {
            remove();
            return;
          }
          this.registerOrbLoc = false;
        }
        displayOrb(soul.getEyeLocLeft());
        displayOrb(soul.getEyeLocRight());
      explodeOrb();
    
  }
  
  public void displayOrb(Location location)
  {
    if (this.playDormant)
    {
      this.progressExplosion = false;
      ParticleEffect.ENCHANTMENT_TABLE.display(location, 3.0F, 1.0F, 3.0F, 0.0F, 1);
      ParticleEffect.END_ROD.display(location, 0.0F, 0.0F, 0.0F, 0.0F, 2);
      ParticleEffect.MAGIC_CRIT.display(location, 0.2F, 0.2F, 0.2F, 0.0F, 3);
    }
    if (System.currentTimeMillis() > this.time + this.duration)
    {
      this.playDormant = false;
      ParticleEffect.FIREWORKS_SPARK.display(location, 0.0F, 0.0F, 0.0F, 0.05F, 10);
      remove();
      return;
    }
    this.checkEntities = true;
  }
  
  public void explodeOrb()
  {
    if (this.checkEntities) {
      for (Entity entity : GeneralMethods.getEntitiesAroundPoint(this.targetLoc, this.detonateRange)) {
        if (((entity instanceof LivingEntity)) && (entity.getUniqueId() != this.player.getUniqueId()))
        {
          this.progressExplosion = true;
          this.playDormant = false;
        }
      }
    }
    if (this.progressExplosion)
    {
      ParticleEffect.FIREWORKS_SPARK.display(this.targetLoc, 0.2F, 0.2F, 0.2F, 0.5F, 50);
      ParticleEffect.END_ROD.display(this.targetLoc, 2.0F, 3.0F, 2.0F, 0.0F, 30);
      for (Entity entity : GeneralMethods.getEntitiesAroundPoint(this.targetLoc, this.effectRange)) {
        if (((entity instanceof LivingEntity)) && (entity.getUniqueId() != this.player.getUniqueId()))
        {
          LivingEntity le = (LivingEntity)entity;
          le.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, this.blindDuration, this.potionAmp));
          le.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, this.nauseaDuration, this.potionAmp));
          DamageHandler.damageEntity(entity, this.damage, this);
        }
      }
      remove();
      return;
    }
  }
  
  private boolean canSpawn(Location loc)
  {
    Block targetBlock = loc.getBlock();
    if ((targetBlock.getRelative(BlockFace.DOWN).getType() == Material.AIR) && 
      (targetBlock.getRelative(BlockFace.UP).getType() == Material.AIR) && 
      (targetBlock.getRelative(BlockFace.EAST).getType() == Material.AIR) && 
      (targetBlock.getRelative(BlockFace.WEST).getType() == Material.AIR) && 
      (targetBlock.getRelative(BlockFace.NORTH).getType() == Material.AIR) && 
      (targetBlock.getRelative(BlockFace.SOUTH).getType() == Material.AIR)) {
      return false;
    }
    return true;
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
    return "SoulOrb";
  }
  
  public String getDescription()
  {
    return "Make your souls cast Orb.\n(I give all credits to the xNuminousx)";
  }
  
  public String getInstructions()
  {
    return "Orb (Tap Sneak) -> Orb (Tap Sneak) -> Orb (Left Click)";
  }
  
  public String getAuthor()
  {
    return "Hiro3";
  }
  
  public String getVersion()
  {
    return "1.0";
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
  
  @Override
  public void remove() {
	  super.remove();
	  if (soul.isAlive())
		  soul.setEyeColor(eye);
  }
  
  public void load() {
	  ProjectKorra.log.info("Succesfully enabled " + getName() + " by " + "Hiro3");
	  
	  ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Spirit.LightSpirit.SoulOrb.Damage", 3);
	  ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Spirit.LightSpirit.SoulOrb.EffectRange", 5);
	  ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Spirit.LightSpirit.SoulOrb.BlindDuration", 120);
	  ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Spirit.LightSpirit.SoulOrb.NauseaDuration", 300);
	  ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Spirit.LightSpirit.SoulOrb.PotionPower", 2);
	  ConfigManager.defaultConfig.save();
  }
  
  public void stop() {
	  ProjectKorra.log.info("Successfully disabled " + getName() + " by " + "Hiro3");
	  super.remove();
  }

  @Override
  public Object createNewComboInstance(Player player) {
  	if (CoreAbility.hasAbility(player, SoulSplit.class) && !CoreAbility.hasAbility(player, SoulOrb.class)) {
  		if (CoreAbility.getAbility(player, SoulSplit.class).getSoulNo() != 0) {
  			for (Soul s : CoreAbility.getAbility(player, SoulSplit.class).getSouls()) {
  				//s.setEyeColor("00FFFF");
  				new SoulOrb(player, s);
  			}
  		}
  	}
  	return null;
  }

  @Override
  public ArrayList<AbilityInformation> getCombination() {
  	ArrayList<AbilityInformation> combination = new ArrayList<>();
  	combination.add(new AbilityInformation("Orb", ClickType.SHIFT_DOWN));
  	combination.add(new AbilityInformation("Orb", ClickType.SHIFT_UP));
  	combination.add(new AbilityInformation("Orb", ClickType.SHIFT_DOWN));
  	combination.add(new AbilityInformation("Orb", ClickType.SHIFT_UP));
  	combination.add(new AbilityInformation("Orb", ClickType.LEFT_CLICK));

  	return combination;
  }
}
