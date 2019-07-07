package me.Hiro.MyAbilities;

import java.util.LinkedList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.inventivetalent.glow.GlowAPI;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.ability.PassiveAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;

public class TerraSense extends EarthAbility implements AddonAbility, PassiveAbility {

	private double r;
	private double ar;
	private BendingPlayer bPlayer;
	private LinkedList<LivingEntity> entities;
	private LinkedList<LivingEntity> tmpEntities;

	public TerraSense(Player player) {
		super(player);
		setFields();
	}

	public void setFields() {
		//Entity part
		entities = new LinkedList<LivingEntity>();
		tmpEntities = new LinkedList<LivingEntity>();
		//Entity part
	}

	@Override
	public void progress() {
		if(hasAbility(player, EarthPulse.class) && getAbility(player, EarthPulse.class).isActive())	
			entityPart();
		else if (!entities.isEmpty()) {
			for(LivingEntity e : entities) {
				GlowAPI.setGlowing(e, null, player);
				tmpEntities.add(e);
			}
			for(LivingEntity e : tmpEntities) {
				entities.remove(e);
			}
			tmpEntities.clear();
		}
	}
	
	public void entityPart() {
		r = ConfigManager.getConfig().getDouble("ExtraAbilities.Hiro3.Earth.TerraSense.Radius");
		ar = ConfigManager.getConfig().getDouble("ExtraAbilities.Hiro3.Earth.TerraSense.AirbenderRadius");
		int flag = 0;
		Location loc = player.getLocation();
		for (PotionEffect p : player.getActivePotionEffects()) {
			if (p.getType().getName() == "BLINDNESS") {
				flag = 1;
				r = ConfigManager.getConfig().getDouble("ExtraAbilities.Hiro3.Earth.TerraSense.BlindRadius");
				ar = ConfigManager.getConfig().getDouble("ExtraAbilities.Hiro3.Earth.TerraSense.AirbenderBlindRadius");
				break;
			}
		}
		
		if(loc.clone().add(0, -1, 0).getBlock().getType().equals(Material.SAND)) {
			r *= 0.4;
			ar *= 0.4;
		}
		
		long time = ConfigManager.getConfig().getLong("ExtraAbilities.Hiro3.Earth.TerraSense.TimeBetweenPulsesForSneak");
		
		if(player.isSneaking() || flag == 1) {
			if(time > 0 && System.currentTimeMillis() % (time*1000) < 100) {
				makePulse(player.getLocation().clone().add(0, -1, 0).getBlock(), r, (int)r/3, player);
			}
			Block pBlock = player.getLocation().getBlock().getRelative(BlockFace.DOWN, 1);
			if (GeneralMethods.isSolid(pBlock) && isEarthbendable(pBlock)) {
				for(Entity e : GeneralMethods.getEntitiesAroundPoint(loc, r)) {
					if (e instanceof LivingEntity && e.getUniqueId() != player.getUniqueId()) {
						
						//Airbender part
						if(e instanceof Player) {
							int airflag = 0;
							bPlayer = BendingPlayer.getBendingPlayer((Player) e);
							for(Element elm : bPlayer.getElements()) {
								if(elm.getName().compareTo("Air") == 0) {
									airflag = 1;
								}
							}
							if(airflag == 1 && e.getLocation().distance(player.getLocation()) > ar) {
								continue;
							}
						}
						//Airbender part
						
						Block eBlock = e.getLocation().getBlock().getRelative(BlockFace.DOWN, 1);
						if (GeneralMethods.isSolid(eBlock) && isEarthbendable(eBlock)) {
							GlowAPI.setGlowing(e, GlowAPI.Color.GRAY, player);
							if(!entities.contains((LivingEntity) e)) {
								entities.add((LivingEntity) e);
							}
						}
					}
				}
			}
		}
		check();
	}
	
