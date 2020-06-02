package Hiro3;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAnimationEvent;
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
		} else if (bPlayer.getBoundAbilityName().equalsIgnoreCase("SoulControl")) {
			if (CoreAbility.getAbility(player, SoulControl.class) == null) {
				new SoulControl(player);
			} else {
				CoreAbility.getAbility(player, SoulControl.class).remove();
				new SoulControl(player);
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
		
		if(bPlayer.getBoundAbilityName().equalsIgnoreCase("SoulSplit")) {
			if (CoreAbility.getAbility(player, SoulSplit.class) != null) {
				SoulSplit split = CoreAbility.getAbility(player, SoulSplit.class);
				if (split.getState() % 2 == 1)
					split.setIsClicked(true);
			}
		} else if (bPlayer.getBoundAbilityName().equalsIgnoreCase("SoulControl")) {
			if (CoreAbility.getAbility(player, SoulControl.class) != null) {
				SoulControl sc = CoreAbility.getAbility(player, SoulControl.class);
				sc.setAbilityState(1);
			}
		}
	}
}
