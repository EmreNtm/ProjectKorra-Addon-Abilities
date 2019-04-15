package me.Hiro.MyAbilities.EarthMole;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.configuration.ConfigManager;

public class EarthMole extends EarthAbility implements AddonAbility {

	private Vector vel;
	private double speed;
	private long duration;
	private long cooldown;
	private Block floorblock;
	private Material[] bArr;
	private int flag = 0;

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

		if (!check()) {
			return;
		}

		setFields();
		start();
	}

	public void setFields() {
		speed = ConfigManager.getConfig().getDouble("ExtraAbilities.Hiro3.Earth.EarthMole.Speed");
		duration = ConfigManager.getConfig().getLong("ExtraAbilities.Hiro3.Earth.EarthMole.Duration");
		cooldown = ConfigManager.getConfig().getLong("ExtraAbilities.Hiro3.Earth.EarthMole.Cooldown");
	}

	@Override
	public void progress() {

		if (GeneralMethods.isRegionProtectedFromBuild(this, player.getLocation())) {
			this.remove();
			return;
		}

		if (flag == 0) {
			firstDig();
			flag = 1;
		}

		if (!player.isSneaking()) {
			remove();
			lastDig();
			new BukkitRunnable() {
				@Override
				public void run() {
					onStuck();
				}
			}.runTaskLater(ProjectKorra.plugin, 20);
			return;
		}

		if (duration != 0 && System.currentTimeMillis() >= getStartTime() + duration) {
			remove();
			lastDig();
			new BukkitRunnable() {
				@Override
				public void run() {
					onStuck();
				}
			}.runTaskLater(ProjectKorra.plugin, 20);
			return;
		}

		vel = player.getLocation().getDirection();
		if (vel.getY() <= -0.93 || vel.getY() >= 0.93) {
			vel.setX(1);
		}
		vel.setY(0);
		vel.multiply(speed);
		getFloor();
		if (floorblock != null) {
			player.setVelocity(vel);
		}

		clearWay();
	}

	@SuppressWarnings("deprecation")
	private void clearWay() {
		int i;
		for (i = -1; i <= 1; i++) {
			Location loc = player.getEyeLocation();
			Vector dir = loc.getDirection();
			if (dir.getY() <= -0.93 || dir.getY() >= 0.93) {
				dir.setX(1);
			}
			dir.setY(0);
			loc.add(dir);
			Block block = loc.getBlock().getRelative(BlockFace.DOWN, i);
			if (GeneralMethods.isSolid(block) || block.isLiquid()) {
				if (!isEarthbendable(block)) {
					continue;
				}
			}
			Material tmp = block.getType();
			byte data = block.getData();
			if (i == -1) {
				if (block.getType() != Material.AIR && (block.getType() == Material.SAND
						|| block.getType() == Material.GRAVEL || block.getType() == Material.ANVIL)) {
					block.setType(Material.SANDSTONE);
				}
			} else {
				block.setType(Material.AIR);
			}
			if ((i != -1 && tmp != Material.AIR) || (i == -1 && tmp != Material.SANDSTONE)) {
				new BukkitRunnable() {

					@Override
					public void run() {
						block.setType(tmp);
						block.setData(data);
					}

				}.runTaskLater(ProjectKorra.plugin, 20);
			}
		}
	}

	@SuppressWarnings("deprecation")
	private void lastDig() {
		bArr = new Material[5];
		int i = -1;
		int j = 1;
		int c = 0;
		int count = 0;
		Location loc1 = player.getEyeLocation();
		Location loc2 = player.getEyeLocation();
		Location loc = player.getEyeLocation();
		Vector dir = loc1.getDirection();
		dir.setY(0);
		loc2.add(dir);
		if (loc2.getBlockX() == loc1.getBlockX() && loc2.getBlockZ() == loc1.getBlockZ())
			loc2.setX(loc2.getX() + 1);
		do {
			if (j == 1) {
				loc = loc1;
				j = -j;
			} else {
				loc = loc2;
				j = -j;
			}
			Block block = loc.getBlock().getRelative(BlockFace.DOWN, i);
			if (j == -1) {
				bArr[(-i - 1) % 5] = block.getType();
			}
			Material tmp = block.getType();
			byte data = block.getData();
			if (GeneralMethods.isSolid(block) || block.isLiquid()) {
				if (isEarthbendable(block)) {
					block.setType(Material.AIR);
				}
			}
			new BukkitRunnable() {
				@Override
				public void run() {
					block.setType(tmp);
					block.setData(data);
				}
			}.runTaskLater(ProjectKorra.plugin, 15 * ((-i / 30) + 1));
			if (tmp == Material.AIR) {
				count++;
			} else {
				count = 0;
			}
			if (j == 1)
				i--;
		} while (count != 4);
		Vector vec = new Vector();
		vec.setX(0);
		vec.setY(Math.sqrt(-i));
		vec.setZ(0);
		player.setVelocity(vec.multiply(0.5 + 0.1 * (-i / 30)));
		if (bArr[1] == Material.SAND || bArr[1] == Material.SANDSTONE || bArr[0] == Material.SAND
				|| bArr[0] == Material.SANDSTONE)
			c = 1;
		particles(player, -i, c);
	}

	@SuppressWarnings("deprecation")
	private void firstDig() {
		int i;
		int level = 4;
		for (i = 1; i <= 4; i++) {
			Block block = player.getLocation().getBlock().getRelative(BlockFace.DOWN, i);
			Material tmp = block.getType();
			byte data = block.getData();
			if (GeneralMethods.isSolid(block) || block.isLiquid()) {
				if (isEarthbendable(block)) {
					block.setType(Material.AIR);

					new BukkitRunnable() {

						@Override
						public void run() {
							block.setType(tmp);
							block.setData(data);
						}

					}.runTaskLater(ProjectKorra.plugin, 20);
				} else {
					level = i - 1;
				}
			}
		}
		Location loc = player.getLocation();
		loc.setY(loc.getY() - level);
		player.teleport(loc);
	}

	private void getFloor() {
		floorblock = null;
		Block block = player.getLocation().getBlock().getRelative(BlockFace.DOWN, 1);
		if (GeneralMethods.isSolid(block) || block.isLiquid()) {
			floorblock = block;
			return;
		}
	}

	@SuppressWarnings("deprecation")
	private void onStuck() {
		int i;
		for (i = 1; i >= -1; i--) {
			Location loc = player.getEyeLocation();
			Block block = loc.getBlock().getRelative(BlockFace.DOWN, i);
			if (!block.isLiquid() && block.getType() != Material.AIR) {
				bPlayer.removeCooldown(this);
				Material tmp = block.getType();
				byte data = block.getData();
				if (i == -1) {
					if (block.getType() != Material.AIR)
						block.setType(Material.SANDSTONE);
				} else
					block.setType(Material.AIR);
				new BukkitRunnable() {
					@Override
					public void run() {
						block.setType(tmp);
						block.setData(data);
					}
				}.runTaskLater(ProjectKorra.plugin, 600);

			}
		}
	}

	public void particles(Player player, int i, int c) {
		i -= 3;
		double r = 0;
		double tmp = 0.0025;
		double angle = 10;
		double angle1 = Math.toRadians(angle);
		String color;
		if (c == 1) {
			color = "DCBE5B";
		} else {
			color = "844329";
		}
		Location loc = player.getLocation();
		loc.setY(loc.getY() + i);
		while (r >= 0) {
			if (r >= 0.25) {
				tmp = -tmp;
				r = 0.25;
			}
			loc.setY(loc.getY() + 0.03);
			loc.setX(loc.getX() + r * Math.cos(angle1));
			loc.setZ(loc.getZ() + r * Math.sin(angle1));
			GeneralMethods.displayColoredParticle(loc, color, 0, 0, 0);
			angle += 10;
			angle1 = Math.toRadians(angle);
			r += tmp;
		}
	}

	private boolean check() {
		int i;
		Block block;
		for (i = 1; i <= 4; i++) {
			block = player.getLocation().getBlock().getRelative(BlockFace.DOWN, i);
			if (GeneralMethods.isSolid(block) || block.isLiquid()) {
				if (!isEarthbendable(block) && i <= 2) {
					return false;
				}
			}
		}
		return true;
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
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getDescription() {
		return "Move like a mole underground as Bumi did! (If you stuck, use the move again to get out.)";
	}

	@Override
	public String getInstructions() {
		return "Hold Shift";
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
		ProjectKorra.plugin.getServer().getPluginManager().registerEvents(new EarthMoleListener(), ProjectKorra.plugin);
		ProjectKorra.log.info("Succesfully enabled " + getName() + " by " + getAuthor());

		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Earth.EarthMole.Cooldown", 5000);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Earth.EarthMole.Speed", 0.5);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.Earth.EarthMole.Duration", 10000);
		ConfigManager.defaultConfig.save();
	}

	@Override
	public void remove() {
		super.remove();
		this.bPlayer.addCooldown(this);
	}

	@Override
	public void stop() {
		ProjectKorra.log.info("Successfully disabled " + getName() + " by " + getAuthor());
		super.remove();
	}

}
