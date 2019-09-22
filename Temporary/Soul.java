package Hiro3;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;

public class Soul {

	public Player owner;
	public Location loc;
	public double hearts;
	public Vector dir;
	public long startTime;
	private String eyeColor;
	private boolean isAlive;
	private Location eyeLocLeft;
	private Location eyeLocRight;
	
	public Soul(Player owner, Location loc, double hearts) {
		this.owner = owner;
		this.dir = owner.getLocation().getDirection().clone();
		if (dir.getY() <= -0.93 || dir.getY() >= 0.93) {
			dir.setX(1);
		}
		this.loc = loc;
		this.hearts = hearts;
		this.startTime = System.currentTimeMillis();
		this.eyeColor = "9ECCFF";
		setAlive(true);
	}
	
	public void display() {
		Location tmpLoc = getLoc().clone();
		Vector rightTmpVec = new Vector(-dir.getZ(), 0, +dir.getX());
		rightTmpVec.normalize();
		rightTmpVec.multiply(0.40);
		Vector leftTmpVec = rightTmpVec.clone().multiply(-1);
		
		Location tmpLocLeft = tmpLoc.clone();
		tmpLocLeft.add(leftTmpVec);
		Location tmpLocRight = tmpLoc.clone();
		tmpLocRight.add(rightTmpVec);
		
		for (double i = 0; i < 1.8; i+=0.2) {
			owner.getWorld().spawnParticle(Particle.CLOUD, tmpLoc, 0);
			if (i >= 1 && i <= 1.2) {
				owner.getWorld().spawnParticle(Particle.CLOUD, tmpLocLeft, 0);
				owner.getWorld().spawnParticle(Particle.CLOUD, tmpLocRight, 0);
			}
			if (i == 1.4) {
				Location l = tmpLoc.clone().add(dir.clone().setY(0).multiply(0.2));
				eyeLocLeft = l.clone().add(leftTmpVec.getX() * 0.4, 0.4, leftTmpVec.getZ() * 0.4);
				eyeLocRight = l.clone().add(rightTmpVec.getX() * 0.4, 0.4, rightTmpVec.getZ() * 0.4);
				GeneralMethods.displayColoredParticle(eyeLocLeft, eyeColor, 0, 0, 0);
				GeneralMethods.displayColoredParticle(eyeLocRight, eyeColor, 0, 0, 0);
				//owner.getWorld().spawnParticle(Particle.DRAGON_BREATH, l.clone().add(leftTmpVec.getX() * 0.4, 0.4, leftTmpVec.getZ() * 0.4), 0);
				//owner.getWorld().spawnParticle(Particle.DRAGON_BREATH, l.clone().add(rightTmpVec.getX() * 0.4, 0.4, rightTmpVec.getZ() * 0.4), 0);
			}
			tmpLoc.add(0, 0.2, 0);
			tmpLocLeft.add(0, 0.2, 0);
			tmpLocRight.add(0, 0.2, 0);
		}
	}
	
	public Location getEyeLocLeft() {
		return eyeLocLeft;
	}

	public Location getEyeLocRight() {
		return eyeLocRight;
	}

	public Player getOwner() {
		return owner;
	}
	public void setOwner(Player owner) {
		this.owner = owner;
	}
	public Location getLoc() {
		return loc;
	}
	public void setLoc(Location loc) {
		this.loc = loc;
	}
	public double getHearts() {
		return hearts;
	}
	public void setHearts(double hearts) {
		this.hearts = hearts;
	}
	public long getStartTime() {
		return this.startTime;
	}
	public String getEyeColor() {
		return eyeColor;
	}
	public Vector getDir() {
		return dir;
	}

	public void setDir(Vector dir) {
		this.dir = dir;
	}

	public void setEyeColor(String eyeColor) {
		this.eyeColor = eyeColor;
	}

	public boolean isAlive() {
		return isAlive;
	}

	public void setAlive(boolean isAlive) {
		this.isAlive = isAlive;
	}
}

