package me.hiro3.plantwalk;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import com.projectkorra.projectkorra.ability.CoreAbility;

import me.hiro3.plantwalk.PlantWalk.AbilityState;

public class PlantWalkListener implements Listener {
	
	@EventHandler
	public void onTeleport(PlayerTeleportEvent event) {
		Player player = event.getPlayer();
		if(CoreAbility.getAbility(player, PlantWalk.class) != null) {
			CoreAbility.getAbility(player, PlantWalk.class).remove();
		}
	}

	@EventHandler
	public void onRightClick(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if (CoreAbility.hasAbility(player, PlantWalk.class) && event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			PlantWalk pw = CoreAbility.getAbility(player, PlantWalk.class);
			if (pw.getAbilityState() == AbilityState.GROWN && pw.getSourceLocation().distanceSquared(event.getClickedBlock().getLocation()) < 4) {
				pw.remove();
			}
		}
	}
	
}