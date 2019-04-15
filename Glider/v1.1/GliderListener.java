package me.Hiro.MyAbilities.Glider;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.earthbending.EarthArmor;

public class GliderListener implements Listener {

	@EventHandler
	public void onClick(PlayerAnimationEvent event) {

		Player player = event.getPlayer();
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);

		if (event.isCancelled() || bPlayer == null) {
			return;
		}

		if (bPlayer.getBoundAbilityName().equalsIgnoreCase(null)) {
			return;
		}

		if (bPlayer.getBoundAbilityName().equalsIgnoreCase("Glider")) {
			if(CoreAbility.getAbility(player, GliderClass.class) != null) {
				return;
			}
			if(CoreAbility.getAbility(player, EarthArmor.class) != null) {
				CoreAbility.getAbility(player, EarthArmor.class).remove();
			}
			new GliderClass(player);
		}

	}
	
	@EventHandler
	public void onShift(PlayerToggleSneakEvent event) {
		Player player = event.getPlayer();
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		
		if (event.isCancelled() || bPlayer == null) {
			return;
		}

		if (bPlayer.getBoundAbilityName().equalsIgnoreCase(null)) {
			return;
		}

		if (bPlayer.getBoundAbilityName().equalsIgnoreCase("EarthArmor")) {
			if(CoreAbility.getAbility(player, GliderClass.class) != null) {
				CoreAbility.getAbility(player, GliderClass.class).remove();
			}
		}
	}
	
	@EventHandler
	public void onTeleport(PlayerTeleportEvent event) {
		Player player = event.getPlayer();
		if(CoreAbility.getAbility(player, GliderClass.class) != null) {
			CoreAbility.getAbility(player, GliderClass.class).geri();
		}
	}
}
