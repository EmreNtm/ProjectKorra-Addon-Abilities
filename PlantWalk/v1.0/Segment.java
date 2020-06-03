package com.plantwalk.Hiro3;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;

public class Segment {

	private Player player;
	private Location locationA;
	private Location locationB;
	private double length;
	private double angleY;
	private double angleZ;
	
	public Segment(Player player, Location locationA, double length, double angleY, double angleZ) {
		this.setPlayer(player);
		this.locationA = locationA;
		this.locationB = locationA.clone();
		this.length = length;
		this.angleY = angleY;
		this.angleZ = angleZ;
		calculateB();
	}
	
	public Segment(Player player, Segment parent, double length, double angleY, double angleZ) {
		this.setPlayer(player);
		this.locationA = parent.getLocationB();
		this.locationB = locationA.clone();
		this.length = length;
		this.angleY = angleY;
		this.angleZ = angleZ;
		calculateB();
	}
	
	public void update() {
		calculateB();
	}
	
	public void display() {
		Vector direction = toVector().multiply(0.2);
		Location startLoc = locationA.clone();
		Location currentLoc = startLoc.clone();
		while(startLoc.distance(currentLoc) < this.length) {
			GeneralMethods.displayColoredParticle(currentLoc, "25351B", 0, 0, 0);
			//player.getWorld().spawnParticle(Particle.REDSTONE, currentLoc, 0, -1, 0.9, 0);
			currentLoc.add(direction);
		};
		//player.getWorld().spawnParticle(Particle.REDSTONE, currentLoc, 0, 1, 0, 0);
	}
	
	public void follow(Location targetLocation) {
		/* CHANGE ANGLES */
		if (targetLocation.getBlockX() == locationA.getBlockX() &&
				targetLocation.getBlockY() == locationA.getBlockY() &&
				targetLocation.getBlockZ() == locationA.getBlockZ()) {
			return;
		}
		
		if (targetLocation.getX() == locationB.getX() &&
				targetLocation.getY() == locationB.getY() &&
				targetLocation.getZ() == locationB.getZ()) {
			return;
		}

		this.angleZ = Math.atan( (targetLocation.getZ() - locationA.getZ()) / (targetLocation.getX() - locationA.getX()) );
		this.angleZ = Math.toDegrees(angleZ);
		if (targetLocation.getX() < locationA.getX()) {
			this.angleZ += 180;
		}
		
		if (Math.abs(angleZ) < 45) {
			this.angleY = Math.atan( (targetLocation.getY() - locationA.getY()) / (targetLocation.getX() - locationA.getX()) );
			this.angleY = Math.toDegrees(-angleY);
		} else if (angleZ > 135 && angleZ < 225) {
			this.angleY = Math.atan( (targetLocation.getY() - locationA.getY()) / (targetLocation.getX() - locationA.getX()) );
			this.angleY = Math.toDegrees(angleY);
		}else if (angleZ >= 45 && angleZ <= 135) {
			this.angleY = Math.atan( (targetLocation.getY() - locationA.getY()) / (targetLocation.getZ() - locationA.getZ()) );
			this.angleY = Math.toDegrees(-angleY);
		} else {
			this.angleY = Math.atan( (targetLocation.getY() - locationA.getY()) / (targetLocation.getZ() - locationA.getZ()) );
			this.angleY = Math.toDegrees(angleY);
		}
		this.angleY += 90;
		/* CHANGE ANGLES */
		
		calculateB();
		locationA = targetLocation.clone().add(toVector().multiply(-this.length));
		
	}
	
	public void calculateB() {
		double x = locationA.getX() + this.length * Math.sin(Math.toRadians(this.angleY)) * Math.cos(Math.toRadians(this.angleZ));
		double z = locationA.getZ() + this.length * Math.sin(Math.toRadians(this.angleY)) * Math.sin(Math.toRadians(this.angleZ));
		double y = locationA.getY() + this.length * Math.cos(Math.toRadians(this.angleY));
		this.locationB.setX(x);
		this.locationB.setY(y);
		this.locationB.setZ(z);
	}
	
	public Vector toVector() {
		return locationB.clone().toVector().subtract(locationA.clone().toVector()).normalize();
	}
	
	public Location getLocationA() {
		return this.locationA;
	}
	
	public void setLocationA(Location loc) {
		this.locationA = loc;
		calculateB();
	}
	
	public Location getLocationB() {
		return this.locationB;
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}
	
}
