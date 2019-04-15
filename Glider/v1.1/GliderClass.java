package me.Hiro.MyAbilities.Glider;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.configuration.Config;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempArmor;
import java.util.logging.Logger;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class GliderClass
  extends AirAbility
  implements AddonAbility
{
  private long duration;
  private long cooldown;
  private long jumpspeed;
  private boolean jumpEnabled;
  private boolean jumpOnAirEnabled;
  private int amount = 50;
  private ItemStack item = new ItemStack(Material.ELYTRA);
  private ItemMeta meta = this.item.getItemMeta();
  private TempArmor armor;
  
  public GliderClass(Player player)
  {
    super(player);
    if (this.bPlayer.isOnCooldown(this)) {
      return;
    }
    if (!this.bPlayer.canBend(this)) {
      return;
    }
    setFields();
    start();
    jump();
  }
  
  private void setFields()
  {
    this.cooldown = ConfigManager.getConfig().getLong("ExtraAbilities.Hiro3.Air.Glider.Cooldown");
    this.duration = ConfigManager.getConfig().getLong("ExtraAbilities.Hiro3.Air.Glider.Duration");
    this.jumpspeed = ConfigManager.getConfig().getLong("ExtraAbilities.Hiro3.Air.Glider.Jump.JumpSpeed");
    this.jumpEnabled = ConfigManager.getConfig().getBoolean("ExtraAbilities.Hiro3.Air.Glider.Jump.Enable");
    this.jumpOnAirEnabled = ConfigManager.getConfig().getBoolean("ExtraAbilities.Hiro3.Air.Glider.Jump.JumpOnAirEnable");
    
    this.meta.setDisplayName(ChatColor.BLUE + "Glider");
    this.item.setItemMeta(this.meta);
    this.item.addEnchantment(Enchantment.BINDING_CURSE, 1);
    this.item.addEnchantment(Enchantment.VANISHING_CURSE, 1);
    ItemStack hava = new ItemStack(Material.AIR);
    ItemStack[] armors = { hava, hava, this.item, hava };
    playAirbendingSound(this.player.getLocation());
    this.armor = new TempArmor(this.player, this.duration * 1000L, this, armors);
    this.armor.setRemovesAbilityOnForceRevert(true);
  }
  
  public void geri()
  {
    this.armor.revert();
  }
  
  public void progress()
  {
    if ((this.player.isDead()) || (!this.player.isOnline()) || (GeneralMethods.isRegionProtectedFromBuild(this, this.player.getLocation())))
    {
      remove();
      return;
    }
    if (System.currentTimeMillis() - getStartTime() > this.duration * 1000L)
    {
      this.player.getLocation().getWorld().playSound(this.player.getLocation(), Sound.ENTITY_ITEM_BREAK, 2.0F, 1.0F);
      this.bPlayer.addCooldown(this);
    }
  }
  
  public void jump()
  {
    if ((isJumpEnabled()) && (
      (this.player.isOnGround()) || (isJumpOnAirEnabled())))
    {
      Vector vec = this.player.getVelocity();
      Vector dir = this.player.getLocation().getDirection();
      vec.add(dir.multiply((float)this.jumpspeed));
      this.player.setVelocity(vec);
      for (int i = 0; i < this.amount; i++) {
        if (this.player.getVelocity().getBlockY() != 0) {
          new BukkitRunnable()
          {
            public void run()
            {
              GliderClass.getAirbendingParticles().display(GliderClass.this.player.getLocation(), 1.0F, 1.0F, 1.0F, 0.0F, 3);
            }
          }.runTaskLater(ProjectKorra.plugin, i);
        }
      }
    }
  }
  
  public boolean isJumpEnabled()
  {
    return this.jumpEnabled;
  }
  
  public boolean isJumpOnAirEnabled()
  {
    return this.jumpOnAirEnabled;
  }
  
  public long getCooldown()
  {
    return this.cooldown;
  }
  
  public Location getLocation()
  {
    return null;
  }
  
  public String getDescription()
  {
    return "This ability gives you an air glider like Aang's. Use it to feel like a real airbender!";
  }
  
  public String getInstructions()
  {
    return "Left Click.";
  }
  
  public String getName()
  {
    return "Glider";
  }
  
  public boolean isHarmlessAbility()
  {
    return false;
  }
  
  public boolean isSneakAbility()
  {
    return false;
  }
  
  public String getAuthor()
  {
    return "Hiro3";
  }
  
  public String getVersion()
  {
    return "1.1";
  }
  
  public void load()
  {
    ProjectKorra.plugin.getServer().getPluginManager().registerEvents(new GliderListener(), ProjectKorra.plugin);
    ProjectKorra.log.info("Succesfully enabled " + getName() + " by " + getAuthor());
    
    ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Air.Glider.Cooldown", Integer.valueOf(5000));
    ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Air.Glider.Duration", Integer.valueOf(10));
    ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Air.Glider.Jump.Enable", Boolean.valueOf(true));
    ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Air.Glider.Jump.JumpOnAirEnable", Boolean.valueOf(true));
    ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Air.Glider.Jump.JumpSpeed", Double.valueOf(2.5D));
    ConfigManager.defaultConfig.save();
  }
  
  public void stop()
  {
    ProjectKorra.log.info("Successfully disabled " + getName() + " by " + getAuthor());
    super.remove();
  }
}
