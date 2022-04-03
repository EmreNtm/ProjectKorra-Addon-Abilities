package me.hiro3.plantwalk;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.util.Vector;

/* Segment is one piece of a tentacle. */
public class Segment {
	
	private Location startLocation;
	private Location endLocation;
	private double yaw;
	private double pitch;
	private double length;
	private int pointAmount;
	private DustOptions dustOptions;
	
	private Vector direction;
	private Segment parent;
	private Segment child;

	/* Head segment of a tentacle is created this way. */
	public Segment(Location startLocation, double length, int pointAmount, DustOptions dustOptions) {
		this.parent = null;
		this.child = null;
		this.startLocation = startLocation;
		this.length = length;
		this.pointAmount = pointAmount;
		this.dustOptions = dustOptions;
		
		this.setAngles(0, 0);
		this.calculateEndLocation();
	}
	
	/* Child segments are created this way. */
	public Segment(Segment parent, double length, int pointAmount, DustOptions dustOptions) {
		this.parent = parent;
		this.parent.setChild(this);
		this.child = null;
		this.startLocation = parent.getEndLocation().clone();
		this.length = length;
		this.pointAmount = pointAmount;
		this.dustOptions = dustOptions;
		
		this.setAngles(this.parent.getYaw(), this.parent.getPitch());
		this.calculateEndLocation();
	}
	
	/* Move this segment to the target location 
	 * (End point of the segment is at the target location.) */
	public void follow(Location targetLocation) {
		double dX = targetLocation.getX() - this.startLocation.getX();
		double dY = targetLocation.getY() - this.startLocation.getY();
		double dZ = targetLocation.getZ() - this.startLocation.getZ();
		
		double yaw = Math.atan2(dX, dZ);
		double pitch = -Math.atan2(dY, (Math.sqrt((dZ * dZ) + (dX * dX))));
		
		this.setAngles(yaw, pitch);
		this.startLocation = targetLocation.clone().add(this.direction.clone().multiply(-this.length));
		this.calculateEndLocation();
	}
	
	/* Draw the segment to the world. */
	public void display() {
		drawLine(this.startLocation, this.pointAmount, this.length);
	}
	
	/* Draw a line of particles. */
	private void drawLine(Location a, int pointAmount, double length) {
		double pointIncrement = length / pointAmount;
		Vector direction = this.direction.clone().normalize().multiply((pointIncrement));
		Location pointLocation = a.clone();
		for (double i = 0; i < length; i+= pointIncrement) {
            a.getWorld().spawnParticle(Particle.REDSTONE, pointLocation, 1, 0, 0, 0, this.dustOptions);
            pointLocation.add(direction);
		}
	}
	
	/* Find the end point of this segment using start location, segment direction and segment length. */
	private void calculateEndLocation() {
		this.endLocation = this.startLocation.clone().add(this.direction.clone().multiply(length));
	}
	
	/* Update segment angles and set the correct direction. */
	public void setAngles(double yaw, double pitch) {
		this.yaw = yaw;
		this.pitch = pitch;
		this.updateDirection();
	}
	
	/* Set the correct direction according to segment's angle values. */
	private void updateDirection() {
		this.direction = new Vector(0, 0, 1).rotateAroundX(this.pitch).rotateAroundY(this.yaw);
	}
	
	public void setDustOptions(DustOptions dustOptions) {
		this.dustOptions = dustOptions;
	}
	
	public Location getStartLocation() {
		return this.startLocation;
	}
	
	public void setStartLocation(Location location) {
		this.startLocation = location;
		this.calculateEndLocation();
	}
	
	public Location getEndLocation() {
		return this.endLocation;
	}
	
	public void setChild(Segment child) {
		this.child = child;
	}
	
	public boolean hasChild() {
		return this.child != null;
	}
	
	public Segment getChild() {
		return this.child;
	}
	
	public boolean hasParent() {
		return this.parent != null;
	}
	
	public Segment getParent() {
		return this.parent;
	}
	
	private double getYaw() {
		return this.yaw;
	}
	
	private double getPitch() {
		return this.pitch;
	}
}
