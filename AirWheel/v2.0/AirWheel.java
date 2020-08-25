package me.hiro3.airwheel;

import java.util.ArrayList;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.ComboAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.ElementalAbility;
import com.projectkorra.projectkorra.ability.util.Collision;
import com.projectkorra.projectkorra.ability.util.ComboManager.AbilityInformation;
import com.projectkorra.projectkorra.airbending.AirShield;
import com.projectkorra.projectkorra.airbending.AirSpout;
import com.projectkorra.projectkorra.airbending.AirSwipe;
import com.projectkorra.projectkorra.airbending.Tornado;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.earthbending.EarthBlast;
import com.projectkorra.projectkorra.firebending.FireBlast;
import com.projectkorra.projectkorra.util.ClickType;

public class AirWheel extends AirAbility implements AddonAbility, ComboAbility {

	private Location location;
	private Location origin;
	private Vector direction;
	private int damage;
	private long cooldown;
	private long duration;
	private double val;
	private double speed;
	private double scooterspeed;
	private double interval;
	private double maxHeightFromGround;
	private Block floorblock;
	private double pushFactor;
	private Random random;
	private boolean damageEnabled;

//	private float angle;
//	private float radius;
	private float a;
	private float r;
	private double phase;
	
	private int startMode;
	
	@SuppressWarnings("deprecation")
	public AirWheel(Player player) {
		super(player);

		if (check(player)) {
			return;
		} else if (GeneralMethods.isSolid(player.getEyeLocation().getBlock())
				|| player.getEyeLocation().getBlock().isLiquid()) {
			return;
		} else if (bPlayer.isOnCooldown(this)) {
			return;
		} else if (!bPlayer.canBendIgnoreBindsCooldowns(this)) {
			return;
		}

		startMode = 0;
		if (player.isOnGround()) {
			this.startMode = 1;
			player.setVelocity(player.getVelocity().add(new Vector(0, 0.1, 0)));
			startWheel(0);
		} else {
			if (player.isSprinting())
				startWheel(1.05 * 2.5);
			else
				startWheel(1.05);
		}
		setFields();
		
		
		flightHandler.createInstance(player, this.getName());
		player.setAllowFlight(true);
		player.setFlying(true);

		player.setSprinting(false);
		player.setSneaking(false);
		start();
	}

	public void initializeCollisions() {
		CoreAbility first = CoreAbility.getAbility(AirWheel.class);
		
		ArrayList<CoreAbility> secondAbilities = new ArrayList<CoreAbility>(0);
		secondAbilities.add(CoreAbility.getAbility(AirSwipe.class));
		secondAbilities.add(CoreAbility.getAbility(AirSpout.class));
		secondAbilities.add(CoreAbility.getAbility(EarthBlast.class));
		secondAbilities.add(CoreAbility.getAbility(FireBlast.class));
		
		for (CoreAbility second : secondAbilities) {
			if (second != null) {
				Collision customCollision = new Collision(first, second, false, true);
				ProjectKorra.getCollisionManager().addCollision(customCollision);
			}
		}
		
		secondAbilities.clear();
		secondAbilities.add(CoreAbility.getAbility(AirShield.class));
		secondAbilities.add(CoreAbility.getAbility(Tornado.class));
		for (CoreAbility second : secondAbilities) {
			if (second != null) {
				Collision customCollision = new Collision(first, second, true, false);
				ProjectKorra.getCollisionManager().addCollision(customCollision);
			}
		}
		
	}
	
	public void setFields() {
		cooldown = ConfigManager.getConfig().getLong("ExtraAbilities.Hiro3.UtilityPack.Air.AirWheel.Cooldown");
		duration = ConfigManager.getConfig().getLong("ExtraAbilities.Hiro3.UtilityPack.Air.AirWheel.Duration");
		scooterspeed = ConfigManager.getConfig().getDouble("Abilities.Air.AirScooter.Speed");
		pushFactor = ConfigManager.getConfig().getDouble("ExtraAbilities.Hiro3.UtilityPack.Air.AirWheel.PushFactor");
		damageEnabled = ConfigManager.getConfig().getBoolean("ExtraAbilities.Hiro3.UtilityPack.Air.AirWheel.DamageEnable");
		damage = ConfigManager.getConfig().getInt("ExtraAbilities.Hiro3.UtilityPack.Air.AirWheel.Damage");

		origin = player.getLocation();
		origin.setY(origin.getY() - 2);
//		radius = 0;
		random = new Random();

		speed = 0; // 0.675
		interval = 100;
		maxHeightFromGround = 7;
		if (scooterspeed == 0) {
			scooterspeed = 0.675;
		}
		val = scooterspeed * 150 / duration;
		r = (float) 1.5;
		
		phase = 0;
	}

