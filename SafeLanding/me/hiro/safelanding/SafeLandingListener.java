package me.hiro.safelanding;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.event.AbilityEndEvent;

public class SafeLandingListener implements Listener {
	
	@EventHandler
	public void onWallrunEnd(AbilityEndEvent event) {
		if (event.getAbility().getName().equals("WallRun")) {
			BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(event.getAbility().getPlayer());
			if (!bPlayer.isOnCooldown(event.getAbility()))
				return;
			if (CoreAbility.hasAbility(event.getAbility().getPlayer(), SafeLandingChi.class))
				CoreAbility.getAbility(event.getAbility().getPlayer(), SafeLandingChi.class).activate();
			else if (CoreAbility.hasAbility(event.getAbility().getPlayer(), SafeLandingAir.class))
				CoreAbility.getAbility(event.getAbility().getPlayer(), SafeLandingAir.class).activate();
			else if (CoreAbility.hasAbility(event.getAbility().getPlayer(), SafeLandingWater.class))
				CoreAbility.getAbility(event.getAbility().getPlayer(), SafeLandingWater.class).activate();
			else if (CoreAbility.hasAbility(event.getAbility().getPlayer(), SafeLandingEarth.class))
				CoreAbility.getAbility(event.getAbility().getPlayer(), SafeLandingEarth.class).activate();
			else if (CoreAbility.hasAbility(event.getAbility().getPlayer(), SafeLandingFire.class))
				CoreAbility.getAbility(event.getAbility().getPlayer(), SafeLandingFire.class).activate();
		}
	}
	
	@EventHandler
	public void onFall(EntityDamageEvent event) {
		if (event.getCause().equals(DamageCause.FALL) && event.getEntity() instanceof Player) {
			if (CoreAbility.hasAbility((Player)event.getEntity(), SafeLandingChi.class)
				&& CoreAbility.getAbility((Player)event.getEntity(), SafeLandingChi.class).isActive()) {
				event.setCancelled(true);
				CoreAbility.getAbility((Player)event.getEntity(), SafeLandingChi.class).deactivate();
			} else if (CoreAbility.hasAbility((Player)event.getEntity(), SafeLandingAir.class)
					&& CoreAbility.getAbility((Player)event.getEntity(), SafeLandingAir.class).isActive()) {
				event.setCancelled(true);
				CoreAbility.getAbility((Player)event.getEntity(), SafeLandingAir.class).deactivate();
			} else if (CoreAbility.hasAbility((Player)event.getEntity(), SafeLandingWater.class)
					&& CoreAbility.getAbility((Player)event.getEntity(), SafeLandingWater.class).isActive()) {
				event.setCancelled(true);
				CoreAbility.getAbility((Player)event.getEntity(), SafeLandingWater.class).deactivate();
			} else if (CoreAbility.hasAbility((Player)event.getEntity(), SafeLandingEarth.class)
					&& CoreAbility.getAbility((Player)event.getEntity(), SafeLandingEarth.class).isActive()) {
				event.setCancelled(true);
				CoreAbility.getAbility((Player)event.getEntity(), SafeLandingEarth.class).deactivate();
			} else if (CoreAbility.hasAbility((Player)event.getEntity(), SafeLandingFire.class)
					&& CoreAbility.getAbility((Player)event.getEntity(), SafeLandingFire.class).isActive()) {
				event.setCancelled(true);
				CoreAbility.getAbility((Player)event.getEntity(), SafeLandingFire.class).deactivate();
			}	
		}
	}

}
