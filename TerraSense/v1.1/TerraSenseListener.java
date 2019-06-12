package me.Hiro.MyAbilities;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.inventivetalent.glow.GlowAPI;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;

public class TerraSenseListener implements Listener {
	
	@EventHandler
	public void onSneak(PlayerToggleSneakEvent e) {
		if(e.getPlayer().isSneaking())
			return;
		double r = ConfigManager.getConfig().getDouble("ExtraAbilities.Hiro3.Earth.TerraSense.Radius");
		if(e.getPlayer().getLocation().clone().add(0, -1, 0).getBlock().getType().equals(Material.SAND)) {
			r *= 0.4;
		}
		TerraSense.makePulse(e.getPlayer().getLocation().clone().add(0, -1, 0).getBlock(), r, (int)r/3, e.getPlayer());
	}
	
	@EventHandler
	public void onBlockChange(BlockBreakEvent e) {
		boolean sy = ConfigManager.getConfig().getBoolean("ExtraAbilities.Hiro3.Earth.TerraSense.SenseYourself");
		Double r = ConfigManager.getConfig().getDouble("ExtraAbilities.Hiro3.Earth.TerraSense.BlockSenseRadius");
		Block block = e.getBlock();
		Player source = e.getPlayer();
		if (!(GeneralMethods.isSolid(block) && EarthAbility.isEarthbendable(block.getType(), true, true, false)))
			return;
		
		int earthFlag = 0;
		if(sy == false || (sy == true && !(CoreAbility.hasAbility(source, EarthPulse.class) && CoreAbility.getAbility(source, EarthPulse.class).isActive()))) {
			for(Player p : GeneralMethods.getPlayersAroundPoint(block.getLocation(), r)) {
				{
					if(CoreAbility.hasAbility(p, EarthPulse.class) && CoreAbility.getAbility(p, EarthPulse.class).isActive())
						earthFlag = 1;
				}
				
			}
			if(earthFlag == 0)
				return;
		}
		makePulse(block, 3, 3, source);
	}
	
	@EventHandler
	public void onBlockChange(BlockPlaceEvent e) {
		boolean sy = ConfigManager.getConfig().getBoolean("ExtraAbilities.Hiro3.Earth.TerraSense.SenseYourself");
		Double r = ConfigManager.getConfig().getDouble("ExtraAbilities.Hiro3.Earth.TerraSense.BlockSenseRadius");
		Block block = e.getBlock();
		Player source = e.getPlayer();
		if (!(GeneralMethods.isSolid(block) && EarthAbility.isEarthbendable(block.getType(), true, true, false)))
			return;
		
		int earthFlag = 0;
		if(sy == false || (sy == true && !(CoreAbility.hasAbility(source, EarthPulse.class) && CoreAbility.getAbility(source, EarthPulse.class).isActive()))) {
			for(Player p : GeneralMethods.getPlayersAroundPoint(block.getLocation(), r)) {
				{
					if(CoreAbility.hasAbility(p, EarthPulse.class) && CoreAbility.getAbility(p, EarthPulse.class).isActive())
						earthFlag = 1;
				}
				
			}
			if(earthFlag == 0)
				return;
		}
		makePulse(block, 3, 3, source);
	}
	
	@EventHandler
	public void playerFall(PlayerMoveEvent e) {
		Player source = e.getPlayer();
		
		boolean sy = ConfigManager.getConfig().getBoolean("ExtraAbilities.Hiro3.Earth.TerraSense.SenseYourself");
		Double r = ConfigManager.getConfig().getDouble("ExtraAbilities.Hiro3.Earth.TerraSense.BlockSenseRadius");
		int earthFlag = 0;
		if(sy == false || (sy == true && !(CoreAbility.hasAbility(source, EarthPulse.class) && CoreAbility.getAbility(source, EarthPulse.class).isActive()))) {
			for(Player p : GeneralMethods.getPlayersAroundPoint(source.getLocation(), r)) {
				{
					if(CoreAbility.hasAbility(p, EarthPulse.class) && CoreAbility.getAbility(p, EarthPulse.class).isActive())
						earthFlag = 1;
				}
				
			}
			if(earthFlag == 0)
				return;
		}
		
		//Airbender part
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(e.getPlayer());
		int airflag = 0;
		for(Element elm : bPlayer.getElements()) {
			if(elm.getName().compareTo("Air") == 0) {
				airflag = 1;
			}
		}
		if(airflag == 1) {
			return;
		}
		//Airbender part
		
		int range = ConfigManager.getConfig().getInt("ExtraAbilities.Hiro3.Earth.TerraSense.LowFallRadius");
		if(source.getFallDistance() > 15)
			range = ConfigManager.getConfig().getInt("ExtraAbilities.Hiro3.Earth.TerraSense.HighFallRadius");
		else if(source.getFallDistance() > 8)
			range = ConfigManager.getConfig().getInt("ExtraAbilities.Hiro3.Earth.TerraSense.MediumFallRadius");
		if(source.getLocation().clone().add(0, -1, 0).getBlock().getType().equals(Material.SAND)) {
			range *= 0.4;
		}
		if(source.getFallDistance() >= 1) {
			Location loc = e.getPlayer().getLocation();
			loc.setY(loc.getY() - 1);
			Block block = loc.getBlock().getRelative(BlockFace.DOWN, 0);
			Block block2 = loc.getBlock().getRelative(BlockFace.DOWN, 1);
			if((GeneralMethods.isSolid(block) && EarthAbility.isEarthbendable(block.getType(), true, true, false))
					|| (GeneralMethods.isSolid(block2) && EarthAbility.isEarthbendable(block2.getType(), true, true, false))) {
				makePulse(block, range, 6, source);
			}
		}
	}
	
