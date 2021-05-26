package Hiro3;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.firebending.FireJet;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;

public class SupportProjectile {

	private Location location;
	private Location targetLocation;
	private Location startLocation;
	private double speed;
	private double range;
	private double damage;
	private double activateRange;
	private int tick;
	
	
	public SupportProjectile(RagingFire rf, FireAbility supportAbility) {
		this.location = rf.getRandomLocation().clone();
		this.startLocation = location.clone();
		this.targetLocation = getLocation(supportAbility);
		
		this.activateRange = rf.boostActivationRange;
		if (targetLocation.distance(startLocation) > activateRange)
			return;
		
		if (supportAbility instanceof FireJet) {
			((FireJet) supportAbility).setDuration(((FireJet) supportAbility).getDuration() + rf.boostFireJetDuration);
			((FireJet) supportAbility).setSpeed(((FireJet) supportAbility).getSpeed() + rf.boostFireJetSpeed);
		}
		
		this.speed = rf.boostProjectileSpeed;
		this.range = 20;
		this.damage = rf.boostProjectileDamage;
		this.tick = 0;
		
		Vector dir = targetLocation.toVector().subtract(location.toVector()).normalize().multiply(speed / 20);
		
		new BukkitRunnable() {

			@Override
			public void run() {
				if (targetLocation == null || location == null
						|| startLocation.distance(location) > range || tick * speed / 20 > range * 1.5) {
					this.cancel();
					return;
				}
				
				if (!location.getBlock().isPassable()) {
					this.cancel();
					return;
				}
				
				Vector targetDir = targetLocation.toVector().subtract(location.toVector()).normalize().multiply(speed / 20);
				Vector steeringDir = targetDir.clone().subtract(dir.clone()).multiply(0.25);
				dir.add(steeringDir).normalize().multiply(speed / 20);
				if (isSafeForLocation(supportAbility))
					targetLocation = getLocation(supportAbility);
				location.add(dir);
				
				ParticleEffect.FLAME.display(location, 3, 0.275, 0.275, 0.275);
				
				for (Entity e : GeneralMethods.getEntitiesAroundPoint(location, 2)) {
					if (e instanceof LivingEntity && !e.getUniqueId().equals(rf.getPlayer().getUniqueId())) {
						DamageHandler.damageEntity(e, damage, rf);
						this.cancel();
						return;
					}
				}
				tick++;
			}
			
		}.runTaskTimer(ProjectKorra.plugin, 0, 1);
	}
	
	private Location getLocation(FireAbility supportAbility) {
		return supportAbility.getLocation().clone();
	}
	
	private boolean isSafeForLocation(FireAbility supportAbility) {
		if (supportAbility.isRemoved())
			return false;
		return true;
	}
	
}
