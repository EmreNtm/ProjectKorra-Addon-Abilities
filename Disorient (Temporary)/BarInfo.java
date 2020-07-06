package hiro3.disorient;

import org.bukkit.entity.LivingEntity;

public class BarInfo {

	private LivingEntity targetEntity;
	
	private double capacity;
	private double level;
	
	public BarInfo(LivingEntity targetEntity, double capacity) {
		this.targetEntity = targetEntity;
		this.capacity = capacity;
	}
	
	public void updateLevel(double amount) {
		this.level += amount;
		if (this.level < 0) {
			this.level = 0;
		} else if (this.level > this.capacity) {
			this.level = this.capacity;
		}
	}
	
	public double getLevel() {
		return this.level;
	}
	
	public LivingEntity getTargetEntity() {
		return this.targetEntity;
	}
	
}