	@Override
	public void progress() {

		if (GeneralMethods.isRegionProtectedFromBuild(this, player.getLocation())) {
			stopWheel();
			this.remove();
			return;
		}
		
		if (!this.bPlayer.canBendIgnoreBindsCooldowns(this)) {
			stopWheel();
			remove();
			return;
			
		} else if (duration != 0 && System.currentTimeMillis() > getStartTime() + duration) {
			this.bPlayer.addCooldown(this);
			stopWheel();
			remove();
			return;
		}

		getFloor();
		if (floorblock == null) {
			remove();
			return;
		}

		if (player.isSneaking()) {
			bPlayer.addCooldown(this);
			stopWheel();
			remove();
			return;
		}	
		
		if (duration != 0 && System.currentTimeMillis() < getStartTime() + duration / 3) {
			speed += 2 * val; // speed += 0.00125;
		} else if ((duration != 0 && System.currentTimeMillis() < getStartTime() + duration * 2 / 3)) {

		} else {
			speed -= 2 * val;
		}
		if (startMode == 1)
			speed = scooterspeed;
		//speed = 0;

		Vector velocity = player.getEyeLocation().getDirection().clone().normalize();
		velocity = velocity.clone().normalize().multiply(speed);
		if (System.currentTimeMillis() > getStartTime() + interval) {
			if (player.getVelocity().length() < speed * 0.3) {
				stopWheel();
				remove();
				return;
			}
			phase += 10;
			phase %= 360;
			spinWheel(phase);
		}

		final double distance = player.getLocation().getY() - floorblock.getY();
		final double dx = Math.abs(distance - 2.4);
		if (distance > 2.75) {
			velocity.setY(-.25 * dx * dx);
		} else if (distance < 2) {
			velocity.setY(.25 * dx * dx);
		} else {
			velocity.setY(0);
		}

		final Vector v = velocity.clone().setY(0);
		final Block b = floorblock.getLocation().clone().add(v.multiply(1.2)).getBlock();
		if (!GeneralMethods.isSolid(b) && !b.isLiquid()) {
			velocity.add(new Vector(0, -0.6, 0));
		} else if (GeneralMethods.isSolid(b.getRelative(BlockFace.UP)) || b.getRelative(BlockFace.UP).isLiquid()) {
			velocity.add(new Vector(0, 1.0, 0));
		}

		final Location loc = player.getLocation();
		if (!ElementalAbility.isWater(player.getLocation().add(0, 2, 0).getBlock())) {
			loc.setY(floorblock.getY() + 1.5);
		} else {
			return;
		}

		player.setSprinting(false);
		player.removePotionEffect(PotionEffectType.SPEED);
		player.setVelocity(velocity);

		if (random.nextInt(4) == 0) {
			playAirbendingSound(this.player.getLocation());
		}
	}

//	for (double j = 0; j < 360; j += 360 / 8) {
//		Location tmpLoc = player.getLocation().clone();
//		double baseY = tmpLoc.getY() - 1;
//		Vector tmpDir = new Vector(1, 0, 1);
//		tmpDir = UtilityMethods.rotateVectorAroundY(tmpDir, j);
//		for (double t = 0, i = 0; t < 12; t++, i += 360 / 12) {
//			double angle = Math.toRadians(i);
//			double sinY = Math.sin(angle) * 0.5;
//			tmpLoc.add(tmpDir.clone().multiply(0.2));
//			tmpLoc.setY(baseY + sinY);
//			getAirbendingParticles().display(tmpLoc, 1);
//		}
//	}
	
