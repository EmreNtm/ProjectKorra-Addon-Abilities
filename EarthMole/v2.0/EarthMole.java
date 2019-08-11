package Hiro3;

import org.bukkit.Location;
import org.bukkit.Material;
//import org.bukkit.Particle.DustOptions;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.util.TempBlock;

import net.md_5.bungee.api.ChatColor;

public class EarthMole extends EarthAbility implements AddonAbility {

	private Listener EML;
	
	private Location playerStartLoc;
	private boolean lastDigFlag;
	
	private long blockRevertTime;
	private long cooldown;
	private long duration;
	private double speed;
	
	private int depth;
	
	public EarthMole(Player player) {
		super(player);
		
		if (bPlayer.isOnCooldown(this)) {
			return;
		}

		if (!bPlayer.canBend(this)) {
			return;
		}

		if (!player.isOnGround()) {
			return;
		}
		
		if (checkDepth()) {
			setField();
			firstDig();
			start();
		}
	}

	public void setField() {
		playerStartLoc = player.getLocation().clone();
		blockRevertTime = 1000;
		
		speed = ConfigManager.getConfig().getDouble("ExtraAbilities.Hiro3.Earth.EarthMole.Speed");
		duration = ConfigManager.getConfig().getLong("ExtraAbilities.Hiro3.Earth.EarthMole.Duration");
		cooldown = ConfigManager.getConfig().getLong("ExtraAbilities.Hiro3.Earth.EarthMole.Cooldown");
	}
	
	@Override
	public void progress() {
		
		if (GeneralMethods.isRegionProtectedFromBuild(this, player.getLocation())) {
			bPlayer.addCooldown(this);
			remove();
			return;
		}
		
		if (!player.isSneaking() || System.currentTimeMillis() > getStartTime() + duration) {
			lastDig();
			if (lastDigFlag)
				bPlayer.addCooldown(this);
			remove();
			return;
		}
		
		checkOverGround();
		checkStuck();
		clearWay();
	}
	
	public void checkOverGround() {
		Location loc = player.getLocation().clone();
		boolean flag = true;
		
		for (int i = -1; i < 2; i+=1) {
			for (int j = -1; j < 2; j+=1) {
				for (int k = 0; k < 3; k++) {
					loc.add(i, k, j);
					if (GeneralMethods.isSolid(loc.getBlock())) {
						flag = false;
					}
					loc.subtract(i, k, j);
				}
			}
		}
		
		if (flag == true) {
			bPlayer.addCooldown(this);
			remove();
		}
	}
	
	public void checkStuck() {
		Block block = player.getEyeLocation().getBlock();
		TempBlock tbl;
		
		if (GeneralMethods.isSolid(block)) {
			
			if (block.getRelative(BlockFace.UP, 1).getType().equals(Material.SAND)) {
				tbl = new TempBlock(block.getRelative(BlockFace.UP, 1), Material.SANDSTONE, (byte) 0);
				tbl.setRevertTime(10000);
			} else if (block.getRelative(BlockFace.UP, 1).getType().equals(Material.GRAVEL)) {
				tbl = new TempBlock(block.getRelative(BlockFace.UP, 1), Material.STONE, (byte) 0);
				tbl.setRevertTime(10000);
			}
			
			tbl = new TempBlock(block, Material.AIR, (byte) 0);
			tbl.setRevertTime(10000);
		}
	}
	
	public void lastDig() {
		int colorNo = 0;
		lastDigFlag = true;
		boolean chain = false;
		int airCount = 0;
		int count = 0;
		double launchPower = 0;
		Location loc = player.getEyeLocation();
		Block block = loc.getBlock();
		TempBlock tbl;
		
		Location particleLoc = player.getLocation();
		
		Location tmpLoc = player.getLocation().getBlock().getLocation().add(0.5, 0, 0.5);;
		tmpLoc.setDirection(player.getLocation().getDirection());
		player.teleport(tmpLoc);
		
		while (airCount < 4 && lastDigFlag) {
			block = block.getRelative(BlockFace.UP, 1);
			
			if (!chain) {
				airCount = 0;
			}
			
			if (!GeneralMethods.isSolid(block)) {
				airCount++;
				chain = true;
			} else if ( !(GeneralMethods.isSolid(block) && isEarthbendable(block)) ) {
				lastDigFlag = false;
				chain = false;
				player.sendMessage(ChatColor.GREEN + "You sense the unbendable blocks on your way up. Cooldown reset!");
				remove();
				return;
			} else {
				if (block.getType().equals(Material.SAND) || block.getType().equals(Material.SANDSTONE)) {
					colorNo = 1;
					particleLoc = block.getLocation().add(0.5, 0, 0.5);
				} else {
					colorNo = 0;
					particleLoc = block.getLocation().add(0.5, 0, 0.5);
				}
				tbl = new TempBlock(block, Material.AIR, (byte) 0);
				tbl.setRevertTime(50 * (15 * ((count / 30) + 1)));
				chain = false;
			}
			count++;
		}
		
		launchPower = 0.5 + count/300;
		player.setVelocity(new Vector(0, Math.sqrt(count), 0).multiply(launchPower));
		particles(particleLoc, colorNo);
	}
	
