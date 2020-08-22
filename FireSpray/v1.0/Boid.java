package me.hiro3.firespray;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.util.DamageHandler;

public class Boid {

	private Player player;
	private FireSpray fireSpray;
	private Location location;
	private Vector direction;
	private double lineOfSightRange;
	
	private double speed = 1;
	private double alignmentPower = 0.2;
	private double pushPower = 0.01;
	private double blockPushPower = 0.75;
	private double turnSpeed = 0.75;
	private Vector gravity = new Vector(0, -0.01, 0);
	private double range;
	private double damage;
	
	private int ticksLived;
	
	public Boid(Player player, FireSpray fireSpray, Location loc, Vector dir, double damage, double range) {
		this.player = player;
		this.fireSpray = fireSpray;
		this.location = loc;
		this.direction = dir;
		this.lineOfSightRange = 2;
		this.ticksLived = 0;
		this.range = range;
		this.damage = damage;
	}
	
	public void update() {
		if (this.range <= 0 || GeneralMethods.isSolid(this.location.getBlock()))
			this.ticksLived = fireSpray.getMaximumTick();
		
		if (this.ticksLived > fireSpray.getMaximumTick())
			return;
		this.ticksLived++;
		
		this.location.add(this.direction.normalize().multiply(this.speed));
		this.speed -= 0.01;
		this.range -= this.direction.length();
		
		Vector newDirection = this.direction.clone();
		newDirection.add(seperationVector());
		newDirection.add(alignmentVector());
		Vector turnVector = newDirection.clone().subtract(this.direction).multiply(this.turnSpeed);
		this.direction.add(turnVector);
		this.direction.add(gravity);
		
		this.pushPower += 0.005;
	}
	
	@SuppressWarnings("static-access")
	public void show() {
		if (this.ticksLived > fireSpray.getMaximumTick())
			return;
		if (fireSpray.blueFirePlayers.contains(player.getName())) {
			//player.getWorld().spawnParticle(Particle.CRIT_MAGIC, this.location, 0);
			if (Math.random() > 0.05)
				GeneralMethods.displayColoredParticle(this.location, "2F87FC");
			else
				GeneralMethods.displayColoredParticle(this.location, "B6F9FF");
		}
		else
			//player.getWorld().spawnParticle(Particle.FLAME, this.location, 0);
			FireAbility.playFirebendingParticles(this.location, 1, 0.075f, 0.075f, 0.075f);
		
		for (Entity e : GeneralMethods.getEntitiesAroundPoint(this.location, 1.5)) {
			if (e instanceof LivingEntity && !e.getUniqueId().equals(player.getUniqueId())) {
				DamageHandler.damageEntity(e, this.damage, fireSpray);
				//e.setVelocity(e.getVelocity().add(alignmentVector()));
			}
		}
		
	}
	
	public Vector seperationVector() {
		Vector seperationVector = new Vector(0, 0, 0);
		int flag ;
		
		for (FireSpray fs : CoreAbility.getAbilities(FireSpray.class)) {
			if (fs.getPlayer().getWorld().equals(player.getWorld())) {
				flag = 0;
				for (Boid b : fs.getBoids()) {
					if (b.getLocation().distance(this.location) <= this.lineOfSightRange) {
						if (fs.getPlayer().getUniqueId().equals(player.getUniqueId())) {
							seperationVector.add(calculatePushVector(b, 1));
						} else {
							seperationVector.add(calculatePushVector(b, 25));
							flag = 1;
						}
					}
				}
				if (flag == 1)
					this.ticksLived += fireSpray.getMaximumTick() / 3;
			}
		}
		
		flag = 0;
		for (Block b : GeneralMethods.getBlocksAroundPoint(this.location, this.lineOfSightRange - 0.5)) {
			if (GeneralMethods.isSolid(b) || b.isLiquid()) {
				seperationVector.add(calculatePushVector(b, 1));
				flag = 1;
			}
		}
		if (flag == 1)
			this.ticksLived += fireSpray.getMaximumTick() / 6;
		
		return seperationVector;
	}
	
	public Vector alignmentVector() {
		Vector alignmentVector = new Vector(0, 0, 0);
		for (Boid b : fireSpray.getBoids()) {
			if (b.getLocation().distance(this.location) <= this.lineOfSightRange) {
				alignmentVector.add(b.getDirection());
			}
		}
		alignmentVector.normalize().multiply(alignmentPower);
		return alignmentVector;
	}
	
	public int getTicksLived() {
		return this.ticksLived;
	}
	
	public Vector calculatePushVector(Boid b, double power) {
		double push = this.pushPower * (1/(b.getLocation().distanceSquared(this.getLocation()) + 0.1)) * power;
		return this.getLocation().clone().toVector().subtract(b.getLocation().clone().toVector()).multiply(push);
	}
	
	public Vector calculatePushVector(Block b, double power) {
		double push = this.blockPushPower * (1/(b.getLocation().add(0.5, 0.5, 0.5).distanceSquared(this.getLocation()) + 0.1)) * power;
		return this.location.clone().toVector().subtract(b.getLocation().clone().toVector()).multiply(push);
	}
	
	public Location getLocation() {
		return this.location;
	}
	
    public Vector getDirection() {
    	return this.direction;
    }
}
