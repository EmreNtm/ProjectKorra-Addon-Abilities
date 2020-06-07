package com.plantwalk.Hiro3;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.configuration.ConfigManager;

public class Tentacle {

	private Player player;
	
	private ArrayList<Segment> segments;
	private int maxSegmentAmount;
	private double segmentLength;
	
	private Location startLocation;
	
	public Tentacle(Player player, Location startLocation) {
		this.player = player;
		segments = new ArrayList<Segment>();
		maxSegmentAmount = ConfigManager.getConfig().getInt("ExtraAbilities.Hiro3.Water.PlantWalk.TentacleLength");
		segmentLength = 1;
		
		this.startLocation = startLocation;
		
		segments.add(new Segment(player, startLocation, segmentLength, 0, 0));
//		for (int i = 1; i < maxSegmentAmount; i++) {
//			//Açýyý 0 yapýnca NaN oluyor.
//			segments.add(new Segment(player, segments.get(i-1), segmentLength, 1, 0));
//		}
		
	}
	
	public void follow(Location targetLocation) {
		segments.get(segments.size()-1).follow(targetLocation);
		segments.get(segments.size()-1).update();
		
		for (int i = segments.size()-2; i >= 0; i--) {
			segments.get(i).follow(segments.get(i+1).getLocationA());
			segments.get(i).update();
		}
		
		segments.get(0).setLocationA(startLocation);
		for (int i = 1; i < segments.size(); i++) {
			segments.get(i).setLocationA(segments.get(i-1).getLocationB());
		}
		
		for (Segment s : segments) {
			s.display();
		}
		
	}
	
	public void addNewSegment() {
		if (getSize() < getMaxSegmentAmount())
			segments.add(new Segment(player, segments.get(getSize()-1), segmentLength, 1, 0));
	}
	
	public double getLength() {
		return this.maxSegmentAmount * this.segmentLength;
	}
	
	public int getMaxSegmentAmount() {
		return this.maxSegmentAmount;
	}
	
	public int getSize() {
		return this.segments.size();
	}
	
	public Location getLastLocation() {
		return this.segments.get(segments.size()-1).getLocationB();
	}
	
}
