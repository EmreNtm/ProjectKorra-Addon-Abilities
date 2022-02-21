package me.hiro3.sprinkle;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ability.CoreAbility;

public class SprinkleListener implements Listener {

	@EventHandler
	public void onSneak(PlayerToggleSneakEvent event) {
		Player player = event.getPlayer();
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

		if (event.isCancelled() || bPlayer == null) {
			return;
		}

		if (bPlayer.getBoundAbilityName().equalsIgnoreCase(null)) {
			return;
		}

		if (player.isSneaking())
			return;
		
		if (bPlayer.getBoundAbilityName().equalsIgnoreCase("IceBlast")) {
			for (Sprinkle sprinkle : CoreAbility.getAbilities(Sprinkle.class)) {
				if (sprinkle.canUseIceVolley(player)) {
					sprinkle.startIceVolley(player);
					return;
				}
			}
		}
	}
	
	@EventHandler
	public void onClick(PlayerAnimationEvent event) {
		Player player = event.getPlayer();
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		
		if(event.isCancelled() || bPlayer == null) {
			return;
		}
		
		if(bPlayer.getBoundAbilityName().equalsIgnoreCase(null)) {
			return;
		}
		
		if(bPlayer.getBoundAbilityName().equalsIgnoreCase("IceBlast")) {
			if (CoreAbility.hasAbility(player, IceVolley.class)) {
				IceVolley iv = CoreAbility.getAbility(player, IceVolley.class);
				iv.launchIceVolley();
			}
		}
	}
	
}
