package Hiro3;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ability.CoreAbility;

public class SoulSplitListener implements Listener {
	
	@EventHandler
	public void onSneak(PlayerToggleSneakEvent event) {
		Player player = event.getPlayer();
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		
		if(event.isCancelled() || bPlayer == null) {
			return;
		}
		
		if(bPlayer.getBoundAbilityName().equalsIgnoreCase(null)) {
			return;
		}
		
		if(bPlayer.getBoundAbilityName().equalsIgnoreCase("SoulSplit")) {
			if (CoreAbility.getAbility(player, SoulSplit.class) == null) {
				new SoulSplit(player);
			}
		}
		
	}

}
