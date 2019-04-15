package me.Hiro.MyAbilities.Glider;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.TempArmor;

import net.md_5.bungee.api.ChatColor;

public class GliderClass extends AirAbility implements AddonAbility {

	private long duration;
	private long cooldown;
	private long jumpspeed;
	private boolean jumpEnabled;
	private boolean jumpOnAirEnabled;
	private int amount = 50;
	private ItemStack item = new ItemStack(Material.ELYTRA);
	private ItemMeta meta = item.getItemMeta();
	private TempArmor armor;
	
	public GliderClass(Player player) {
		super(player);

		if (bPlayer.isOnCooldown(this)) {
			return;
		}

		if (!bPlayer.canBend(this)) {
			return;
		}
		setFields();
		start();
		jump();
	}

	private void setFields() {
		this.cooldown = ConfigManager.getConfig().getLong("ExtraAbilities.Hiro3.Air.Glider.Cooldown");
		this.duration = ConfigManager.getConfig().getLong("ExtraAbilities.Hiro3.Air.Glider.Duration");
		this.jumpspeed = ConfigManager.getConfig().getLong("ExtraAbilities.Hiro3.Air.Glider.Jump.JumpSpeed");
		this.jumpEnabled = ConfigManager.getConfig().getBoolean("ExtraAbilities.Hiro3.Air.Glider.Jump.Enable");
		this.jumpOnAirEnabled = ConfigManager.getConfig().getBoolean("ExtraAbilities.Hiro3.Air.Glider.Jump.JumpOnAirEnable");

		meta.setDisplayName(ChatColor.BLUE + "Glider");
		item.setItemMeta(meta);
		item.addEnchantment(Enchantment.BINDING_CURSE, 1);
		item.addEnchantment(Enchantment.VANISHING_CURSE, 1);
		ItemStack hava = new ItemStack(Material.AIR);
		ItemStack[] armors = { hava, hava, item, hava };
		playAirbendingSound(player.getLocation());
		armor = new TempArmor(this.player, duration * 1000, this, armors);
		armor.setRemovesAbilityOnForceRevert(true);
	}

	public void geri() {
		armor.revert();
	}
	
	@Override
	public void progress() {
		if (player.isDead() || !player.isOnline()) {
			remove();
			return;
		}
		if (System.currentTimeMillis() - this.getStartTime() > this.duration * 1000) {
			player.getLocation().getWorld().playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 2, 1);
			bPlayer.addCooldown(this);
		}
	}
	
	public void jump() {
		if (isJumpEnabled()) {
			if (player.isOnGround() || isJumpOnAirEnabled()) {
				Vector vec = player.getVelocity();
				Vector dir = player.getLocation().getDirection();
				vec.add(dir.multiply(jumpspeed));
				player.setVelocity(vec);
				for (int i = 0; i < amount; i++) {
					if (player.getVelocity().getBlockY() != 0) {
						new BukkitRunnable() {

							@Override
							public void run() {
								getAirbendingParticles().display(player.getLocation(), 1, 1, 1, 0, 3);

							}

						}.runTaskLater(ProjectKorra.plugin, i);
					}
				}

			}
		}
	}

	public boolean isJumpEnabled() {
		return jumpEnabled;
	}

	public boolean isJumpOnAirEnabled() {
		return jumpOnAirEnabled;
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
	public String getDescription() {
		return "This ability gives you an air glider like Aang's. Use it to feel like a real airbender!";
	}

	@Override
	public String getInstructions() {
		return "Left Click.";
	}

	@Override
	public String getName() {
		return "Glider";
	}

	@Override
	public boolean isHarmlessAbility() {
		return true;
	}

	@Override
	public boolean isSneakAbility() {
		return false;
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
		ProjectKorra.plugin.getServer().getPluginManager().registerEvents(new GliderListener(), ProjectKorra.plugin);
		ProjectKorra.log.info("Succesfully enabled " + getName() + " by " + getAuthor());

		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Air.Glider.Cooldown", 5000);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Air.Glider.Duration", 10);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Air.Glider.Jump.Enable", true);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Air.Glider.Jump.JumpOnAirEnable", true);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Air.Glider.Jump.JumpSpeed", 2.5);
		ConfigManager.defaultConfig.save();
	}

	@Override
	public void stop() {
		ProjectKorra.log.info("Successfully disabled " + getName() + " by " + getAuthor());
		super.remove();
	}

}