	public void startWheel(double mode) {
		new BukkitRunnable() {
			
			double t = 0;
			double i = 0;
			double phase = 0;
			int timeStep = 12;
			double temp = mode;
			Location origin = player.getLocation().clone();
			
			@Override
			public void run() {
				
				if (t == 12) {
					Bukkit.getScheduler().cancelTask(getTaskId());
				}
				
				for (double j = 0 + phase; j < 360 + phase; j += 360 / 16) {
					Location tmpLoc = origin.clone();
					double baseY = tmpLoc.getY() + 0.5 - temp;
					Vector tmpDir = new Vector(1, 0, 1);
					tmpDir = rotateVectorAroundY(tmpDir, j);

					double angle = Math.toRadians(i);
					double sinY = Math.sin(angle) * 0.5;
					tmpLoc.add(tmpDir.clone().multiply(0.2 * t));
					tmpLoc.setY(baseY + sinY);
					getAirbendingParticles().display(tmpLoc, 3, 0.1, 0.1, 0.1);
					hit(tmpLoc);
					hitBurst(tmpLoc);
				}
				
				phase += 8;
				t++;
				i += 360 / timeStep;
				
			}
		}.runTaskTimer(ProjectKorra.plugin, 0, 1);
	}
	
	public void stopWheel() {
		startWheel(1.05);
	}
	
//	public void startWheel() {
//		origin.setY(origin.getY() - 1);
//		double tmp = origin.getY() + 1.25;
//		while (origin.getY() <= tmp) {
//			origin.setY(origin.getY() + 0.02);
//			angle = (float) (count * 10);
//			angle = (float) Math.toRadians(angle);
//			radius = (float) (0.05 * Math.sqrt(angle));
//			origin.setX(origin.getX() + radius * Math.cos(angle));
//			origin.setZ(origin.getZ() + radius * Math.sin(angle));
//			getAirbendingParticles().display(origin, 0, 0, 0, 0, 5);
//			hit(origin);
//			hitBurst(origin);
//			count++;
//		}
//	}
//
//	public void stopWheel() {
//		origin = player.getLocation();
//		double tmp = origin.getY();
//		origin.setY(origin.getY() + 1);
//		count = 0;
//		radius = 0;
//		while (origin.getY() >= tmp - 2) {
//			origin.setY(origin.getY() - 0.02);
//			angle = (float) (count * 10);
//			angle = (float) Math.toRadians(angle);
//			radius = (float) (0.1 * Math.sqrt(angle));
//			origin.setX(origin.getX() + radius * Math.cos(angle));
//			origin.setZ(origin.getZ() + radius * Math.sin(angle));
//			getAirbendingParticles().display(origin, 0, 0, 0, 0, 5);
//			hit(origin);
//			hitBurst(origin);
//			count++;
//		}
//	}

	public void spinWheel(double phase) {
		phase *= -1;
		for (double i = 0 + phase; i < 360 + phase; i+= 15) {
			a = (float) Math.toRadians(i);
			direction = player.getLocation().getDirection();
			location = player.getEyeLocation();
			location.setX(location.getX() + r * direction.getX() * Math.cos(a));
			location.setY(location.getY() + r * Math.sin(a) - 0.75);
			location.setZ(location.getZ() + r * direction.getZ() * Math.cos(a));
			
			Vector dir = player.getLocation().getDirection().clone();
			Vector rightTmpVec = new Vector(-dir.getZ(), 0, +dir.getX());
			rightTmpVec.normalize();
			rightTmpVec.multiply(0.15);
			Vector leftTmpVec = rightTmpVec.clone().multiply(-1);
			
			//getAirbendingParticles().display(location, 1);
			getAirbendingParticles().display(location.clone().add(rightTmpVec), 0, 0, 0, 0, 1);
			getAirbendingParticles().display(location.clone().add(leftTmpVec), 0, 0, 0, 0, 1);
			
			
			hit(location);
			hitBurst(location);
		}
	}

	public boolean canDealDamage() {
		return damageEnabled;
	}

	public void hit(Location loc) {
		if (canDealDamage()) {
			for (Entity entity : GeneralMethods.getEntitiesAroundPoint(loc, 1)) {
				if (entity instanceof LivingEntity && entity.getUniqueId() != player.getUniqueId()) {
					((LivingEntity) entity).damage(damage);
					return;
				}
			}
		}
	}