	@EventHandler
	public void playerMove(PlayerMoveEvent e) {
		Player source = e.getPlayer();
		
		//Airbender part
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(e.getPlayer());
		int airflag = 0;
		for(Element elm : bPlayer.getElements()) {
			if(elm.getName().compareTo("Air") == 0) {
				airflag = 1;
			}
		}
		int range = ConfigManager.getConfig().getInt("ExtraAbilities.Hiro3.Earth.TerraSense.WalkRadius");
		int sec = 5000;
		if(airflag == 1) {
			range = ConfigManager.getConfig().getInt("ExtraAbilities.Hiro3.Earth.TerraSense.AirbenderWalkRadius");
			sec = 10000;
		}
		//Airbender part
				
		if(e.getFrom().getBlockY() == e.getTo().getBlockY()
				&& (e.getFrom().getBlockX() != e.getTo().getBlockX() || e.getFrom().getBlockZ() != e.getTo().getBlockZ())) {
			if(System.currentTimeMillis() % sec > 100)
				return;
			Location loc = e.getPlayer().getLocation();
			loc.setY(loc.getY() - 1);
			Block block = loc.getBlock().getRelative(BlockFace.DOWN, 0);
			if(GeneralMethods.isSolid(block) && EarthAbility.isEarthbendable(block.getType(), true, true, false)) {
				makePulse(block, range, 5, source);
			}
		}
	}
	
	public void makePulse(Block block, double range, int iterator, Player source) {
		new BukkitRunnable() {
			Location loc = block.getLocation();
			Location tmpLoc = loc.clone();
			double r = 2;
			@Override
			public void run() {
				if(r <= range) {
					for(int i = 0; i <= 360; i+=5) {
						double angle = Math.toRadians(i);
						tmpLoc.setX(loc.getX() + r*Math.cos(angle));
						tmpLoc.setZ(loc.getZ() + r*Math.sin(angle));
						makeGlowingBlock(tmpLoc, source);
					}
					r += range/iterator;
				}
			}
		}.runTaskTimer(ProjectKorra.plugin, 0, 2);
	}
	
	@SuppressWarnings("deprecation")
	public void makeGlowingBlock(Location loc, Player source) {
		boolean sy = ConfigManager.getConfig().getBoolean("ExtraAbilities.Hiro3.Earth.TerraSense.SenseYourself");
		Double r = ConfigManager.getConfig().getDouble("ExtraAbilities.Hiro3.Earth.TerraSense.BlockSenseRadius");
		Block block = loc.getBlock().getRelative(BlockFace.DOWN, 0);
		if (!(GeneralMethods.isSolid(block) && EarthAbility.isEarthbendable(block.getType(), true, true, false)))//isEarth(block)))
			return;
		if(block.getTypeId() == 44)
			return;
		loc.setY(block.getY() + 0.3);
		loc.setX(block.getX() + 0.5);
		loc.setZ(block.getZ() + 0.5);
		FallingBlock b = loc.getWorld().spawnFallingBlock(loc, Material.CAKE_BLOCK, (byte) 0);
		//b.setGlowing(true);
		for(Player p : GeneralMethods.getPlayersAroundPoint(loc, r)) {
			if(sy == true || source == null || p.getUniqueId() != source.getUniqueId()) {
				if(CoreAbility.hasAbility(p, EarthPulse.class) && CoreAbility.getAbility(p, EarthPulse.class).isActive()) {	
					Block pBlock = p.getLocation().getBlock().getRelative(BlockFace.DOWN, 1);
					if (GeneralMethods.isSolid(pBlock) && EarthAbility.isEarthbendable(pBlock.getType(), true, true, false)) {
						GlowAPI.setGlowing(b, GlowAPI.Color.GRAY, p);
					}
				}
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
	
}