	public void clearWay() {
		TempBlock tbl;
		Block block = player.getLocation().add(player.getLocation().getDirection().setY(0)).getBlock();
		if (GeneralMethods.isSolid(block) && isEarthbendable(block)) {
			tbl = new TempBlock(block, Material.AIR, (byte) 0);
			tbl.setRevertTime(blockRevertTime);
		}
		block = block.getRelative(BlockFace.UP, 1);
		if (GeneralMethods.isSolid(block) && isEarthbendable(block)) {
			
			if (block.getRelative(BlockFace.UP, 1).getType().equals(Material.SAND)) {
				tbl = new TempBlock(block.getRelative(BlockFace.UP, 1), Material.SANDSTONE, (byte) 0);
				tbl.setRevertTime(blockRevertTime);
			} else if (block.getRelative(BlockFace.UP, 1).getType().equals(Material.GRAVEL)) {
				tbl = new TempBlock(block.getRelative(BlockFace.UP, 1), Material.STONE, (byte) 0);
				tbl.setRevertTime(blockRevertTime);
			}
			
			tbl = new TempBlock(block, Material.AIR, (byte) 0);
			tbl.setRevertTime(blockRevertTime);
		}
		
		if(player.isOnGround())
			player.setVelocity(player.getLocation().getDirection().setY(0).multiply(speed));
	}
	
	public void firstDig() {
		Block block = playerStartLoc.getBlock().getRelative(BlockFace.DOWN, 1);
		TempBlock tbl;
		
		for (int i = 0; i < depth; i++) {
			tbl = new TempBlock(block, Material.AIR, (byte) 0);
			tbl.setRevertTime(blockRevertTime);
			block = block.getRelative(BlockFace.DOWN, 1);
		}
		
		Location tmpLoc = block.getLocation().add(0.5, 1, 0.5);
		tmpLoc.setDirection(player.getLocation().getDirection());
		player.teleport(tmpLoc);
	}
	
	public boolean checkDepth() {
		int count = 1;
		Block block = player.getLocation().getBlock().getRelative(BlockFace.DOWN, 1);
		while (count < 5 && ((GeneralMethods.isSolid(block) && isEarthbendable(block)) || !GeneralMethods.isSolid(block))) {
			block = block.getRelative(BlockFace.DOWN, 1);
			count++;
		}
		
		depth = count - 1;
		
		if (count > 2)
			return true;
		player.sendMessage(ChatColor.GREEN + "You sense the unbendable blocks on your way down.");
		return false;
	}
	
	public void particles(Location loc, int c) {
		String color;
		double r = 0;
		double tmp = 0.0025;
		double angle = 10;
		double angle1 = Math.toRadians(angle);
		//DustOptions dustOptions;
		if (c == 1) {
			color = "DCBE5B";
			//dustOptions = new DustOptions(Color.fromRGB(220, 190, 91), 1);
		} else {
			color = "844329";
			//dustOptions = new DustOptions(Color.fromRGB(132, 67, 41), 1);
		}

		while (r >= 0) {
			if (r >= 0.25) {
				tmp = -tmp;
				r = 0.25;
			}
			loc.setY(loc.getY() + 0.03);
			loc.setX(loc.getX() + r * Math.cos(angle1));
			loc.setZ(loc.getZ() + r * Math.sin(angle1));
			GeneralMethods.displayColoredParticle(loc, color, 0, 0, 0);
			//player.getWorld().spawnParticle(Particle.REDSTONE, loc, 1, dustOptions);
			angle += 10;
			angle1 = Math.toRadians(angle);
			r += tmp;
		}
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
		return "EarthMole";
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	@Override
	public boolean isSneakAbility() {
		return true;
	}

	@Override
	public String getAuthor() {
		return "Hiro3";
	}

	@Override
	public String getVersion() {
		return "2.0";
	}

	@Override
	public String getDescription() {
		return "Move like a mole underground as Bumi did!";
	}

	@Override
	public String getInstructions() {
		return "Hold Shift";
	}
	
	@Override
	public void remove() {
		super.remove();
		
		new BukkitRunnable() {
			@Override
			public void run() {
				checkStuck();
			}
		}.runTaskLater(ProjectKorra.plugin, 20);
	}
	
	@Override
	public void load() {
		EML = new EarthMoleListener();
		ProjectKorra.plugin.getServer().getPluginManager().registerEvents(EML, ProjectKorra.plugin);
		ProjectKorra.log.info("Succesfully enabled " + getName() + " by " + getAuthor());
		
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Earth.EarthMole.Cooldown", 5000);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Earth.EarthMole.Speed", 0.5);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Earth.EarthMole.Duration", 10000);
		ConfigManager.defaultConfig.save();
	}

	@Override
	public void stop() {
		ProjectKorra.log.info("Successfully disabled " + getName() + " by " + getAuthor());
		HandlerList.unregisterAll(EML);
		super.remove();
	}

}
