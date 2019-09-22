package Hiro3;

import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerAnimationEvent;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ability.CoreAbility;

public class ReviveListener implements Listener {

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
		
		if(bPlayer.getBoundAbilityName().equalsIgnoreCase("Revive")) {
			new Revive(player);
		}
	}
	
	@EventHandler
	public void onDamage(EntityDamageByEntityEvent event) {
		Player player = null;
		
		if (event.getEntityType().equals(EntityType.PLAYER)) {
			player = (Player) event.getEntity();
		} else {
			return;
		}
		
		if (event.isCancelled()) {
			return;
		}
		
		if(player.getHealth() - event.getDamage() < 1) {
			for (Player p : Bukkit.getServer().getOnlinePlayers()) {
				if (CoreAbility.hasAbility(p, Revive.class) && CoreAbility.getAbility(p, Revive.class).getTarget() != null) {
					if (CoreAbility.getAbility(p, Revive.class).getTarget().getUniqueId().equals(player.getUniqueId())) {
						CoreAbility.getAbility(p, Revive.class).setStartRevenge(true);
						player.setHealth(8);
						event.setCancelled(true);
					}
				}
			}
		}
		
	}
	
}