	private void hitBurst(Location loc) {
		if (bPlayer.isOnCooldown("AirWheel")) {
			return;
		}
		double x, y, z;
		double r1 = 1;

		for (double theta = 75; theta < 105; theta += 10) {
			double dphi = 10 / Math.sin(Math.toRadians(theta));
			for (double phi = 0; phi < 360; phi += dphi) {
				double rphi = Math.toRadians(phi);
				double rtheta = Math.toRadians(theta);

				x = r1 * Math.cos(rphi) * Math.sin(rtheta);
				y = r1 * Math.sin(rphi) * Math.sin(rtheta);
				z = r1 * Math.cos(rtheta);

				Vector dir = new Vector(x, z, y);
				for (Entity entity : GeneralMethods.getEntitiesAroundPoint(loc, 1)) {
					if (entity instanceof LivingEntity && entity.getUniqueId() != player.getUniqueId()) {
						dir.add(dir.multiply(pushFactor));
						entity.setVelocity(dir);
						return;
					}
				}

			}
		}
	}

	public static boolean check(Player player) {
		if (hasAbility(player, AirWheel.class)) {
			// getAbility(player, AirWheel.class).remove();
			return true;
		}
		return false;
	}

	private void getFloor() {
		this.floorblock = null;
		for (int i = 0; i <= this.maxHeightFromGround; i++) {
			final Block block = this.player.getEyeLocation().getBlock().getRelative(BlockFace.DOWN, i);
			if (GeneralMethods.isSolid(block) || block.isLiquid()) {
				this.floorblock = block;
				return;
			}
		}
	}

	public static Vector rotateVectorAroundY(Vector vector, double degrees) {
	    double rad = Math.toRadians(degrees);
	   
	    double currentX = vector.getX();
	    double currentZ = vector.getZ();
	   
	    double cosine = Math.cos(rad);
	    double sine = Math.sin(rad);
	   
	    return new Vector((cosine * currentX - sine * currentZ), vector.getY(), (sine * currentX + cosine * currentZ));
	}
	
	@Override
	public Object createNewComboInstance(Player player) {
		return new AirWheel(player);
	}

	@Override
	public ArrayList<AbilityInformation> getCombination() {
		ArrayList<AbilityInformation> combination = new ArrayList<>();
		combination.add(new AbilityInformation("AirBlast", ClickType.LEFT_CLICK));
		combination.add(new AbilityInformation("AirScooter", ClickType.LEFT_CLICK));
		combination.add(new AbilityInformation("AirSuction", ClickType.LEFT_CLICK));

		return combination;
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public Location getLocation() {
		return player != null ? player.getLocation().add(player.getLocation().getDirection().multiply(r)) : null;
	}

	@Override
	public String getName() {
		return "AirWheel";
	}

	@Override
	public String getDescription() {
		return "An improved version of AirScooter as a combo. A.k.a. Tenzin's AirScooter. The wheel starts from 0 speed to 2 times regular AirScooter speed."
				+ " Then it goes back to 0. You can also attack on it. Press SNEAK to get out of it."
				+ " Special thanks to ProjectKorra team who wrote the AirScooter codes.";
	}

	@Override
	public String getInstructions() {
		return "AirBlast (LEFT CLICK) > AirScooter (LEFT CLICK) > AirSuction (LEFT CLICK)\n"
				+ "Jump before AirSuction to make the wheel faster.";
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
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
		return "2.0";
	}

	@Override
	public void remove() {
		super.remove();
		flightHandler.removeInstance(player, this.getName());
		this.bPlayer.addCooldown(this);
	}

	@Override
	public void load() {
		ProjectKorra.log.info("Succesfully enabled " + getName() + " by " + getAuthor());

		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.UtilityPack.Air.AirWheel.Cooldown", 5000);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.UtilityPack.Air.AirWheel.Duration", 15000);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.UtilityPack.Air.AirWheel.maxHeightFromGround", 7);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.UtilityPack.Air.AirWheel.PushFactor", 0.25);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.UtilityPack.Air.AirWheel.DamageEnable", true);
		ConfigManager.getConfig().addDefault("ExtraAbilities.Hiro3.UtilityPack.Air.AirWheel.Damage", 1);
		ConfigManager.defaultConfig.save();

		initializeCollisions();
	}

	@Override
	public void stop() {
		ProjectKorra.log.info("Successfully disabled " + getName() + " by " + getAuthor());
		super.remove();
	}

}