package me.Hiro.MyAbilities;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.inventivetalent.glow.GlowAPI;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;

import net.md_5.bungee.api.ChatColor;

public class EarthPulse extends EarthAbility implements AddonAbility {

	private boolean active;
	private long cooldown;
	private boolean charged;
	private int blindDuration;
	
	public EarthPulse(Player player) {
		super(player);
		
		if (bPlayer.isOnCooldown(this)) {
			return;
		}

		if (!bPlayer.canBend(this)) {
			return;
		}

		if (player.isSneaking()) {
			if(hasAbility(player, EarthPulse.class)) {
				EarthPulse b = getAbility(player, EarthPulse.class);
				b.remove();
			}
		}
		
		if (!player.isSneaking() && hasAbility(player, EarthPulse.class)) {
			EarthPulse b = getAbility(player, EarthPulse.class);
			if(b.isActive())
				player.sendMessage(ChatColor.GREEN + "EarthPulse is disabled!");
			else
				player.sendMessage(ChatColor.GREEN + "EarthPulse is enabled!");
			b.setActive(!b.isActive());
			bPlayer.addCooldown(b);
			remove();
			return;
		}
		
		setFields();
		bPlayer.addCooldown(this);
		start();
	}
	
	public void setFields() {
		cooldown = ConfigManager.getConfig().getLong("ExtraAbilities.Hiro3.Earth.TerraSense.EarthPulse.Cooldown");
		blindDuration = ConfigManager.getConfig().getInt("ExtraAbilities.Hiro3.Earth.TerraSense.EarthPulse.BlindDuration");
		
		if(!player.isSneaking())
			player.sendMessage(ChatColor.GREEN + "EarthPulse is enabled!");
		else {
			player.sendMessage(ChatColor.DARK_GREEN + "You closed your eyes to sharpen your senses!");
			PotionEffect p = new PotionEffect(PotionEffectType.BLINDNESS, blindDuration*20, 1);
			player.addPotionEffect(p);
			sendPulse();
		}
		active = true;
	}

	@Override
	public void progress() {
		return;
	}
	
	public void sendPulse() {
		Location loc = player.getLocation();
		Block block = loc.getBlock().getRelative(BlockFace.DOWN, 1);
		double range = ConfigManager.getConfig().getDouble("ExtraAbilities.Hiro3.Earth.TerraSense.BlindRadius");
		if(block.getType().equals(Material.SAND)) {
			range *= 0.4;
		}
		makePulse(block, range, 10, player);
	}
	
	public void makePulse(Block block, double range, int iterator, Player source) {
		if(ConfigManager.getConfig().getInt("ExtraAbilities.Hiro3.Earth.TerraSense.PulseShapePercentage") == 0)
			return;
		new BukkitRunnable() {
			Location loc = block.getLocation();
			Location tmpLoc = loc.clone();
			double r = 2;
			int accI = ConfigManager.getConfig().getInt("ExtraAbilities.Hiro3.Earth.TerraSense.PulseShapePercentage");
			Block pBlock;
			Location pLoc = null;
			Location cLoc = null;
			@Override
			public void run() {
				if(r <= range) {
					if(accI%101 == 0)
						accI = 1;
					for(int i = 0; i <= 360; i+=100/(accI%101)) {
						double angle = Math.toRadians(i);
						tmpLoc.setX(loc.getX() + r*Math.cos(angle));
						tmpLoc.setZ(loc.getZ() + r*Math.sin(angle));
						pBlock = tmpLoc.getBlock().getRelative(BlockFace.DOWN, 0);
						cLoc = tmpLoc.clone();
						cLoc.setY(pBlock.getY() + 0.3);
						cLoc.setX(pBlock.getX() + 0.5);
						cLoc.setZ(pBlock.getZ() + 0.5);
						if(pLoc != cLoc)
							makeGlowingBlock(tmpLoc, source);
						pLoc = cLoc;
					}
					r += range/iterator;
				}
				else {
					Bukkit.getScheduler().cancelTask(getTaskId());
				}
			}
		}.runTaskTimer(ProjectKorra.plugin, 0, 2);
	}
	
	@SuppressWarnings("deprecation")
	public void makeGlowingBlock(Location loc, Player source) {
		Block block = loc.getBlock().getRelative(BlockFace.DOWN, 0);
		if (!(GeneralMethods.isSolid(block) && EarthAbility.isEarthbendable(block.getType(), true, true, false)))//isEarth(block)))
			return;
		if(block.getType().getId() == 44)
			return;
		loc.setY(block.getY() + 0.3);
		loc.setX(block.getX() + 0.5);
		loc.setZ(block.getZ() + 0.5);
		FallingBlock b = loc.getWorld().spawnFallingBlock(loc, Material.CAKE_BLOCK, (byte) 0);
		if(CoreAbility.hasAbility(player, EarthPulse.class) && CoreAbility.getAbility(player, EarthPulse.class).isActive()) {	
			Block pBlock = player.getLocation().getBlock().getRelative(BlockFace.DOWN, 1);
			if (GeneralMethods.isSolid(pBlock) && EarthAbility.isEarthbendable(pBlock.getType(), true, true, false)) {
				GlowAPI.setGlowing(b, GlowAPI.Color.GRAY, player);
			}
		}
		b.setGravity(false);
		b.setDropItem(false);
		b.setSilent(true);
		new BukkitRunnable() {
			@Override
			public void run() {
				b.remove();			
			}		
		}.runTaskLater(ProjectKorra.plugin, 1);
	}
	
	public boolean isActive() {
		return this.active;
	}
	
	public void setActive(boolean b) {
		this.active = b;
	}
	
	public boolean isCharged() {
		return this.charged;
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
		return "EarthPulse";
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
	public String getDescription() {
		return "Enable EarthPulse to sharpen your seismic sense! (Enables TerraSense passive.)";
	}
	
	@Override
	public String getInstructions() {
		return "Left Click: Enables/Disables the TerraSense passive.\n"
				+ "Sneak + Left Click: Makes you close your eyes.";
	}

	@Override
	public String getAuthor() {
		return "Hiro3";
	}

	@Override
	public String getVersion() {
		return "1.1.1";
	}

	@Override
	public void load() {
		ProjectKorra.plugin.getServer().getPluginManager().registerEvents(new EarthPulseListener(), ProjectKorra.plugin);
		
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Earth.TerraSense.EarthPulse.Cooldown", 2000);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Earth.TerraSense.EarthPulse.BlindDuration", 10);
		ConfigManager.defaultConfig.save();
		
		ProjectKorra.log.info("Succesfully enabled " + getName() + " by " + getAuthor());
	}

	@Override
	public void stop() {
		ProjectKorra.log.info("Successfully disabled " + getName() + " by " + getAuthor());
		super.remove();
	}

}