	public void check() {
		int flag = 0;
		for (PotionEffect p : player.getActivePotionEffects()) {
			if (p.getType().getName() == "BLINDNESS") {
				flag = 1;
			}
		}
		Block pBlock = player.getLocation().getBlock().getRelative(BlockFace.DOWN, 1);
		if(!GeneralMethods.isSolid(pBlock) || !isEarthbendable(pBlock) || (!player.isSneaking() && flag == 0)) {
			for(LivingEntity e : entities) {
				GlowAPI.setGlowing(e, null, player);
			}
			entities.clear();
		} else {
			for(LivingEntity e : entities) {
				Block eBlock = e.getLocation().getBlock().getRelative(BlockFace.DOWN, 1);
				double tmpR = r;
				
				//Airbender part
				if(e instanceof Player) {
					int airflag = 0;
					bPlayer = BendingPlayer.getBendingPlayer((Player) e);
					for(Element elm : bPlayer.getElements()) {
						if(elm.getName().compareTo("Air") == 0) {
							airflag = 1;
						}
					}
					if(airflag == 1) {
						tmpR = ar;
					}
				}
				//Airbender part
				
				if(e.getLocation().distance(player.getLocation()) > tmpR || (!GeneralMethods.isSolid(eBlock) || !isEarthbendable(eBlock))) {
					GlowAPI.setGlowing(e, null, player);
					tmpEntities.add(e);
				}
			}
			for(LivingEntity e : tmpEntities) {
				entities.remove(e);
			}
			tmpEntities.clear();
		}
	}
	
	public static void makePulse(Block block, double range, int iterator, Player source) {
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
	public static void makeGlowingBlock(Location loc, Player source) {
		Block block = loc.getBlock().getRelative(BlockFace.DOWN, 0);
		if (!(GeneralMethods.isSolid(block) && EarthAbility.isEarthbendable(block.getType(), true, true, false)))//isEarth(block)))
			return;
		if(block.getType().getId() == 44)
			return;
		loc.setY(block.getY() + 0.3);
		loc.setX(block.getX() + 0.5);
		loc.setZ(block.getZ() + 0.5);
		FallingBlock b = loc.getWorld().spawnFallingBlock(loc, Material.CAKE_BLOCK, (byte) 0);
		//b.setGlowing(true);
		if(CoreAbility.hasAbility(source, EarthPulse.class) && CoreAbility.getAbility(source, EarthPulse.class).isActive()) {	
			Block pBlock = source.getLocation().getBlock().getRelative(BlockFace.DOWN, 1);
			if (GeneralMethods.isSolid(pBlock) && EarthAbility.isEarthbendable(pBlock.getType(), true, true, false)) {
				GlowAPI.setGlowing(b, GlowAPI.Color.GRAY, source);
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
	
	@Override
	public long getCooldown() {
		return 0;
	}

	@Override
	public Location getLocation() {
		return null;
	}

	@Override
	public String getName() {
		return "TerraSense";
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
	public boolean isInstantiable() {
		return true;
	}

	@Override
	public boolean isProgressable() {
		return true;
	}

	@Override
	public String getDescription() {
		return "Feel the tremor around the earth and locate living entities! With this ability, an earthbender is able to see the walking"
				+ ", falling, digging or building vibrations on earth and able to see nearby players behind walls. If the earthbender"
				+ " is blind, TerraSense will be stronger.";
	}
	
	@Override
	public String getInstructions() {
		return "Stand on an earthbendable block. Sneak to sense nearby.";
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
		ProjectKorra.plugin.getServer().getPluginManager().registerEvents(new TerraSenseListener(), ProjectKorra.plugin);
		
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Earth.TerraSense.Radius", 15);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Earth.TerraSense.BlindRadius", 45);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Earth.TerraSense.BlockSenseRadius", 50);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Earth.TerraSense.SenseYourself", true);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Earth.TerraSense.WalkRadius", 5);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Earth.TerraSense.LowFallRadius", 10);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Earth.TerraSense.MediumFallRadius", 15);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Earth.TerraSense.HighFallRadius", 20);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Earth.TerraSense.AirbenderRadius", 7);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Earth.TerraSense.AirbenderBlindRadius", 21);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Earth.TerraSense.AirbenderWalkRadius", 3);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Earth.TerraSense.TimeBetweenPulses", 5);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Earth.TerraSense.TimeBetweenPulsesForWalk", 5);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Earth.TerraSense.TimeBetweenPulsesForSneak", 5);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Earth.TerraSense.PulseShapePercentage", 20);
		ConfigManager.defaultConfig.save();

		ProjectKorra.log.info("Succesfully enabled " + getName() + " by " + getAuthor());
	}

	@Override
	public void stop() {
		ProjectKorra.log.info("Successfully disabled " + getName() + " by " + getAuthor());
		super.remove();
	}

}
