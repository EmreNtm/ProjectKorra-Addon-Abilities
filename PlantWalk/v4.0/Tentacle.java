package me.hiro3.plantwalk;

import org.bukkit.Location;
import org.bukkit.Particle.DustOptions;
import org.bukkit.util.Vector;

/* This is my 3D, Minecraft version of CodingTrain's InverseKinematics coding challange.
 * https://github.com/CodingTrain/website/tree/main/CodingChallenges/CC_064.3_InverseKinematics_fixed
 * https://www.youtube.com/watch?v=hbgDqyy8bIw */

public class Tentacle {

	private Segment headSegment;
	private Segment terminalSegment;
	private Location startLocation;
	
	private int segmentAmount;
	private double segmentLength;
	private int segmentParticleAmount;
	private double tentacleLength;
	private boolean isAttached;
	private DustOptions dustOptions;
	
	/* Tentacle is a collection of segments. 
	 * tentacleLength: Length of this tentacle.
	 * segmentAmount: "Segment amount - 1" is the joint amount of tentacle.
	 * segmentParticleAmount: Particle amount of each segment.
	 * dustOptions: Color and size of each segment particle. */
	public Tentacle(Location startLocation, double tentacleLength, int segmentAmount, int segmentParticleAmount, DustOptions dustOptions) {
		this.startLocation = startLocation;
		
		segmentAmount = (segmentAmount < 1) ? 1 : segmentAmount;
		this.segmentLength = tentacleLength / segmentAmount;
		this.segmentParticleAmount = segmentParticleAmount;
		this.tentacleLength = tentacleLength;
		this.isAttached = false;
		this.dustOptions = dustOptions;
		
		this.headSegment = new Segment(startLocation, this.segmentLength, this.segmentParticleAmount, this.dustOptions);
		this.segmentAmount = 1;
		Segment current = this.headSegment;
		for (int i = 1; i < segmentAmount; i++) {
			Segment next = new Segment(current, this.segmentLength, this.segmentParticleAmount, this.dustOptions);
			this.segmentAmount++;
			current = next;
		}
		this.terminalSegment = current;
	}
	
	/* Move the end point of this tentacle to the targetLocation and calculate the correct shape.
	 * if isAttached is true, shift tentacle towards it's start location. */
	public void reach(Location targetLocation) {
		Segment current = this.terminalSegment;
		current.follow(targetLocation);
		while (current.hasParent()) {
			current = current.getParent();
			current.follow(current.getChild().getStartLocation());
		}
		
		if (this.isAttached)
			this.shiftStartLocation(this.startLocation);
	}
	
	/* Draw the tentacle to the world. */
	public void display() {
		Segment current = this.headSegment;
		current.display();
		while (current.hasChild()) {
			current = current.getChild();
			current.display();
		}
	}
	
	/* Change the length of the tentacle.
	 * This method will handle creation and 
	 * subtraction of segments. */
	public void setLength(double tentacleLength) {
		if (tentacleLength < this.segmentLength) {
			tentacleLength = this.segmentLength;
		}
		
		int targetSegmentAmount = (int) (tentacleLength / this.segmentLength);
		int segmentAmountChange = targetSegmentAmount - this.segmentAmount;
		
		if (segmentAmountChange > 0) {
			while (segmentAmountChange > 0) {
				this.addSegment();
				segmentAmountChange--;
			}
		} else {
			while (segmentAmountChange < 0) {
				this.popSegment();
				segmentAmountChange++;
			}
		}
		
		this.tentacleLength = tentacleLength;
	}
	
	/* If isAttached is true, the tentacle will shift towards this location. */
	public void setStartLocation(Location location) {
		this.startLocation = location;
	}
	
	/* If isAttached is true, the tentacle will shift towards it's startLocation. */
	public void setAttached(boolean isAttachedToStartLocation) {
		this.isAttached = isAttachedToStartLocation;
	}
	
	/* Change dustOptions of all of the segments. */
	public void setDustOptions(DustOptions dustOptions) {
		this.dustOptions = dustOptions;
		
		Segment current = this.headSegment;
		current.setDustOptions(this.dustOptions);
		while (current.hasChild()) {
			current = current.getChild();
			current.setDustOptions(this.dustOptions);
		}
	}
	
	/* Shift all segments of tentacle towards the target location. */
	private void shiftStartLocation(Location location) {
		Vector differenceVector = location.toVector().subtract(this.headSegment.getStartLocation().toVector());
		
		Segment current = this.headSegment;
		current.setStartLocation(current.getStartLocation().add(differenceVector));
		while (current.hasChild()) {
			current = current.getChild();
			current.setStartLocation(current.getStartLocation().add(differenceVector));
		}
	}
	
	/* Delete a segment from the end. */
	private void popSegment() {
		if (!this.hasMultipleSegments()) {
			return;
		}
		
		Segment segment = this.terminalSegment.getParent();
		segment.setChild(null);
		this.segmentAmount--;
		this.terminalSegment = segment;
	}
	
	/* Add a segment to the end. */
	private void addSegment() {	
		Segment segment = new Segment(this.terminalSegment, this.segmentLength, this.segmentParticleAmount, this.dustOptions);
		this.segmentAmount++;
		this.terminalSegment = segment;
	}
	
	public boolean hasMultipleSegments() {
		return !this.headSegment.equals(this.terminalSegment);
	}
	
	public double getLength() {
		return this.tentacleLength;
	}
	
	public double getSegmentLength() {
		return this.segmentLength;
	}
	
	public Location getLastLocation() {
		return this.terminalSegment.getEndLocation().clone();
	}
	
}
