package me.hiro3.smokebomb;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerAnimationEvent;

import com.projectkorra.projectkorra.BendingPlayer;

public class SmokebombListener implements Listener {

	@EventHandler
	public void onLeftClick(PlayerAnimationEvent event) {
		Player player = event.getPlayer();
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		
		if(event.isCancelled() || bPlayer == null) {
			return;
		}
		
		if(bPlayer.getBoundAbilityName().equalsIgnoreCase(null)) {
			return;
		}
		
		if(bPlayer.getBoundAbilityName().equalsIgnoreCase("Smokebomb")) {
			new Smokebomb(player);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onProjectileHit(final ProjectileHitEvent event) {
		if (!event.getEntity().hasMetadata("Smokebomb"))
			return;
		Smokebomb.createSmokeArea(event.getEntity().getLocation());
	}
	
}
